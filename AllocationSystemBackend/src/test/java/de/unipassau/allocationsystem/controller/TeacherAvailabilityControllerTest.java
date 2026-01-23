package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.TeacherAvailabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private final MockMvc mockMvc;
    private final InternshipTypeRepository internshipTypeRepository;
    private final TeacherAvailabilityRepository teacherAvailabilityRepository;

    TeacherAvailabilityControllerTest(
            MockMvc mockMvc,
            InternshipTypeRepository internshipTypeRepository,
            TeacherAvailabilityRepository teacherAvailabilityRepository
    ) {
        this.mockMvc = mockMvc;
        this.internshipTypeRepository = internshipTypeRepository;
        this.teacherAvailabilityRepository = teacherAvailabilityRepository;
    }

    @BeforeEach
    void setUp() {
        teacherAvailabilityRepository.deleteAll();
        internshipTypeRepository.deleteAll();
        internshipTypeRepository.save(buildInternshipType());
    }

    private InternshipType buildInternshipType() {
        InternshipType it = new InternshipType();
        it.setInternshipCode("TEST-INT");
        it.setFullName("Test Internship");
        it.setIsSubjectSpecific(false);
        it.setSemester(1);
        return it;
    }

    @Test
    void getSortFieldsSuccess() throws Exception {
        mockMvc.perform(get("/api/teacher-availability/sort-fields")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getPaginateSuccessWithNoData() throws Exception {
        mockMvc.perform(get("/api/teacher-availability/paginate")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getAllSuccessWithNoData() throws Exception {
        mockMvc.perform(get("/api/teacher-availability")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getByIdNotFoundShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/teacher-availability/{id}", 9999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAvailabilityNotFoundShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/teacher-availability/{id}", 9999L))
                .andExpect(status().isNotFound());
    }
}
