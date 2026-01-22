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
}
