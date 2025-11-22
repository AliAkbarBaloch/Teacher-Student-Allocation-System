package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingResponseDto;
import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingUpdateDto;
import de.unipassau.allocationsystem.entity.*;
import de.unipassau.allocationsystem.mapper.CreditHourTrackingMapper;
import de.unipassau.allocationsystem.repository.CreditHourTrackingRepository;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
 

@Service
@RequiredArgsConstructor
public class CreditHourTrackingService {

    private final CreditHourTrackingRepository repository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TeacherRepository teacherRepository;
    private final AcademicYearRepository academicYearRepository;
    private final CreditHourTrackingMapper mapper;

    public CreditHourTrackingResponseDto getById(Long id) {
        CreditHourTracking e = repository.findById(id).orElseThrow(() -> new NoSuchElementException("CreditHourTracking not found"));
        return mapper.toResponseDto(e);
    }

    public CreditHourTrackingResponseDto getByTeacherAndYear(Long teacherId, Long yearId) {
        return repository.findByTeacherIdAndAcademicYearId(teacherId, yearId)
                .map(mapper::toResponseDto)
                .orElse(null);
    }

    public Page<CreditHourTrackingResponseDto> listByYearWithFilters(Long yearId, Long teacherId, Double minBalance, Double maxBalance, Double minHours, Double maxHours, Pageable pageable) {
        Specification<CreditHourTracking> spec = (root, query, cb) -> cb.equal(root.get("academicYear").get("id"), yearId);
        if (teacherId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("teacher").get("id"), teacherId));
        }
        if (minBalance != null) {
            spec = spec.and((root, q, cb) -> cb.ge(root.get("creditBalance"), minBalance));
        }
        if (maxBalance != null) {
            spec = spec.and((root, q, cb) -> cb.le(root.get("creditBalance"), maxBalance));
        }
        if (minHours != null) {
            spec = spec.and((root, q, cb) -> cb.ge(root.get("creditHoursAllocated"), minHours));
        }
        if (maxHours != null) {
            spec = spec.and((root, q, cb) -> cb.le(root.get("creditHoursAllocated"), maxHours));
        }

        return repository.findAll(spec, pageable).map(mapper::toResponseDto);
    }

    @Transactional
    @Audited(action = de.unipassau.allocationsystem.entity.AuditLog.AuditAction.UPDATE, entityName = AuditEntityNames.CREDIT_HOUR_TRACKING, description = "Updated credit hour tracking", captureNewValue = true)
    public CreditHourTrackingResponseDto update(Long id, CreditHourTrackingUpdateDto dto) {
        CreditHourTracking existing = repository.findById(id).orElseThrow(() -> new NoSuchElementException("CreditHourTracking not found"));
        mapper.updateEntityFromDto(dto, existing);
        existing = repository.save(existing);
        return mapper.toResponseDto(existing);
    }

    @Transactional
    public void recalculateForTeacherAndYear(Long teacherId, Long yearId) {
        // Validation of teacher and year existence
        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(() -> new NoSuchElementException("Teacher not found"));
        AcademicYear year = academicYearRepository.findById(yearId).orElseThrow(() -> new NoSuchElementException("AcademicYear not found"));

        List<TeacherAssignment> assignments = teacherAssignmentRepository.findByTeacherIdAndYearId(teacherId, yearId);
        // Count active assignments (PLANNED or CONFIRMED)
        long activeCount = assignments.stream()
                .filter(a -> a.getAssignmentStatus() == TeacherAssignment.AssignmentStatus.PLANNED || a.getAssignmentStatus() == TeacherAssignment.AssignmentStatus.CONFIRMED)
                .count();

        // Determine hours per assignment based on school's type
        double hoursPerAssignment = year.getTotalCreditHours();
        if (teacher.getSchool() != null && year != null) {
            School.SchoolType st = teacher.getSchool().getSchoolType();
            if (st == School.SchoolType.PRIMARY) {
                hoursPerAssignment = year.getElementarySchoolHours();
            } else if (st == School.SchoolType.MIDDLE) {
                hoursPerAssignment = year.getMiddleSchoolHours();
            } else {
                hoursPerAssignment = year.getTotalCreditHours();
            }
        }

        int assignmentsCount = (int) activeCount;
        double creditHoursAllocated = assignmentsCount * hoursPerAssignment;
        // For balance, use total credit hours minus allocated as a simple business rule
        double creditBalance = year.getTotalCreditHours() - creditHoursAllocated;

        CreditHourTracking record = repository.findByTeacherIdAndAcademicYearId(teacherId, yearId).orElseGet(() -> {
            CreditHourTracking c = new CreditHourTracking();
            c.setTeacher(teacher);
            c.setAcademicYear(year);
            return c;
        });

        record.setAssignmentsCount(assignmentsCount);
        record.setCreditHoursAllocated(creditHoursAllocated);
        record.setCreditBalance(creditBalance);

        repository.save(record);
    }
}

