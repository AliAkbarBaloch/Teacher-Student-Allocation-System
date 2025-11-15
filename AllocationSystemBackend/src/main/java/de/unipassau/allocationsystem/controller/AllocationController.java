package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.AllocationCreateRequest;
import de.unipassau.allocationsystem.dto.AllocationDto;
import de.unipassau.allocationsystem.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/allocations")
public class AllocationController {

    private final List<AllocationDto> memory = new ArrayList<>();

    @GetMapping
    public ResponseEntity<List<AllocationDto>> listAllocations() {
        // return in-memory list (dummy)
        return ResponseEntity.ok(memory);
    }

    @PostMapping
    public ResponseEntity<?> createAllocation(@RequestBody AllocationCreateRequest req) {
        if (req.getStudentId() == null || req.getStudentId().isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_INPUT", "studentId is required"));
        }
        if (req.getRoomId() == null || req.getRoomId().isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_INPUT", "roomId is required"));
        }

        AllocationDto dto = new AllocationDto(UUID.randomUUID().toString(), req.getStudentId(), req.getRoomId(), Instant.now().toString());
        memory.add(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
