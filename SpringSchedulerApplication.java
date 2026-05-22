package com.scheduler.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Spring Multi-Threaded Scheduler Application.
 *
 * Features:
 *  - Multi-threaded scheduling via a configurable thread pool
 *  - Cron expressions loaded from application.properties via InputStream
 *  - Global exception handling with structured API responses
 *  - Multiple independently configurable services
 */
@Slf4j
@SpringBootApplication
@EnableScheduling
public class SpringSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSchedulerApplication.class, args);
        log.info("╔══════════════════════════════════════════════╗");
        log.info("║   Spring Multi-Threaded Scheduler Started    ║");
        log.info("╚══════════════════════════════════════════════╝");
    }
}
