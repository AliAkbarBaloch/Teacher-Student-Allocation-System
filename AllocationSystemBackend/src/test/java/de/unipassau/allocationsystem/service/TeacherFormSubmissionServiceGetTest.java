package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionResponseDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherFormSubmission;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.TeacherFormSubmissionMapper;
import de.unipassau.allocationsystem.repository.TeacherFormSubmissionRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TeacherFormSubmissionService} (GET operations).
 */
@ExtendWith(MockitoExtension.class)
class TeacherFormSubmissionServiceGetTest {

    @Mock
    private TeacherFormSubmissionRepository teacherFormSubmissionRepository;

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

    @Test
    @DisplayName("Should get all form submissions without filters")
    void shouldGetAllFormSubmissionsWithoutFilters() {
        Map<String, String> queryParams = Map.of("page", "1", "pageSize", "10");
        Page<TeacherFormSubmission> page = new PageImpl<>(List.of(submission));

        when(teacherFormSubmissionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(teacherFormSubmissionMapper.toResponseDto(any(TeacherFormSubmission.class)))
                .thenReturn(responseDto);

        Map<String, Object> result =
                teacherFormSubmissionService.getFormSubmissions(null, null, null, queryParams);

        assertNotNull(result);
        assertEquals(1L, result.get("totalItems"));

        verify(teacherFormSubmissionRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get form submissions filtered by teacher ID")
    void shouldGetFormSubmissionsFilteredByTeacherId() {
        Map<String, String> queryParams = Map.of("page", "1", "pageSize", "10");
        Page<TeacherFormSubmission> page = new PageImpl<>(List.of(submission));

        when(teacherFormSubmissionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(teacherFormSubmissionMapper.toResponseDto(any(TeacherFormSubmission.class)))
                .thenReturn(responseDto);

        Map<String, Object> result =
                teacherFormSubmissionService.getFormSubmissions(1L, null, null, queryParams);

        assertNotNull(result);
        assertEquals(1L, result.get("totalItems"));

        verify(teacherFormSubmissionRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get form submissions filtered by year ID")
    void shouldGetFormSubmissionsFilteredByYearId() {
        Map<String, String> queryParams = Map.of("page", "1", "pageSize", "10");
        Page<TeacherFormSubmission> page = new PageImpl<>(List.of(submission));

        when(teacherFormSubmissionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(teacherFormSubmissionMapper.toResponseDto(any(TeacherFormSubmission.class)))
                .thenReturn(responseDto);

        Map<String, Object> result =
                teacherFormSubmissionService.getFormSubmissions(null, 1L, null, queryParams);

        assertNotNull(result);
        assertEquals(1L, result.get("totalItems"));

        verify(teacherFormSubmissionRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get form submissions filtered by processed status")
    void shouldGetFormSubmissionsFilteredByProcessedStatus() {
        Map<String, String> queryParams = Map.of("page", "1", "pageSize", "10");
        Page<TeacherFormSubmission> page = new PageImpl<>(List.of(submission));

        when(teacherFormSubmissionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(teacherFormSubmissionMapper.toResponseDto(any(TeacherFormSubmission.class)))
                .thenReturn(responseDto);

        Map<String, Object> result =
                teacherFormSubmissionService.getFormSubmissions(null, null, false, queryParams);

        assertNotNull(result);
        assertEquals(1L, result.get("totalItems"));

        verify(teacherFormSubmissionRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get form submission by ID successfully")
    void shouldGetFormSubmissionByIdSuccessfully() {
        when(teacherFormSubmissionRepository.findById(1L))
                .thenReturn(Optional.of(submission));
        when(teacherFormSubmissionMapper.toResponseDto(submission))
                .thenReturn(responseDto);

        TeacherFormSubmissionResponseDto result =
                teacherFormSubmissionService.getFormSubmissionById(1L);

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
}
