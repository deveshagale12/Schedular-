package com.scheduler.app.service;

import com.scheduler.app.exception.SchedulerExecutionException;
import com.scheduler.app.model.SchedulerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Email Service.
 * Handles email dispatch logic called by the Email Scheduler.
 */
@Slf4j
@Service
public class EmailService {

    /**
     * Processes pending email notifications.
     * Called by the scheduler at the configured cron interval.
     *
     * @return SchedulerResult with execution details
     * @throws SchedulerExecutionException if email processing fails
     */
    public SchedulerResult processPendingEmails() {
        long startTime = System.currentTimeMillis();
        String thread = Thread.currentThread().getName();

        log.info("📧 [{}] EmailService: Processing pending emails at {}",
                thread, LocalDateTime.now());

        try {
            // Simulate email processing logic
            simulateEmailProcessing();

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("✅ [{}] EmailService: Processed emails in {}ms", thread, elapsed);

            return SchedulerResult.builder()
                    .schedulerName("EmailScheduler")
                    .status("SUCCESS")
                    .threadName(thread)
                    .executionTimeMs(elapsed)
                    .message("Processed 15 pending emails successfully")
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SchedulerExecutionException("EmailScheduler",
                    "INTERRUPTED", "Email processing was interrupted", e);
        } catch (Exception e) {
            throw new SchedulerExecutionException("EmailScheduler",
                    "Failed to process emails: " + e.getMessage(), e);
        }
    }

    private void simulateEmailProcessing() throws InterruptedException {
        // Simulate actual email processing work
        Thread.sleep(500);
        log.debug("📤 Sent batch of emails...");
    }
}
