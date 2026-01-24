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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    private static final String BASE_URL = "/api/subject-categories";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final SubjectCategoryRepository subjectCategoryRepository;

    private SubjectCategory testCategory;

    @Autowired
    SubjectCategoryControllerTest(
            @Autowired
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            SubjectCategoryRepository subjectCategoryRepository
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.subjectCategoryRepository = subjectCategoryRepository;
    }

    @BeforeEach
    void setUp() {
        subjectCategoryRepository.deleteAll();
        testCategory = subjectCategoryRepository.save(buildCategory("Test Category"));
    }

    private SubjectCategory buildCategory(String title) {
        SubjectCategory category = new SubjectCategory();
        category.setCategoryTitle(title);
        return category;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCategoriesSuccess() throws Exception {
        performGet(BASE_URL)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].categoryTitle").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCategoriesWithSearchFilterSuccess() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("searchValue", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getAllCategoriesUnauthorizedShouldFail() throws Exception {
        performGet(BASE_URL)
                .andExpect(status().isUnauthorized());
    }

    // Read endpoints are allowed for authenticated USERs
    @Test
    @WithMockUser(roles = "USER")
    void getAllCategoriesWithUserRoleShouldSucceed() throws Exception {
        performGet(BASE_URL)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCategoryByIdSuccess() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testCategory.getId()))
                .andExpect(jsonPath("$.data.categoryTitle").value("Test Category"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCategoryByIdNotFoundShouldFail() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    // Read by id allowed for USER
    @Test
    @WithMockUser(roles = "USER")
    void getCategoryByIdWithUserRoleShouldSucceed() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testCategory.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategorySuccess() throws Exception {
        SubjectCategoryCreateDto dto = new SubjectCategoryCreateDto();
        dto.setCategoryTitle("New Category");

        performPost(BASE_URL, dto)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categoryTitle").value("New Category"));
    }

    // Controller/service changed: duplicate title results in Conflict (409)
    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategoryDuplicateTitleShouldFail() throws Exception {
        SubjectCategoryCreateDto dto = new SubjectCategoryCreateDto();
        dto.setCategoryTitle("Test Category");

        performPost(BASE_URL, dto)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Subject category with title 'Test Category' already exists"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCategoryWithoutAdminRoleShouldSucceed() throws Exception {
        SubjectCategoryCreateDto dto = new SubjectCategoryCreateDto();
        dto.setCategoryTitle("Unauthorized Category");

        performPost(BASE_URL, dto)
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategorySuccess() throws Exception {
        SubjectCategoryUpdateDto dto = new SubjectCategoryUpdateDto();
        dto.setCategoryTitle("Updated Category");

        mockMvc.perform(put(BASE_URL + "/{id}", testCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categoryTitle").value("Updated Category"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategoryNotFoundShouldFail() throws Exception {
        SubjectCategoryUpdateDto dto = new SubjectCategoryUpdateDto();
        dto.setCategoryTitle("Non Existing");

        mockMvc.perform(put(BASE_URL + "/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCategoryWithoutAdminRoleShouldSucceed() throws Exception {
        SubjectCategoryUpdateDto dto = new SubjectCategoryUpdateDto();
        dto.setCategoryTitle("Unauthorized Update");

        mockMvc.perform(put(BASE_URL + "/{id}", testCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    // ==================== DELETE /subject-categories/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategorySuccess() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", testCategory.getId()))
                .andExpect(status().isNoContent());

        boolean exists = subjectCategoryRepository.existsById(testCategory.getId());
        assert !exists;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategoryNotFoundShouldFail() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteCategoryWithoutAdminRoleShouldSucceed() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", testCategory.getId()))
                .andExpect(status().isNoContent());
    }

    private ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url));
    }

    private ResultActions performPost(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }
}
