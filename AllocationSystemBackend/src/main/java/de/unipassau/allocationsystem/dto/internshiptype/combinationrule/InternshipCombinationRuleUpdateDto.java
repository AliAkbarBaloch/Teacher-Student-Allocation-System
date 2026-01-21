package de.unipassau.allocationsystem.dto.internshiptype.combinationrule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing internship combination rule.
 * Allows modification of the combination validity status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternshipCombinationRuleUpdateDto {
    private Boolean isValidCombination;
}
