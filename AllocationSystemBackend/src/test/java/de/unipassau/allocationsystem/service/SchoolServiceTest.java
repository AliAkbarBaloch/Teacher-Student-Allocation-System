package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.school.SchoolCreateDto;
import de.unipassau.allocationsystem.dto.school.SchoolResponseDto;
import de.unipassau.allocationsystem.dto.school.SchoolUpdateDto;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.School.SchoolType;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.SchoolMapper;
import de.unipassau.allocationsystem.repository.SchoolRepository;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SchoolService.
 */
@ExtendWith(MockitoExtension.class)
class SchoolServiceTest {

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private SchoolMapper schoolMapper;

    @InjectMocks
    private SchoolService schoolService;

    private School testSchool;
    private SchoolCreateDto createDto;
    private SchoolUpdateDto updateDto;
    private SchoolResponseDto responseDto;

    @BeforeEach
    void setUp() {
        // Setup test school entity
        testSchool = new School();
        testSchool.setId(1L);
        testSchool.setSchoolName("Test Elementary School");
        testSchool.setSchoolType(SchoolType.PRIMARY);
        testSchool.setZoneNumber(1);
        testSchool.setAddress("Test Street 1");
        testSchool.setLatitude(new BigDecimal("48.5734053"));
        testSchool.setLongitude(new BigDecimal("13.4579944"));
        testSchool.setDistanceFromCenter(new BigDecimal("2.5"));
        testSchool.setTransportAccessibility("Bus Line 1");
        testSchool.setContactEmail("test@school.de");
        testSchool.setContactPhone("+49841123456");
        testSchool.setIsActive(true);

        // Setup create DTO
        createDto = new SchoolCreateDto();
        createDto.setSchoolName("New Test School");
        createDto.setSchoolType(SchoolType.MIDDLE);
        createDto.setZoneNumber(2);
        createDto.setAddress("New Street 10");
        createDto.setContactEmail("new@school.de");
        createDto.setContactPhone("+49841999999");

        // Setup update DTO
        updateDto = new SchoolUpdateDto();
        updateDto.setSchoolName("Updated School Name");
        updateDto.setAddress("Updated Address");

        // Setup response DTO
        responseDto = new SchoolResponseDto();
        responseDto.setId(1L);
        responseDto.setSchoolName("Test Elementary School");
        responseDto.setSchoolType(SchoolType.PRIMARY);
        responseDto.setZoneNumber(1);
        responseDto.setIsActive(true);
    }

    // ==================== getAllSchools Tests ====================

