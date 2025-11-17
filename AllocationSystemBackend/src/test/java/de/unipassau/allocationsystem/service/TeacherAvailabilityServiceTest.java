package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.TeacherAvailabilityCreateDto;
import de.unipassau.allocationsystem.dto.TeacherAvailabilityResponseDto;
import de.unipassau.allocationsystem.dto.TeacherAvailabilityUpdateDto;
import de.unipassau.allocationsystem.entity.*;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.TeacherAvailabilityMapper;
import de.unipassau.allocationsystem.repository.*;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TeacherAvailabilityService.
 */
@ExtendWith(MockitoExtension.class)
class TeacherAvailabilityServiceTest {

    @Mock
    private TeacherAvailabilityRepository teacherAvailabilityRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private AcademicYearRepository academicYearRepository;

    @Mock
    private InternshipTypeRepository internshipTypeRepository;

    @Mock
    private TeacherAvailabilityMapper teacherAvailabilityMapper;

    @InjectMocks
    private TeacherAvailabilityService teacherAvailabilityService;

    private Teacher testTeacher;
    private AcademicYear testYear;
    private InternshipType testInternshipType;
    private TeacherAvailability testAvailability;
    private TeacherAvailabilityCreateDto createDto;
    private TeacherAvailabilityUpdateDto updateDto;
    private TeacherAvailabilityResponseDto responseDto;

    @BeforeEach
    void setUp() {
        // Setup test teacher
        School testSchool = new School();
        testSchool.setId(1L);
        testSchool.setSchoolName("Test School");
        testSchool.setIsActive(true);

        testTeacher = new Teacher();
        testTeacher.setId(1L);
        testTeacher.setSchool(testSchool);
        testTeacher.setFirstName("John");
        testTeacher.setLastName("Doe");
        testTeacher.setEmail("john.doe@school.de");
        testTeacher.setIsActive(true);

        // Setup test academic year
        testYear = new AcademicYear();
        testYear.setId(1L);
        testYear.setYearName("2024/2025");

        // Setup test internship type
        testInternshipType = new InternshipType();
        testInternshipType.setId(1L);
        testInternshipType.setInternshipCode("SFP");
        testInternshipType.setFullName("Schulisches Fachpraktikum");
        testInternshipType.setTiming("Block");
        testInternshipType.setPeriodType("Continuous");
        testInternshipType.setSemester("Winter");
        testInternshipType.setIsSubjectSpecific(true);
        testInternshipType.setPriorityOrder(1);

        // Setup test availability entity
        testAvailability = new TeacherAvailability();
        testAvailability.setAvailabilityId(1L);
        testAvailability.setTeacher(testTeacher);
        testAvailability.setAcademicYear(testYear);
        testAvailability.setInternshipType(testInternshipType);
        testAvailability.setIsAvailable(true);
        testAvailability.setPreferenceRank(1);
        testAvailability.setNotes("Available all semester");

        // Setup create DTO
        createDto = new TeacherAvailabilityCreateDto();
        createDto.setTeacherId(1L);
        createDto.setYearId(1L);
        createDto.setInternshipTypeId(1L);
        createDto.setIsAvailable(true);
        createDto.setPreferenceRank(1);
        createDto.setNotes("Available all semester");

        // Setup update DTO
        updateDto = new TeacherAvailabilityUpdateDto();
        updateDto.setIsAvailable(false);
        updateDto.setPreferenceRank(null);
        updateDto.setNotes("Not available this semester");

        // Setup response DTO
        responseDto = TeacherAvailabilityResponseDto.builder()
                .availabilityId(1L)
                .teacherId(1L)
                .teacherFirstName("John")
                .teacherLastName("Doe")
                .teacherEmail("john.doe@school.de")
                .yearId(1L)
                .yearName("2024/2025")
                .internshipTypeId(1L)
                .internshipTypeName("Schulisches Fachpraktikum")
                .internshipTypeCode("SFP")
                .isAvailable(true)
                .preferenceRank(1)
                .notes("Available all semester")
                .build();
    }

