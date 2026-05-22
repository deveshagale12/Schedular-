package com.scheduler.app.service;

import com.scheduler.app.exception.SchedulerExecutionException;
import com.scheduler.app.model.SchedulerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Data Sync Service.
 * Synchronizes data with external systems/databases on a scheduled interval.
 */
@Slf4j
@Service
public class DataSyncService {

    /**
     * Runs the data synchronization job.
     *
     * @return SchedulerResult with execution details
     * @throws SchedulerExecutionException if sync fails
     */
    public SchedulerResult syncData() {
        long startTime = System.currentTimeMillis();
        String thread = Thread.currentThread().getName();

        log.info("🔄 [{}] DataSyncService: Starting data sync at {}", thread, LocalDateTime.now());

        try {
            int syncedRecords = simulateDataSync();

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("✅ [{}] DataSyncService: Synced {} records in {}ms",
                    thread, syncedRecords, elapsed);

            return SchedulerResult.builder()
                    .schedulerName("DataSyncScheduler")
                    .status("SUCCESS")
                    .threadName(thread)
                    .executionTimeMs(elapsed)
                    .message("Synced " + syncedRecords + " records with external system")
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SchedulerExecutionException("DataSyncScheduler",
                    "INTERRUPTED", "Data sync interrupted", e);
        } catch (Exception e) {
            throw new SchedulerExecutionException("DataSyncScheduler",
                    "Data sync failed: " + e.getMessage(), e);
        }
    }

    private int simulateDataSync() throws InterruptedException {
        Thread.sleep(400);
        log.debug("📥 Pulling records from external source...");
        Thread.sleep(300);
        log.debug("📤 Pushing delta changes...");
        return (int) (Math.random() * 1000 + 200);
    }
}
