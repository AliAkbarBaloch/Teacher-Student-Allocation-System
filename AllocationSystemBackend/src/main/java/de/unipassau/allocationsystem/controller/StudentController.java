package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.StudentDto;
import de.unipassau.allocationsystem.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {
    private static final int CODE_404 = 404;

    @GetMapping("/{studentId}")
    public ResponseEntity<?> getStudent(@PathVariable String studentId) {
        // dummy implementation: return a sample student for any id except "not-found"
        if ("not-found".equals(studentId)) {
            return ResponseEntity.status(CODE_404).body(new ErrorResponse("NOT_FOUND", "Student not found"));
        }

        StudentDto dto = new StudentDto(studentId, "Alice Example", studentId + "@example.edu");
        return ResponseEntity.ok(dto);
    }
}
