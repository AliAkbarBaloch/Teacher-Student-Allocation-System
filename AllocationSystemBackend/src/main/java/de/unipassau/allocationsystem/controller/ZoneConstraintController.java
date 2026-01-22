package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintCreateDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintResponseDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintUpdateDto;
import de.unipassau.allocationsystem.entity.ZoneConstraint;
import de.unipassau.allocationsystem.mapper.BaseMapper;
import de.unipassau.allocationsystem.mapper.ZoneConstraintMapper;
import de.unipassau.allocationsystem.service.CrudService;
import de.unipassau.allocationsystem.service.ZoneConstraintService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing zone constraints.
 * Handles CRUD operations for zone-based internship type restrictions.
 */
@RestController
@RequestMapping("/zone-constraints")
@RequiredArgsConstructor
@Tag(name = "Zone Constraints", description = "Zone constraint management APIs")
public class ZoneConstraintController
        extends CrudControllerBase<ZoneConstraint, ZoneConstraintCreateDto, ZoneConstraintUpdateDto, ZoneConstraintResponseDto> {

    private final ZoneConstraintService zoneConstraintService;
    private final ZoneConstraintMapper zoneConstraintMapper;

    @Override
    protected CrudService<ZoneConstraint, Long> getService() {
        return zoneConstraintService;
    }

    @Override
    protected BaseMapper<ZoneConstraint, ZoneConstraintCreateDto, ZoneConstraintUpdateDto, ZoneConstraintResponseDto> getMapper() {
        return zoneConstraintMapper;
    }
}
