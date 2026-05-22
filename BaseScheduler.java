package com.scheduler.app.scheduler;

import com.scheduler.app.exception.SchedulerExecutionException;
import com.scheduler.app.model.SchedulerResult;
import com.scheduler.app.properties.SchedulerProperties;
import com.scheduler.app.service.SchedulerExecutionTracker;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base class for all individual schedulers.
 *
 * Provides:
 *  - Standardised execution wrapper with retry logic
 *  - Structured log entry/exit banners per scheduler run
 *  - Exception capture → SchedulerResult(FAILED) recording
 *  - Each subclass uses its OWN logger so Logback routes logs
 *    to the correct per-scheduler rolling file.
 */
public abstract class BaseScheduler {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    protected final SchedulerProperties       props;
    protected final SchedulerExecutionTracker tracker;

    protected BaseScheduler(SchedulerProperties props, SchedulerExecutionTracker tracker) {
        this.props   = props;
        this.tracker = tracker;
    }

    /**
     * Executes a scheduler task with retry, logging, and tracking.
     *
     * @param schedulerName human-readable name for logs
     * @param task          the work to execute
     * @param log           the CALLER's logger (ensures correct Logback routing)
     */
    protected void execute(String schedulerName, SchedulerTask task, Logger log) {
        int  maxAttempts = props.getRetryMaxAttempts();
        long retryDelay  = props.getRetryDelayMs();
        String thread    = Thread.currentThread().getName();

        log.info("┌──────────────────────────────────────────────────");
        log.info("│  SCHEDULER  : {}", schedulerName);
        log.info("│  THREAD     : {}", thread);
        log.info("│  STARTED AT : {}", LocalDateTime.now().format(FMT));
        log.info("└──────────────────────────────────────────────────");

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                if (attempt > 1) {
                    log.warn("♻️  [{}] Retry attempt {}/{}", schedulerName, attempt, maxAttempts);
                }

                SchedulerResult result = task.execute();
                tracker.record(result);

                log.info("✅ [{}] Completed in {}ms — {}", schedulerName,
                        result.getExecutionTimeMs(), result.getMessage());
                log.info("──────────────────────────────────────────────────\n");
                return; // success

            } catch (SchedulerExecutionException ex) {
                log.error("❌ [{}] Attempt {}/{} failed | code={} | reason={}",
                        schedulerName, attempt, maxAttempts, ex.getErrorCode(), ex.getMessage());

                if (props.isLogStackTrace()) {
                    log.error("   Stack trace:", ex);
                }

                recordFailure(schedulerName, thread, attempt, ex.getMessage(), ex.getErrorCode());

                if (attempt < maxAttempts) {
                    log.warn("⏳ [{}] Waiting {}ms before retry…", schedulerName, retryDelay);
                    sleep(retryDelay, log, schedulerName);
                } else {
                    log.error("🔴 [{}] All {} attempts exhausted. Will retry on next scheduled trigger.",
                            schedulerName, maxAttempts);
                }

            } catch (Exception ex) {
                // Unknown exception — do NOT retry
                log.error("🔴 [{}] Unexpected exception (no retry): {}", schedulerName, ex.getMessage(), ex);
                recordFailure(schedulerName, thread, attempt, ex.getMessage(), ex.getClass().getSimpleName());
                break;
            }
        }

        log.info("──────────────────────────────────────────────────\n");
    }

    private void recordFailure(String name, String thread, int attempt, String msg, String code) {
        tracker.record(SchedulerResult.builder()
                .schedulerName(name)
                .status("FAILED")
                .threadName(thread)
                .message("Attempt " + attempt + " failed: " + msg)
                .errorDetail(code)
                .executedAt(LocalDateTime.now())
                .build());
    }

    private void sleep(long ms, Logger log, String name) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("⚠️  [{}] Retry sleep interrupted", name);
        }
    }

    /** Functional interface for the task body. */
    @FunctionalInterface
    protected interface SchedulerTask {
        SchedulerResult execute() throws SchedulerExecutionException;
    }
}
