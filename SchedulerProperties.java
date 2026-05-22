package com.scheduler.app.properties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads scheduler configuration from application.properties using InputStream.
 *
 * This demonstrates reading properties programmatically via InputStream,
 * allowing dynamic access independent of Spring's @Value injection.
 */
@Slf4j
@Component
public class SchedulerProperties {

    private final Properties properties = new Properties();

    public SchedulerProperties() {
        loadPropertiesFromInputStream();
    }

    /**
     * Loads application.properties from the classpath using InputStream.
     * This approach allows runtime reading and dynamic property access.
     */
    private void loadPropertiesFromInputStream() {
        // Use InputStream to read properties from classpath
        try (InputStream inputStream =
                     getClass().getClassLoader().getResourceAsStream("application.properties")) {

            if (inputStream == null) {
                log.error("❌ application.properties not found on classpath");
                throw new IllegalStateException("application.properties not found");
            }

            properties.load(inputStream);
            log.info("✅ Loaded {} properties from application.properties via InputStream",
                    properties.size());

        } catch (IOException e) {
            log.error("❌ Failed to load application.properties via InputStream", e);
            throw new IllegalStateException("Cannot load scheduler properties", e);
        }
    }

    // ─── Thread Pool ──────────────────────────────────────

    public int getThreadPoolSize() {
        return Integer.parseInt(getProperty("scheduler.thread-pool.size", "5"));
    }

    public String getThreadNamePrefix() {
        return getProperty("scheduler.thread-pool.name-prefix", "Scheduler-");
    }

    // ─── Cron Expressions ─────────────────────────────────

    public String getEmailCron() {
        return getProperty("scheduler.email.cron", "0 0/5 * * * ?");
    }

    public String getReportCron() {
        return getProperty("scheduler.report.cron", "0 0 8 * * ?");
    }

    public String getCleanupCron() {
        return getProperty("scheduler.cleanup.cron", "0 0 0 * * ?");
    }

    public String getNotificationCron() {
        return getProperty("scheduler.notification.cron", "0 0/2 * * * ?");
    }

    public String getDataSyncCron() {
        return getProperty("scheduler.data-sync.cron", "0 0/5 * * * ?");
    }

    // ─── Enabled Flags ────────────────────────────────────

    public boolean isEmailEnabled() {
        return Boolean.parseBoolean(getProperty("scheduler.email.enabled", "true"));
    }

    public boolean isReportEnabled() {
        return Boolean.parseBoolean(getProperty("scheduler.report.enabled", "true"));
    }

    public boolean isCleanupEnabled() {
        return Boolean.parseBoolean(getProperty("scheduler.cleanup.enabled", "true"));
    }

    public boolean isNotificationEnabled() {
        return Boolean.parseBoolean(getProperty("scheduler.notification.enabled", "true"));
    }

    public boolean isDataSyncEnabled() {
        return Boolean.parseBoolean(getProperty("scheduler.data-sync.enabled", "true"));
    }

    // ─── Timeouts ─────────────────────────────────────────

    public long getEmailTimeout() {
        return Long.parseLong(getProperty("scheduler.email.timeout", "30000"));
    }

    public long getReportTimeout() {
        return Long.parseLong(getProperty("scheduler.report.timeout", "60000"));
    }

    public long getCleanupTimeout() {
        return Long.parseLong(getProperty("scheduler.cleanup.timeout", "120000"));
    }

    // ─── Retry ────────────────────────────────────────────

    public int getRetryMaxAttempts() {
        return Integer.parseInt(getProperty("scheduler.retry.max-attempts", "3"));
    }

    public long getRetryDelayMs() {
        return Long.parseLong(getProperty("scheduler.retry.delay-ms", "2000"));
    }

    // ─── Exception Handling ───────────────────────────────

    public boolean isNotifyOnFailure() {
        return Boolean.parseBoolean(getProperty("scheduler.exception.notify-on-failure", "true"));
    }

    public boolean isLogStackTrace() {
        return Boolean.parseBoolean(getProperty("scheduler.exception.log-stack-trace", "true"));
    }

    // ─── Utility ──────────────────────────────────────────

    private String getProperty(String key, String defaultValue) {
        // First check system/Spring environment, then fall back to loaded properties
        String envValue = System.getProperty(key);
        if (envValue != null) return envValue;
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Expose raw properties for debugging.
     */
    public Properties getAllProperties() {
        return new Properties(properties);
    }
}
