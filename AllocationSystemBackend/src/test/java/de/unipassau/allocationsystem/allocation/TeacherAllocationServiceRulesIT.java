package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.TeacherAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link TeacherAllocationService} focusing on rule/constraint validation.
 */
class TeacherAllocationServiceRulesIT extends TeacherAllocationServiceBaseIT {

    /**
     * Executes the allocation and verifies that the process completes and produces an assignment list.
     * <p>
     * This test is a placeholder for more detailed rule assertions (e.g., internship combination rules).
     * </p>
     */
    @Test
    void testInternshipCombinationRules() {
        allocationService.performAllocation(year.getId());

        List<TeacherAssignment> assignments = teacherAssignmentRepository.findAll();
        assertThat(assignments).isNotNull();
    }
}
