package com.scheduler.app.service;

import com.scheduler.app.exception.SchedulerExecutionException;
import com.scheduler.app.model.SchedulerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Report Service.
 * Generates and distributes scheduled reports.
 */
@Slf4j
@Service
public class ReportService {

    /**
     * Generates the daily report.
     *
     * @return SchedulerResult with execution details
     * @throws SchedulerExecutionException if report generation fails
     */
    public SchedulerResult generateDailyReport() {
        long startTime = System.currentTimeMillis();
        String thread = Thread.currentThread().getName();

        log.info("📊 [{}] ReportService: Generating daily report at {}", thread, LocalDateTime.now());

        try {
            simulateReportGeneration();

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("✅ [{}] ReportService: Daily report generated in {}ms", thread, elapsed);

            return SchedulerResult.builder()
                    .schedulerName("ReportScheduler")
                    .status("SUCCESS")
                    .threadName(thread)
                    .executionTimeMs(elapsed)
                    .message("Daily report generated and sent to 5 recipients")
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SchedulerExecutionException("ReportScheduler",
                    "INTERRUPTED", "Report generation interrupted", e);
        } catch (Exception e) {
            throw new SchedulerExecutionException("ReportScheduler",
                    "Daily report generation failed: " + e.getMessage(), e);
        }
    }

    private void simulateReportGeneration() throws InterruptedException {
        Thread.sleep(800);
        log.debug("📈 Aggregating report data...");
        Thread.sleep(300);
        log.debug("📝 Formatting report output...");
    }
}
