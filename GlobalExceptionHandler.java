package com.scheduler.app.exception;

import com.scheduler.app.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global Exception Handler.
 *
 * Intercepts all exceptions thrown by controllers and schedulers (via REST endpoints)
 * and returns a unified {@link ApiResponse} with structured error details.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── Scheduler Execution Exception ────────────────────

    @ExceptionHandler(SchedulerExecutionException.class)
    public ResponseEntity<ApiResponse<Void>> handleSchedulerExecutionException(
            SchedulerExecutionException ex, WebRequest request) {

        log.error("⚠️  Scheduler [{}] failed with code [{}]: {}",
                ex.getSchedulerName(), ex.getErrorCode(), ex.getMessage(), ex);

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.builder()
                .errorCode(ex.getErrorCode())
                .description(ex.getMessage())
                .schedulerName(ex.getSchedulerName())
                .threadName(Thread.currentThread().getName())
                .exceptionClass(ex.getClass().getSimpleName())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(
                        "Scheduler '" + ex.getSchedulerName() + "' execution failed", error));
    }

    // ─── Service Not Found Exception ──────────────────────

    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleServiceNotFoundException(
            ServiceNotFoundException ex, WebRequest request) {

        log.warn("🔍 Service not found: {}", ex.getServiceName());

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.builder()
                .errorCode("SERVICE_NOT_FOUND")
                .description(ex.getMessage())
                .schedulerName(ex.getServiceName())
                .threadName(Thread.currentThread().getName())
                .exceptionClass(ex.getClass().getSimpleName())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure("Service not found: " + ex.getServiceName(), error));
    }

    // ─── Illegal Argument Exception ───────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        log.warn("❌ Invalid argument: {}", ex.getMessage());

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.builder()
                .errorCode("INVALID_ARGUMENT")
                .description(ex.getMessage())
                .threadName(Thread.currentThread().getName())
                .exceptionClass(ex.getClass().getSimpleName())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("Invalid request argument", error));
    }

    // ─── Illegal State Exception ───────────────────────────

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {

        log.error("⚠️  Illegal state: {}", ex.getMessage());

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.builder()
                .errorCode("ILLEGAL_STATE")
                .description(ex.getMessage())
                .threadName(Thread.currentThread().getName())
                .exceptionClass(ex.getClass().getSimpleName())
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure("Application state error", error));
    }

    // ─── Generic Runtime Exception ────────────────────────

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        log.error("🔴 Unexpected runtime exception: {}", ex.getMessage(), ex);

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.builder()
                .errorCode("RUNTIME_ERROR")
                .description(ex.getMessage())
                .threadName(Thread.currentThread().getName())
                .exceptionClass(ex.getClass().getSimpleName())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("An unexpected error occurred", error));
    }

    // ─── Catch-All Exception ──────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception ex, WebRequest request) {

        log.error("🔴 Unhandled exception: {}", ex.getMessage(), ex);

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .description("An internal server error occurred. Please contact support.")
                .threadName(Thread.currentThread().getName())
                .exceptionClass(ex.getClass().getSimpleName())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("Internal server error", error));
    }
}
