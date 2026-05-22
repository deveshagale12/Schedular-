package com.scheduler.app.service;

import com.scheduler.app.exception.SchedulerExecutionException;
import com.scheduler.app.model.SchedulerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Cleanup Service.
 * Purges old/expired data and temporary files on a schedule.
 */
@Slf4j
@Service
public class CleanupService {

    /**
     * Runs the nightly cleanup job.
     *
     * @return SchedulerResult with execution details
     * @throws SchedulerExecutionException if cleanup fails
     */
    public SchedulerResult runNightlyCleanup() {
        long startTime = System.currentTimeMillis();
        String thread = Thread.currentThread().getName();

        log.info("🧹 [{}] CleanupService: Starting nightly cleanup at {}", thread, LocalDateTime.now());

        try {
            int deletedRecords = simulateCleanup();

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("✅ [{}] CleanupService: Cleanup completed in {}ms, deleted {} records",
                    thread, elapsed, deletedRecords);

            return SchedulerResult.builder()
                    .schedulerName("CleanupScheduler")
                    .status("SUCCESS")
                    .threadName(thread)
                    .executionTimeMs(elapsed)
                    .message("Cleaned up " + deletedRecords + " expired records and temp files")
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SchedulerExecutionException("CleanupScheduler",
                    "INTERRUPTED", "Cleanup was interrupted", e);
        } catch (Exception e) {
            throw new SchedulerExecutionException("CleanupScheduler",
                    "Nightly cleanup failed: " + e.getMessage(), e);
        }
    }

    private int simulateCleanup() throws InterruptedException {
        Thread.sleep(300);
        log.debug("🗑️  Deleting expired sessions...");
        Thread.sleep(200);
        log.debug("🗑️  Purging temp files...");
        return (int) (Math.random() * 500 + 100);
    }
}
