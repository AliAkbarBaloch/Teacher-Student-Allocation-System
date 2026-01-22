package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.teacher.BulkImportResponseDto;
import de.unipassau.allocationsystem.dto.teacher.ImportResultRowDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherResponseDto;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.TeacherMapper;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.utils.ExcelParser;
import de.unipassau.allocationsystem.utils.ParsedRow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implements bulk import of teachers from Excel.
 * Extracted from {@link TeacherService} to satisfy file size / complexity checks while preserving logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherImportService {

    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;
    private final TeacherMapper teacherMapper;

    /**
     * Used to attach saved teacher entities back to the correct Excel row.
     */
    @Getter
    @AllArgsConstructor
    private static class TeacherRowPair {
        private final Teacher teacher;
        private final int rowNumber;
    }

    /**
     * Imports teachers from an Excel file.
     *
     * @param file            Excel file
     * @param skipInvalidRows if false, stop on first error (same semantics as before)
     * @return bulk import response
     * @throws IOException if file parsing fails
     */
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

    private Map<String, School> indexByLowerName(List<School> schools) {
        return schools.stream().collect(Collectors.toMap(
                s -> s.getSchoolName().toLowerCase(),
                Function.identity(),
                (a, b) -> a
        ));
    }

    private Set<String> loadExistingEmails(List<ParsedRow> parsedRows) {
        Set<String> emailsToCheck = parsedRows.stream()
                .map(r -> r.getDto().getEmail().toLowerCase().trim())
                .collect(Collectors.toSet());
        return teacherRepository.findExistingEmails(emailsToCheck);
    }

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
            } catch (DuplicateResourceException | ResourceNotFoundException e) {
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

    private Teacher buildTeacherForRow(
            ParsedRow parsedRow,
            Set<String> existingEmails,
            Map<Long, School> schoolById,
            Map<String, School> schoolByName
    ) {
        TeacherCreateDto dto = parsedRow.getDto();

        String rawEmail = dto.getEmail();
        if (rawEmail == null || rawEmail.trim().isEmpty()) {
            throw new ResourceNotFoundException("Email is required");
        }

        String emailLower = rawEmail.toLowerCase().trim();
        if (existingEmails.contains(emailLower)) {
            throw new DuplicateResourceException("Email already exists: " + rawEmail);
        }

        School school = resolveSchool(parsedRow, dto, schoolById, schoolByName);
        Teacher teacher = teacherMapper.toEntityCreate(dto);
        teacher.setSchool(school);
        return teacher;
    }

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

    private void setRowError(ParsedRow parsedRow, ImportResultRowDto result, Exception e) {
        String msg = e.getMessage();
        if (msg == null || msg.isEmpty()) {
            msg = "Failed to import teacher: " + e.getClass().getSimpleName();
        }
        result.setSuccess(false);
        result.setError(msg);
        log.warn("Failed to import teacher at row {}: {}", parsedRow.getRowNumber(), msg);
    }

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
            } catch (DataAccessException dae) {
                log.error("Error saving batch of teachers", dae);
                markBatchFailed(batch, resultMap, dae);
            }
        }

        return successfulRows;
    }

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
                TeacherResponseDto responseDto = teacherMapper.toResponseDto(saved);
                result.setTeacher(responseDto);
                successes++;
            }
        }
        return successes;
    }

    private void markBatchFailed(List<TeacherRowPair> batch, Map<Integer, ImportResultRowDto> resultMap, DataAccessException e) {
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
