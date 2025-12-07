package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanCreateDto;
import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanResponseDto;
import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.AllocationPlan.PlanStatus;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.AllocationPlanMapper;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import de.unipassau.allocationsystem.repository.UserRepository;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AllocationPlanService (updated to the latest service version).
 */
@ExtendWith(MockitoExtension.class)
class AllocationPlanServiceTest {

    @Mock
    private AllocationPlanRepository allocationPlanRepository;

    @Mock
    private AcademicYearRepository academicYearRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AllocationPlanMapper allocationPlanMapper;

    @Mock
    private PlanChangeLogService planChangeLogService;

    @InjectMocks
    private AllocationPlanService allocationPlanService;

    private AcademicYear testYear;
    private User testUser;
    private AllocationPlan testPlan;
    private AllocationPlanCreateDto createDto;
    private AllocationPlanUpdateDto updateDto;
    private AllocationPlanResponseDto responseDto;

    @BeforeEach
    void setUp() {
        // Setup test academic year
        testYear = new AcademicYear();
        testYear.setId(1L);
        testYear.setYearName("2024/2025");

        // Setup test user (not directly used by createPlan in current service but kept)
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("admin@example.com");
        testUser.setFullName("Admin User");
        testUser.setEnabled(true);

        // Setup test allocation plan
        testPlan = new AllocationPlan();
        testPlan.setId(1L);
        testPlan.setAcademicYear(testYear);
        testPlan.setPlanName("Initial Draft");
        testPlan.setPlanVersion("v1.0");
        testPlan.setStatus(PlanStatus.DRAFT);
        testPlan.setIsCurrent(false);
        testPlan.setNotes("Test plan");
        testPlan.setCreatedAt(LocalDateTime.now());
        testPlan.setUpdatedAt(LocalDateTime.now());

        // Setup create DTO
        createDto = new AllocationPlanCreateDto();
        createDto.setYearId(1L);
        createDto.setPlanName("New Plan");
        createDto.setPlanVersion("v2.0");
        createDto.setStatus(PlanStatus.DRAFT);
        createDto.setIsCurrent(false);
        createDto.setNotes("New plan notes");

        // Setup update DTO
        updateDto = new AllocationPlanUpdateDto();
        updateDto.setPlanName("Updated Plan");
        updateDto.setStatus(PlanStatus.IN_REVIEW);
        updateDto.setNotes("Updated notes");

        // Setup response DTO
        responseDto = AllocationPlanResponseDto.builder()
                .id(1L)
                .yearId(1L)
                .yearName("2024/2025")
                .planName("Initial Draft")
                .planVersion("v1.0")
                .status(PlanStatus.DRAFT)
                .statusDisplayName("Draft")
                .isCurrent(false)
                .notes("Test plan")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ========== NEW: GET SORT FIELDS & GET ALL ==========

    @Test
    void getSortFields_ShouldReturnConfiguredFields() {
        var fields = allocationPlanService.getSortFields();
        assertNotNull(fields);
        assertEquals(7, fields.size());
        assertEquals("id", fields.get(0).get("key"));
    }

    @Test
    void getAll_ShouldReturnAllPlans() {
        List<AllocationPlan> plans = List.of(testPlan);
        when(allocationPlanRepository.findAll()).thenReturn(plans);

        List<AllocationPlan> result = allocationPlanService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPlan.getId(), result.get(0).getId());
        verify(allocationPlanRepository).findAll();
    }

    // ========== GET ALL PLANS (paginated) ==========

    @Test
    void getAllPlans_Success() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("pageSize", "10");
        queryParams.put("sortBy", "id");
        queryParams.put("sortOrder", "ASC");

        List<AllocationPlan> plans = Arrays.asList(testPlan);
        Page<AllocationPlan> page = new PageImpl<>(plans);

