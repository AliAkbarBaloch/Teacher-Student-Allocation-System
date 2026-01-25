package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.AllocationPlan.PlanStatus;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllocationPlanServiceCreateUpdateTest extends AllocationPlanServiceTestBase {

    @Test
    void createPlanShouldSucceed() {
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testYear));
        when(allocationPlanRepository.existsByAcademicYearIdAndPlanVersion(1L, "v2.0")).thenReturn(false);

        stubSaveReturns(testPlan);
        stubMapperToResponse(testPlan, responseDto);

        assertNotNull(allocationPlanService.createPlan(createDto));

        verify(academicYearRepository).findById(1L);
        verify(allocationPlanRepository).save(any(AllocationPlan.class));
        verify(planChangeLogService).logPlanChange(anyLong(), anyString(), anyString(), anyLong(), any(), any(), anyString());
    }

    @Test
    void createPlanShouldThrowWhenYearMissing() {
        when(academicYearRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> allocationPlanService.createPlan(createDto));
    }

    @Test
    void createPlanShouldThrowWhenDuplicateVersion() {
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testYear));
        when(allocationPlanRepository.existsByAcademicYearIdAndPlanVersion(1L, "v2.0")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> allocationPlanService.createPlan(createDto));
    }

    @Test
    void createPlanWithIsCurrentShouldUnsetOtherPlans() {
        createDto.setIsCurrent(true);

        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testYear));
        when(allocationPlanRepository.existsByAcademicYearIdAndPlanVersion(1L, "v2.0")).thenReturn(false);

        stubSaveReturns(testPlan);
        stubMapperToResponse(testPlan, responseDto);

        assertNotNull(allocationPlanService.createPlan(createDto));

        verify(allocationPlanRepository).unsetCurrentForYear(1L);
        verify(allocationPlanRepository).save(any(AllocationPlan.class));
    }

    @Test
    void updatePlanShouldSucceed() {
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

        stubSaveReturns(testPlan);
        stubMapperToResponse(testPlan, responseDto);

        assertNotNull(allocationPlanService.updatePlan(1L, updateDto));

        verify(allocationPlanRepository).findById(1L);
        verify(allocationPlanMapper).updateEntityFromDto(updateDto, testPlan);
        verify(allocationPlanRepository).save(testPlan);
        verify(planChangeLogService).logPlanChange(anyLong(), anyString(), anyString(), anyLong(), any(), any(), anyString());
    }

    @Test
    void updatePlanShouldThrowWhenMissing() {
        when(allocationPlanRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> allocationPlanService.updatePlan(999L, updateDto));
    }

    @Test
    void updatePlanShouldThrowWhenArchived() {
        testPlan.setStatus(PlanStatus.ARCHIVED);
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

        assertThrows(IllegalArgumentException.class, () -> allocationPlanService.updatePlan(1L, updateDto));
    }

    @Test
    void updatePlanSettingIsCurrentShouldUnsetOtherPlans() {
        updateDto.setIsCurrent(true);
        testPlan.setIsCurrent(false);

        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

        stubSaveReturns(testPlan);
        stubMapperToResponse(testPlan, responseDto);

        assertNotNull(allocationPlanService.updatePlan(1L, updateDto));

        verify(allocationPlanRepository).unsetCurrentForYearExcept(1L, 1L);
        verify(allocationPlanRepository).save(testPlan);
    }
}
