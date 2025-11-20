package de.unipassau.allocationsystem.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the result of importing a single teacher row.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultRowDto {
    private int rowNumber;
    private boolean success;
    private String error;
    private TeacherResponseDto teacher; // Only present if success is true
}

