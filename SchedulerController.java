package com.scheduler.app.controller;

import com.scheduler.app.exception.ServiceNotFoundException;
import com.scheduler.app.model.ApiResponse;
import com.scheduler.app.model.SchedulerResult;
import com.scheduler.app.properties.SchedulerProperties;
import com.scheduler.app.service.SchedulerExecutionTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * REST Controller to monitor and manage schedulers.
 *
 * Endpoints:
 *   GET  /api/schedulers/status          → Summary of all scheduler execution counts
 *   GET  /api/schedulers/{name}/history  → Execution history for a specific scheduler
 *   GET  /api/schedulers/{name}/last     → Last execution result for a specific scheduler
 *   GET  /api/schedulers/properties      → View loaded scheduler properties
 */
@Slf4j
@RestController
@RequestMapping("/api/schedulers")
@RequiredArgsConstructor
public class SchedulerController {

    private final SchedulerExecutionTracker tracker;
    private final SchedulerProperties       schedulerProperties;

    /**
     * GET /api/schedulers/status
     * Returns a summary of all scheduler execution counts (success/failed/total).
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Map<String, Long>>>> getStatus() {
        log.info("📊 Fetching scheduler status summary");
        Map<String, Map<String, Long>> summary = tracker.getSummary();

        return ResponseEntity.ok(ApiResponse.success(summary,
                "Scheduler status summary retrieved. Tracking " + summary.size() + " schedulers."));
    }

    /**
     * GET /api/schedulers/{schedulerName}/history
     * Returns execution history list for the named scheduler.
     */
    @GetMapping("/{schedulerName}/history")
    public ResponseEntity<ApiResponse<List<SchedulerResult>>> getHistory(
            @PathVariable String schedulerName) {

        log.info("📋 Fetching history for scheduler: {}", schedulerName);

        List<SchedulerResult> history = tracker.getHistory(schedulerName);

        if (history.isEmpty()) {
            throw new ServiceNotFoundException(schedulerName,
                    "No history found for scheduler '" + schedulerName + "'. " +
                    "Either it has not run yet or the name is incorrect.");
        }

        return ResponseEntity.ok(ApiResponse.success(history,
                "Found " + history.size() + " execution records for " + schedulerName));
    }

    /**
     * GET /api/schedulers/{schedulerName}/last
     * Returns the most recent execution result for the named scheduler.
     */
    @GetMapping("/{schedulerName}/last")
    public ResponseEntity<ApiResponse<SchedulerResult>> getLastResult(
            @PathVariable String schedulerName) {

        log.info("🔍 Fetching last result for scheduler: {}", schedulerName);

        SchedulerResult last = tracker.getLastResult(schedulerName);

        if (last == null) {
            throw new ServiceNotFoundException(schedulerName,
                    "Scheduler '" + schedulerName + "' has no recorded executions yet.");
        }

        return ResponseEntity.ok(ApiResponse.success(last,
                "Last execution result for " + schedulerName));
    }

    /**
     * GET /api/schedulers
     * Lists all known scheduler names.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<String>>> listSchedulers() {
        List<String> names = tracker.getSchedulerNames();
        return ResponseEntity.ok(ApiResponse.success(names,
                "Found " + names.size() + " registered schedulers"));
    }

    /**
     * GET /api/schedulers/properties
     * Returns the scheduler properties loaded from application.properties via InputStream.
     * Useful for debugging — shows what the scheduler actually loaded.
     */
    @GetMapping("/properties")
    public ResponseEntity<ApiResponse<Properties>> getProperties() {
        log.info("⚙️  Fetching loaded scheduler properties");
        Properties props = schedulerProperties.getAllProperties();

        return ResponseEntity.ok(ApiResponse.success(props,
                "Loaded " + props.size() + " properties from application.properties via InputStream"));
    }

    /**
     * GET /api/schedulers/health
     * Quick health check for the scheduler subsystem.
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "threadPoolSize", schedulerProperties.getThreadPoolSize(),
                "threadNamePrefix", schedulerProperties.getThreadNamePrefix(),
                "activeSchedulers", tracker.getSchedulerNames().size(),
                "retryMaxAttempts", schedulerProperties.getRetryMaxAttempts()
        );

        return ResponseEntity.ok(ApiResponse.success(health, "Scheduler subsystem is healthy"));
    }
}
