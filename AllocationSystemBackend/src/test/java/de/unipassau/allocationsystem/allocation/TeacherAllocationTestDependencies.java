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
 * Exposes required Spring beans via a single injectable component, keeping test constructors
 * small while avoiding constructors with excessive parameter counts.
 * </p>
 */
@Component
final class TeacherAllocationTestDependencies {

    @Autowired
    private TeacherAllocationService allocationService;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private TeacherSubjectRepository teacherSubjectRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private InternshipDemandRepository internshipDemandRepository;

    @Autowired
    private TeacherAssignmentRepository teacherAssignmentRepository;

    @Autowired
    private InternshipTypeRepository internshipTypeRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private SubjectCategoryRepository subjectCategoryRepository;

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