        when(allocationPlanRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(allocationPlanMapper.toResponseDto(any(AllocationPlan.class)))
                .thenReturn(responseDto);

        // Act
        Map<String, Object> result = allocationPlanService.getAllPlans(
                1L, null, null, queryParams);

        // Assert
        assertNotNull(result);
        verify(allocationPlanRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // ========== GET PLAN BY ID TESTS ==========

    @Test
    void getPlanById_Success() {
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(allocationPlanMapper.toResponseDto(testPlan)).thenReturn(responseDto);

        AllocationPlanResponseDto result = allocationPlanService.getPlanById(1L);

        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
        verify(allocationPlanRepository).findById(1L);
    }

    @Test
    void getPlanById_NotFound_ThrowsException() {
        when(allocationPlanRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () ->
                allocationPlanService.getPlanById(999L)
        );
    }

    // ========== CREATE PLAN TESTS ==========

    @Test
    void createPlan_Success() {
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testYear));
        when(allocationPlanRepository.existsByAcademicYearIdAndPlanVersion(1L, "v2.0"))
                .thenReturn(false);
        when(allocationPlanRepository.save(any(AllocationPlan.class))).thenReturn(testPlan);
        when(allocationPlanMapper.toResponseDto(testPlan)).thenReturn(responseDto);

        AllocationPlanResponseDto result = allocationPlanService.createPlan(createDto);

        assertNotNull(result);
        verify(academicYearRepository).findById(1L);
        verify(allocationPlanRepository).save(any(AllocationPlan.class));
        verify(planChangeLogService).logPlanChange(anyLong(), any(), anyString(), anyString(), anyLong(), any(), any(), anyString());
    }

    @Test
    void createPlan_AcademicYearNotFound_ThrowsException() {
        when(academicYearRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () ->
                allocationPlanService.createPlan(createDto)
        );
    }