    // ==================== getTeacherAvailability Tests ====================

    @Test
    void getTeacherAvailability_WithoutFilters_Success() {
        // Arrange
        Page<TeacherAvailability> availabilityPage = new PageImpl<>(Collections.singletonList(testAvailability));
        Map<String, String> queryParams = new HashMap<>();
        
        when(teacherRepository.existsById(1L)).thenReturn(true);
        when(teacherAvailabilityRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(availabilityPage);
        when(teacherAvailabilityMapper.toDto(testAvailability)).thenReturn(responseDto);

        // Act
        Map<String, Object> result = 
                teacherAvailabilityService.getTeacherAvailability(1L, null, null, queryParams);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("items"));
        verify(teacherRepository).existsById(1L);
        verify(teacherAvailabilityRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getTeacherAvailability_WithYearFilter_Success() {
        // Arrange
        Page<TeacherAvailability> availabilityPage = new PageImpl<>(Collections.singletonList(testAvailability));
        Map<String, String> queryParams = new HashMap<>();
        
        when(teacherRepository.existsById(1L)).thenReturn(true);
        when(teacherAvailabilityRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(availabilityPage);
        when(teacherAvailabilityMapper.toDto(testAvailability)).thenReturn(responseDto);

        // Act
        Map<String, Object> result = 
                teacherAvailabilityService.getTeacherAvailability(1L, 1L, null, queryParams);

        // Assert
        assertNotNull(result);
        verify(teacherAvailabilityRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getTeacherAvailability_WithInternshipTypeFilter_Success() {
        // Arrange
        Page<TeacherAvailability> availabilityPage = new PageImpl<>(Collections.singletonList(testAvailability));
        Map<String, String> queryParams = new HashMap<>();
        
        when(teacherRepository.existsById(1L)).thenReturn(true);
        when(teacherAvailabilityRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(availabilityPage);
        when(teacherAvailabilityMapper.toDto(testAvailability)).thenReturn(responseDto);

        // Act
        Map<String, Object> result = 
                teacherAvailabilityService.getTeacherAvailability(1L, null, 1L, queryParams);

        // Assert
        assertNotNull(result);
        verify(teacherAvailabilityRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getTeacherAvailability_TeacherNotFound_ThrowsException() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        when(teacherRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            teacherAvailabilityService.getTeacherAvailability(99L, null, null, queryParams);
        });
        verify(teacherRepository).existsById(99L);
        verify(teacherAvailabilityRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    // ==================== getAvailabilityById Tests ====================

    @Test
    void getAvailabilityById_Success() {
        // Arrange
        when(teacherAvailabilityRepository.findByAvailabilityIdAndTeacherId(1L, 1L))
                .thenReturn(Optional.of(testAvailability));
        when(teacherAvailabilityMapper.toDto(testAvailability)).thenReturn(responseDto);

        // Act
        TeacherAvailabilityResponseDto result = 
                teacherAvailabilityService.getAvailabilityById(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getAvailabilityId());
        assertEquals("John", result.getTeacherFirstName());
        verify(teacherAvailabilityRepository).findByAvailabilityIdAndTeacherId(1L, 1L);
    }

    @Test
    void getAvailabilityById_NotFound_ThrowsException() {
        // Arrange
        when(teacherAvailabilityRepository.findByAvailabilityIdAndTeacherId(99L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            teacherAvailabilityService.getAvailabilityById(1L, 99L);
        });
        verify(teacherAvailabilityRepository).findByAvailabilityIdAndTeacherId(99L, 1L);
    }

    // ==================== createAvailability Tests ====================

    @Test
    void createAvailability_Success() {
        // Arrange
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testYear));
        when(internshipTypeRepository.findById(1L)).thenReturn(Optional.of(testInternshipType));
        when(teacherAvailabilityRepository.existsByTeacherIdAndAcademicYearIdAndInternshipTypeId(1L, 1L, 1L))
                .thenReturn(false);
        when(teacherAvailabilityRepository.save(any(TeacherAvailability.class)))
                .thenReturn(testAvailability);
        when(teacherAvailabilityMapper.toDto(testAvailability)).thenReturn(responseDto);

        // Act
        TeacherAvailabilityResponseDto result = 
                teacherAvailabilityService.createAvailability(1L, createDto);

        // Assert
        assertNotNull(result);
        verify(teacherRepository).findById(1L);
        verify(academicYearRepository).findById(1L);
        verify(internshipTypeRepository).findById(1L);
        verify(teacherAvailabilityRepository).save(any(TeacherAvailability.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void createAvailability_TeacherIdMismatch_ThrowsException() {
        // Arrange
        createDto.setTeacherId(2L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            teacherAvailabilityService.createAvailability(1L, createDto);
        });
        verify(teacherRepository, never()).findById(any());
    }

    @Test
    void createAvailability_TeacherNotFound_ThrowsException() {
        // Arrange
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            teacherAvailabilityService.createAvailability(1L, createDto);
        });
        verify(teacherRepository).findById(1L);
        verify(teacherAvailabilityRepository, never()).save(any());
    }

    @Test
    void createAvailability_InactiveTeacher_ThrowsException() {
        // Arrange
        testTeacher.setIsActive(false);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            teacherAvailabilityService.createAvailability(1L, createDto);
        });
        verify(teacherRepository).findById(1L);
        verify(teacherAvailabilityRepository, never()).save(any());
    }

    @Test
    void createAvailability_YearNotFound_ThrowsException() {
        // Arrange
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(academicYearRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            teacherAvailabilityService.createAvailability(1L, createDto);
        });
        verify(academicYearRepository).findById(1L);
        verify(teacherAvailabilityRepository, never()).save(any());
    }

    @Test
    void createAvailability_InternshipTypeNotFound_ThrowsException() {
        // Arrange
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testYear));
        when(internshipTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            teacherAvailabilityService.createAvailability(1L, createDto);
        });
        verify(internshipTypeRepository).findById(1L);
        verify(teacherAvailabilityRepository, never()).save(any());
    }

