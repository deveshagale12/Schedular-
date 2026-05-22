package com.scheduler.app.scheduler;

import com.scheduler.app.properties.SchedulerProperties;
import com.scheduler.app.service.DataSyncService;
import com.scheduler.app.service.SchedulerExecutionTracker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * DataSync Scheduler — runs independently on its own thread.
 *
 * Log output → logs/datasync-scheduler.log
 * Rolls to   → logs/archived/datasync-scheduler.yyyy-MM-dd.N.log.gz  (when > 10 MB)
 *
 * Cron expression is read from application.properties:
 *   scheduler.data-sync.cron=0 0/5 * * * ?
 */
@Slf4j
@Component
public class DataSyncScheduler extends BaseScheduler {

    private final DataSyncService dataSyncService;

    public DataSyncScheduler(SchedulerProperties props,
                             SchedulerExecutionTracker tracker,
                             DataSyncService dataSyncService) {
        super(props, tracker);
        this.dataSyncService = dataSyncService;
    }

    /**
     * Triggered every 5 minutes (configurable via application.properties).
     * Runs on a dedicated thread from the ThreadPoolTaskScheduler.
     */
    @Scheduled(cron = "${scheduler.data-sync.cron}")
    public void run() {
        if (!props.isDataSyncEnabled()) {
            log.info("🔄 DataSyncScheduler is DISABLED — skipping this trigger.");
            return;
        }

        execute("DataSyncScheduler", dataSyncService::syncData, log);
    }
}
