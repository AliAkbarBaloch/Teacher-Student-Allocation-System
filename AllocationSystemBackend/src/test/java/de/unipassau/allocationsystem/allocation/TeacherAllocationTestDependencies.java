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
 * Uses setter injection to satisfy rules that forbid field injection while also avoiding
 * constructors with too many parameters.
 * </p>
 */
@Component
final class TeacherAllocationTestDependencies {

    private TeacherAllocationService allocationService;
    private AcademicYearRepository academicYearRepository;
    private TeacherSubjectRepository teacherSubjectRepository;
    private TeacherRepository teacherRepository;
    private InternshipDemandRepository internshipDemandRepository;
    private TeacherAssignmentRepository teacherAssignmentRepository;
    private InternshipTypeRepository internshipTypeRepository;
    private SubjectRepository subjectRepository;
    private SchoolRepository schoolRepository;
    private SubjectCategoryRepository subjectCategoryRepository;

    /** @param allocationService allocation service under test */
    @Autowired
    void setAllocationService(TeacherAllocationService allocationService) {
        this.allocationService = allocationService;
    }

    /** @param academicYearRepository academic year repository */
    @Autowired
    void setAcademicYearRepository(AcademicYearRepository academicYearRepository) {
        this.academicYearRepository = academicYearRepository;
    }

    /** @param teacherSubjectRepository teacher-subject repository */
    @Autowired
    void setTeacherSubjectRepository(TeacherSubjectRepository teacherSubjectRepository) {
        this.teacherSubjectRepository = teacherSubjectRepository;
    }

    /** @param teacherRepository teacher repository */
    @Autowired
    void setTeacherRepository(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    /** @param internshipDemandRepository internship demand repository */
    @Autowired
    void setInternshipDemandRepository(InternshipDemandRepository internshipDemandRepository) {
        this.internshipDemandRepository = internshipDemandRepository;
    }

    /** @param teacherAssignmentRepository teacher assignment repository */
    @Autowired
    void setTeacherAssignmentRepository(TeacherAssignmentRepository teacherAssignmentRepository) {
        this.teacherAssignmentRepository = teacherAssignmentRepository;
    }

    /** @param internshipTypeRepository internship type repository */
    @Autowired
    void setInternshipTypeRepository(InternshipTypeRepository internshipTypeRepository) {
        this.internshipTypeRepository = internshipTypeRepository;
    }

    /** @param subjectRepository subject repository */
    @Autowired
    void setSubjectRepository(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    /** @param schoolRepository school repository */
    @Autowired
    void setSchoolRepository(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    /** @param subjectCategoryRepository subject category repository */
    @Autowired
    void setSubjectCategoryRepository(SubjectCategoryRepository subjectCategoryRepository) {
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
