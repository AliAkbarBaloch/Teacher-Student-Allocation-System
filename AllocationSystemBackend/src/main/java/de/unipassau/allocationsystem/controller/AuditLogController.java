package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.auditlog.AuditLogDto;
import de.unipassau.allocationsystem.dto.auditlog.AuditLogFilterDto;
import de.unipassau.allocationsystem.dto.auditlog.AuditLogStatsDto;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.mapper.AuditLogMapper;
import de.unipassau.allocationsystem.service.audit.AuditLogExportService;
import de.unipassau.allocationsystem.service.audit.AuditLogQueryService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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

    private final AuditLogQueryService queryService;
    private final AuditLogExportService exportService;
    private final AuditLogMapper auditLogMapper;

    /**
     * Get all audit logs with pagination and optional filters.
     * Uses DTO wrapper to encapsulate query parameters.
     *
     * @param filterDto DTO containing all filter and pagination parameters
     * @return ResponseEntity containing paginated audit logs
     */
        @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get audit logs",
            description = "Retrieve audit logs with optional filtering and pagination. Admin access required."
    )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully",
                content = @Content(schema = @Schema(implementation = AuditLogDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or filter parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<?> getAuditLogs(@ModelAttribute AuditLogFilterDto filterDto) {
        Pageable pageable = createPageable(
            filterDto.getPage() != null ? filterDto.getPage() : 0, 
            filterDto.getSize() != null ? filterDto.getSize() : 10, 
            filterDto.getSortBy(), 
            filterDto.getSortDirection()
        );
        Page<AuditLog> auditLogs = queryService.getAuditLogs(
            filterDto.getUserId(), 
            filterDto.getAction(), 
            filterDto.getTargetEntity(), 
            filterDto.getStartDate(), 
            filterDto.getEndDate(), 
            pageable
        );

        return ResponseHandler.success("Audit logs retrieved successfully", auditLogs.map(auditLogMapper::toDto));
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
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully",
                content = @Content(schema = @Schema(implementation = AuditLogDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    public ResponseEntity<?> getAuditLogsForEntity(
        @PathVariable String entityName,
        @PathVariable String recordId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = createPageable(page, size, "eventTimestamp", "DESC");
        Page<AuditLogDto> auditLogDtos = queryService
                .getAuditLogsForEntity(entityName, recordId, pageable)
                .map(auditLogMapper::toDto);

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
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully",
                content = @Content(schema = @Schema(implementation = AuditLogDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    public ResponseEntity<?> getAuditLogsForUser(
        @PathVariable String userIdentifier,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = createPageable(page, size, "eventTimestamp", "DESC");
        Page<AuditLogDto> auditLogDtos = queryService
                .getAuditLogsForUser(userIdentifier, pageable)
                .map(auditLogMapper::toDto);

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
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully",
                content = @Content(schema = @Schema(implementation = AuditLogDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    public ResponseEntity<?> getRecentAuditLogs() {
        List<AuditLogDto> auditLogDtos = queryService.getRecentAuditLogs()
                .stream()
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
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                content = @Content(schema = @Schema(implementation = AuditLogStatsDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or missing date parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    public ResponseEntity<?> getStatistics(
        @Parameter(description = "Start date (ISO format)") 
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        
        @Parameter(description = "End date (ISO format)") 
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        AuditLogStatsDto stats = AuditLogStatsDto.builder()
                .actionStatistics(queryService.getActionStatistics(startDate, endDate))
                .entityStatistics(queryService.getEntityStatistics(startDate, endDate))
                .userActivityStatistics(queryService.getUserActivityStatistics(startDate, endDate))
                .build();

        long total = stats.getActionStatistics().values().stream()
                .mapToLong(Long::longValue)
                .sum();
        stats.setTotalLogs(total);

        return ResponseHandler.success("Statistics retrieved successfully", stats);
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV export generated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<byte[]> exportAuditLogs(
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) AuditAction action,
        @RequestParam(required = false) String targetEntity,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(defaultValue = "1000") int maxRecords
    ) {
            Pageable pageable = PageRequest.of(0, maxRecords,
                    Sort.by(Sort.Direction.DESC, "eventTimestamp"));
            Page<AuditLog> auditLogs = queryService.getAuditLogs(
                    userId, action, targetEntity, startDate, endDate, pageable);

            byte[] csvBytes = exportService.exportToCsv(auditLogs.getContent());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment",
                    exportService.generateFileName());
            headers.setContentLength(csvBytes.length);

            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        // Default sort field and direction if not provided
        String actualSortBy = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "eventTimestamp";
        Sort.Direction direction = (sortDirection != null && sortDirection.equalsIgnoreCase("ASC"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, actualSortBy));
    }
}
