package com.scheduler.app.service;

import com.scheduler.app.exception.SchedulerExecutionException;
import com.scheduler.app.model.SchedulerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Notification Service.
 * Dispatches push/in-app notifications on a scheduled interval.
 */
@Slf4j
@Service
public class NotificationService {

    /**
     * Dispatches pending notifications.
     *
     * @return SchedulerResult with execution details
     * @throws SchedulerExecutionException if notification dispatch fails
     */
    public SchedulerResult dispatchNotifications() {
        long startTime = System.currentTimeMillis();
        String thread = Thread.currentThread().getName();

        log.info("🔔 [{}] NotificationService: Dispatching notifications at {}",
                thread, LocalDateTime.now());

        try {
            int dispatched = simulateNotificationDispatch();

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("✅ [{}] NotificationService: Dispatched {} notifications in {}ms",
                    thread, dispatched, elapsed);

            return SchedulerResult.builder()
                    .schedulerName("NotificationScheduler")
                    .status("SUCCESS")
                    .threadName(thread)
                    .executionTimeMs(elapsed)
                    .message("Dispatched " + dispatched + " notifications")
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SchedulerExecutionException("NotificationScheduler",
                    "INTERRUPTED", "Notification dispatch interrupted", e);
        } catch (Exception e) {
            throw new SchedulerExecutionException("NotificationScheduler",
                    "Notification dispatch failed: " + e.getMessage(), e);
        }
    }

    private int simulateNotificationDispatch() throws InterruptedException {
        Thread.sleep(200);
        log.debug("📲 Pushing notifications to mobile devices...");
        return (int) (Math.random() * 50 + 5);
    }
}
