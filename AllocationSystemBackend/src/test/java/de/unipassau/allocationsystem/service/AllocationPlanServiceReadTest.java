package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllocationPlanServiceReadTest extends AllocationPlanServiceTestBase {

    @Test
    void getSortFieldsShouldReturnConfiguredFields() {
        List<Map<String, String>> fields = allocationPlanService.getSortFields();
        assertNotNull(fields);
        assertEquals(7, fields.size());
        assertEquals("id", fields.get(0).get("key"));
    }

    @Test
    void getAllShouldReturnAllPlans() {
        when(allocationPlanRepository.findAll()).thenReturn(List.of(testPlan));

        List<AllocationPlan> result = allocationPlanService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPlan.getId(), result.get(0).getId());
        verify(allocationPlanRepository).findAll();
    }

    @Test
    void getAllPlansShouldReturnPaginatedPlans() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("pageSize", "10");
        queryParams.put("sortBy", "id");
        queryParams.put("sortOrder", "ASC");

        Page<AllocationPlan> page = new PageImpl<>(List.of(testPlan));

        when(allocationPlanRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        stubMapperToResponse(testPlan, responseDto);

        Map<String, Object> result = allocationPlanService.getAllPlans(1L, null, null, queryParams);

        assertNotNull(result);
        verify(allocationPlanRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getPlanByIdShouldReturnPlan() {
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        stubMapperToResponse(testPlan, responseDto);

        assertEquals(responseDto.getId(), allocationPlanService.getPlanById(1L).getId());
        verify(allocationPlanRepository).findById(1L);
    }

    @Test
    void getPlanByIdShouldThrowWhenMissing() {
        when(allocationPlanRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> allocationPlanService.getPlanById(999L));
    }

    @Test
    void getCurrentPlanForYearShouldReturnCurrentPlan() {
        when(allocationPlanRepository.findByAcademicYearIdAndIsCurrentTrue(1L)).thenReturn(Optional.of(testPlan));
        stubMapperToResponse(testPlan, responseDto);

        assertEquals(responseDto.getId(), allocationPlanService.getCurrentPlanForYear(1L).getId());
        verify(allocationPlanRepository).findByAcademicYearIdAndIsCurrentTrue(1L);
    }

    @Test
    void getCurrentPlanForYearShouldThrowWhenMissing() {
        when(allocationPlanRepository.findByAcademicYearIdAndIsCurrentTrue(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> allocationPlanService.getCurrentPlanForYear(999L));
    }
}
