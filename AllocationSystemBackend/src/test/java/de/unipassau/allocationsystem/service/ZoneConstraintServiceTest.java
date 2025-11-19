package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintCreateDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintResponseDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintUpdateDto;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.ZoneConstraint;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.ZoneConstraintMapper;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.ZoneConstraintRepository;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZoneConstraintServiceTest {

    @Mock
    private ZoneConstraintRepository zoneConstraintRepository;

    @Mock
    private InternshipTypeRepository internshipTypeRepository;

    @Mock
    private ZoneConstraintMapper zoneConstraintMapper;

    @InjectMocks
    private ZoneConstraintService zoneConstraintService;

    private ZoneConstraint testConstraint;
    private InternshipType testInternshipType;
    private ZoneConstraintResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        testInternshipType = new InternshipType();
        testInternshipType.setId(1L);
        testInternshipType.setInternshipCode("TEST-01");
        testInternshipType.setFullName("Test Internship");

        testConstraint = new ZoneConstraint();
        testConstraint.setId(1L);
        testConstraint.setZoneNumber(1);
        testConstraint.setInternshipType(testInternshipType);
        testConstraint.setIsAllowed(true);
        testConstraint.setDescription("Test constraint");

        testResponseDto = new ZoneConstraintResponseDto();
        testResponseDto.setId(1L);
        testResponseDto.setZoneNumber(1);
        testResponseDto.setInternshipTypeId(1L);
        testResponseDto.setInternshipTypeCode("TEST-01");
        testResponseDto.setInternshipTypeName("Test Internship");
        testResponseDto.setIsAllowed(true);
        testResponseDto.setDescription("Test constraint");
    }

    @Test
    void getAll_ReturnsAllConstraints() {
        when(zoneConstraintRepository.findAll()).thenReturn(Arrays.asList(testConstraint));
        List<ZoneConstraint> result = zoneConstraintService.getAll();
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(zoneConstraintRepository).findAll();
    }

    @Test
    void getPaginated_WithoutSearch_ReturnsPagedResults() {
        Page<ZoneConstraint> page = new PageImpl<>(Arrays.asList(testConstraint));
        when(zoneConstraintRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        Map<String, String> queryParams = Map.of("page", "1", "pageSize", "10", "sortBy", "id", "sortOrder", "asc");
        Map<String, Object> result = zoneConstraintService.getPaginated(queryParams, null);
        assertNotNull(result);
        assertTrue(result.containsKey("items"));
        verify(zoneConstraintRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getById_ExistingId_ReturnsConstraint() {
        when(zoneConstraintRepository.findById(1L)).thenReturn(Optional.of(testConstraint));
        Optional<ZoneConstraint> result = zoneConstraintService.getById(1L);
        assertTrue(result.isPresent());
        verify(zoneConstraintRepository).findById(1L);
    }

    @Test
    void create_ValidDto_ReturnsCreatedConstraint() {
        ZoneConstraintCreateDto createDto = new ZoneConstraintCreateDto();
        createDto.setZoneNumber(1);
        createDto.setInternshipTypeId(1L);
        createDto.setIsAllowed(true);
        
        ZoneConstraint mappedConstraint = new ZoneConstraint();
        mappedConstraint.setZoneNumber(1);
        mappedConstraint.setInternshipType(testInternshipType);
        
        when(internshipTypeRepository.findById(1L)).thenReturn(Optional.of(testInternshipType));
        when(zoneConstraintMapper.toEntityCreate(createDto)).thenReturn(mappedConstraint);
        when(zoneConstraintRepository.existsByZoneNumberAndInternshipTypeId(1, 1L)).thenReturn(false);
        when(zoneConstraintRepository.save(any(ZoneConstraint.class))).thenReturn(testConstraint);
        when(zoneConstraintMapper.toResponseDto(testConstraint)).thenReturn(testResponseDto);
        
        ZoneConstraintResponseDto result = zoneConstraintService.create(createDto);
        assertNotNull(result);
        verify(zoneConstraintRepository).save(any(ZoneConstraint.class));
    }

    @Test
    void create_DuplicateConstraint_ThrowsException() {
        ZoneConstraintCreateDto createDto = new ZoneConstraintCreateDto();
        createDto.setZoneNumber(1);
        createDto.setInternshipTypeId(1L);
        createDto.setIsAllowed(true);
        
        ZoneConstraint mappedConstraint = new ZoneConstraint();
        mappedConstraint.setZoneNumber(1);
        mappedConstraint.setInternshipType(testInternshipType);
        
        when(internshipTypeRepository.findById(1L)).thenReturn(Optional.of(testInternshipType));
        when(zoneConstraintMapper.toEntityCreate(createDto)).thenReturn(mappedConstraint);
        when(zoneConstraintRepository.existsByZoneNumberAndInternshipTypeId(1, 1L)).thenReturn(true);
        
        assertThrows(DuplicateResourceException.class, () -> zoneConstraintService.create(createDto));
        verify(zoneConstraintRepository, never()).save(any());
    }

    @Test
    void update_ValidDto_ReturnsUpdatedConstraint() {
        ZoneConstraintUpdateDto updateDto = new ZoneConstraintUpdateDto();
        updateDto.setDescription("Updated");
        
        when(zoneConstraintRepository.findById(1L)).thenReturn(Optional.of(testConstraint));
        when(zoneConstraintRepository.save(any(ZoneConstraint.class))).thenReturn(testConstraint);
        when(zoneConstraintMapper.toResponseDto(testConstraint)).thenReturn(testResponseDto);
        
        ZoneConstraintResponseDto result = zoneConstraintService.update(1L, updateDto);
        assertNotNull(result);
        verify(zoneConstraintRepository).save(testConstraint);
    }

    @Test
    void delete_ExistingId_DeletesSuccessfully() {
        when(zoneConstraintRepository.existsById(1L)).thenReturn(true);
        doNothing().when(zoneConstraintRepository).deleteById(1L);
        assertDoesNotThrow(() -> zoneConstraintService.delete(1L));
        verify(zoneConstraintRepository).deleteById(1L);
    }

    @Test
    void delete_NonExistingId_ThrowsException() {
        when(zoneConstraintRepository.existsById(999L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> zoneConstraintService.delete(999L));
        verify(zoneConstraintRepository, never()).deleteById(any());
    }
}
