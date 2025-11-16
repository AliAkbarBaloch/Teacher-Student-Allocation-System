package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.AuditLogDto;
import de.unipassau.allocationsystem.dto.AuditLogStatsDto;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.mapper.AuditLogMapper;
import de.unipassau.allocationsystem.service.AuditLogService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing audit logs.
 * Provides endpoints for viewing, filtering, and exporting audit logs.
 * Access restricted to admin users only.
 */
@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@Tag(name = "AuditLogs", description = "Audit log management and reporting APIs")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final AuditLogMapper auditLogMapper;

    /**
     * Get all audit logs with pagination and optional filters.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get audit logs", 
        description = "Retrieve audit logs with optional filtering and pagination. Admin access required."
    )
    public ResponseEntity<Page<AuditLogDto>> getAuditLogs(
        @Parameter(description = "User ID to filter by") 
        @RequestParam(required = false) Long userId,
        
        @Parameter(description = "Action type to filter by") 
        @RequestParam(required = false) AuditAction action,
        
        @Parameter(description = "Target entity to filter by") 
        @RequestParam(required = false) String targetEntity,
        
        @Parameter(description = "Start date (ISO format)") 
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        
        @Parameter(description = "End date (ISO format)") 
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        
        @Parameter(description = "Page number (0-indexed)") 
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(description = "Page size") 
        @RequestParam(defaultValue = "20") int size,
        
        @Parameter(description = "Sort field") 
        @RequestParam(defaultValue = "eventTimestamp") String sortBy,
        
        @Parameter(description = "Sort direction (ASC or DESC)") 
        @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<AuditLog> auditLogs = auditLogService.getAuditLogs(
            userId, action, targetEntity, startDate, endDate, pageable
        );

        Page<AuditLogDto> auditLogDtos = auditLogs.map(auditLogMapper::toDto);
        return ResponseEntity.ok(auditLogDtos);
    }

    /**
     * Get audit logs for a specific entity and record.
     */
    @GetMapping("/entity/{entityName}/{recordId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get audit logs for entity", 
        description = "Retrieve all audit logs for a specific entity and record ID. Admin access required."
    )
    public ResponseEntity<?> getAuditLogsForEntity(
        @PathVariable String entityName,
        @PathVariable String recordId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventTimestamp"));
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsForEntity(entityName, recordId, pageable);
        Page<AuditLogDto> auditLogDtos = auditLogs.map(auditLogMapper::toDto);

        return ResponseHandler.success("Audit logs retrieved successfully", auditLogDtos);
    }

    /**
     * Get audit logs for a specific user.
     */
    @GetMapping("/user/{userIdentifier}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get audit logs for user", 
        description = "Retrieve all audit logs for a specific user. Admin access required."
    )
    public ResponseEntity<?> getAuditLogsForUser(
        @PathVariable String userIdentifier,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventTimestamp"));
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsForUser(userIdentifier, pageable);
        Page<AuditLogDto> auditLogDtos = auditLogs.map(auditLogMapper::toDto);

        return ResponseHandler.success("Audit logs retrieved successfully", auditLogDtos);
    }

    /**
     * Get recent audit logs for monitoring dashboard.
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get recent audit logs", 
        description = "Retrieve the 100 most recent audit logs for monitoring. Admin access required."
    )
    public ResponseEntity<?> getRecentAuditLogs() {
        List<AuditLog> auditLogs = auditLogService.getRecentAuditLogs();
        List<AuditLogDto> auditLogDtos = auditLogs.stream()
            .map(auditLogMapper::toDto)
            .collect(Collectors.toList());

        return ResponseHandler.success("Audit logs retrieved successfully", auditLogDtos);
    }

    /**
     * Get audit log statistics for reporting.
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get audit log statistics", 
        description = "Retrieve statistics about audit logs for a given date range. Admin access required."
    )
    public ResponseEntity<?> getStatistics(
        @Parameter(description = "Start date (ISO format)") 
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        
        @Parameter(description = "End date (ISO format)") 
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        AuditLogStatsDto stats = AuditLogStatsDto.builder()
            .actionStatistics(auditLogService.getActionStatistics(startDate, endDate))
            .entityStatistics(auditLogService.getEntityStatistics(startDate, endDate))
            .userActivityStatistics(auditLogService.getUserActivityStatistics(startDate, endDate))
            .build();

        // Calculate total
        long total = stats.getActionStatistics().values().stream()
            .mapToLong(Long::longValue)
            .sum();
        stats.setTotalLogs(total);

        return ResponseHandler.success("Audit logs retrieved successfully", stats);
    }

    /**
     * Export audit logs as CSV.
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Export audit logs", 
        description = "Export audit logs as CSV file with optional filters. Admin access required."
    )
    public ResponseEntity<byte[]> exportAuditLogs(
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) AuditAction action,
        @RequestParam(required = false) String targetEntity,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(defaultValue = "1000") int maxRecords
    ) {
        try {
            Pageable pageable = PageRequest.of(0, maxRecords, Sort.by(Sort.Direction.DESC, "eventTimestamp"));
            Page<AuditLog> auditLogs = auditLogService.getAuditLogs(
                userId, action, targetEntity, startDate, endDate, pageable
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);

            // Write CSV header
            writer.println("ID,User,Event Time,Action,Target Entity,Target Record ID,Description,IP Address");

            // Write data rows
            for (AuditLog log : auditLogs.getContent()) {
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

            writer.flush();
            byte[] csvBytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", 
                "audit-logs-" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv");
            headers.setContentLength(csvBytes.length);

            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper methods

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
