package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandCreateDto;
import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandResponseDto;
import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandUpdateDto;
import de.unipassau.allocationsystem.entity.*;
import de.unipassau.allocationsystem.mapper.InternshipDemandMapper;
import de.unipassau.allocationsystem.repository.InternshipDemandAggregation;
import de.unipassau.allocationsystem.repository.InternshipDemandRepository;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InternshipDemandService {

    private final InternshipDemandRepository repository;
    private final InternshipTypeRepository internshipTypeRepository;
    private final SubjectRepository subjectRepository;
    private final de.unipassau.allocationsystem.repository.AcademicYearRepository academicYearRepository;
    private final InternshipDemandMapper mapper;

    public InternshipDemandResponseDto getById(Long id) {
        InternshipDemand d = repository.findById(id).orElseThrow(() -> new NoSuchElementException("InternshipDemand not found with id: " + id));
        return mapper.toResponseDto(d);
    }

    public Page<InternshipDemand> listByYearWithFilters(Long yearId, Long internshipTypeId, School.SchoolType schoolType, Long subjectId, Boolean isForecasted, Pageable pageable) {
        Specification<InternshipDemand> spec = (root, query, cb) -> cb.conjunction();
        spec = spec.and((root, query, cb) -> cb.equal(root.get("academicYear").get("id"), yearId));
        if (internshipTypeId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("internshipType").get("id"), internshipTypeId));
        }
        if (schoolType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("schoolType"), schoolType));
        }
        if (subjectId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("subject").get("id"), subjectId));
        }
        if (isForecasted != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isForecasted"), isForecasted));
        }
        return repository.findAll(spec, pageable);
    }

    public List<InternshipDemandResponseDto> getAllByYear(Long yearId) {
        return repository.findAll((root, query, cb) -> cb.equal(root.get("academicYear").get("id"), yearId))
                .stream().map(mapper::toResponseDto).collect(Collectors.toList());
    }

    public List<InternshipDemandAggregation> aggregateByYear(Long yearId) {
        return repository.aggregateByYear(yearId);
    }

    /**
     * Return typed aggregation DTOs for a given academic year.
     */
    public java.util.List<de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandAggregationDto> getAggregationForYear(Long yearId) {
        return repository.aggregateByYear(yearId).stream()
                .map(a -> new de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandAggregationDto(a.getInternshipTypeId(), a.getTotalRequiredTeachers()))
                .collect(Collectors.toList());
    }

    @Transactional
    @Audited(action = de.unipassau.allocationsystem.entity.AuditLog.AuditAction.CREATE, entityName = AuditEntityNames.INTERNSHIP_DEMAND, description = "Created internship demand", captureNewValue = true)
    public InternshipDemandResponseDto create(InternshipDemandCreateDto dto) {
        AcademicYear year = academicYearRepository.findById(dto.getYearId()).orElseThrow(() -> new NoSuchElementException("AcademicYear not found"));
        InternshipType it = internshipTypeRepository.findById(dto.getInternshipTypeId()).orElseThrow(() -> new NoSuchElementException("InternshipType not found"));
        Subject subject = subjectRepository.findById(dto.getSubjectId()).orElseThrow(() -> new NoSuchElementException("Subject not found"));
        if (!Boolean.TRUE.equals(subject.getIsActive())) throw new IllegalStateException("Subject is not active");

        // validate schoolType by attempting to map to enum
        School.SchoolType st;
        try {
            st = School.SchoolType.valueOf(dto.getSchoolType());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid school type: " + dto.getSchoolType());
        }

        // uniqueness check
        Specification<InternshipDemand> uniqueSpec = (root, query, cb) -> cb.and(
                cb.equal(root.get("academicYear").get("id"), dto.getYearId()),
                cb.equal(root.get("internshipType").get("id"), dto.getInternshipTypeId()),
                cb.equal(root.get("schoolType"), st),
                cb.equal(root.get("subject").get("id"), dto.getSubjectId()),
                cb.equal(root.get("isForecasted"), dto.getIsForecasted() != null ? dto.getIsForecasted() : Boolean.FALSE)
        );
        if (repository.count(uniqueSpec) > 0) {
            throw new IllegalStateException("Duplicate internship demand for the same dimensions");
        }

        InternshipDemand entity = mapper.toEntityCreate(dto);
        entity.setAcademicYear(year);
        entity.setInternshipType(it);
        entity.setSubject(subject);
        entity.setSchoolType(st);

        InternshipDemand saved = repository.save(entity);
        return mapper.toResponseDto(saved);
    }

    @Transactional
    @Audited(action = de.unipassau.allocationsystem.entity.AuditLog.AuditAction.UPDATE, entityName = AuditEntityNames.INTERNSHIP_DEMAND, description = "Updated internship demand", captureNewValue = true)
    public InternshipDemandResponseDto update(Long id, InternshipDemandUpdateDto dto) {
        InternshipDemand existing = repository.findById(id).orElseThrow(() -> new NoSuchElementException("InternshipDemand not found"));
        if (dto.getSubjectId() != null) {
            Subject s = subjectRepository.findById(dto.getSubjectId()).orElseThrow(() -> new NoSuchElementException("Subject not found"));
            if (!Boolean.TRUE.equals(s.getIsActive())) throw new IllegalStateException("Subject is not active");
            existing.setSubject(s);
        }
        if (dto.getInternshipTypeId() != null) {
            InternshipType it = internshipTypeRepository.findById(dto.getInternshipTypeId()).orElseThrow(() -> new NoSuchElementException("InternshipType not found"));
            existing.setInternshipType(it);
        }
        if (dto.getSchoolType() != null) {
            try {
                existing.setSchoolType(School.SchoolType.valueOf(dto.getSchoolType()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid school type: " + dto.getSchoolType());
            }
        }

        mapper.updateEntityFromDto(dto, existing);

        InternshipDemand saved = repository.save(existing);
        return mapper.toResponseDto(saved);
    }

    @Transactional
    @Audited(action = de.unipassau.allocationsystem.entity.AuditLog.AuditAction.DELETE, entityName = AuditEntityNames.INTERNSHIP_DEMAND, description = "Deleted internship demand", captureNewValue = false)
    public void delete(Long id) {
        InternshipDemand existing = repository.findById(id).orElseThrow(() -> new NoSuchElementException("InternshipDemand not found"));
        repository.delete(existing);
    }
}
