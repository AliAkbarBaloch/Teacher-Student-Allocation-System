package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityCreateDto;
import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityResponseDto;
import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityUpdateDto;
import de.unipassau.allocationsystem.entity.*;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.TeacherAvailabilityMapper;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.TeacherAvailabilityRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TeacherAvailabilityService {

    private final TeacherAvailabilityRepository teacherAvailabilityRepository;
    private final TeacherRepository teacherRepository;
    private final AcademicYearRepository academicYearRepository;
    private final InternshipTypeRepository internshipTypeRepository;
    private final TeacherAvailabilityMapper teacherAvailabilityMapper;

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER_AVAILABILITY,
            description = "Viewed list of teacher availability entries",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    public Map<String, Object> getTeacherAvailability(
            Long teacherId, Long yearId, Long internshipTypeId, Map<String, String> queryParams) {

        if (!teacherRepository.existsById(teacherId)) {
            throw new ResourceNotFoundException("Teacher not found with ID: " + teacherId);
        }

        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);

        Specification<TeacherAvailability> spec = (root, query, cb) ->
                cb.equal(root.get("teacher").get("id"), teacherId);

        if (yearId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("academicYear").get("id"), yearId));
        }

        if (internshipTypeId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("internshipType").get("id"), internshipTypeId));
        }

        String sortField = "id".equals(params.sortBy()) ? "availabilityId" : params.sortBy();
        Sort sort = Sort.by(params.sortOrder(), sortField);
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Page<TeacherAvailability> page = teacherAvailabilityRepository.findAll(spec, pageable);
        Page<TeacherAvailabilityResponseDto> dtoPage = page.map(teacherAvailabilityMapper::toDto);

        return PaginationUtils.formatPaginationResponse(dtoPage);
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER_AVAILABILITY,
            description = "Viewed teacher availability entry details",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    public TeacherAvailabilityResponseDto getAvailabilityById(Long teacherId, Long availabilityId) {
        TeacherAvailability availability = teacherAvailabilityRepository
                .findByAvailabilityIdAndTeacherId(availabilityId, teacherId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Availability entry not found with ID: " + availabilityId + " for teacher ID: " + teacherId));

        return teacherAvailabilityMapper.toDto(availability);
    }

    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.TEACHER_AVAILABILITY,
            description = "Created new teacher availability entry",
            captureNewValue = true
    )
    @Transactional
    public TeacherAvailabilityResponseDto createAvailability(Long teacherId, TeacherAvailabilityCreateDto createDto) {
        if (!teacherId.equals(createDto.getTeacherId())) {
            throw new IllegalArgumentException("Teacher ID in path and request body must match");
        }

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + teacherId));

        if (!teacher.getIsActive()) {
            throw new IllegalArgumentException("Cannot create availability for inactive teacher: " +
                    teacher.getFirstName() + " " + teacher.getLastName());
        }

        AcademicYear academicYear = academicYearRepository.findById(createDto.getYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with ID: " + createDto.getYearId()));

        InternshipType internshipType = internshipTypeRepository.findById(createDto.getInternshipTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Internship type not found with ID: " + createDto.getInternshipTypeId()));

        validatePreferenceRank(createDto.getIsAvailable(), createDto.getPreferenceRank());

        if (teacherAvailabilityRepository.existsByTeacherIdAndAcademicYearIdAndInternshipTypeId(
                teacherId, createDto.getYearId(), createDto.getInternshipTypeId())) {
            throw new DuplicateResourceException(
                    "Availability entry already exists for this teacher, year, and internship type");
        }

        TeacherAvailability availability = new TeacherAvailability();
        availability.setTeacher(teacher);
        availability.setAcademicYear(academicYear);
        availability.setInternshipType(internshipType);
        availability.setIsAvailable(createDto.getIsAvailable());
        availability.setPreferenceRank(createDto.getPreferenceRank());
        availability.setNotes(createDto.getNotes());

        TeacherAvailability saved = teacherAvailabilityRepository.save(availability);
        return teacherAvailabilityMapper.toDto(saved);
    }

    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.TEACHER_AVAILABILITY,
            description = "Updated teacher availability entry",
            captureNewValue = true
    )
    @Transactional
    public TeacherAvailabilityResponseDto updateAvailability(
            Long teacherId, Long availabilityId, TeacherAvailabilityUpdateDto updateDto) {

        TeacherAvailability availability = teacherAvailabilityRepository
                .findByAvailabilityIdAndTeacherId(availabilityId, teacherId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Availability entry not found with ID: " + availabilityId + " for teacher ID: " + teacherId));

        Long newYearId = updateDto.getYearId() != null ? updateDto.getYearId() : availability.getAcademicYear().getId();
        Long newInternshipTypeId = updateDto.getInternshipTypeId() != null ? updateDto.getInternshipTypeId() : availability.getInternshipType().getId();

        if ((updateDto.getYearId() != null && !updateDto.getYearId().equals(availability.getAcademicYear().getId())) ||
            (updateDto.getInternshipTypeId() != null && !updateDto.getInternshipTypeId().equals(availability.getInternshipType().getId()))) {

            if (teacherAvailabilityRepository.existsByTeacherIdAndYearIdAndInternshipTypeIdAndIdNot(
                    teacherId, newYearId, newInternshipTypeId, availabilityId)) {
                throw new DuplicateResourceException(
                        "Availability entry already exists for this teacher, year, and internship type");
            }
        }

        if (updateDto.getYearId() != null) {
            AcademicYear academicYear = academicYearRepository.findById(updateDto.getYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with ID: " + updateDto.getYearId()));
            availability.setAcademicYear(academicYear);
        }

        if (updateDto.getInternshipTypeId() != null) {
            InternshipType internshipType = internshipTypeRepository.findById(updateDto.getInternshipTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Internship type not found with ID: " + updateDto.getInternshipTypeId()));
            availability.setInternshipType(internshipType);
        }

        teacherAvailabilityMapper.updateEntityFromDto(availability, updateDto);

        validatePreferenceRank(availability.getIsAvailable(), availability.getPreferenceRank());

        TeacherAvailability updated = teacherAvailabilityRepository.save(availability);
        return teacherAvailabilityMapper.toDto(updated);
    }

    @Audited(
            action = AuditLog.AuditAction.DELETE,
            entityName = AuditEntityNames.TEACHER_AVAILABILITY,
            description = "Deleted teacher availability entry",
            captureNewValue = false
    )
    @Transactional
    public void deleteAvailability(Long teacherId, Long availabilityId) {
        TeacherAvailability availability = teacherAvailabilityRepository
                .findByAvailabilityIdAndTeacherId(availabilityId, teacherId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Availability entry not found with ID: " + availabilityId + " for teacher ID: " + teacherId));

        teacherAvailabilityRepository.delete(availability);
    }

    private void validatePreferenceRank(Boolean isAvailable, Integer preferenceRank) {
        if (Boolean.FALSE.equals(isAvailable) && preferenceRank != null) {
            throw new IllegalArgumentException(
                    "Preference rank should be null when teacher is not available");
        }
    }
}