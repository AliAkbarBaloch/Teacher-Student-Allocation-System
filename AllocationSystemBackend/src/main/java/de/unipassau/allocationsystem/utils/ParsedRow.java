package de.unipassau.allocationsystem.utils;

import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import lombok.Data;

/**
 * Represents a parsed row with its DTO and metadata.
 */
@Data
public class ParsedRow {
    private int rowNumber;
    private TeacherCreateDto dto;
    private String schoolName;
    private Long schoolId;
}

