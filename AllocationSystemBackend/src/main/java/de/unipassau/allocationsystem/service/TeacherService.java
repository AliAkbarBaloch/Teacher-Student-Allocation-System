package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.dto.teacher.BulkImportResponseDto;
import de.unipassau.allocationsystem.dto.teacher.ImportResultRowDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherResponseDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherUpdateDto;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.TeacherMapper;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.utils.ExcelParser;
import de.unipassau.allocationsystem.utils.ParsedRow;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import de.unipassau.allocationsystem.utils.SearchSpecificationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for managing teacher entities.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>CRUD operations for teachers (via DTO-based API)</li>
 *   <li>Employment status updates</li>
 *   <li>Bulk import from Excel with validation, batching, and detailed per-row results</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherService implements CrudService<TeacherResponseDto, Long> {

    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;
    private final TeacherMapper teacherMapper;

    /**
     * Helper class to pair a Teacher entity with its original Excel row number.
     */
    @Getter
    @AllArgsConstructor
    private static class TeacherRowPair {
        private final Teacher teacher;
        private final int rowNumber;
    }

    /**
     * Resolves subject IDs into Subject entities.
     * <ul>
     *   <li>If subjectIds is null/empty: returns empty set</li>
     *   <li>If any subject ID does not exist: throws {@link ResourceNotFoundException}</li>
     * </ul>
     *
     * @param subjectIds list of subject IDs (nullable)
     * @return set of resolved subjects (never null)
     */
    private Set<Subject> resolveSubjects(List<Long> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return Set.of();
        }
        Set<Subject> subjects = new HashSet<>(subjectRepository.findAllById(subjectIds));
        if (subjects.size() != subjectIds.size()) {
            throw new ResourceNotFoundException("One or more subjects not found");
        }
        return subjects;
    }

    @Override
    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields("id", "firstName", "lastName", "email", "createdAt", "updatedAt");
    }

    /**
     * Returns the list of sortable field keys exposed by {@link #getSortFields()}.
     *
     * @return list of field keys
     */
    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(f -> f.get("key")).toList();
    }

    @Override
    public boolean existsById(Long id) {
        return teacherRepository.existsById(id);
    }

    /**
     * Returns a paginated and filtered list of teachers.
     * Supports:
     * <ul>
     *   <li>search (firstName, lastName, email)</li>
     *   <li>schoolId filter</li>
     *   <li>employmentStatus filter</li>
     * </ul>
     */
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

    /**
     * Builds the Specification used for filtering and searching teachers.
     */
    private Specification<Teacher> buildFilterSpecification(Map<String, String> queryParams, String searchValue) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchValue != null && !searchValue.trim().isEmpty()) {
                Specification<Teacher> searchSpec = SearchSpecificationUtils.buildMultiFieldLikeSpecification(
                        new String[]{"firstName", "lastName", "email"}, searchValue
                );
                predicates.add(searchSpec.toPredicate(root, query, cb));
            }

            parseLong(queryParams.get("schoolId"))
                    .ifPresent(id -> predicates.add(cb.equal(root.get("school").get("id"), id)));

            parseEmploymentStatus(queryParams.get("employmentStatus"))
                    .ifPresent(s -> predicates.add(cb.equal(root.get("employmentStatus"), s)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Parses a Long value from a query parameter.
     *
     * @param raw raw query value
     * @return parsed long if valid, otherwise empty
     */
    private Optional<Long> parseLong(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(raw.trim()));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Parses an employment status value without throwing/catching IllegalArgumentException.
     *
     * @param raw raw query value
     * @return parsed status if valid, otherwise empty
     */
    private Optional<Teacher.EmploymentStatus> parseEmploymentStatus(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Optional.empty();
        }
        String normalized = raw.trim().toUpperCase();
        for (Teacher.EmploymentStatus s : Teacher.EmploymentStatus.values()) {
            if (s.name().equals(normalized)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns all teachers.
     */
    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER,
            description = "Viewed all teachers",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public List<TeacherResponseDto> getAll() {
        return teacherRepository.findAll().stream().map(teacherMapper::toResponseDto).toList();
    }

    /**
     * Returns a teacher by id.
     */
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

    /**
     * Unsupported API method. Use {@link #createTeacher(TeacherCreateDto)}.
     */
    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Created new teacher",
            captureNewValue = true
    )
    @Override
    public TeacherResponseDto create(TeacherResponseDto dto) {
        throw new UnsupportedOperationException("Use createTeacher with TeacherCreateDto");
    }

    /**
     * Creates a new teacher with subject qualifications.
     *
     * @param createDto teacher creation data
     * @return created teacher response DTO
     */
    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Created new teacher",
            captureNewValue = true
    )
    public TeacherResponseDto createTeacher(TeacherCreateDto createDto) {
        if (teacherRepository.existsByEmail(createDto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + createDto.getEmail());
        }

        School school = schoolRepository.findById(createDto.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + createDto.getSchoolId()));

        Teacher teacher = teacherMapper.toEntityCreate(createDto);
        teacher.setSchool(school);
        teacher.setSubjects(resolveSubjects(createDto.getSubjectIds()));

        Teacher saved = teacherRepository.save(teacher);
        return teacherMapper.toResponseDto(saved);
    }

    /**
     * Unsupported API method. Use {@link #updateTeacher(Long, TeacherUpdateDto)}.
     */
    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Updated teacher",
            captureNewValue = true
    )
    @Override
    public TeacherResponseDto update(Long id, TeacherResponseDto dto) {
        throw new UnsupportedOperationException("Use updateTeacher with TeacherUpdateDto");
    }

    /**
     * Updates an existing teacher's information.
     *
     * @param id        the teacher ID
     * @param updateDto teacher update data
     * @return updated teacher response DTO
     */
    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Updated teacher",
            captureNewValue = true
    )
    public TeacherResponseDto updateTeacher(Long id, TeacherUpdateDto updateDto) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));

        validateEmailChange(id, updateDto, teacher);
        applySchoolChange(updateDto, teacher);

        teacherMapper.updateEntityFromDto(updateDto, teacher);

        if (updateDto.getSubjectIds() != null) {
            teacher.setSubjects(resolveSubjects(updateDto.getSubjectIds()));
        }

        Teacher updated = teacherRepository.save(teacher);
        return teacherMapper.toResponseDto(updated);
    }

    /**
     * Validates that an email change does not violate uniqueness constraints.
     */
    private void validateEmailChange(Long id, TeacherUpdateDto updateDto, Teacher teacher) {
        String incomingEmail = updateDto.getEmail();
        if (incomingEmail == null || incomingEmail.equals(teacher.getEmail())) {
            return;
        }
        if (teacherRepository.existsByEmailAndIdNot(incomingEmail, id)) {
            throw new DuplicateResourceException("Email already exists: " + incomingEmail);
        }
    }

    /**
     * Applies school change if schoolId is provided and differs from current.
     */
    private void applySchoolChange(TeacherUpdateDto updateDto, Teacher teacher) {
        Long incomingSchoolId = updateDto.getSchoolId();
        if (incomingSchoolId == null) {
            return;
        }
        if (teacher.getSchool() != null && incomingSchoolId.equals(teacher.getSchool().getId())) {
            return;
        }

        School newSchool = schoolRepository.findById(incomingSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + incomingSchoolId));
        teacher.setSchool(newSchool);
    }

    /**
     * Updates a teacher's employment status.
     *
     * @param id               teacher ID
     * @param employmentStatus new status
     * @return updated teacher response DTO
     */
    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Updated teacher employment status",
            captureNewValue = true
    )
    public TeacherResponseDto updateEmploymentStatus(Long id, Teacher.EmploymentStatus employmentStatus) {
        Teacher existing = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));
        existing.setEmploymentStatus(employmentStatus);
        Teacher updated = teacherRepository.save(existing);
        return teacherMapper.toResponseDto(updated);
    }

    /**
     * Deletes a teacher by id.
     */
    @Audited(
            action = AuditLog.AuditAction.DELETE,
            entityName = AuditEntityNames.TEACHER,
            description = "Deleted teacher",
            captureNewValue = false
    )
    @Override
    public void delete(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Teacher not found with id: " + id);
        }
        teacherRepository.deleteById(id);
    }

    /**
     * Deletes a teacher by ID (kept for API compatibility).
     * <p>
     * Important: This method does not call {@link #delete(Long)} to avoid
     * transactional self-invocation warnings.
     *
     * @param id teacher ID
     */
    public void deleteTeacher(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Teacher not found with id: " + id);
        }
        teacherRepository.deleteById(id);
    }

    /**
     * Finds existing emails from a list of emails.
     * Used for bulk import validation.
     *
     * @param emails list of email addresses
     * @return set of emails already existing in the DB
     */
    @Transactional(readOnly = true)
    public Set<String> findExistingEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return Set.of();
        }
        Set<String> normalizedEmails = emails.stream()
                .map(e -> e.toLowerCase().trim())
                .collect(Collectors.toSet());
        return teacherRepository.findExistingEmails(normalizedEmails);
    }

    /**
     * Imports teachers from an Excel file.
     *
     * @param file            the Excel file
     * @param skipInvalidRows whether to skip invalid rows or stop on first error
     * @return import response containing per-row results
     * @throws IOException if parsing fails
     */
    @Audited(
            action = AuditLog.AuditAction.IMPORT,
            entityName = AuditEntityNames.TEACHER,
            description = "Bulk imported teachers",
            captureNewValue = false
    )
    public BulkImportResponseDto bulkImportTeachers(MultipartFile file, boolean skipInvalidRows) throws IOException {
        List<ParsedRow> parsedRows = ExcelParser.parseExcelFile(file);

        Map<Integer, ImportResultRowDto> resultMap = initResultMap(parsedRows);
        List<School> activeSchools = schoolRepository.findByIsActive(true);

        Map<String, School> schoolByName = indexByLowerName(activeSchools);
        Map<Long, School> schoolById = activeSchools.stream()
                .collect(Collectors.toMap(School::getId, Function.identity()));

        Set<String> existingEmails = loadExistingEmails(parsedRows);
        List<TeacherRowPair> validRows = new ArrayList<>();

        boolean prepared = validateAndPrepareRows(
                parsedRows, resultMap, validRows, existingEmails, schoolById, schoolByName, skipInvalidRows
        );

        int successful = 0;
        if (prepared || skipInvalidRows) {
            successful = saveBatches(validRows, resultMap, 50);
        }

        List<ImportResultRowDto> results = parsedRows.stream()
                .map(r -> resultMap.get(r.getRowNumber()))
                .toList();

        int total = parsedRows.size();
        int failed = total - successful;

        return BulkImportResponseDto.builder()
                .totalRows(total)
                .successfulRows(successful)
                .failedRows(failed)
                .results(results)
                .build();
    }

    /**
     * Initializes the result map with a default failed result for each row.
     */
    private Map<Integer, ImportResultRowDto> initResultMap(List<ParsedRow> parsedRows) {
        Map<Integer, ImportResultRowDto> resultMap = new HashMap<>();
        for (ParsedRow row : parsedRows) {
            ImportResultRowDto result = ImportResultRowDto.builder()
                    .rowNumber(row.getRowNumber())
                    .success(false)
                    .build();
            resultMap.put(row.getRowNumber(), result);
        }
        return resultMap;
    }

    /**
     * Creates an index of active schools by lower-cased school name.
     */
    private Map<String, School> indexByLowerName(List<School> schools) {
        return schools.stream().collect(Collectors.toMap(
                s -> s.getSchoolName().toLowerCase(),
                Function.identity(),
                (a, b) -> a
        ));
    }

    /**
     * Loads emails from rows and asks repository for existing ones.
     */
    private Set<String> loadExistingEmails(List<ParsedRow> parsedRows) {
        Set<String> emailsToCheck = parsedRows.stream()
                .map(r -> r.getDto().getEmail().toLowerCase().trim())
                .collect(Collectors.toSet());
        return teacherRepository.findExistingEmails(emailsToCheck);
    }

    /**
     * Validates each row and prepares Teacher entities for batch save.
     * Handles skip/stop behavior based on skipInvalidRows.
     *
     * @return true if processed fully; false if stopped early due to error (and skipInvalidRows=false)
     */
    private boolean validateAndPrepareRows(
            List<ParsedRow> parsedRows,
            Map<Integer, ImportResultRowDto> resultMap,
            List<TeacherRowPair> validRows,
            Set<String> existingEmails,
            Map<Long, School> schoolById,
            Map<String, School> schoolByName,
            boolean skipInvalidRows
    ) {
        for (int idx = 0; idx < parsedRows.size(); idx++) {
            ParsedRow parsedRow = parsedRows.get(idx);
            ImportResultRowDto result = resultMap.get(parsedRow.getRowNumber());

            try {
                Teacher teacher = buildTeacherForRow(parsedRow, existingEmails, schoolById, schoolByName);
                validRows.add(new TeacherRowPair(teacher, parsedRow.getRowNumber()));
                result.setSuccess(true);
            } catch (DuplicateResourceException | ResourceNotFoundException | IllegalArgumentException e) {
                setRowError(parsedRow, result, e);
                if (!skipInvalidRows) {
                    markRemainingAsStopped(parsedRows, resultMap, idx, parsedRow.getRowNumber());
                    validRows.clear();
                    return false;
                }
            } catch (RuntimeException e) {
                setRowError(parsedRow, result, e);
                if (!skipInvalidRows) {
                    markRemainingAsStopped(parsedRows, resultMap, idx, parsedRow.getRowNumber());
                    validRows.clear();
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Builds a Teacher entity for a parsed row, performing the same validations as before.
     */
    private Teacher buildTeacherForRow(
            ParsedRow parsedRow,
            Set<String> existingEmails,
            Map<Long, School> schoolById,
            Map<String, School> schoolByName
    ) {
        TeacherCreateDto dto = parsedRow.getDto();
        String emailLower = dto.getEmail().toLowerCase().trim();

        if (existingEmails.contains(emailLower)) {
            throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
        }

        School school = resolveSchool(parsedRow, dto, schoolById, schoolByName);
        Teacher teacher = teacherMapper.toEntityCreate(dto);
        teacher.setSchool(school);
        return teacher;
    }

    /**
     * Resolves the school for a row using either:
     * <ul>
     *   <li>dto.schoolId (preferred)</li>
     *   <li>parsedRow.schoolName (fallback)</li>
     * </ul>
     */
    private School resolveSchool(
            ParsedRow parsedRow,
            TeacherCreateDto dto,
            Map<Long, School> schoolById,
            Map<String, School> schoolByName
    ) {
        if (dto.getSchoolId() != null) {
            School byId = schoolById.get(dto.getSchoolId());
            if (byId == null) {
                throw new ResourceNotFoundException("School not found with ID: " + dto.getSchoolId());
            }
            return byId;
        }

        String schoolName = parsedRow.getSchoolName();
        if (schoolName != null && !schoolName.trim().isEmpty()) {
            School byName = schoolByName.get(schoolName.toLowerCase().trim());
            if (byName == null) {
                throw new ResourceNotFoundException("School not found with name: " + schoolName);
            }
            dto.setSchoolId(byName.getId());
            return byName;
        }

        throw new ResourceNotFoundException("School ID or School Name is required");
    }

    /**
     * Populates an error message for a failing import row.
     */
    private void setRowError(ParsedRow parsedRow, ImportResultRowDto result, Exception e) {
        String msg = e.getMessage();
        if (msg == null || msg.isEmpty()) {
            msg = "Failed to import teacher: " + e.getClass().getSimpleName();
        }
        result.setSuccess(false);
        result.setError(msg);
        log.warn("Failed to import teacher at row {}: {}", parsedRow.getRowNumber(), msg);
    }

    /**
     * Marks remaining rows after a stopping error.
     */
    private void markRemainingAsStopped(
            List<ParsedRow> parsedRows,
            Map<Integer, ImportResultRowDto> resultMap,
            int currentIndex,
            int errorRowNumber
    ) {
        for (int i = currentIndex + 1; i < parsedRows.size(); i++) {
            ParsedRow remainingRow = parsedRows.get(i);
            ImportResultRowDto remainingResult = resultMap.get(remainingRow.getRowNumber());
            if (remainingResult != null && remainingResult.getError() == null) {
                remainingResult.setError("Import stopped due to error in row " + errorRowNumber);
            }
        }
    }

    /**
     * Saves teachers in batches and attaches saved entities to per-row results.
     *
     * @return number of successfully saved rows
     */
    private int saveBatches(List<TeacherRowPair> validRows, Map<Integer, ImportResultRowDto> resultMap, int batchSize) {
        int successfulRows = 0;

        for (int i = 0; i < validRows.size(); i += batchSize) {
            int end = Math.min(i + batchSize, validRows.size());
            List<TeacherRowPair> batch = validRows.subList(i, end);

            List<Teacher> teachersToSave = batch.stream()
                    .map(TeacherRowPair::getTeacher)
                    .toList();

            try {
                List<Teacher> savedTeachers = teacherRepository.saveAll(teachersToSave);
                successfulRows += attachSavedTeachers(savedTeachers, batch, resultMap);
            } catch (RuntimeException e) {
                log.error("Error saving batch of teachers", e);
                markBatchFailed(batch, resultMap, e);
            }
        }

        return successfulRows;
    }

    /**
     * Attaches saved teachers to the corresponding row results.
     */
    private int attachSavedTeachers(
            List<Teacher> savedTeachers,
            List<TeacherRowPair> batch,
            Map<Integer, ImportResultRowDto> resultMap
    ) {
        int successes = 0;
        for (int j = 0; j < savedTeachers.size(); j++) {
            Teacher saved = savedTeachers.get(j);
            int rowNumber = batch.get(j).getRowNumber();
            ImportResultRowDto result = resultMap.get(rowNumber);
            if (result != null && result.isSuccess()) {
                result.setTeacher(teacherMapper.toResponseDto(saved));
                successes++;
            }
        }
        return successes;
    }

    /**
     * Marks all rows in a batch as failed when the batch save fails.
     */
    private void markBatchFailed(List<TeacherRowPair> batch, Map<Integer, ImportResultRowDto> resultMap, RuntimeException e) {
        String msg = e.getMessage();
        for (TeacherRowPair pair : batch) {
            ImportResultRowDto result = resultMap.get(pair.getRowNumber());
            if (result != null && result.isSuccess()) {
                result.setSuccess(false);
                result.setError("Failed to save teacher: " + msg);
            }
        }
    }
}
