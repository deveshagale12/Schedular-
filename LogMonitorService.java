package com.scheduler.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

/**
 * LogMonitorService
 *
 * Monitors the logs/ directory at runtime:
 *  - Reports current size of each scheduler log file
 *  - Lists all archived .gz files in logs/archived/
 *  - Exposes data to LogMonitorController for the REST API
 *
 * Note: actual rolling & compression is handled automatically by
 * Logback's SizeAndTimeBasedRollingPolicy (threshold = 10 MB).
 * This service is purely for observability.
 */
@Slf4j
@Service
public class LogMonitorService {

    private static final long   TEN_MB_BYTES    = 10L * 1024 * 1024;
    private static final String LOG_DIR         = "logs";
    private static final String ARCHIVE_DIR     = "logs/archived";

    /**
     * Returns size metadata for every active .log file.
     */
    public List<Map<String, Object>> getActiveLogFileSizes() {
        File dir = new File(LOG_DIR);
        List<Map<String, Object>> result = new ArrayList<>();

        if (!dir.exists() || !dir.isDirectory()) {
            log.warn("⚠️  Log directory '{}' does not exist yet.", LOG_DIR);
            return result;
        }

        File[] logFiles = dir.listFiles(f -> f.isFile() && f.getName().endsWith(".log"));
        if (logFiles == null) return result;

        for (File f : logFiles) {
            long sizeBytes = f.length();
            double sizeMb  = sizeBytes / (1024.0 * 1024.0);

            Map<String, Object> info = new LinkedHashMap<>();
            info.put("fileName",       f.getName());
            info.put("sizeBytes",      sizeBytes);
            info.put("sizeMB",         String.format("%.3f MB", sizeMb));
            info.put("exceedsLimit",   sizeBytes >= TEN_MB_BYTES);
            info.put("limitMB",        "10 MB");
            info.put("lastModified",   new Date(f.lastModified()).toString());

            if (sizeBytes >= TEN_MB_BYTES) {
                log.warn("⚠️  Log file '{}' has reached {:.3f} MB — Logback will roll it on next write.",
                        f.getName(), sizeMb);
            }

            result.add(info);
        }

        // Sort by size descending
        result.sort((a, b) -> Long.compare((Long) b.get("sizeBytes"), (Long) a.get("sizeBytes")));
        return result;
    }

    /**
     * Lists all archived .gz files with their sizes.
     */
    public List<Map<String, Object>> getArchivedLogs() {
        File archiveDir = new File(ARCHIVE_DIR);
        List<Map<String, Object>> result = new ArrayList<>();

        if (!archiveDir.exists() || !archiveDir.isDirectory()) {
            return result;  // No archives yet — perfectly normal
        }

        File[] gzFiles = archiveDir.listFiles(f -> f.isFile() && f.getName().endsWith(".gz"));
        if (gzFiles == null) return result;

        for (File f : gzFiles) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("fileName",  f.getName());
            info.put("sizeBytes", f.length());
            info.put("sizeMB",    String.format("%.3f MB", f.length() / (1024.0 * 1024.0)));
            info.put("archived",  new Date(f.lastModified()).toString());
            result.add(info);
        }

        result.sort((a, b) -> b.get("fileName").toString().compareTo(a.get("fileName").toString()));
        return result;
    }

    /**
     * Returns a summary: total active log size, total archive size, file counts.
     */
    public Map<String, Object> getSummary() {
        List<Map<String, Object>> active   = getActiveLogFileSizes();
        List<Map<String, Object>> archived = getArchivedLogs();

        long totalActiveBytes   = active.stream().mapToLong(m -> (Long) m.get("sizeBytes")).sum();
        long totalArchivedBytes = archived.stream().mapToLong(m -> (Long) m.get("sizeBytes")).sum();
        long nearLimitCount     = active.stream().filter(m -> (Boolean) m.get("exceedsLimit")).count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("checkedAt",              LocalDateTime.now().toString());
        summary.put("activeLogFiles",         active.size());
        summary.put("archivedGzFiles",        archived.size());
        summary.put("totalActiveSizeMB",      String.format("%.3f MB", totalActiveBytes  / (1024.0 * 1024.0)));
        summary.put("totalArchivedSizeMB",    String.format("%.3f MB", totalArchivedBytes/ (1024.0 * 1024.0)));
        summary.put("filesExceedingLimit",    nearLimitCount);
        summary.put("rolloverThreshold",      "10 MB — Logback auto-rolls to .gz");
        summary.put("archiveLocation",        ARCHIVE_DIR);

        return summary;
    }
}