    // Note: Removed test for inactive internship type as new schema doesn't have is_active field
    // @Test
    // void createAvailability_InactiveInternshipType_ThrowsException() {
    //     // Arrange
    //     testInternshipType.setIsActive(false);
    //     when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
    //     when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testYear));
    //     when(internshipTypeRepository.findById(1L)).thenReturn(Optional.of(testInternshipType));
    //
    //     // Act & Assert
    //     assertThrows(IllegalArgumentException.class, () -> {
    //         teacherAvailabilityService.createAvailability(1L, createDto);
    //     });
    //     verify(internshipTypeRepository).findById(1L);
    //     verify(teacherAvailabilityRepository, never()).save(any());
    // }

    @Test
    void createAvailability_DuplicateEntry_ThrowsException() {
        // Arrange
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testYear));
        when(internshipTypeRepository.findById(1L)).thenReturn(Optional.of(testInternshipType));
        when(teacherAvailabilityRepository.existsByTeacherIdAndAcademicYearIdAndInternshipTypeId(1L, 1L, 1L))
                .thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            teacherAvailabilityService.createAvailability(1L, createDto);
        });
        verify(teacherAvailabilityRepository, never()).save(any());
    }

    @Test
    void createAvailability_PreferenceRankWithNotAvailable_ThrowsException() {
        // Arrange
        createDto.setIsAvailable(false);
        createDto.setPreferenceRank(1); // Should be null when not available

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testYear));
        when(internshipTypeRepository.findById(1L)).thenReturn(Optional.of(testInternshipType));
        // No need to mock existsByTeacherIdAndAcademicYearIdAndInternshipTypeId
        // because validation fails before the duplicate check

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            teacherAvailabilityService.createAvailability(1L, createDto);
        });
        verify(teacherAvailabilityRepository, never()).save(any());
    }

    // ==================== updateAvailability Tests ====================

    @Test
    void updateAvailability_Success() {
        // Arrange
        when(teacherAvailabilityRepository.findByAvailabilityIdAndTeacherId(1L, 1L))
                .thenReturn(Optional.of(testAvailability));
        when(teacherAvailabilityRepository.save(any(TeacherAvailability.class)))
                .thenReturn(testAvailability);
        when(teacherAvailabilityMapper.toDto(testAvailability)).thenReturn(responseDto);
        doAnswer(invocation -> {
            TeacherAvailability entity = invocation.getArgument(0);
            TeacherAvailabilityUpdateDto dto = invocation.getArgument(1);
            entity.setIsAvailable(dto.getIsAvailable());
            entity.setPreferenceRank(dto.getPreferenceRank());
            entity.setNotes(dto.getNotes());
            return null;
        }).when(teacherAvailabilityMapper)
                .updateEntityFromDto(any(TeacherAvailability.class), any(TeacherAvailabilityUpdateDto.class));

        // Act
        TeacherAvailabilityResponseDto result = 
                teacherAvailabilityService.updateAvailability(1L, 1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(teacherAvailabilityRepository).findByAvailabilityIdAndTeacherId(1L, 1L);
        verify(teacherAvailabilityRepository).save(any(TeacherAvailability.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void updateAvailability_NotFound_ThrowsException() {
        // Arrange
        when(teacherAvailabilityRepository.findByAvailabilityIdAndTeacherId(99L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            teacherAvailabilityService.updateAvailability(1L, 99L, updateDto);
        });
        verify(teacherAvailabilityRepository).findByAvailabilityIdAndTeacherId(99L, 1L);
        verify(teacherAvailabilityRepository, never()).save(any());
    }

    @Test
    void updateAvailability_ChangeYear_Success() {
        // Arrange
        AcademicYear newYear = new AcademicYear();
        newYear.setId(2L);
        newYear.setYearName("2025/2026");

        updateDto.setYearId(2L);
        when(teacherAvailabilityRepository.findByAvailabilityIdAndTeacherId(1L, 1L))
                .thenReturn(Optional.of(testAvailability));
        when(academicYearRepository.findById(2L)).thenReturn(Optional.of(newYear));
        when(teacherAvailabilityRepository.existsByTeacherIdAndYearIdAndInternshipTypeIdAndIdNot(1L, 2L, 1L, 1L))
                .thenReturn(false);
        when(teacherAvailabilityRepository.save(any(TeacherAvailability.class)))
                .thenReturn(testAvailability);
        when(teacherAvailabilityMapper.toDto(testAvailability)).thenReturn(responseDto);
        doAnswer(invocation -> {
            TeacherAvailability entity = invocation.getArgument(0);
            TeacherAvailabilityUpdateDto dto = invocation.getArgument(1);
            entity.setIsAvailable(dto.getIsAvailable());
            entity.setPreferenceRank(dto.getPreferenceRank());
            entity.setNotes(dto.getNotes());
            return null;
        }).when(teacherAvailabilityMapper)
                .updateEntityFromDto(any(TeacherAvailability.class), any(TeacherAvailabilityUpdateDto.class));

        // Act
        TeacherAvailabilityResponseDto result = 
                teacherAvailabilityService.updateAvailability(1L, 1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(academicYearRepository).findById(2L);
        verify(teacherAvailabilityRepository).save(any(TeacherAvailability.class));
    }

    @Test
    void updateAvailability_ChangeInternshipType_Success() {
        // Arrange
        InternshipType newInternshipType = new InternshipType();
        newInternshipType.setId(2L);
        newInternshipType.setInternshipCode("ZSP");
        newInternshipType.setFullName("Additional Subject Practicum");

        updateDto.setInternshipTypeId(2L);
        when(teacherAvailabilityRepository.findByAvailabilityIdAndTeacherId(1L, 1L))
                .thenReturn(Optional.of(testAvailability));
        when(internshipTypeRepository.findById(2L)).thenReturn(Optional.of(newInternshipType));
        when(teacherAvailabilityRepository.existsByTeacherIdAndYearIdAndInternshipTypeIdAndIdNot(1L, 1L, 2L, 1L))
                .thenReturn(false);
        when(teacherAvailabilityRepository.save(any(TeacherAvailability.class)))
                .thenReturn(testAvailability);
        when(teacherAvailabilityMapper.toDto(testAvailability)).thenReturn(responseDto);
        doAnswer(invocation -> {
            TeacherAvailability entity = invocation.getArgument(0);
            TeacherAvailabilityUpdateDto dto = invocation.getArgument(1);
            entity.setIsAvailable(dto.getIsAvailable());
            entity.setPreferenceRank(dto.getPreferenceRank());
            entity.setNotes(dto.getNotes());
            return null;
        }).when(teacherAvailabilityMapper)
                .updateEntityFromDto(any(TeacherAvailability.class), any(TeacherAvailabilityUpdateDto.class));

        // Act
        TeacherAvailabilityResponseDto result = 
                teacherAvailabilityService.updateAvailability(1L, 1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(internshipTypeRepository).findById(2L);
        verify(teacherAvailabilityRepository).save(any(TeacherAvailability.class));
    }

    @Test
    void updateAvailability_DuplicateAfterChange_ThrowsException() {
        // Arrange
        updateDto.setYearId(2L);
        when(teacherAvailabilityRepository.findByAvailabilityIdAndTeacherId(1L, 1L))
                .thenReturn(Optional.of(testAvailability));
        when(teacherAvailabilityRepository.existsByTeacherIdAndYearIdAndInternshipTypeIdAndIdNot(1L, 2L, 1L, 1L))
                .thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            teacherAvailabilityService.updateAvailability(1L, 1L, updateDto);
        });
        verify(teacherAvailabilityRepository, never()).save(any());
    }

    @Test
    void updateAvailability_PreferenceRankWithNotAvailable_ThrowsException() {
        // Arrange
        updateDto.setIsAvailable(false);
        updateDto.setPreferenceRank(2); // Should be null

        when(teacherAvailabilityRepository.findByAvailabilityIdAndTeacherId(1L, 1L))
                .thenReturn(Optional.of(testAvailability));
        doAnswer(invocation -> {
            TeacherAvailability entity = invocation.getArgument(0);
            TeacherAvailabilityUpdateDto dto = invocation.getArgument(1);
            entity.setIsAvailable(dto.getIsAvailable());
            entity.setPreferenceRank(dto.getPreferenceRank());
            entity.setNotes(dto.getNotes());
            return null;
        }).when(teacherAvailabilityMapper)
                .updateEntityFromDto(any(TeacherAvailability.class), any(TeacherAvailabilityUpdateDto.class));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            teacherAvailabilityService.updateAvailability(1L, 1L, updateDto);
        });
        verify(teacherAvailabilityRepository, never()).save(any());
    }

    // ==================== deleteAvailability Tests ====================

    @Test
    void deleteAvailability_Success() {
        // Arrange
        when(teacherAvailabilityRepository.findByAvailabilityIdAndTeacherId(1L, 1L))
                .thenReturn(Optional.of(testAvailability));
        doNothing().when(teacherAvailabilityRepository).delete(any(TeacherAvailability.class));

        // Act
        teacherAvailabilityService.deleteAvailability(1L, 1L);

        // Assert
        verify(teacherAvailabilityRepository).findByAvailabilityIdAndTeacherId(1L, 1L);
        verify(teacherAvailabilityRepository).delete(testAvailability);
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void deleteAvailability_NotFound_ThrowsException() {
        // Arrange
        when(teacherAvailabilityRepository.findByAvailabilityIdAndTeacherId(99L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            teacherAvailabilityService.deleteAvailability(1L, 99L);
        });
        verify(teacherAvailabilityRepository).findByAvailabilityIdAndTeacherId(99L, 1L);
        verify(teacherAvailabilityRepository, never()).delete(any(TeacherAvailability.class));
    }
}
