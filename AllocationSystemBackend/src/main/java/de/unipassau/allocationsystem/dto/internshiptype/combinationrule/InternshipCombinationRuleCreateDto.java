package de.unipassau.allocationsystem.dto.internshiptype.combinationrule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new internship combination rule.
 * Defines whether two internship types can be validly combined for a teacher.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternshipCombinationRuleCreateDto {
    private Long internshipType1Id;
    private Long internshipType2Id;
    private Boolean isValidCombination;
}
