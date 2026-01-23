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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ZoneConstraintService}.
 * <p>
 * Validates zone constraint CRUD operations, pagination, and validation.
 * </p>
 */
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

    private InternshipType testInternshipType;
    private ZoneConstraint testConstraint;
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
    void getAllReturnsAllConstraints() {
        when(zoneConstraintRepository.findAll()).thenReturn(List.of(testConstraint));

        List<ZoneConstraint> result = zoneConstraintService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(zoneConstraintRepository).findAll();
    }

    @Test
    void getPaginatedWithoutSearchReturnsPagedResults() {
        Page<ZoneConstraint> page = new PageImpl<>(List.of(testConstraint));
        when(zoneConstraintRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Map<String, String> queryParams = Map.of(
                "page", "1",
                "pageSize", "10",
                "sortBy", "id",
                "sortOrder", "asc"
        );

        Map<String, Object> result = zoneConstraintService.getPaginated(queryParams, null);

        assertNotNull(result);
        assertTrue(result.containsKey("items"));
        verify(zoneConstraintRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getByIdExistingIdReturnsConstraint() {
        when(zoneConstraintRepository.findById(1L)).thenReturn(Optional.of(testConstraint));

        Optional<ZoneConstraint> result = zoneConstraintService.getById(1L);

        assertTrue(result.isPresent());
        verify(zoneConstraintRepository).findById(1L);
    }

    @Test
    void createValidDtoReturnsCreatedConstraint() {
        ZoneConstraintCreateDto createDto = createDto(1, 1L, true);
        ZoneConstraint mappedConstraint = mappedConstraint(1, testInternshipType);

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
    void createDuplicateConstraintThrowsException() {
        ZoneConstraintCreateDto createDto = createDto(1, 1L, true);
        ZoneConstraint mappedConstraint = mappedConstraint(1, testInternshipType);

        when(internshipTypeRepository.findById(1L)).thenReturn(Optional.of(testInternshipType));
        when(zoneConstraintMapper.toEntityCreate(createDto)).thenReturn(mappedConstraint);
        when(zoneConstraintRepository.existsByZoneNumberAndInternshipTypeId(1, 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> zoneConstraintService.create(createDto));
        verify(zoneConstraintRepository, never()).save(any(ZoneConstraint.class));
    }

    @Test
    void updateValidDtoReturnsUpdatedConstraint() {
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
    void deleteExistingIdDeletesSuccessfully() {
        when(zoneConstraintRepository.existsById(1L)).thenReturn(true);
        doNothing().when(zoneConstraintRepository).deleteById(1L);

        assertDoesNotThrow(() -> zoneConstraintService.delete(1L));
        verify(zoneConstraintRepository).deleteById(1L);
    }

    @Test
    void deleteNonExistingIdThrowsException() {
        when(zoneConstraintRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> zoneConstraintService.delete(999L));
        verify(zoneConstraintRepository, never()).deleteById(any());
    }

    private static ZoneConstraintCreateDto createDto(int zoneNumber, Long internshipTypeId, boolean allowed) {
        ZoneConstraintCreateDto dto = new ZoneConstraintCreateDto();
        dto.setZoneNumber(zoneNumber);
        dto.setInternshipTypeId(internshipTypeId);
        dto.setIsAllowed(allowed);
        return dto;
    }

    private static ZoneConstraint mappedConstraint(int zoneNumber, InternshipType type) {
        ZoneConstraint constraint = new ZoneConstraint();
        constraint.setZoneNumber(zoneNumber);
        constraint.setInternshipType(type);
        return constraint;
    }
}
