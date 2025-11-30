package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentCreateDto;
import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentResponseDto;
import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentUpdateDto;
import de.unipassau.allocationsystem.entity.*;
import de.unipassau.allocationsystem.mapper.TeacherAssignmentMapper;
import de.unipassau.allocationsystem.repository.*;
import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.entity.AuditLog;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherAssignmentService {

    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final AllocationPlanRepository allocationPlanRepository;
    private final TeacherRepository teacherRepository;
    private final InternshipTypeRepository internshipTypeRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherAssignmentMapper mapper;
    private final CreditHourTrackingService creditHourTrackingService;

    public Page<TeacherAssignment> listByPlan(Long planId, Long teacherId, Long internshipTypeId, Long subjectId, String status, Pageable pageable) {
        TeacherAssignment.AssignmentStatus parsedStatus = null;
        if (status != null && !status.isBlank()) {
            parsedStatus = TeacherAssignment.AssignmentStatus.valueOf(status);
        }
        return teacherAssignmentRepository.findByPlanWithFilters(planId, teacherId, internshipTypeId, subjectId, parsedStatus, pageable);
    }

    public TeacherAssignmentResponseDto getById(Long id) {
        TeacherAssignment ta = teacherAssignmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("TeacherAssignment not found with id: " + id));
        return mapper.toResponseDto(ta);
    }

    @Transactional
        @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.TEACHER_ASSIGNMENT,
            description = "Created teacher assignment",
            captureNewValue = true
        )
        public TeacherAssignmentResponseDto create(Long planId, TeacherAssignmentCreateDto dto, boolean isAdmin) {
        AllocationPlan plan = allocationPlanRepository.findById(planId)
                .orElseThrow(() -> new NoSuchElementException("AllocationPlan not found with id: " + planId));

        if (!plan.getStatus().isEditable() && !isAdmin) {
            throw new IllegalStateException("Allocation plan is not editable");
        }

        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new NoSuchElementException("Teacher not found with id: " + dto.getTeacherId()));

        if (!teacher.getEmploymentStatus().equals(Teacher.EmploymentStatus.ACTIVE)) {
            throw new IllegalStateException("Teacher is not active");
        }

        InternshipType internshipType = internshipTypeRepository.findById(dto.getInternshipTypeId())
                .orElseThrow(() -> new NoSuchElementException("InternshipType not found with id: " + dto.getInternshipTypeId()));

        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new NoSuchElementException("Subject not found with id: " + dto.getSubjectId()));

        if (!Boolean.TRUE.equals(subject.getIsActive())) {
            throw new IllegalStateException("Subject is not active");
        }

        // Uniqueness check
        if (teacherAssignmentRepository.existsByAllocationPlanIdAndTeacherIdAndInternshipTypeIdAndSubjectId(planId, dto.getTeacherId(), dto.getInternshipTypeId(), dto.getSubjectId())) {
            throw new IllegalStateException("Duplicate assignment for same plan/teacher/internship/subject");
        }

        TeacherAssignment ta = mapper.toEntity(dto, plan, teacher, internshipType, subject);
        ta.setAssignedAt(java.time.LocalDateTime.now());
        ta = teacherAssignmentRepository.save(ta);

        // update credit tracking
        Long yearId = plan.getAcademicYear().getId();
        creditHourTrackingService.recalculateForTeacherAndYear(teacher.getId(), yearId);

        return mapper.toResponseDto(ta);
    }

    @Transactional
        @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.TEACHER_ASSIGNMENT,
            description = "Updated teacher assignment",
            captureNewValue = true
        )
        public TeacherAssignmentResponseDto update(Long planId, Long id, TeacherAssignmentUpdateDto dto, boolean isAdmin) {
        TeacherAssignment existing = teacherAssignmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("TeacherAssignment not found with id: " + id));

        if (!existing.getAllocationPlan().getId().equals(planId)) {
            throw new IllegalArgumentException("Assignment does not belong to the provided plan");
        }

        AllocationPlan plan = existing.getAllocationPlan();
        if (!plan.getStatus().isEditable() && !isAdmin) {
            throw new IllegalStateException("Allocation plan is not editable");
        }

        TeacherAssignment before = new TeacherAssignment();
        // shallow copy for audit previous value
        before.setId(existing.getId());
        before.setStudentGroupSize(existing.getStudentGroupSize());
        before.setAssignmentStatus(existing.getAssignmentStatus());
        before.setIsManualOverride(existing.getIsManualOverride());
        before.setNotes(existing.getNotes());

        boolean recalcNeeded = false;

        if (dto.getStudentGroupSize() != null) {
            existing.setStudentGroupSize(dto.getStudentGroupSize());
            recalcNeeded = true;
        }
        if (dto.getAssignmentStatus() != null) {
            existing.setAssignmentStatus(TeacherAssignment.AssignmentStatus.valueOf(dto.getAssignmentStatus()));
            recalcNeeded = true;
        }
        if (dto.getIsManualOverride() != null) {
            if (dto.getIsManualOverride() && !isAdmin) {
                throw new IllegalStateException("Manual override requires ADMIN privileges");
            }
            existing.setIsManualOverride(dto.getIsManualOverride());
        }
        if (dto.getNotes() != null) {
            existing.setNotes(dto.getNotes());
        }


        existing = teacherAssignmentRepository.save(existing);

        if (recalcNeeded) {
            Long yearId = plan.getAcademicYear().getId();
            creditHourTrackingService.recalculateForTeacherAndYear(existing.getTeacher().getId(), yearId);
        }

        return mapper.toResponseDto(existing);
    }

    @Transactional
        @Audited(
            action = AuditLog.AuditAction.DELETE,
            entityName = AuditEntityNames.TEACHER_ASSIGNMENT,
            description = "Deleted teacher assignment",
            captureNewValue = false
        )
        public void delete(Long planId, Long id, boolean isAdmin) {
        TeacherAssignment existing = teacherAssignmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("TeacherAssignment not found with id: " + id));

        if (!existing.getAllocationPlan().getId().equals(planId)) {
            throw new IllegalArgumentException("Assignment does not belong to the provided plan");
        }

        AllocationPlan plan = existing.getAllocationPlan();
        if (!plan.getStatus().isEditable() && !isAdmin) {
            throw new IllegalStateException("Allocation plan is not editable");
        }

        TeacherAssignment before = existing;
        teacherAssignmentRepository.delete(existing);

        // recalc credit hours
        Long yearId = plan.getAcademicYear().getId();
        creditHourTrackingService.recalculateForTeacherAndYear(before.getTeacher().getId(), yearId);
    }

    public List<TeacherAssignment> listByTeacherAndYear(Long teacherId, Long yearId) {
        return teacherAssignmentRepository.findByTeacherIdAndYearId(teacherId, yearId);
    }
}
