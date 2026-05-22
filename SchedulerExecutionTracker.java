package com.scheduler.app.service;

import com.scheduler.app.model.SchedulerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tracks execution results of all schedulers in memory.
 * Thread-safe: uses ConcurrentHashMap and CopyOnWriteArrayList.
 */
@Slf4j
@Component
public class SchedulerExecutionTracker {

    // Map: schedulerName -> last 50 execution results
    private final Map<String, CopyOnWriteArrayList<SchedulerResult>> executionHistory =
            new ConcurrentHashMap<>();

    private static final int MAX_HISTORY_PER_SCHEDULER = 50;

    /**
     * Records a scheduler execution result.
     */
    public void record(SchedulerResult result) {
        executionHistory.computeIfAbsent(result.getSchedulerName(),
                k -> new CopyOnWriteArrayList<>());

        CopyOnWriteArrayList<SchedulerResult> history =
                executionHistory.get(result.getSchedulerName());

        history.add(result);

        // Trim to max history size
        while (history.size() > MAX_HISTORY_PER_SCHEDULER) {
            history.remove(0);
        }

        log.debug("📋 Recorded result for [{}]: status={}", result.getSchedulerName(), result.getStatus());
    }

    /**
     * Returns execution history for a specific scheduler.
     */
    public List<SchedulerResult> getHistory(String schedulerName) {
        CopyOnWriteArrayList<SchedulerResult> history = executionHistory.get(schedulerName);
        return history != null ? new ArrayList<>(history) : Collections.emptyList();
    }

    /**
     * Returns the last execution result for a scheduler.
     */
    public SchedulerResult getLastResult(String schedulerName) {
        List<SchedulerResult> history = getHistory(schedulerName);
        return history.isEmpty() ? null : history.get(history.size() - 1);
    }

    /**
     * Returns all known scheduler names.
     */
    public List<String> getSchedulerNames() {
        return new ArrayList<>(executionHistory.keySet());
    }

    /**
     * Returns summary counts (success/failed) for all schedulers.
     */
    public Map<String, Map<String, Long>> getSummary() {
        Map<String, Map<String, Long>> summary = new ConcurrentHashMap<>();
        executionHistory.forEach((name, results) -> {
            long success = results.stream().filter(r -> "SUCCESS".equals(r.getStatus())).count();
            long failed  = results.stream().filter(r -> "FAILED".equals(r.getStatus())).count();
            summary.put(name, Map.of("success", success, "failed", failed, "total", (long) results.size()));
        });
        return summary;
    }
}
