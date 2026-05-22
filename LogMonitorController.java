package com.scheduler.app.controller;

import com.scheduler.app.model.ApiResponse;
import com.scheduler.app.service.LogMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for log file monitoring.
 *
 * Endpoints:
 *   GET /api/logs/summary   → Overview of all log files + archive totals
 *   GET /api/logs/active    → Current .log file sizes (shows if any are near 10 MB)
 *   GET /api/logs/archived  → List of .gz archive files in logs/archived/
 */
@Slf4j
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogMonitorController {

    private final LogMonitorService logMonitorService;

    /**
     * GET /api/logs/summary
     * High-level overview: file counts, total sizes, rollover threshold.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary() {
        log.info("📋 Log summary requested");
        Map<String, Object> summary = logMonitorService.getSummary();
        return ResponseEntity.ok(ApiResponse.success(summary,
                "Log file summary — rollover occurs automatically at 10 MB"));
    }

    /**
     * GET /api/logs/active
     * Returns name, size, and whether each active .log file exceeds 10 MB.
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActiveLogs() {
        log.info("📄 Active log files requested");
        List<Map<String, Object>> files = logMonitorService.getActiveLogFileSizes();
        return ResponseEntity.ok(ApiResponse.success(files,
                "Found " + files.size() + " active log files. Files over 10 MB are auto-rolled to .gz by Logback."));
    }

    /**
     * GET /api/logs/archived
     * Returns all .gz archive files with their sizes.
     */
    @GetMapping("/archived")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getArchivedLogs() {
        log.info("📦 Archived log files requested");
        List<Map<String, Object>> files = logMonitorService.getArchivedLogs();
        return ResponseEntity.ok(ApiResponse.success(files,
                "Found " + files.size() + " archived .gz log files in logs/archived/"));
    }
}
