package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.dto.SchoolCreateDto;
import de.unipassau.allocationsystem.dto.SchoolResponseDto;
import de.unipassau.allocationsystem.dto.SchoolUpdateDto;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.School.SchoolType;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.SchoolMapper;
import de.unipassau.allocationsystem.repository.SchoolRepository;
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

/**
 * Service for managing School entities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final SchoolMapper schoolMapper;

    /**
     * Get all schools with optional filtering and pagination.
     */
    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.SCHOOL,
            description = "Viewed list of schools",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    public Map<String, Object> getAllSchools(Map<String, String> queryParams, String search, 
                                             SchoolType schoolType, Integer zoneNumber, 
                                             Boolean isActive) {
        log.debug("Fetching schools with filters - search: {}, type: {}, zone: {}, active: {}", 
                  search, schoolType, zoneNumber, isActive);

        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<School> spec = (root, query, cb) -> cb.conjunction();

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("schoolName")), 
                       "%" + search.toLowerCase().trim() + "%"));
        }

        if (schoolType != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("schoolType"), schoolType));
        }

        if (zoneNumber != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("zoneNumber"), zoneNumber));
        }

        if (isActive != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("isActive"), isActive));
        }

        Page<School> page = schoolRepository.findAll(spec, pageable);
        Page<SchoolResponseDto> dtoPage = page.map(schoolMapper::toResponseDto);

        return PaginationUtils.formatPaginationResponse(dtoPage);
    }

    /**
     * Get school by ID.
     */
    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.SCHOOL,
            description = "Viewed school information",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    public SchoolResponseDto getSchoolById(Long id) {
        log.debug("Fetching school with id: {}", id);
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + id));
        return schoolMapper.toResponseDto(school);
    }

    /**
     * Create a new school.
     */
    @Audited(
            action = AuditAction.CREATE,
            entityName = AuditEntityNames.SCHOOL,
            description = "Created new school",
            captureNewValue = true
    )
    public SchoolResponseDto createSchool(SchoolCreateDto createDto) {
        log.info("Creating new school: {}", createDto.getSchoolName());

        // Validate unique school name
        if (schoolRepository.existsBySchoolName(createDto.getSchoolName())) {
            throw new DuplicateResourceException("School with name '" + 
                createDto.getSchoolName() + "' already exists");
        }

        School school = schoolMapper.toEntity(createDto);
        School savedSchool = schoolRepository.save(school);
        
        log.info("School created successfully with id: {}", savedSchool.getId());
        return schoolMapper.toResponseDto(savedSchool);
    }

    /**
     * Update an existing school.
     */
    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.SCHOOL,
            description = "Updated school information",
            captureNewValue = true
    )
    public SchoolResponseDto updateSchool(Long id, SchoolUpdateDto updateDto) {
        log.info("Updating school with id: {}", id);

        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + id));

        // Validate unique school name if it's being changed
        if (updateDto.getSchoolName() != null && 
            !updateDto.getSchoolName().equals(school.getSchoolName())) {
            if (schoolRepository.existsBySchoolNameAndIdNot(updateDto.getSchoolName(), id)) {
                throw new DuplicateResourceException("School with name '" + 
                    updateDto.getSchoolName() + "' already exists");
            }
        }

        schoolMapper.updateEntityFromDto(updateDto, school);
        School updatedSchool = schoolRepository.save(school);

        log.info("School updated successfully: {}", updatedSchool.getId());
        return schoolMapper.toResponseDto(updatedSchool);
    }

    /**
     * Update school status (activate/deactivate).
     */
    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.SCHOOL,
            description = "Updated school status",
            captureNewValue = true
    )
    public SchoolResponseDto updateSchoolStatus(Long id, Boolean isActive) {
        log.info("Updating school status - id: {}, isActive: {}", id, isActive);

        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + id));

        String action = isActive ? "ACTIVATE" : "DEACTIVATE";
        school.setIsActive(isActive);
        School updatedSchool = schoolRepository.save(school);

        log.info("School {} successfully: {}", action.toLowerCase(), updatedSchool.getId());
        return schoolMapper.toResponseDto(updatedSchool);
    }

    /**
     * Soft delete school (set isActive to false).
     */
    @Audited(
            action = AuditAction.DELETE,
            entityName = AuditEntityNames.SCHOOL,
            description = "Soft deleted school (deactivated)",
            captureNewValue = true
    )
    public void deleteSchool(Long id) {
        log.info("Soft deleting school with id: {}", id);

        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + id));

        school.setIsActive(false);
        schoolRepository.save(school);

        log.info("School deactivated successfully: {}", id);
    }

    /**
     * Check if school is active (for assignment validation).
     */
    @Transactional(readOnly = true)
    public boolean isSchoolActive(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));
        return school.getIsActive();
    }
}
