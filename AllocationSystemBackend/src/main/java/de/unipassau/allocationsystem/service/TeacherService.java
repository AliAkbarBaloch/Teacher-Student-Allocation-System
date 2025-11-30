package de.unipassau.allocationsystem.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import de.unipassau.allocationsystem.utils.ExcelParser;
import de.unipassau.allocationsystem.dto.teacher.BulkImportResponseDto;
import de.unipassau.allocationsystem.dto.teacher.ImportResultRowDto;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherService implements CrudService<TeacherResponseDto, Long> {

    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;
    private final TeacherMapper teacherMapper;

    /**
     * Helper class to pair Teacher with its row number for batch processing
     */
    @Getter
    @AllArgsConstructor
    private static class TeacherRowPair {
        private final Teacher teacher;
        private final int rowNumber;
    }

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

        Specification<Teacher> spec = buildFilterSpecification(queryParams, searchValue);

        Page<Teacher> page = teacherRepository.findAll(spec, pageable);
        return PaginationUtils.formatPaginationResponse(page);
    }

    private Specification<Teacher> buildFilterSpecification(Map<String, String> queryParams, String searchValue) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search filter
            if (searchValue != null && !searchValue.trim().isEmpty()) {
                String likePattern = "%" + searchValue.trim().toLowerCase() + "%";
                Predicate firstNameLike = cb.like(cb.lower(root.get("firstName")), likePattern);
                Predicate lastNameLike = cb.like(cb.lower(root.get("lastName")), likePattern);
                Predicate emailLike = cb.like(cb.lower(root.get("email")), likePattern);
                predicates.add(cb.or(firstNameLike, lastNameLike, emailLike));
            }

            // School ID filter
            String schoolIdParam = queryParams.get("schoolId");
            if (schoolIdParam != null && !schoolIdParam.trim().isEmpty()) {
                try {
                    Long schoolId = Long.parseLong(schoolIdParam);
                    predicates.add(cb.equal(root.get("school").get("id"), schoolId));
                } catch (NumberFormatException e) {
                    // Invalid school ID, ignore filter
                }
            }

            // Employment status filter
            String employmentStatusParam = queryParams.get("employmentStatus");
            if (employmentStatusParam != null && !employmentStatusParam.trim().isEmpty()) {
                try {
                    Teacher.EmploymentStatus employmentStatus = Teacher.EmploymentStatus.valueOf(employmentStatusParam.toUpperCase());
                    predicates.add(cb.equal(root.get("employmentStatus"), employmentStatus));
                } catch (IllegalArgumentException e) {
                    // Invalid employment status, ignore filter
                }
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };
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
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Updated teacher employment status",
            captureNewValue = true
    )
    @Transactional
    public TeacherResponseDto updateEmploymentStatus(Long id, Teacher.EmploymentStatus employmentStatus) {
        Teacher existing = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));
        existing.setEmploymentStatus(employmentStatus);
        Teacher updated = teacherRepository.save(existing);
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

    /**
     * Find existing emails in the database from a list of emails.
     * Used for bulk import validation.
     *
     * @param emails List of email addresses to check
     * @return Set of emails that already exist in the database
     */
    @Transactional(readOnly = true)
    public Set<String> findExistingEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return Set.of();
        }
        Set<String> normalizedEmails = emails.stream()
                .map(email -> email.toLowerCase().trim())
                .collect(Collectors.toSet());
        return teacherRepository.findExistingEmails(normalizedEmails);
    }

    @Audited(
            action = AuditLog.AuditAction.IMPORT,
            entityName = AuditEntityNames.TEACHER,
            description = "Bulk imported teachers",
            captureNewValue = false
    )
    @Transactional
    public BulkImportResponseDto bulkImportTeachers(MultipartFile file, boolean skipInvalidRows) throws IOException {
        List<ExcelParser.ParsedRow> parsedRows = ExcelParser.parseExcelFile(file);
        List<ImportResultRowDto> results = new ArrayList<>();
        
        // Batch processing: Load all schools once
        List<School> allSchools = schoolRepository.findByIsActive(true);
        Map<String, School> schoolNameMap = allSchools.stream()
                .collect(Collectors.toMap(
                        school -> school.getSchoolName().toLowerCase(),
                        school -> school,
                        (existing, replacement) -> existing
                ));
        Map<Long, School> schoolIdMap = allSchools.stream()
                .collect(Collectors.toMap(School::getId, school -> school));

        // Batch check existing emails
        Set<String> emailsToCheck = parsedRows.stream()
                .map(row -> row.getDto().getEmail().toLowerCase().trim())
                .collect(Collectors.toSet());
        Set<String> existingEmails = teacherRepository.findExistingEmails(emailsToCheck);

        // Prepare valid rows for batch insert
        List<TeacherRowPair> validRows = new ArrayList<>();
        Map<Integer, ImportResultRowDto> resultMap = new HashMap<>();

        // Process each row and validate
        for (ExcelParser.ParsedRow parsedRow : parsedRows) {
            ImportResultRowDto result = ImportResultRowDto.builder()
                    .rowNumber(parsedRow.getRowNumber())
                    .success(false)
                    .build();
            resultMap.put(parsedRow.getRowNumber(), result);

            try {
                TeacherCreateDto dto = parsedRow.getDto();
                String emailLower = dto.getEmail().toLowerCase().trim();

                // Check for duplicate email in database
                if (existingEmails.contains(emailLower)) {
                    throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
                }

                // Resolve school ID from name if needed
                School school = null;
                if (dto.getSchoolId() != null) {
                    school = schoolIdMap.get(dto.getSchoolId());
                    if (school == null) {
                        throw new ResourceNotFoundException("School not found with ID: " + dto.getSchoolId());
                    }
                } else if (parsedRow.getSchoolName() != null && !parsedRow.getSchoolName().trim().isEmpty()) {
                    school = schoolNameMap.get(parsedRow.getSchoolName().toLowerCase().trim());
                    if (school == null) {
                        throw new ResourceNotFoundException("School not found with name: " + parsedRow.getSchoolName());
                    }
                    dto.setSchoolId(school.getId());
                } else {
                    throw new ResourceNotFoundException("School ID or School Name is required");
                }

                // Create teacher entity (not saved yet)
                Teacher teacher = teacherMapper.toEntityCreate(dto);
                teacher.setSchool(school);
                validRows.add(new TeacherRowPair(teacher, parsedRow.getRowNumber()));
                
                // Mark as valid for now (will be set to success after save)
                result.setSuccess(true);

            } catch (Exception e) {
                String errorMessage = e.getMessage();
                if (errorMessage == null || errorMessage.isEmpty()) {
                    errorMessage = "Failed to import teacher: " + e.getClass().getSimpleName();
                }
                result.setError(errorMessage);
                log.warn("Failed to import teacher at row {}: {}", parsedRow.getRowNumber(), errorMessage);

                // If skipInvalidRows is false, stop processing on first error
                if (!skipInvalidRows) {
                    // Clear valid rows and mark remaining as failed
                    validRows.clear();
                    // Set all remaining rows as failed
                    int currentIndex = parsedRows.indexOf(parsedRow);
                    for (int i = currentIndex + 1; i < parsedRows.size(); i++) {
                        ExcelParser.ParsedRow remainingRow = parsedRows.get(i);
                        ImportResultRowDto remainingResult = resultMap.get(remainingRow.getRowNumber());
                        if (remainingResult != null && remainingResult.getError() == null) {
                            remainingResult.setError("Import stopped due to error in row " + parsedRow.getRowNumber());
                        }
                    }
                    break;
                }
            }
        }

        // Batch save valid teachers in smaller transactions (batch size: 50)
        int batchSize = 50;
        int successfulRows = 0;
        
        for (int i = 0; i < validRows.size(); i += batchSize) {
            int end = Math.min(i + batchSize, validRows.size());
            List<TeacherRowPair> batch = validRows.subList(i, end);
            List<Teacher> teachersToSave = batch.stream()
                    .map(pair -> pair.teacher)
                    .collect(Collectors.toList());
            
            try {
                List<Teacher> savedTeachers = teacherRepository.saveAll(teachersToSave);
                
                // Update results for successfully saved teachers
                for (int j = 0; j < savedTeachers.size(); j++) {
                    Teacher saved = savedTeachers.get(j);
                    int rowNumber = batch.get(j).rowNumber;
                    ImportResultRowDto result = resultMap.get(rowNumber);
                    if (result != null && result.isSuccess()) {
                        TeacherResponseDto responseDto = teacherMapper.toResponseDto(saved);
                        result.setTeacher(responseDto);
                        successfulRows++;
                    }
                }
            } catch (Exception e) {
                log.error("Error saving batch of teachers", e);
                // Mark batch as failed
                for (TeacherRowPair pair : batch) {
                    ImportResultRowDto result = resultMap.get(pair.rowNumber);
                    if (result != null && result.isSuccess()) {
                        result.setSuccess(false);
                        result.setError("Failed to save teacher: " + e.getMessage());
                    }
                }
            }
        }

        // Build final results list
        for (ExcelParser.ParsedRow parsedRow : parsedRows) {
            results.add(resultMap.get(parsedRow.getRowNumber()));
        }

        int failedRows = parsedRows.size() - successfulRows;

        return BulkImportResponseDto.builder()
                .totalRows(parsedRows.size())
                .successfulRows(successfulRows)
                .failedRows(failedRows)
                .results(results)
                .build();
    }
}