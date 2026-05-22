package com.scheduler.app.scheduler;

import com.scheduler.app.properties.SchedulerProperties;
import com.scheduler.app.service.CleanupService;
import com.scheduler.app.service.SchedulerExecutionTracker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cleanup Scheduler — runs independently on its own thread.
 *
 * Log output → logs/cleanup-scheduler.log
 * Rolls to   → logs/archived/cleanup-scheduler.yyyy-MM-dd.N.log.gz  (when > 10 MB)
 *
 * Cron expression is read from application.properties:
 *   scheduler.cleanup.cron=0 0 0 * * ?
 */
@Slf4j
@Component
public class CleanupScheduler extends BaseScheduler {

    private final CleanupService cleanupService;

    public CleanupScheduler(SchedulerProperties props,
                            SchedulerExecutionTracker tracker,
                            CleanupService cleanupService) {
        super(props, tracker);
        this.cleanupService = cleanupService;
    }

    /**
     * Triggered at midnight (configurable via application.properties).
     * Runs on a dedicated thread from the ThreadPoolTaskScheduler.
     */
    @Scheduled(cron = "${scheduler.cleanup.cron}")
    public void run() {
        if (!props.isCleanupEnabled()) {
            log.info("🧹 CleanupScheduler is DISABLED — skipping this trigger.");
            return;
        }

        execute("CleanupScheduler", cleanupService::runNightlyCleanup, log);
    }
}
