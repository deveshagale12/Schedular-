package com.scheduler.app.config;

import com.scheduler.app.properties.SchedulerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Multi-Threading Configuration for Schedulers.
 *
 * By default, Spring runs ALL @Scheduled tasks on a SINGLE thread.
 * This config provides a ThreadPoolTaskScheduler so multiple schedulers
 * run concurrently on dedicated threads.
 *
 * Thread pool size is loaded from application.properties via InputStream.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ThreadPoolConfig implements SchedulingConfigurer {

    private final SchedulerProperties schedulerProperties;

    /**
     * Creates a shared TaskScheduler bean with a thread pool.
     * All @Scheduled tasks will use this pool instead of the default single thread.
     */
    @Bean
    public TaskScheduler taskScheduler() {
        int poolSize = schedulerProperties.getThreadPoolSize();
        String threadPrefix = schedulerProperties.getThreadNamePrefix();

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix(threadPrefix);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);

        // Global exception handler for uncaught scheduler exceptions
        scheduler.setErrorHandler(throwable -> {
            log.error("🔴 Uncaught exception in scheduled task thread: {}", throwable.getMessage());
            if (schedulerProperties.isLogStackTrace()) {
                log.error("Stack trace:", throwable);
            }
        });

        scheduler.initialize();

        log.info("✅ ThreadPoolTaskScheduler initialized: pool-size={}, prefix={}",
                poolSize, threadPrefix);
        return scheduler;
    }

    /**
     * Wires the custom task scheduler into Spring's scheduling infrastructure.
     * This is what enables multi-threading for @Scheduled methods.
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        registrar.setTaskScheduler(taskScheduler());
        log.info("✅ ScheduledTaskRegistrar configured with custom ThreadPoolTaskScheduler");
    }
}
