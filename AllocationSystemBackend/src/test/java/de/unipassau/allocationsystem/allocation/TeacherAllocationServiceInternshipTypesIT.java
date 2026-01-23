package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.TeacherAssignment;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.InternshipDemandRepository;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.repository.TeacherSubjectRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link TeacherAllocationService} verifying that allocations are created
 * for each supported internship type.
 */
class TeacherAllocationServiceInternshipTypesIT extends TeacherAllocationServiceBaseIT {

    /**
     * Creates the integration test using constructor injection.
     *
     * @param allocationService allocation service under test
     * @param academicYearRepository academic year repository
     * @param teacherSubjectRepository teacher-subject repository
     * @param teacherRepository teacher repository
     * @param internshipDemandRepository internship demand repository
     * @param teacherAssignmentRepository teacher assignment repository
     * @param internshipTypeRepository internship type repository
     * @param subjectRepository subject repository
     * @param schoolRepository school repository
     * @param subjectCategoryRepository subject category repository
     */
    @Autowired
    TeacherAllocationServiceInternshipTypesIT(
            TeacherAllocationService allocationService,
            AcademicYearRepository academicYearRepository,
            TeacherSubjectRepository teacherSubjectRepository,
            TeacherRepository teacherRepository,
            InternshipDemandRepository internshipDemandRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            InternshipTypeRepository internshipTypeRepository,
            SubjectRepository subjectRepository,
            SchoolRepository schoolRepository,
            SubjectCategoryRepository subjectCategoryRepository
    ) {
        super(
                allocationService,
                academicYearRepository,
                teacherSubjectRepository,
                teacherRepository,
                internshipDemandRepository,
                teacherAssignmentRepository,
                internshipTypeRepository,
                subjectRepository,
                schoolRepository,
                subjectCategoryRepository
        );
    }


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
