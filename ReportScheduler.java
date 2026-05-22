package com.scheduler.app.scheduler;

import com.scheduler.app.properties.SchedulerProperties;
import com.scheduler.app.service.ReportService;
import com.scheduler.app.service.SchedulerExecutionTracker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Report Scheduler — runs independently on its own thread.
 *
 * Log output → logs/report-scheduler.log
 * Rolls to   → logs/archived/report-scheduler.yyyy-MM-dd.N.log.gz  (when > 10 MB)
 *
 * Cron expression is read from application.properties:
 *   scheduler.report.cron=0 0 8 * * ?
 */
@Slf4j
@Component
public class ReportScheduler extends BaseScheduler {

    private final ReportService reportService;

    public ReportScheduler(SchedulerProperties props,
                           SchedulerExecutionTracker tracker,
                           ReportService reportService) {
        super(props, tracker);
        this.reportService = reportService;
    }

    /**
     * Triggered daily at 8:00 AM (configurable via application.properties).
     * Runs on a dedicated thread from the ThreadPoolTaskScheduler.
     */
    @Scheduled(cron = "${scheduler.report.cron}")
    public void run() {
        if (!props.isReportEnabled()) {
            log.info("📊 ReportScheduler is DISABLED — skipping this trigger.");
            return;
        }

        execute("ReportScheduler", reportService::generateDailyReport, log);
    }
}
