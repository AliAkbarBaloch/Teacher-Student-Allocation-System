package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherFormSubmission;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionCreateDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionResponseDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionStatusUpdateDto;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.TeacherFormSubmissionMapper;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.TeacherFormSubmissionRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TeacherFormSubmissionService}.
 * <p>
 * This test class validates form submission CRUD operations, status updates,
 * filtering, pagination, and duplicate submission detection.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class TeacherFormSubmissionServiceTest {

    @Mock
    private TeacherFormSubmissionRepository teacherFormSubmissionRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private AcademicYearRepository academicYearRepository;

    @Mock
    private TeacherFormSubmissionMapper teacherFormSubmissionMapper;

    @InjectMocks
    private TeacherFormSubmissionService teacherFormSubmissionService;

    private Teacher teacher;
    private AcademicYear academicYear;
    private TeacherFormSubmission submission;
    private TeacherFormSubmissionResponseDto responseDto;

    @BeforeEach
    void setUp() {
        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        teacher.setEmail("john.doe@example.com");

        academicYear = new AcademicYear();
        academicYear.setId(1L);
        academicYear.setYearName("2024/2025");
        academicYear.setIsLocked(false);

        submission = new TeacherFormSubmission();
        submission.setId(1L);
        submission.setTeacher(teacher);
        submission.setAcademicYear(academicYear);
        submission.setFormToken("unique-token-123");
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setIsProcessed(false);

        responseDto = TeacherFormSubmissionResponseDto.builder()
                .id(1L)
                .teacherId(1L)
                .teacherFirstName("John")
                .teacherLastName("Doe")
                .teacherEmail("john.doe@example.com")
                .yearId(1L)
                .yearName("2024/2025")
                .formToken("unique-token-123")
                .submittedAt(LocalDateTime.now())
                .isProcessed(false)
                .build();
    }

    // GET ALL Tests
    @Test
    @DisplayName("Should get all form submissions without filters")
    void shouldGetAllFormSubmissionsWithoutFilters() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("pageSize", "10");

        Page<TeacherFormSubmission> page = new PageImpl<>(List.of(submission));

        when(teacherFormSubmissionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(teacherFormSubmissionMapper.toResponseDto(any(TeacherFormSubmission.class)))
                .thenReturn(responseDto);

        Map<String, Object> result = teacherFormSubmissionService
                .getFormSubmissions(null, null, null, queryParams);

        assertNotNull(result);
        assertEquals(1L, result.get("totalItems"));

        verify(teacherFormSubmissionRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get form submissions filtered by teacher ID")
    void shouldGetFormSubmissionsFilteredByTeacherId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("pageSize", "10");

        Page<TeacherFormSubmission> page = new PageImpl<>(List.of(submission));

        when(teacherFormSubmissionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(teacherFormSubmissionMapper.toResponseDto(any(TeacherFormSubmission.class)))
                .thenReturn(responseDto);

        Map<String, Object> result = teacherFormSubmissionService
                .getFormSubmissions(1L, null, null, queryParams);

        assertNotNull(result);
        assertEquals(1L, result.get("totalItems"));

        verify(teacherFormSubmissionRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get form submissions filtered by year ID")
    void shouldGetFormSubmissionsFilteredByYearId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("pageSize", "10");

        Page<TeacherFormSubmission> page = new PageImpl<>(List.of(submission));

        when(teacherFormSubmissionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(teacherFormSubmissionMapper.toResponseDto(any(TeacherFormSubmission.class)))
                .thenReturn(responseDto);

        Map<String, Object> result = teacherFormSubmissionService
                .getFormSubmissions(null, 1L, null, queryParams);

        assertNotNull(result);
        assertEquals(1L, result.get("totalItems"));

        verify(teacherFormSubmissionRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get form submissions filtered by processed status")
    void shouldGetFormSubmissionsFilteredByProcessedStatus() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("pageSize", "10");

        Page<TeacherFormSubmission> page = new PageImpl<>(List.of(submission));

        when(teacherFormSubmissionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(teacherFormSubmissionMapper.toResponseDto(any(TeacherFormSubmission.class)))
                .thenReturn(responseDto);

        Map<String, Object> result = teacherFormSubmissionService
                .getFormSubmissions(null, null, false, queryParams);

        assertNotNull(result);
        assertEquals(1L, result.get("totalItems"));

        verify(teacherFormSubmissionRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // GET BY ID Tests
    @Test
    @DisplayName("Should get form submission by ID successfully")
    void shouldGetFormSubmissionByIdSuccessfully() {
        when(teacherFormSubmissionRepository.findById(1L))
                .thenReturn(Optional.of(submission));
        when(teacherFormSubmissionMapper.toResponseDto(submission))
                .thenReturn(responseDto);

        TeacherFormSubmissionResponseDto result = teacherFormSubmissionService
                .getFormSubmissionById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("unique-token-123", result.getFormToken());

        verify(teacherFormSubmissionRepository).findById(1L);
        verify(teacherFormSubmissionMapper).toResponseDto(submission);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when form submission not found by ID")
    void shouldThrowExceptionWhenFormSubmissionNotFoundById() {
        when(teacherFormSubmissionRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                teacherFormSubmissionService.getFormSubmissionById(1L)
        );

        verify(teacherFormSubmissionRepository).findById(1L);
    }

    // CREATE Tests
    @Test
    @DisplayName("Should create form submission successfully")
    void shouldCreateFormSubmissionSuccessfully() {
        TeacherFormSubmissionCreateDto createDto = new TeacherFormSubmissionCreateDto();
        createDto.setTeacherId(1L);
        createDto.setYearId(1L);
        createDto.setFormToken("new-token-456");
        createDto.setSubmittedAt(LocalDateTime.now());

        TeacherFormSubmission mappedSubmission = new TeacherFormSubmission();
        mappedSubmission.setTeacher(teacher);
        mappedSubmission.setAcademicYear(academicYear);
        mappedSubmission.setFormToken("new-token-456");
        mappedSubmission.setSubmittedAt(LocalDateTime.now());

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(academicYear));
        when(teacherFormSubmissionRepository.existsByFormToken("new-token-456")).thenReturn(false);
        when(teacherFormSubmissionMapper.toEntityCreate(createDto)).thenReturn(mappedSubmission);
        when(teacherFormSubmissionRepository.save(any(TeacherFormSubmission.class)))
                .thenAnswer(invocation -> {
                    TeacherFormSubmission saved = invocation.getArgument(0);
                    saved.setId(2L);
                    return saved;
                });
        when(teacherFormSubmissionMapper.toResponseDto(any(TeacherFormSubmission.class)))
                .thenReturn(responseDto);

        TeacherFormSubmissionResponseDto result = teacherFormSubmissionService
                .createFormSubmission(createDto);

        assertNotNull(result);

        verify(teacherRepository).findById(1L);
        verify(academicYearRepository).findById(1L);
        verify(teacherFormSubmissionRepository).existsByFormToken("new-token-456");
        verify(teacherFormSubmissionRepository).save(any(TeacherFormSubmission.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when teacher not found")
    void shouldThrowExceptionWhenTeacherNotFound() {
        TeacherFormSubmissionCreateDto createDto = new TeacherFormSubmissionCreateDto();
        createDto.setTeacherId(999L);
        createDto.setYearId(1L);
        createDto.setFormToken("token");
        createDto.setSubmittedAt(LocalDateTime.now());

        when(teacherRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                teacherFormSubmissionService.createFormSubmission(createDto)
        );

        verify(teacherRepository).findById(999L);
        verify(academicYearRepository, never()).findById(anyLong());
        verify(teacherFormSubmissionRepository, never()).save(any(TeacherFormSubmission.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when academic year not found")
    void shouldThrowExceptionWhenAcademicYearNotFound() {
        TeacherFormSubmissionCreateDto createDto = new TeacherFormSubmissionCreateDto();
        createDto.setTeacherId(1L);
        createDto.setYearId(999L);
        createDto.setFormToken("token");
        createDto.setSubmittedAt(LocalDateTime.now());

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(academicYearRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                teacherFormSubmissionService.createFormSubmission(createDto)
        );

        verify(teacherRepository).findById(1L);
        verify(academicYearRepository).findById(999L);
        verify(teacherFormSubmissionRepository, never()).save(any(TeacherFormSubmission.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when academic year is locked")
    void shouldThrowExceptionWhenAcademicYearIsLocked() {
        academicYear.setIsLocked(true);

        TeacherFormSubmissionCreateDto createDto = new TeacherFormSubmissionCreateDto();
        createDto.setTeacherId(1L);
        createDto.setYearId(1L);
        createDto.setFormToken("token");
        createDto.setSubmittedAt(LocalDateTime.now());

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(academicYear));

        assertThrows(IllegalArgumentException.class, () ->
                teacherFormSubmissionService.createFormSubmission(createDto)
        );

        verify(teacherRepository).findById(1L);
        verify(academicYearRepository).findById(1L);
        verify(teacherFormSubmissionRepository, never()).save(any(TeacherFormSubmission.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when form token already exists")
    void shouldThrowExceptionWhenFormTokenExists() {
        TeacherFormSubmissionCreateDto createDto = new TeacherFormSubmissionCreateDto();
        createDto.setTeacherId(1L);
        createDto.setYearId(1L);
        createDto.setFormToken("existing-token");
        createDto.setSubmittedAt(LocalDateTime.now());

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(academicYear));
        when(teacherFormSubmissionRepository.existsByFormToken("existing-token")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                teacherFormSubmissionService.createFormSubmission(createDto)
        );

        verify(teacherRepository).findById(1L);
        verify(academicYearRepository).findById(1L);
        verify(teacherFormSubmissionRepository).existsByFormToken("existing-token");
        verify(teacherFormSubmissionRepository, never()).save(any(TeacherFormSubmission.class));
    }

    // UPDATE STATUS Tests
    @Test
    @DisplayName("Should update form submission status successfully")
    void shouldUpdateFormSubmissionStatusSuccessfully() {
        TeacherFormSubmissionStatusUpdateDto updateDto = new TeacherFormSubmissionStatusUpdateDto();
        updateDto.setIsProcessed(true);

        when(teacherFormSubmissionRepository.findById(1L))
                .thenReturn(Optional.of(submission));
        when(teacherFormSubmissionRepository.save(any(TeacherFormSubmission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(teacherFormSubmissionMapper.toResponseDto(any(TeacherFormSubmission.class)))
                .thenReturn(responseDto);

        TeacherFormSubmissionResponseDto result = teacherFormSubmissionService
                .updateFormSubmissionStatus(1L, updateDto);

        assertNotNull(result);

        verify(teacherFormSubmissionRepository).findById(1L);
        verify(teacherFormSubmissionRepository).save(any(TeacherFormSubmission.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent form submission")
    void shouldThrowExceptionWhenUpdatingNonExistentFormSubmission() {
        TeacherFormSubmissionStatusUpdateDto updateDto = new TeacherFormSubmissionStatusUpdateDto();
        updateDto.setIsProcessed(true);

        when(teacherFormSubmissionRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                teacherFormSubmissionService.updateFormSubmissionStatus(999L, updateDto)
        );

        verify(teacherFormSubmissionRepository).findById(999L);
        verify(teacherFormSubmissionRepository, never()).save(any(TeacherFormSubmission.class));
    }
}
