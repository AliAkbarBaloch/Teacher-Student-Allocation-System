package de.unipassau.allocationsystem.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.unipassau.allocationsystem.entity.School.SchoolType;

@RestController
@RequestMapping("/meta")
public class MetaController {

    @GetMapping("/school-type")
    public List<Map<String, String>> schoolTypes() {

        System.out.println("MetaController.schoolTypes HIT");

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

