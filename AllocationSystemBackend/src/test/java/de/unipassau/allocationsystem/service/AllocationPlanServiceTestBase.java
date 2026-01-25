package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.allocation.TeacherAllocationService;
import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanCreateDto;
import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanResponseDto;
import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.AllocationPlan.PlanStatus;
import de.unipassau.allocationsystem.mapper.AllocationPlanMapper;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

abstract class AllocationPlanServiceTestBase {

    @Mock
    protected AllocationPlanRepository allocationPlanRepository;

    @Mock
    protected AcademicYearRepository academicYearRepository;

    @Mock
    protected AllocationPlanMapper allocationPlanMapper;

    @Mock
    protected PlanChangeLogService planChangeLogService;

    protected AllocationPlanWriteSupport writeSupport;

    @Mock
    protected TeacherAllocationService teacherAllocationService;

    protected AllocationPlanService allocationPlanService;

    protected AcademicYear testYear;
    protected AllocationPlan testPlan;
    protected AllocationPlanCreateDto createDto;
    protected AllocationPlanUpdateDto updateDto;
    protected AllocationPlanResponseDto responseDto;

    @BeforeEach
    void setUpBase() {
        testYear = buildAcademicYear(1L, "2024/2025");
        testPlan = buildPlan(1L, testYear, "Initial Draft", "v1.0", PlanStatus.DRAFT, false, "Test plan");
        createDto = buildCreateDto(1L, "New Plan", "v2.0", PlanStatus.DRAFT, false, "New plan notes");
        updateDto = buildUpdateDto("Updated Plan", PlanStatus.IN_REVIEW, null, "Updated notes");
        responseDto = buildResponseDtoFromPlan(testPlan);

        // Use a real writeSupport (spied) so validation and logging behave like production
        writeSupport = spy(new AllocationPlanWriteSupport(allocationPlanRepository, academicYearRepository, allocationPlanMapper, planChangeLogService));

        // Instantiate service with mocks and the spied writeSupport
        allocationPlanService = new AllocationPlanService(allocationPlanRepository, allocationPlanMapper, planChangeLogService, teacherAllocationService, writeSupport);
    }

    protected AcademicYear buildAcademicYear(Long id, String yearName) {
        AcademicYear year = new AcademicYear();
        year.setId(id);
        year.setYearName(yearName);
        return year;
    }

    protected AllocationPlan buildPlan(Long id,
                                      AcademicYear year,
                                      String name,
                                      String version,
                                      PlanStatus status,
                                      boolean isCurrent,
                                      String notes) {
        AllocationPlan plan = new AllocationPlan();
        plan.setId(id);
        plan.setAcademicYear(year);
        plan.setPlanName(name);
        plan.setPlanVersion(version);
        plan.setStatus(status);
        plan.setIsCurrent(isCurrent);
        plan.setNotes(notes);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        return plan;
    }

    protected AllocationPlanCreateDto buildCreateDto(Long yearId,
                                                    String name,
                                                    String version,
                                                    PlanStatus status,
                                                    boolean isCurrent,
                                                    String notes) {
        AllocationPlanCreateDto dto = new AllocationPlanCreateDto();
        dto.setYearId(yearId);
        dto.setPlanName(name);
        dto.setPlanVersion(version);
        dto.setStatus(status);
        dto.setIsCurrent(isCurrent);
        dto.setNotes(notes);
        return dto;
    }

    protected AllocationPlanUpdateDto buildUpdateDto(String name,
                                                    PlanStatus status,
                                                    Boolean isCurrent,
                                                    String notes) {
        AllocationPlanUpdateDto dto = new AllocationPlanUpdateDto();
        dto.setPlanName(name);
        dto.setStatus(status);
        dto.setIsCurrent(isCurrent);
        dto.setNotes(notes);
        return dto;
    }

    protected AllocationPlanResponseDto buildResponseDtoFromPlan(AllocationPlan plan) {
        return AllocationPlanResponseDto.builder()
                .id(plan.getId())
                .yearId(plan.getAcademicYear().getId())
                .yearName(plan.getAcademicYear().getYearName())
                .planName(plan.getPlanName())
                .planVersion(plan.getPlanVersion())
                .status(plan.getStatus())
                .statusDisplayName("Draft")
                .isCurrent(Boolean.TRUE.equals(plan.getIsCurrent()))
                .notes(plan.getNotes())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    protected void stubMapperToResponse(AllocationPlan plan, AllocationPlanResponseDto dto) {
        when(allocationPlanMapper.toResponseDto(plan)).thenReturn(dto);
    }

    protected void stubSaveReturns(AllocationPlan plan) {
        when(allocationPlanRepository.save(any(AllocationPlan.class))).thenReturn(plan);
    }
}
