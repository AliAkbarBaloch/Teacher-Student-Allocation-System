package de.unipassau.allocationsystem.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherResponseDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherUpdateDto;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.TeacherMapper;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherService implements CrudService<TeacherResponseDto, Long> {

    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;
    private final TeacherMapper teacherMapper;

    @Override
    public List<Map<String, String>> getSortFields() {
        List<Map<String, String>> fields = new ArrayList<>();
        fields.add(Map.of("key", "id", "label", "ID"));
        fields.add(Map.of("key", "firstName", "label", "First Name"));
        fields.add(Map.of("key", "lastName", "label", "Last Name"));
        fields.add(Map.of("key", "email", "label", "Email"));
        fields.add(Map.of("key", "createdAt", "label", "Creation Date"));
        fields.add(Map.of("key", "updatedAt", "label", "Last Updated"));
        return fields;
    }

    @Override
    public boolean existsById(Long id) {
        return teacherRepository.existsById(id);
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER,
            description = "Viewed list of teachers",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<Teacher> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String searchTerm = searchValue;
            if (searchTerm != null && !searchTerm.isBlank()) {
                String likePattern = "%" + searchTerm.toLowerCase() + "%";
                Predicate firstNameLike = cb.like(cb.lower(root.get("firstName")), likePattern);
                Predicate lastNameLike = cb.like(cb.lower(root.get("lastName")), likePattern);
                Predicate emailLike = cb.like(cb.lower(root.get("email")), likePattern);
                predicates.add(cb.or(firstNameLike, lastNameLike, emailLike));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Teacher> page = teacherRepository.findAll(spec, pageable);
        return PaginationUtils.formatPaginationResponse(page);
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER,
            description = "Viewed all teachers",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public List<TeacherResponseDto> getAll() {
        return teacherRepository.findAll().stream()
                .map(teacherMapper::toResponseDto)
                .toList();
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER,
            description = "Viewed teacher by id",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<TeacherResponseDto> getById(Long id) {
        return teacherRepository.findById(id).map(teacherMapper::toResponseDto);
    }

    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Created new teacher",
            captureNewValue = true
    )
    @Transactional
    @Override
    public TeacherResponseDto create(TeacherResponseDto dto) {
        throw new UnsupportedOperationException("Use createTeacher with TeacherCreateDto");
    }

    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Created new teacher",
            captureNewValue = true
    )
    @Transactional
    public TeacherResponseDto createTeacher(TeacherCreateDto createDto) {
        if (teacherRepository.existsByEmail(createDto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + createDto.getEmail());
        }
        School school = schoolRepository.findById(createDto.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + createDto.getSchoolId()));
        Teacher teacher = teacherMapper.toEntityCreate(createDto);
        teacher.setSchool(school);
        Teacher saved = teacherRepository.save(teacher);
        return teacherMapper.toResponseDto(saved);
    }

    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Updated teacher",
            captureNewValue = true
    )
    @Transactional
    @Override
    public TeacherResponseDto update(Long id, TeacherResponseDto dto) {
        throw new UnsupportedOperationException("Use updateTeacher with TeacherUpdateDto");
    }

    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Updated teacher",
            captureNewValue = true
    )
    @Transactional
    public TeacherResponseDto updateTeacher(Long id, TeacherUpdateDto updateDto) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(teacher.getEmail())) {
            if (teacherRepository.existsByEmailAndIdNot(updateDto.getEmail(), id)) {
                throw new DuplicateResourceException("Email already exists: " + updateDto.getEmail());
            }
        }
        if (updateDto.getSchoolId() != null && !updateDto.getSchoolId().equals(teacher.getSchool().getId())) {
            School newSchool = schoolRepository.findById(updateDto.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + updateDto.getSchoolId()));
            teacher.setSchool(newSchool);
        }
        teacherMapper.updateEntityFromDto(updateDto, teacher);
        Teacher updated = teacherRepository.save(teacher);
        return teacherMapper.toResponseDto(updated);
    }

    @Audited(
            action = AuditLog.AuditAction.DELETE,
            entityName = AuditEntityNames.TEACHER,
            description = "Deleted teacher",
            captureNewValue = false
    )
    @Transactional
    @Override
    public void delete(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Teacher not found with id: " + id);
        }
        teacherRepository.deleteById(id);
    }

    @Transactional
    public void deleteTeacher(Long id) {
        delete(id);
    }
}