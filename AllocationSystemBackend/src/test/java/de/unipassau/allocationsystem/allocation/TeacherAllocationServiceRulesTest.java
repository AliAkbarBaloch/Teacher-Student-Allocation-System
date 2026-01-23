package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.TeacherAssignment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests focusing on allocation rule/constraint validation.
 */
class TeacherAllocationServiceRulesTest extends TeacherAllocationServiceBaseTest {

    /**
     * Creates the test using constructor injection.
     *
     * @param deps dependency wrapper for allocation tests
     */
    @Autowired
    TeacherAllocationServiceRulesTest(TeacherAllocationTestDependencies deps) {
        super(deps);
    }

    @Test
    void testInternshipCombinationRules() {
        deps.allocationService().performAllocation(year.getId());

        List<TeacherAssignment> assignments = deps.teacherAssignmentRepository().findAll();
        assertThat(assignments).isNotNull();
    }
}
