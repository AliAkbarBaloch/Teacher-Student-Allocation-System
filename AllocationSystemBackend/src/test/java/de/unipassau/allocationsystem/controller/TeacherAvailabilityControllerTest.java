package de.unipassau.allocationsystem.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.TeacherAvailabilityRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link TeacherAvailabilityController}.
 * <p>
 * This test class validates teacher availability CRUD operations, pagination,
 * and sorting functionality.
 * </p>
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
@WithMockUser // authenticate all tests in this class
class TeacherAvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InternshipTypeRepository internshipTypeRepository;

    @Autowired
    private TeacherAvailabilityRepository teacherAvailabilityRepository;

    @BeforeEach
    void setUp() {
        // ensure clean state
        teacherAvailabilityRepository.deleteAll();
        internshipTypeRepository.deleteAll();

        // create a minimal InternshipType with required non-null semester to avoid validation errors
        InternshipType it = new InternshipType();
        it.setInternshipCode("TEST-INT");
        it.setFullName("Test Internship");
        it.setIsSubjectSpecific(false);
        it.setSemester(1); // required
        internshipTypeRepository.save(it);
    }

    @Test
    void getSortFields_Success() throws Exception {
        mockMvc.perform(get("/api/teacher-availability/sort-fields")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getPaginate_Success_WithNoData() throws Exception {
        mockMvc.perform(get("/api/teacher-availability/paginate")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getAll_Success_WithNoData() throws Exception {
        mockMvc.perform(get("/api/teacher-availability")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getById_NotFound_ShouldReturn404() throws Exception {
        // choose an id that does not exist
        mockMvc.perform(get("/api/teacher-availability/{id}", 9999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAvailability_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/teacher-availability/{id}", 9999L))
                .andExpect(status().isNotFound());
    }
}
