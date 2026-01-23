package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.InternshipDemandRepository;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.repository.TeacherSubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Dependency wrapper for teacher allocation integration tests.
 * <p>
 * Groups required Spring beans into a single injectable component to keep constructors
 * within parameter-count limits and enforce constructor injection.
 * </p>
 */
@Component
final class TeacherAllocationTestDependencies {

    private final TeacherAllocationService allocationService;
    private final AcademicYearRepository academicYearRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final TeacherRepository teacherRepository;
    private final InternshipDemandRepository internshipDemandRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final InternshipTypeRepository internshipTypeRepository;
    private final SubjectRepository subjectRepository;
    private final SchoolRepository schoolRepository;
    private final SubjectCategoryRepository subjectCategoryRepository;

    /**
     * Creates the dependency wrapper.
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
    TeacherAllocationTestDependencies(
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
        this.allocationService = allocationService;
        this.academicYearRepository = academicYearRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.teacherRepository = teacherRepository;
        this.internshipDemandRepository = internshipDemandRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.internshipTypeRepository = internshipTypeRepository;
        this.subjectRepository = subjectRepository;
        this.schoolRepository = schoolRepository;
        this.subjectCategoryRepository = subjectCategoryRepository;
    }

    TeacherAllocationService allocationService() {
        return allocationService;
    }

    AcademicYearRepository academicYearRepository() {
        return academicYearRepository;
    }

    TeacherSubjectRepository teacherSubjectRepository() {
        return teacherSubjectRepository;
    }

    TeacherRepository teacherRepository() {
        return teacherRepository;
    }

    InternshipDemandRepository internshipDemandRepository() {
        return internshipDemandRepository;
    }

    TeacherAssignmentRepository teacherAssignmentRepository() {
        return teacherAssignmentRepository;
    }

    InternshipTypeRepository internshipTypeRepository() {
        return internshipTypeRepository;
    }

    SubjectRepository subjectRepository() {
        return subjectRepository;
    }

    SchoolRepository schoolRepository() {
        return schoolRepository;
    }

    SubjectCategoryRepository subjectCategoryRepository() {
        return subjectCategoryRepository;
    }
}