    @Test
    void createPlan_DuplicateVersion_ThrowsException() {
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testYear));
        when(allocationPlanRepository.existsByAcademicYearIdAndPlanVersion(1L, "v2.0"))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                allocationPlanService.createPlan(createDto)
        );
    }

    @Test
    void createPlan_WithIsCurrent_UnsetsOtherPlans() {
        createDto.setIsCurrent(true);
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testYear));
        when(allocationPlanRepository.existsByAcademicYearIdAndPlanVersion(1L, "v2.0"))
                .thenReturn(false);
        when(allocationPlanRepository.save(any(AllocationPlan.class))).thenReturn(testPlan);
        when(allocationPlanMapper.toResponseDto(testPlan)).thenReturn(responseDto);

        AllocationPlanResponseDto result = allocationPlanService.createPlan(createDto);

        assertNotNull(result);
        verify(allocationPlanRepository).unsetCurrentForYear(1L);
        verify(allocationPlanRepository).save(any(AllocationPlan.class));
    }

    // ========== UPDATE PLAN TESTS ==========

    @Test
    void updatePlan_Success() {
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(allocationPlanRepository.save(any(AllocationPlan.class))).thenReturn(testPlan);
        when(allocationPlanMapper.toResponseDto(testPlan)).thenReturn(responseDto);

        AllocationPlanResponseDto result = allocationPlanService.updatePlan(1L, updateDto);

        assertNotNull(result);
        verify(allocationPlanRepository).findById(1L);
        verify(allocationPlanMapper).updateEntityFromDto(updateDto, testPlan);
        verify(allocationPlanRepository).save(testPlan);
        verify(planChangeLogService).logPlanChange(anyLong(), any(), anyString(), anyString(), anyLong(), any(), any(), anyString());
    }

    @Test
    void updatePlan_NotFound_ThrowsException() {
        when(allocationPlanRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () ->
                allocationPlanService.updatePlan(999L, updateDto)
        );
    }

    @Test
    void updatePlan_ArchivedPlan_ThrowsException() {
        testPlan.setStatus(PlanStatus.ARCHIVED);
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        assertThrows(IllegalArgumentException.class, () ->
                allocationPlanService.updatePlan(1L, updateDto)
        );
    }

    @Test
    void updatePlan_SetIsCurrent_UnsetsOtherPlans() {
        updateDto.setIsCurrent(true);
        testPlan.setIsCurrent(false);
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(allocationPlanRepository.save(any(AllocationPlan.class))).thenReturn(testPlan);
        when(allocationPlanMapper.toResponseDto(testPlan)).thenReturn(responseDto);

        AllocationPlanResponseDto result = allocationPlanService.updatePlan(1L, updateDto);

        assertNotNull(result);
        verify(allocationPlanRepository).unsetCurrentForYearExcept(1L, 1L);
        verify(allocationPlanRepository).save(testPlan);
    }

    // ========== SET CURRENT PLAN TESTS ==========

    @Test
    void setCurrentPlan_Success() {
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(allocationPlanRepository.save(any(AllocationPlan.class))).thenReturn(testPlan);
        when(allocationPlanMapper.toResponseDto(testPlan)).thenReturn(responseDto);

        AllocationPlanResponseDto result = allocationPlanService.setCurrentPlan(1L);

        assertNotNull(result);
        verify(allocationPlanRepository).unsetCurrentForYearExcept(1L, 1L);
        verify(allocationPlanRepository).save(testPlan);
        verify(planChangeLogService).logPlanChange(anyLong(), any(), anyString(), anyString(), anyLong(), any(), any(), anyString());
    }

    @Test
    void setCurrentPlan_NotFound_ThrowsException() {
        when(allocationPlanRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () ->
                allocationPlanService.setCurrentPlan(999L)
        );
    }

    @Test
    void setCurrentPlan_ArchivedPlan_ThrowsException() {
        testPlan.setStatus(PlanStatus.ARCHIVED);
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        assertThrows(IllegalArgumentException.class, () ->
                allocationPlanService.setCurrentPlan(1L)
        );
    }

    // ========== ARCHIVE PLAN TESTS ==========

    @Test
    void archivePlan_Success() {
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(allocationPlanRepository.save(any(AllocationPlan.class))).thenReturn(testPlan);
        when(allocationPlanMapper.toResponseDto(testPlan)).thenReturn(responseDto);

        AllocationPlanResponseDto result = allocationPlanService.archivePlan(1L);

        assertNotNull(result);
        verify(allocationPlanRepository).findById(1L);
        verify(allocationPlanRepository).save(testPlan);
        verify(planChangeLogService).logPlanChange(anyLong(), any(), anyString(), anyString(), anyLong(), any(), any(), anyString());
    }

    @Test
    void archivePlan_NotFound_ThrowsException() {
        when(allocationPlanRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () ->
                allocationPlanService.archivePlan(999L)
        );
    }

    @Test
    void archivePlan_AlreadyArchived_ThrowsException() {
        testPlan.setStatus(PlanStatus.ARCHIVED);
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        assertThrows(IllegalArgumentException.class, () ->
                allocationPlanService.archivePlan(1L)
        );
    }

    // ========== GET CURRENT PLAN FOR YEAR TESTS ==========

    @Test
    void getCurrentPlanForYear_Success() {
        when(allocationPlanRepository.findByAcademicYearIdAndIsCurrentTrue(1L))
                .thenReturn(Optional.of(testPlan));
        when(allocationPlanMapper.toResponseDto(testPlan)).thenReturn(responseDto);

        AllocationPlanResponseDto result = allocationPlanService.getCurrentPlanForYear(1L);

        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
        verify(allocationPlanRepository).findByAcademicYearIdAndIsCurrentTrue(1L);
    }

    @Test
    void getCurrentPlanForYear_NotFound_ThrowsException() {
        when(allocationPlanRepository.findByAcademicYearIdAndIsCurrentTrue(999L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                allocationPlanService.getCurrentPlanForYear(999L)
        );
    }
}
