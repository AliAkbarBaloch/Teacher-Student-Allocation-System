package de.unipassau.allocationsystem.service.audit;

import de.unipassau.allocationsystem.entity.AuditLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
/**
 * Service for exporting audit logs to various formats.
 * Provides CSV export functionality with proper formatting and timestamps.
 */
public class AuditLogExportService {
    private static final String[] CSV_HEADERS = {
            "ID", "User", "Event Time", "Action", "Target Entity",
            "Target Record ID", "Description", "IP Address"
    };

    /**
     * Export audit logs to CSV format.
     */
    public byte[] exportToCsv(List<AuditLog> auditLogs) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8)) {
            writeCSVHeader(writer);
            writeCSVData(writer, auditLogs);
        }
        return outputStream.toByteArray();
    }

    /**
     * Generate filename for export with timestamp.
     */
    public String generateFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return String.format("audit-logs-%s.csv", timestamp);
    }

    private void writeCSVHeader(PrintWriter writer) {
        writer.println(String.join(",", CSV_HEADERS));
    }

    private void writeCSVData(PrintWriter writer, List<AuditLog> auditLogs) {
        for (AuditLog log : auditLogs) {
            writer.printf("%d,%s,%s,%s,%s,%s,%s,%s%n",
                    log.getId(),
                    escapeCSV(log.getUserIdentifier()),
                    log.getEventTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    log.getAction().name(),
                    escapeCSV(log.getTargetEntity()),
                    escapeCSV(log.getTargetRecordId()),
                    escapeCSV(log.getDescription()),
                    escapeCSV(log.getIpAddress())
            );
        }
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
