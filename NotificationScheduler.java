package com.scheduler.app.scheduler;

import com.scheduler.app.properties.SchedulerProperties;
import com.scheduler.app.service.NotificationService;
import com.scheduler.app.service.SchedulerExecutionTracker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Notification Scheduler — runs independently on its own thread.
 *
 * Log output → logs/notification-scheduler.log
 * Rolls to   → logs/archived/notification-scheduler.yyyy-MM-dd.N.log.gz  (when > 10 MB)
 *
 * Cron expression is read from application.properties:
 *   scheduler.notification.cron=0 0/2 * * * ?
 */
@Slf4j
@Component
public class NotificationScheduler extends BaseScheduler {

    private final NotificationService notificationService;

    public NotificationScheduler(SchedulerProperties props,
                                 SchedulerExecutionTracker tracker,
                                 NotificationService notificationService) {
        super(props, tracker);
        this.notificationService = notificationService;
    }

    /**
     * Triggered every 2 minutes (configurable via application.properties).
     * Runs on a dedicated thread from the ThreadPoolTaskScheduler.
     */
    @Scheduled(cron = "${scheduler.notification.cron}")
    public void run() {
        if (!props.isNotificationEnabled()) {
            log.info("🔔 NotificationScheduler is DISABLED — skipping this trigger.");
            return;
        }

        execute("NotificationScheduler", notificationService::dispatchNotifications, log);
    }
}
