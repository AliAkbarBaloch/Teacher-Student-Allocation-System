package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.planchangelog.PlanChangeLogCreateDto;
import de.unipassau.allocationsystem.dto.planchangelog.PlanChangeLogResponseDto;
import de.unipassau.allocationsystem.dto.planchangelog.PlanChangeLogUpdateDto;
import de.unipassau.allocationsystem.entity.PlanChangeLog;
import de.unipassau.allocationsystem.mapper.BaseMapper;
import de.unipassau.allocationsystem.mapper.PlanChangeLogMapper;
import de.unipassau.allocationsystem.service.CrudService;
import de.unipassau.allocationsystem.service.PlanChangeLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import de.unipassau.allocationsystem.utils.PaginationUtils;

 

/**
 * REST controller for managing plan change logs.
 * Provides CRUD operations for plan change log entities.
 */
@RestController
@RequestMapping("/plan-change-logs")
@RequiredArgsConstructor
@Tag(name = "PlanChangeLogs", description = "Plan change log APIs")
public class PlanChangeLogController
        extends CrudControllerBase<PlanChangeLog, PlanChangeLogCreateDto, PlanChangeLogUpdateDto, PlanChangeLogResponseDto> {

    private final PlanChangeLogService planChangeLogService;
    private final PlanChangeLogMapper planChangeLogMapper;

    @Override
    protected CrudService<PlanChangeLog, Long> getService() {
        return planChangeLogService;
    }

    @Override
    protected BaseMapper<PlanChangeLog, PlanChangeLogCreateDto, PlanChangeLogUpdateDto, PlanChangeLogResponseDto> getMapper() {
        return planChangeLogMapper;
    }

    @GetMapping("/plans/{planId}/change-logs")
    public Object getLogsForPlan(@PathVariable Long planId,
                                  @RequestParam Map<String, String> queryParams) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        return PaginationUtils.formatPaginationResponse(planChangeLogService.getLogsByPlan(planId, null, null, null, null, pageable));
    }
}
