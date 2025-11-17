package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.SubjectCategoryDto;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SubjectCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubjectCategoryRepository subjectCategoryRepository;

    @BeforeEach
    void setUp() {
        subjectCategoryRepository.deleteAll();
    }

    private SubjectCategory createCategory(String title) {
        SubjectCategory category = new SubjectCategory();
        category.setCategoryTitle(title);
        return subjectCategoryRepository.save(category);
    }

    @Test
    void getSortFields_ShouldReturnConfiguredFields() throws Exception {
        mockMvc.perform(get("/subject-categories/sort-fields"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(4)))
                .andExpect(jsonPath("$.data[0].key").value("id"));
    }

    @Test
    void getPaginate_ShouldReturnFilteredItems() throws Exception {
        createCategory("Alpha");
        createCategory("Beta");
        createCategory("Alphabet Soup");

        mockMvc.perform(get("/subject-categories/paginate")
                        .param("page", "1")
                        .param("pageSize", "2")
                        .param("sortBy", "categoryTitle")
                        .param("sortOrder", "asc")
                        .param("searchValue", "Alpha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(2))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[0].categoryTitle", is("Alpha")))
                .andExpect(jsonPath("$.data.totalItems").value(2));
    }

    @Test
    void getAll_ShouldReturnAllCategories() throws Exception {
        createCategory("Math");
        createCategory("Science");

        mockMvc.perform(get("/subject-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void getById_ShouldReturnCategory() throws Exception {
        SubjectCategory saved = createCategory("History");

        mockMvc.perform(get("/subject-categories/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(saved.getId()))
                .andExpect(jsonPath("$.data.categoryTitle").value("History"));
    }

    @Test
    void create_ShouldPersistCategory() throws Exception {
        SubjectCategoryDto dto = new SubjectCategoryDto();
        dto.setCategoryTitle("Philosophy");

        mockMvc.perform(post("/subject-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.categoryTitle").value("Philosophy"));
    }

    @Test
    void create_DuplicateTitle_ShouldReturnConflict() throws Exception {
        createCategory("Geology");

        SubjectCategoryDto dto = new SubjectCategoryDto();
        dto.setCategoryTitle("Geology");

        mockMvc.perform(post("/subject-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void update_ShouldModifyCategory() throws Exception {
        SubjectCategory saved = createCategory("Economics");

        SubjectCategoryDto dto = new SubjectCategoryDto();
        dto.setCategoryTitle("Macro Economics");

        mockMvc.perform(put("/subject-categories/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categoryTitle").value("Macro Economics"));
    }

    @Test
    void update_NotFound_ShouldReturnNotFound() throws Exception {
        SubjectCategoryDto dto = new SubjectCategoryDto();
        dto.setCategoryTitle("Non Existing");

        mockMvc.perform(put("/subject-categories/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_ShouldRemoveCategory() throws Exception {
        SubjectCategory saved = createCategory("Astronomy");

        mockMvc.perform(delete("/subject-categories/" + saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/subject-categories"))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void delete_NotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/subject-categories/9999"))
                .andExpect(status().isNotFound());
    }
}