    @Test
    void getAllSchools_WithoutFilters_Success() {
        // Arrange
        Page<School> schoolPage = new PageImpl<>(Arrays.asList(testSchool));
        java.util.Map<String, String> queryParams = new java.util.HashMap<>();
        
        when(schoolRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(schoolPage);
        when(schoolMapper.toResponseDto(testSchool)).thenReturn(responseDto);

        // Act
        java.util.Map<String, Object> result = schoolService.getAllSchools(queryParams, null, null, null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("items"));
        assertTrue(result.containsKey("totalItems"));
        verify(schoolRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(schoolMapper).toResponseDto(testSchool);
    }

    @Test
    void getAllSchools_WithSearchFilter_Success() {
        // Arrange
        Page<School> schoolPage = new PageImpl<>(Arrays.asList(testSchool));
        java.util.Map<String, String> queryParams = new java.util.HashMap<>();
        
        when(schoolRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(schoolPage);
        when(schoolMapper.toResponseDto(testSchool)).thenReturn(responseDto);

        // Act
        java.util.Map<String, Object> result = schoolService.getAllSchools(queryParams, "Elementary", null, null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("items"));
        verify(schoolRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllSchools_WithTypeFilter_Success() {
        // Arrange
        Page<School> schoolPage = new PageImpl<>(Arrays.asList(testSchool));
        java.util.Map<String, String> queryParams = new java.util.HashMap<>();
        
        when(schoolRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(schoolPage);
        when(schoolMapper.toResponseDto(testSchool)).thenReturn(responseDto);

        // Act
        java.util.Map<String, Object> result = schoolService.getAllSchools(queryParams, null, SchoolType.PRIMARY, null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("totalItems"));
        verify(schoolRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllSchools_WithZoneFilter_Success() {
        // Arrange
        Page<School> schoolPage = new PageImpl<>(Arrays.asList(testSchool));
        java.util.Map<String, String> queryParams = new java.util.HashMap<>();
        
        when(schoolRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(schoolPage);
        when(schoolMapper.toResponseDto(testSchool)).thenReturn(responseDto);

        // Act
        java.util.Map<String, Object> result = schoolService.getAllSchools(queryParams, null, null, 1, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("totalItems"));
        verify(schoolRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllSchools_WithIsActiveFilter_Success() {
        // Arrange
        Page<School> schoolPage = new PageImpl<>(Arrays.asList(testSchool));
        java.util.Map<String, String> queryParams = new java.util.HashMap<>();
        
        when(schoolRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(schoolPage);
        when(schoolMapper.toResponseDto(testSchool)).thenReturn(responseDto);

        // Act
        java.util.Map<String, Object> result = schoolService.getAllSchools(queryParams, null, null, null, true);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("totalItems"));
        verify(schoolRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllSchools_WithAllFilters_Success() {
        // Arrange
        Page<School> schoolPage = new PageImpl<>(Arrays.asList(testSchool));
        java.util.Map<String, String> queryParams = new java.util.HashMap<>();
        
        when(schoolRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(schoolPage);
        when(schoolMapper.toResponseDto(testSchool)).thenReturn(responseDto);

        // Act
        java.util.Map<String, Object> result = schoolService.getAllSchools(queryParams, "Test", SchoolType.PRIMARY, 1, true);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("totalItems"));
        verify(schoolRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // ==================== getSchoolById Tests ====================

    @Test
    void getSchoolById_Success() {
        // Arrange
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(schoolMapper.toResponseDto(testSchool)).thenReturn(responseDto);

        // Act
        SchoolResponseDto result = schoolService.getSchoolById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(responseDto, result);
        verify(schoolRepository).findById(1L);
        verify(schoolMapper).toResponseDto(testSchool);
    }

    @Test
    void getSchoolById_NotFound_ThrowsException() {
        // Arrange
        when(schoolRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            schoolService.getSchoolById(999L);
        });
        verify(schoolRepository).findById(999L);
        verify(schoolMapper, never()).toResponseDto(any());
    }

    // ==================== createSchool Tests ====================

    @Test
    void createSchool_Success() {
        // Arrange
        School newSchool = new School();
        newSchool.setSchoolName(createDto.getSchoolName());
        newSchool.setSchoolType(createDto.getSchoolType());
        
        School savedSchool = new School();
        savedSchool.setId(2L);
        savedSchool.setSchoolName(createDto.getSchoolName());
        savedSchool.setSchoolType(createDto.getSchoolType());

        when(schoolRepository.existsBySchoolName(createDto.getSchoolName())).thenReturn(false);
        when(schoolMapper.toEntityCreate(createDto)).thenReturn(newSchool);
        when(schoolRepository.save(newSchool)).thenReturn(savedSchool);
        when(schoolMapper.toResponseDto(savedSchool)).thenReturn(responseDto);

        // Act
        SchoolResponseDto result = schoolService.createSchool(createDto);

        // Assert
        assertNotNull(result);
        assertEquals(responseDto, result);
        verify(schoolRepository).existsBySchoolName(createDto.getSchoolName());
        verify(schoolMapper).toEntityCreate(createDto);
        verify(schoolRepository).save(newSchool);
        verify(schoolMapper).toResponseDto(savedSchool);
    }

    @Test
    void createSchool_DuplicateName_ThrowsException() {
        // Arrange
        when(schoolRepository.existsBySchoolName(createDto.getSchoolName())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            schoolService.createSchool(createDto);
        });
        verify(schoolRepository).existsBySchoolName(createDto.getSchoolName());
        verify(schoolMapper, never()).toEntityCreate(any());
        verify(schoolRepository, never()).save(any());
    }

    // ==================== updateSchool Tests ====================

    @Test
    void updateSchool_Success() {
        // Arrange
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(schoolRepository.existsBySchoolNameAndIdNot(updateDto.getSchoolName(), 1L)).thenReturn(false);
        when(schoolRepository.save(testSchool)).thenReturn(testSchool);
        when(schoolMapper.toResponseDto(testSchool)).thenReturn(responseDto);
        doNothing().when(schoolMapper).updateEntityFromDto(updateDto, testSchool);

        // Act
        SchoolResponseDto result = schoolService.updateSchool(1L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(responseDto, result);
        verify(schoolRepository).findById(1L);
        verify(schoolRepository).existsBySchoolNameAndIdNot(updateDto.getSchoolName(), 1L);
        verify(schoolMapper).updateEntityFromDto(updateDto, testSchool);
        verify(schoolRepository).save(testSchool);
        verify(schoolMapper).toResponseDto(testSchool);
    }

    @Test
    void updateSchool_NotFound_ThrowsException() {
        // Arrange
        when(schoolRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            schoolService.updateSchool(999L, updateDto);
        });
        verify(schoolRepository).findById(999L);
        verify(schoolMapper, never()).updateEntityFromDto(any(), any());
        verify(schoolRepository, never()).save(any());
    }

    @Test
    void updateSchool_DuplicateName_ThrowsException() {
        // Arrange
        updateDto.setSchoolName("Another School");
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(schoolRepository.existsBySchoolNameAndIdNot("Another School", 1L)).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            schoolService.updateSchool(1L, updateDto);
        });
        verify(schoolRepository).findById(1L);
        verify(schoolRepository).existsBySchoolNameAndIdNot("Another School", 1L);
        verify(schoolMapper, never()).updateEntityFromDto(any(), any());
        verify(schoolRepository, never()).save(any());
    }

    @Test
    void updateSchool_SameNameAsExisting_Success() {
        // Arrange
        updateDto.setSchoolName(testSchool.getSchoolName()); // Same name
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(schoolRepository.save(testSchool)).thenReturn(testSchool);
        when(schoolMapper.toResponseDto(testSchool)).thenReturn(responseDto);
        doNothing().when(schoolMapper).updateEntityFromDto(updateDto, testSchool);

        // Act
        SchoolResponseDto result = schoolService.updateSchool(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(schoolRepository).findById(1L);
        verify(schoolRepository, never()).existsBySchoolNameAndIdNot(anyString(), anyLong());
        verify(schoolMapper).updateEntityFromDto(updateDto, testSchool);
        verify(schoolRepository).save(testSchool);
    }

    // ==================== updateSchoolStatus Tests ====================

    @Test
    void updateSchoolStatus_Deactivate_Success() {
        // Arrange
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(schoolRepository.save(testSchool)).thenReturn(testSchool);
        when(schoolMapper.toResponseDto(testSchool)).thenReturn(responseDto);

        // Act
        SchoolResponseDto result = schoolService.updateSchoolStatus(1L, false);

        // Assert
        assertNotNull(result);
        assertFalse(testSchool.getIsActive());
        verify(schoolRepository).findById(1L);
        verify(schoolRepository).save(testSchool);
        verify(schoolMapper).toResponseDto(testSchool);
    }

    @Test
    void updateSchoolStatus_Activate_Success() {
        // Arrange
        testSchool.setIsActive(false);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(schoolRepository.save(testSchool)).thenReturn(testSchool);
        when(schoolMapper.toResponseDto(testSchool)).thenReturn(responseDto);

        // Act
        SchoolResponseDto result = schoolService.updateSchoolStatus(1L, true);

        // Assert
        assertNotNull(result);
        assertTrue(testSchool.getIsActive());
        verify(schoolRepository).findById(1L);
        verify(schoolRepository).save(testSchool);
        verify(schoolMapper).toResponseDto(testSchool);
    }

    @Test
    void updateSchoolStatus_NotFound_ThrowsException() {
        // Arrange
        when(schoolRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            schoolService.updateSchoolStatus(999L, false);
        });
        verify(schoolRepository).findById(999L);
        verify(schoolRepository, never()).save(any());
    }

    // ==================== deleteSchool Tests ====================

    @Test
    void deleteSchool_Success() {
        // Arrange
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(schoolRepository.save(testSchool)).thenReturn(testSchool);

        // Act
        schoolService.deleteSchool(1L);

        // Assert
        assertFalse(testSchool.getIsActive());
        verify(schoolRepository).findById(1L);
        verify(schoolRepository).save(testSchool);
    }

    @Test
    void deleteSchool_NotFound_ThrowsException() {
        // Arrange
        when(schoolRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            schoolService.deleteSchool(999L);
        });
        verify(schoolRepository).findById(999L);
        verify(schoolRepository, never()).save(any());
    }

    // ==================== isSchoolActive Tests ====================

    @Test
    void isSchoolActive_ActiveSchool_ReturnsTrue() {
        // Arrange
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));

        // Act
        boolean result = schoolService.isSchoolActive(1L);

        // Assert
        assertTrue(result);
        verify(schoolRepository).findById(1L);
    }

    @Test
    void isSchoolActive_InactiveSchool_ReturnsFalse() {
        // Arrange
        testSchool.setIsActive(false);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));

        // Act
        boolean result = schoolService.isSchoolActive(1L);

        // Assert
        assertFalse(result);
        verify(schoolRepository).findById(1L);
    }

    @Test
    void isSchoolActive_NotFound_ThrowsException() {
        // Arrange
        when(schoolRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            schoolService.isSchoolActive(999L);
        });
        verify(schoolRepository).findById(999L);
    }
}
