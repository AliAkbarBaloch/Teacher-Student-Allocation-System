package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.TeacherAssignment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests verifying that allocations are created for each supported internship type.
 */
class TeacherAllocationServiceInternshipTypesTest extends TeacherAllocationServiceBaseTest {

    /**
     * Creates the test using constructor injection.
     *
     * @param deps dependency wrapper for allocation tests
     */
    @Autowired
    TeacherAllocationServiceInternshipTypesTest(TeacherAllocationTestDependencies deps) {
        super(deps);
    }

    @Test
    void testAllocateTeachersToSfp() {
        deps.allocationService().performAllocation(year.getId());

        List<TeacherAssignment> assignments = deps.teacherAssignmentRepository().findAll();
        assertThat(assignments)
                .anyMatch(a -> a.getInternshipType().getInternshipCode().startsWith("SFP"));
    }

    @Test
    void testAllocateTeachersToZsp() {
        deps.allocationService().performAllocation(year.getId());

        List<TeacherAssignment> assignments = deps.teacherAssignmentRepository().findAll();
        assertThat(assignments)
                .anyMatch(a -> a.getInternshipType().getInternshipCode().startsWith("ZSP"));
    }

    @Test
    void testAllocateTeachersToPdp1AndPdp2() {
        deps.allocationService().performAllocation(year.getId());

        List<TeacherAssignment> assignments = deps.teacherAssignmentRepository().findAll();
        assertThat(assignments)
                .anyMatch(a -> a.getInternshipType().getInternshipCode().startsWith("PDP1"));
        assertThat(assignments)
                .anyMatch(a -> a.getInternshipType().getInternshipCode().startsWith("PDP2"));
    }
}
