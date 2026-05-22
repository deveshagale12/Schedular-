package com.scheduler.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Holds the execution result of a scheduled task.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerResult {

    private String schedulerName;
    private String status;           // SUCCESS, FAILED, SKIPPED
    private String threadName;
    private long executionTimeMs;
    private String message;
    private String errorDetail;

    @Builder.Default
    private LocalDateTime executedAt = LocalDateTime.now();
}
