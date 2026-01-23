package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.TeacherAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link TeacherAllocationService} verifying that allocations are created
 * for each supported internship type.
 */
class TeacherAllocationServiceInternshipTypesIT extends TeacherAllocationServiceBaseIT {

    /**
     * Verifies that the allocation process creates at least one assignment for SFP internship types.
     */
    @Test
    void testAllocateTeachersToSfp() {
        allocationService.performAllocation(year.getId());

        List<TeacherAssignment> assignments = teacherAssignmentRepository.findAll();
        assertThat(assignments)
                .anyMatch(a -> a.getInternshipType().getInternshipCode().startsWith("SFP"));
    }

    /**
     * Verifies that the allocation process creates at least one assignment for ZSP internship types.
     */
    @Test
    void testAllocateTeachersToZsp() {
        allocationService.performAllocation(year.getId());

        List<TeacherAssignment> assignments = teacherAssignmentRepository.findAll();
        assertThat(assignments)
                .anyMatch(a -> a.getInternshipType().getInternshipCode().startsWith("ZSP"));
    }

    /**
     * Verifies that the allocation process creates assignments for both PDP1 and PDP2 internship types.
     */
    @Test
    void testAllocateTeachersToPdp1AndPdp2() {
        allocationService.performAllocation(year.getId());

        List<TeacherAssignment> assignments = teacherAssignmentRepository.findAll();
        assertThat(assignments)
                .anyMatch(a -> a.getInternshipType().getInternshipCode().startsWith("PDP1"));
        assertThat(assignments)
                .anyMatch(a -> a.getInternshipType().getInternshipCode().startsWith("PDP2"));
    }
}
