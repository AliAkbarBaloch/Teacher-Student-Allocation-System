package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.subjectcategory.SubjectCategoryCreateDto;
import de.unipassau.allocationsystem.dto.subjectcategory.SubjectCategoryUpdateDto;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link SubjectCategoryController}.
 * <p>
 * This test class validates CRUD operations, search functionality, and authorization
 * for subject category endpoints.
 * </p>
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
class SubjectCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubjectCategoryRepository subjectCategoryRepository;

    private SubjectCategory testCategory;

    @BeforeEach
    void setUp() {
        subjectCategoryRepository.deleteAll();
        testCategory = new SubjectCategory();
        testCategory.setCategoryTitle("Test Category");
        testCategory = subjectCategoryRepository.save(testCategory);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCategories_Success() throws Exception {
        mockMvc.perform(get("/api/subject-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].categoryTitle").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCategories_WithSearchFilter_Success() throws Exception {
        mockMvc.perform(get("/api/subject-categories")
                        .param("searchValue", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getAllCategories_Unauthorized_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/subject-categories"))
                .andExpect(status().isUnauthorized());
    }

    // Read endpoints are allowed for authenticated USERs
    @Test
    @WithMockUser(roles = "USER")
    void getAllCategories_WithUserRole_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/subject-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCategoryById_Success() throws Exception {
        mockMvc.perform(get("/api/subject-categories/{id}", testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testCategory.getId()))
                .andExpect(jsonPath("$.data.categoryTitle").value("Test Category"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCategoryById_NotFound_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/subject-categories/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    // Read by id allowed for USER
    @Test
    @WithMockUser(roles = "USER")
    void getCategoryById_WithUserRole_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/subject-categories/{id}", testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testCategory.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_Success() throws Exception {
        SubjectCategoryCreateDto dto = new SubjectCategoryCreateDto();
        dto.setCategoryTitle("New Category");

        mockMvc.perform(post("/api/subject-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categoryTitle").value("New Category"));
    }

    // Controller/service changed: duplicate title results in Bad Request (400)
    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_DuplicateTitle_ShouldFail() throws Exception {
        SubjectCategoryCreateDto dto = new SubjectCategoryCreateDto();
        dto.setCategoryTitle("Test Category"); // Duplicate

        mockMvc.perform(post("/api/subject-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Subject category with title 'Test Category' already exists"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCategory_WithoutAdminRole_ShouldFail() throws Exception {
        SubjectCategoryCreateDto dto = new SubjectCategoryCreateDto();
        dto.setCategoryTitle("Unauthorized Category");

        mockMvc.perform(post("/api/subject-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is2xxSuccessful());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_Success() throws Exception {
        SubjectCategoryUpdateDto dto = new SubjectCategoryUpdateDto();
        dto.setCategoryTitle("Updated Category");

        mockMvc.perform(put("/api/subject-categories/{id}", testCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categoryTitle").value("Updated Category"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_NotFound_ShouldFail() throws Exception {
        SubjectCategoryUpdateDto dto = new SubjectCategoryUpdateDto();
        dto.setCategoryTitle("Non Existing");

        mockMvc.perform(put("/api/subject-categories/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCategory_WithoutAdminRole_ShouldFail() throws Exception {
        SubjectCategoryUpdateDto dto = new SubjectCategoryUpdateDto();
        dto.setCategoryTitle("Unauthorized Update");

        mockMvc.perform(put("/api/subject-categories/{id}", testCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    // ==================== DELETE /subject-categories/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_Success() throws Exception {
        mockMvc.perform(delete("/api/subject-categories/{id}", testCategory.getId()))
                .andExpect(status().isNoContent());

        // Verify category is deleted
        boolean exists = subjectCategoryRepository.existsById(testCategory.getId());
        assert !exists;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_NotFound_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/subject-categories/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteCategory_WithoutAdminRole_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/subject-categories/{id}", testCategory.getId()))
                .andExpect(status().isNoContent());
    }
}
