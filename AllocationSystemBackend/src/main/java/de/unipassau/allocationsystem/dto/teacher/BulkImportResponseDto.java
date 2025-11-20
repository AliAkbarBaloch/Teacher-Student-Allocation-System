package de.unipassau.allocationsystem.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for bulk import response containing summary and detailed results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportResponseDto {
    private int totalRows;
    private int successfulRows;
    private int failedRows;
    private List<ImportResultRowDto> results;
}

