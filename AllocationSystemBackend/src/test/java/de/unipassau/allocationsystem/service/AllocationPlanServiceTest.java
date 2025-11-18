package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.AllocationPlanCreateDto;
import de.unipassau.allocationsystem.dto.AllocationPlanResponseDto;
import de.unipassau.allocationsystem.dto.AllocationPlanUpdateDto;
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
 * Unit tests for AllocationPlanService.
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

        // Setup test user
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
        testPlan.setCreatedByUser(testUser);
        testPlan.setIsCurrent(false);
        testPlan.setNotes("Test plan");
        testPlan.setCreatedAt(LocalDateTime.now());
        testPlan.setLastModified(LocalDateTime.now());

        // Setup create DTO
        createDto = new AllocationPlanCreateDto();
        createDto.setYearId(1L);
        createDto.setPlanName("New Plan");
        createDto.setPlanVersion("v2.0");
        createDto.setStatus(PlanStatus.DRAFT);
        createDto.setCreatedByUserId(1L);
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
                .createdByUserId(1L)
                .createdByUserName("Admin User")
                .createdByUserEmail("admin@example.com")
                .isCurrent(false)
                .notes("Test plan")
                .createdAt(LocalDateTime.now())
                .lastModified(LocalDateTime.now())
                .build();
    }

    // ========== GET ALL PLANS TESTS ==========

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

    @Test
    void getAllPlans_MissingYearId_ThrowsException() {
        // Arrange
        Map<String, String> queryParams = new HashMap<>();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            allocationPlanService.getAllPlans(null, null, null, queryParams)
        );
    }

    // ========== GET PLAN BY ID TESTS ==========

    @Test
    void getPlanById_Success() {
        // Arrange
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(allocationPlanMapper.toResponseDto(testPlan)).thenReturn(responseDto);

        // Act
        AllocationPlanResponseDto result = allocationPlanService.getPlanById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
        verify(allocationPlanRepository).findById(1L);
    }

    @Test
    void getPlanById_NotFound_ThrowsException() {
        // Arrange
        when(allocationPlanRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            allocationPlanService.getPlanById(999L)
        );
    }

    // ========== CREATE PLAN TESTS ==========

    @Test
    void createPlan_Success() {
        // Arrange
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testYear));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(allocationPlanRepository.existsByAcademicYearIdAndPlanVersion(1L, "v2.0"))
                .thenReturn(false);
        when(allocationPlanRepository.save(any(AllocationPlan.class))).thenReturn(testPlan);
        when(allocationPlanMapper.toResponseDto(testPlan)).thenReturn(responseDto);

        // Act
        AllocationPlanResponseDto result = allocationPlanService.createPlan(createDto);

        // Assert
        assertNotNull(result);
        verify(academicYearRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(allocationPlanRepository).save(any(AllocationPlan.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void createPlan_AcademicYearNotFound_ThrowsException() {
        // Arrange
        when(academicYearRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            allocationPlanService.createPlan(createDto)
        );
    }

    @Test
    void createPlan_UserNotFound_ThrowsException() {
        // Arrange
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testYear));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            allocationPlanService.createPlan(createDto)
        );
    }

    @Test
    void createPlan_InactiveUser_ThrowsException() {
        // Arrange
        testUser.setEnabled(false);
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testYear));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            allocationPlanService.createPlan(createDto)
        );
    }

    @Test
    void createPlan_DuplicateVersion_ThrowsException() {
        // Arrange
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testYear));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(allocationPlanRepository.existsByAcademicYearIdAndPlanVersion(1L, "v2.0"))
                .thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () ->
            allocationPlanService.createPlan(createDto)
        );
    }

    @Test
    void createPlan_WithIsCurrent_UnsetsOtherPlans() {
        // Arrange
        createDto.setIsCurrent(true);
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testYear));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(allocationPlanRepository.existsByAcademicYearIdAndPlanVersion(1L, "v2.0"))
                .thenReturn(false);
        when(allocationPlanRepository.save(any(AllocationPlan.class))).thenReturn(testPlan);
        when(allocationPlanMapper.toResponseDto(testPlan)).thenReturn(responseDto);

        // Act
        AllocationPlanResponseDto result = allocationPlanService.createPlan(createDto);

        // Assert
        assertNotNull(result);
        verify(allocationPlanRepository).unsetCurrentForYear(1L);
        verify(allocationPlanRepository).save(any(AllocationPlan.class));
    }

    // ========== UPDATE PLAN TESTS ==========

    @Test
    void updatePlan_Success() {
        // Arrange
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(allocationPlanRepository.save(any(AllocationPlan.class))).thenReturn(testPlan);
        when(allocationPlanMapper.toResponseDto(testPlan)).thenReturn(responseDto);

        // Act
        AllocationPlanResponseDto result = allocationPlanService.updatePlan(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(allocationPlanRepository).findById(1L);
        verify(allocationPlanMapper).updateEntityFromDto(testPlan, updateDto);
        verify(allocationPlanRepository).save(testPlan);
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void updatePlan_NotFound_ThrowsException() {
        // Arrange
        when(allocationPlanRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            allocationPlanService.updatePlan(999L, updateDto)
        );
    }

    @Test
    void updatePlan_ArchivedPlan_ThrowsException() {
        // Arrange
        testPlan.setStatus(PlanStatus.ARCHIVED);
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            allocationPlanService.updatePlan(1L, updateDto)
        );
    }

    @Test
    void updatePlan_SetIsCurrent_UnsetsOtherPlans() {
        // Arrange
        updateDto.setIsCurrent(true);
        testPlan.setIsCurrent(false);
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(allocationPlanRepository.save(any(AllocationPlan.class))).thenReturn(testPlan);
        when(allocationPlanMapper.toResponseDto(testPlan)).thenReturn(responseDto);

        // Act
        AllocationPlanResponseDto result = allocationPlanService.updatePlan(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(allocationPlanRepository).unsetCurrentForYearExcept(1L, 1L);
        verify(allocationPlanRepository).save(testPlan);
    }

    // ========== SET CURRENT PLAN TESTS ==========

    @Test
    void setCurrentPlan_Success() {
        // Arrange
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(allocationPlanRepository.save(any(AllocationPlan.class))).thenReturn(testPlan);
        when(allocationPlanMapper.toResponseDto(testPlan)).thenReturn(responseDto);

        // Act
        AllocationPlanResponseDto result = allocationPlanService.setCurrentPlan(1L);

        // Assert
        assertNotNull(result);
        verify(allocationPlanRepository).unsetCurrentForYearExcept(1L, 1L);
        verify(allocationPlanRepository).save(testPlan);
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void setCurrentPlan_NotFound_ThrowsException() {
        // Arrange
        when(allocationPlanRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            allocationPlanService.setCurrentPlan(999L)
        );
    }

    @Test
    void setCurrentPlan_ArchivedPlan_ThrowsException() {
        // Arrange
        testPlan.setStatus(PlanStatus.ARCHIVED);
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            allocationPlanService.setCurrentPlan(1L)
        );
    }

    // ========== ARCHIVE PLAN TESTS ==========

    @Test
    void archivePlan_Success() {
        // Arrange
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(allocationPlanRepository.save(any(AllocationPlan.class))).thenReturn(testPlan);
        when(allocationPlanMapper.toResponseDto(testPlan)).thenReturn(responseDto);

        // Act
        AllocationPlanResponseDto result = allocationPlanService.archivePlan(1L);

        // Assert
        assertNotNull(result);
        verify(allocationPlanRepository).findById(1L);
        verify(allocationPlanRepository).save(testPlan);
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void archivePlan_NotFound_ThrowsException() {
        // Arrange
        when(allocationPlanRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            allocationPlanService.archivePlan(999L)
        );
    }

    @Test
    void archivePlan_AlreadyArchived_ThrowsException() {
        // Arrange
        testPlan.setStatus(PlanStatus.ARCHIVED);
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            allocationPlanService.archivePlan(1L)
        );
    }

    // ========== GET CURRENT PLAN FOR YEAR TESTS ==========

    @Test
    void getCurrentPlanForYear_Success() {
        // Arrange
        when(allocationPlanRepository.findByAcademicYearIdAndIsCurrentTrue(1L))
                .thenReturn(Optional.of(testPlan));
        when(allocationPlanMapper.toResponseDto(testPlan)).thenReturn(responseDto);

        // Act
        AllocationPlanResponseDto result = allocationPlanService.getCurrentPlanForYear(1L);

        // Assert
        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
        verify(allocationPlanRepository).findByAcademicYearIdAndIsCurrentTrue(1L);
    }

    @Test
    void getCurrentPlanForYear_NotFound_ThrowsException() {
        // Arrange
        when(allocationPlanRepository.findByAcademicYearIdAndIsCurrentTrue(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            allocationPlanService.getCurrentPlanForYear(999L)
        );
    }
}
