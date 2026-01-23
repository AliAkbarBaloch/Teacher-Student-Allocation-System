package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.AllocationPlan.PlanStatus;
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
class AllocationPlanServiceStateChangeTest extends AllocationPlanServiceTestBase {

    @Test
    void setCurrentPlanShouldSucceed() {
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

        stubSaveReturns(testPlan);
        stubMapperToResponse(testPlan, responseDto);

        assertNotNull(allocationPlanService.setCurrentPlan(1L));

        verify(allocationPlanRepository).unsetCurrentForYearExcept(1L, 1L);
        verify(allocationPlanRepository).save(any(AllocationPlan.class));
        verify(planChangeLogService).logPlanChange(anyLong(), anyString(), anyString(), anyLong(), any(), any(), anyString());
    }

    @Test
    void setCurrentPlanShouldThrowWhenMissing() {
        when(allocationPlanRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> allocationPlanService.setCurrentPlan(999L));
    }

    @Test
    void setCurrentPlanShouldThrowWhenArchived() {
        testPlan.setStatus(PlanStatus.ARCHIVED);
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

        assertThrows(IllegalArgumentException.class, () -> allocationPlanService.setCurrentPlan(1L));
    }

    @Test
    void archivePlanShouldSucceed() {
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

        stubSaveReturns(testPlan);
        stubMapperToResponse(testPlan, responseDto);

        assertNotNull(allocationPlanService.archivePlan(1L));

        verify(allocationPlanRepository).findById(1L);
        verify(allocationPlanRepository).save(testPlan);
        verify(planChangeLogService).logPlanChange(anyLong(), anyString(), anyString(), anyLong(), any(), any(), anyString());
    }

    @Test
    void archivePlanShouldThrowWhenMissing() {
        when(allocationPlanRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> allocationPlanService.archivePlan(999L));
    }

    @Test
    void archivePlanShouldThrowWhenAlreadyArchived() {
        testPlan.setStatus(PlanStatus.ARCHIVED);
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

        assertThrows(IllegalArgumentException.class, () -> allocationPlanService.archivePlan(1L));
    }
}
