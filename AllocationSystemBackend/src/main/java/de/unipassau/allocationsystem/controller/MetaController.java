package de.unipassau.allocationsystem.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.unipassau.allocationsystem.entity.School.SchoolType;

/**
 * REST controller for metadata endpoints.
 * Provides enumeration values and metadata for the application.
 */
@Slf4j
@RestController
@RequestMapping("/meta")
public class MetaController {

    /**
     * Retrieves all available school types with labels.
     * 
     * @return List of school type values and labels
     */
    @GetMapping("/school-type")
    public List<Map<String, String>> schoolTypes() {

        log.debug("MetaController.schoolTypes HIT");

        return Arrays.stream(SchoolType.values()).map(
            v -> Map.of(
                "value", v.name(),
                "label", toLabel(v)
            ))
            .toList();
    }

    private String toLabel(SchoolType v)  {
        return switch (v) {
            case PRIMARY -> "Primary";
            case MIDDLE -> "Middle";
            case SECONDARY -> "Secondary";
            case VOCATIONAL -> "Vocational";
            case SPECIAL_EDUCATION -> "Special education";
        };

    }

}

