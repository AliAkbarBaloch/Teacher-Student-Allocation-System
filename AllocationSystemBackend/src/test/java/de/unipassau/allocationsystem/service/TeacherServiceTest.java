package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherResponseDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherUpdateDto;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.School.SchoolType;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.Teacher.EmploymentStatus;
import de.unipassau.allocationsystem.entity.Teacher.UsageCycle;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.TeacherMapper;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TeacherService.
 */
@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private TeacherMapper teacherMapper;

    @InjectMocks
    private TeacherService teacherService;

    private School testSchool;
    private Teacher testTeacher;
    private TeacherCreateDto createDto;
    private TeacherUpdateDto updateDto;
    private TeacherResponseDto responseDto;

    @BeforeEach
    void setUp() {
        // Setup test school
        testSchool = new School();
        testSchool.setId(1L);
        testSchool.setSchoolName("Test School");
        testSchool.setSchoolType(SchoolType.PRIMARY);
        testSchool.setZoneNumber(1);
        testSchool.setIsActive(true);

        // Setup test teacher entity
        testTeacher = new Teacher();
        testTeacher.setId(1L);
        testTeacher.setSchool(testSchool);
        testTeacher.setFirstName("John");
        testTeacher.setLastName("Doe");
        testTeacher.setEmail("john.doe@school.de");
        testTeacher.setPhone("+49841123456");
        testTeacher.setIsPartTime(false);
        testTeacher.setEmploymentStatus(EmploymentStatus.FULL_TIME);
        testTeacher.setUsageCycle(UsageCycle.FULL_YEAR);
        testTeacher.setIsActive(true);

        // Setup create DTO
        createDto = new TeacherCreateDto();
        createDto.setSchoolId(1L);
        createDto.setFirstName("Jane");
        createDto.setLastName("Smith");
        createDto.setEmail("jane.smith@school.de");
        createDto.setPhone("+49841654321");
        createDto.setIsPartTime(false);
        createDto.setEmploymentStatus(EmploymentStatus.FULL_TIME);
        createDto.setUsageCycle(UsageCycle.FULL_YEAR);

        // Setup update DTO
        updateDto = new TeacherUpdateDto();
        updateDto.setFirstName("Updated");
        updateDto.setLastName("Name");
        updateDto.setEmail("updated@school.de");

        // Setup response DTO
        responseDto = TeacherResponseDto.builder()
                .id(1L)
                .schoolId(1L)
                .schoolName("Test School")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@school.de")
                .phone("+49841123456")
                .isPartTime(false)
                .employmentStatus(EmploymentStatus.FULL_TIME)
                .usageCycle(UsageCycle.FULL_YEAR)
                .isActive(true)
                .build();
    }

    // ==================== getAllTeachers Tests ====================

    @Test
    void getAllTeachers_WithoutFilters_Success() {
        // Arrange
        Page<Teacher> teacherPage = new PageImpl<>(Collections.singletonList(testTeacher));
        Map<String, String> queryParams = new HashMap<>();
        
        when(teacherRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(teacherPage);
        when(teacherMapper.toResponseDto(testTeacher)).thenReturn(responseDto);

        // Act
        Map<String, Object> result = teacherService.getAllTeachers(queryParams);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("items"));
        verify(teacherRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllTeachers_WithSchoolFilter_Success() {
        // Arrange
        Page<Teacher> teacherPage = new PageImpl<>(Collections.singletonList(testTeacher));
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("schoolId", "1");
        
        when(teacherRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(teacherPage);
        when(teacherMapper.toResponseDto(testTeacher)).thenReturn(responseDto);

        // Act
        Map<String, Object> result = teacherService.getAllTeachers(queryParams);

        // Assert
        assertNotNull(result);
        verify(teacherRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllTeachers_WithEmploymentStatusFilter_Success() {
        // Arrange
        Page<Teacher> teacherPage = new PageImpl<>(Collections.singletonList(testTeacher));
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("employmentStatus", "FULL_TIME");
        
        when(teacherRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(teacherPage);
        when(teacherMapper.toResponseDto(testTeacher)).thenReturn(responseDto);

        // Act
        Map<String, Object> result = teacherService.getAllTeachers(queryParams);

        // Assert
        assertNotNull(result);
        verify(teacherRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllTeachers_WithSearchFilter_Success() {
        // Arrange
        Page<Teacher> teacherPage = new PageImpl<>(Collections.singletonList(testTeacher));
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("search", "John");
        
        when(teacherRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(teacherPage);
        when(teacherMapper.toResponseDto(testTeacher)).thenReturn(responseDto);

        // Act
        Map<String, Object> result = teacherService.getAllTeachers(queryParams);

        // Assert
        assertNotNull(result);
        verify(teacherRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllTeachers_WithPagination_Success() {
        // Arrange
        Page<Teacher> teacherPage = new PageImpl<>(Collections.singletonList(testTeacher));
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "2");
        queryParams.put("pageSize", "20");
        queryParams.put("sortBy", "lastName");
        queryParams.put("sortOrder", "asc");
        
        when(teacherRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(teacherPage);
        when(teacherMapper.toResponseDto(testTeacher)).thenReturn(responseDto);

        // Act
        Map<String, Object> result = teacherService.getAllTeachers(queryParams);

        // Assert
        assertNotNull(result);
        verify(teacherRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // ==================== getTeacherById Tests ====================

    @Test
    void getTeacherById_Success() {
        // Arrange
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherMapper.toResponseDto(testTeacher)).thenReturn(responseDto);

        // Act
        Optional<TeacherResponseDto> result = teacherService.getById(1l);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.get().getFirstName());
        assertEquals("Doe", result.get().getLastName());
        verify(teacherRepository).findById(1L);
        verify(teacherMapper).toResponseDto(testTeacher);
    }

    @Test
    void getTeacherById_NotFound_ThrowsException() {
        // Arrange
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            teacherService.getById(99L);
        });
        verify(teacherRepository).findById(99L);
    }

    // ==================== createTeacher Tests ====================

    @Test
    void createTeacher_Success() {
        // Arrange
        when(teacherRepository.existsByEmail(createDto.getEmail())).thenReturn(false);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(testTeacher);
        when(teacherMapper.toResponseDto(testTeacher)).thenReturn(responseDto);

        // Act
        TeacherResponseDto result = teacherService.createTeacher(createDto);

        // Assert
        assertNotNull(result);
        verify(teacherRepository).existsByEmail(createDto.getEmail());
        verify(schoolRepository).findById(1L);
        verify(teacherRepository).save(any(Teacher.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void createTeacher_DuplicateEmail_ThrowsException() {
        // Arrange
        when(teacherRepository.existsByEmail(createDto.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            teacherService.createTeacher(createDto);
        });
        verify(teacherRepository).existsByEmail(createDto.getEmail());
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void createTeacher_SchoolNotFound_ThrowsException() {
        // Arrange
        when(teacherRepository.existsByEmail(createDto.getEmail())).thenReturn(false);
        when(schoolRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            teacherService.createTeacher(createDto);
        });
        verify(teacherRepository).existsByEmail(createDto.getEmail());
        verify(schoolRepository).findById(1L);
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void createTeacher_InactiveSchool_ThrowsException() {
        // Arrange
        testSchool.setIsActive(false);
        when(teacherRepository.existsByEmail(createDto.getEmail())).thenReturn(false);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            teacherService.createTeacher(createDto);
        });
        verify(teacherRepository).existsByEmail(createDto.getEmail());
        verify(schoolRepository).findById(1L);
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void createTeacher_PartTimeInconsistency_ThrowsException() {
        // Arrange
        createDto.setIsPartTime(true);
        createDto.setEmploymentStatus(EmploymentStatus.FULL_TIME);
        when(teacherRepository.existsByEmail(createDto.getEmail())).thenReturn(false);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            teacherService.createTeacher(createDto);
        });
    }

    // ==================== updateTeacher Tests ====================

    @Test
    void updateTeacher_Success() {
        // Arrange
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(testTeacher);
        when(teacherMapper.toResponseDto(testTeacher)).thenReturn(responseDto);
        doNothing().when(teacherMapper).updateEntityFromDto(any(TeacherUpdateDto.class), any(Teacher.class));

        // Act
        TeacherResponseDto result = teacherService.updateTeacher(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(teacherRepository).findById(1L);
        verify(teacherMapper).updateEntityFromDto(any(TeacherUpdateDto.class), any(Teacher.class));
        verify(teacherRepository).save(any(Teacher.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void updateTeacher_NotFound_ThrowsException() {
        // Arrange
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            teacherService.updateTeacher(99L, updateDto);
        });
        verify(teacherRepository).findById(99L);
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void updateTeacher_DuplicateEmail_ThrowsException() {
        // Arrange
        updateDto.setEmail("existing@school.de");
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherRepository.existsByEmailAndIdNot("existing@school.de", 1L)).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            teacherService.updateTeacher(1L, updateDto);
        });
        verify(teacherRepository).findById(1L);
        verify(teacherRepository).existsByEmailAndIdNot("existing@school.de", 1L);
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void updateTeacher_ChangeSchool_Success() {
        // Arrange
        School newSchool = new School();
        newSchool.setId(2L);
        newSchool.setSchoolName("New School");
        newSchool.setIsActive(true);

        updateDto.setSchoolId(2L);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(schoolRepository.findById(2L)).thenReturn(Optional.of(newSchool));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(testTeacher);
        when(teacherMapper.toResponseDto(testTeacher)).thenReturn(responseDto);
        doNothing().when(teacherMapper).updateEntityFromDto(any(TeacherUpdateDto.class), any(Teacher.class));

        // Act
        TeacherResponseDto result = teacherService.updateTeacher(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(schoolRepository).findById(2L);
        verify(teacherRepository).save(any(Teacher.class));
    }

    // ==================== updateTeacherStatus Tests ====================

    @Test
    void updateTeacherStatus_Deactivate_Success() {
        // Arrange
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(testTeacher);
        when(teacherMapper.toResponseDto(testTeacher)).thenReturn(responseDto);

        // Act
        TeacherResponseDto result = teacherService.updateTeacherStatus(1L, false);

        // Assert
        assertNotNull(result);
        verify(teacherRepository).findById(1L);
        verify(teacherRepository).save(any(Teacher.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void updateTeacherStatus_Activate_Success() {
        // Arrange
        testTeacher.setIsActive(false);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(testTeacher);
        when(teacherMapper.toResponseDto(testTeacher)).thenReturn(responseDto);

        // Act
        TeacherResponseDto result = teacherService.updateTeacherStatus(1L, true);

        // Assert
        assertNotNull(result);
        verify(teacherRepository).findById(1L);
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void updateTeacherStatus_NotFound_ThrowsException() {
        // Arrange
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            teacherService.updateTeacherStatus(99L, false);
        });
        verify(teacherRepository).findById(99L);
        verify(teacherRepository, never()).save(any());
    }

    // ==================== deleteTeacher Tests ====================

    @Test
    void deleteTeacher_Success() {
        // Arrange
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(testTeacher);

        // Act
        teacherService.deleteTeacher(1L);

        // Assert
        verify(teacherRepository).findById(1L);
        verify(teacherRepository).save(any(Teacher.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void deleteTeacher_NotFound_ThrowsException() {
        // Arrange
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            teacherService.deleteTeacher(99L);
        });
        verify(teacherRepository).findById(99L);
        verify(teacherRepository, never()).save(any());
    }
}
