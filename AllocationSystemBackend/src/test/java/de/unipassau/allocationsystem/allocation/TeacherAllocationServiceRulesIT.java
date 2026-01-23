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
 * Integration tests for {@link TeacherAllocationService} focusing on rule/constraint validation.
 */
class TeacherAllocationServiceRulesIT extends TeacherAllocationServiceBaseIT {

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
    TeacherAllocationServiceRulesIT(
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
