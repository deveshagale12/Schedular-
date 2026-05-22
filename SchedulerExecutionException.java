package com.scheduler.app.exception;

/**
 * Thrown when a scheduled service fails during execution.
 */
public class SchedulerExecutionException extends RuntimeException {

    private final String schedulerName;
    private final String errorCode;

    public SchedulerExecutionException(String schedulerName, String message) {
        super(message);
        this.schedulerName = schedulerName;
        this.errorCode = "SCHEDULER_EXECUTION_FAILED";
    }

    public SchedulerExecutionException(String schedulerName, String message, Throwable cause) {
        super(message, cause);
        this.schedulerName = schedulerName;
        this.errorCode = "SCHEDULER_EXECUTION_FAILED";
    }

    public SchedulerExecutionException(String schedulerName, String errorCode,
                                       String message, Throwable cause) {
        super(message, cause);
        this.schedulerName = schedulerName;
        this.errorCode = errorCode;
    }

    public String getSchedulerName() { return schedulerName; }
    public String getErrorCode()     { return errorCode; }
}
