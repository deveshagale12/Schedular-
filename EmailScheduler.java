package com.scheduler.app.scheduler;

import com.scheduler.app.properties.SchedulerProperties;
import com.scheduler.app.service.EmailService;
import com.scheduler.app.service.SchedulerExecutionTracker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Email Scheduler — runs independently on its own thread.
 *
 * Log output → logs/email-scheduler.log
 * Rolls to   → logs/archived/email-scheduler.yyyy-MM-dd.N.log.gz  (when > 10 MB)
 *
 * Cron expression is read from application.properties:
 *   scheduler.email.cron=0 0/1 * * * ?
 */
@Slf4j
@Component
public class EmailScheduler extends BaseScheduler {

    private final EmailService emailService;

    public EmailScheduler(SchedulerProperties props,
                          SchedulerExecutionTracker tracker,
                          EmailService emailService) {
        super(props, tracker);
        this.emailService = emailService;
    }

    /**
     * Triggered by cron defined in application.properties.
     * Runs on a dedicated thread from the ThreadPoolTaskScheduler.
     */
    @Scheduled(cron = "${scheduler.email.cron}")
    public void run() {
        if (!props.isEmailEnabled()) {
            log.info("📧 EmailScheduler is DISABLED — skipping this trigger.");
            return;
        }

        // Pass THIS class's logger so Logback routes to email-scheduler.log
        execute("EmailScheduler", emailService::processPendingEmails, log);
    }
}
