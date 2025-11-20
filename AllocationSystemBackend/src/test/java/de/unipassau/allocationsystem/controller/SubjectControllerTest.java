package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.subject.SubjectCreateDto;
import de.unipassau.allocationsystem.dto.subject.SubjectUpdateDto;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SubjectCategoryRepository subjectCategoryRepository;

    private SubjectCategory testCategory;
    private Subject testSubject;

    @BeforeEach
    void setUp() {
        subjectRepository.deleteAll();
        subjectCategoryRepository.deleteAll();
        
        testCategory = new SubjectCategory();
        testCategory.setCategoryTitle("Mathematics");
        testCategory = subjectCategoryRepository.save(testCategory);

        testSubject = new Subject();
        testSubject.setSubjectCode("MATH101");
        testSubject.setSubjectTitle("Mathematics");
        testSubject.setSubjectCategory(testCategory);
        testSubject.setSchoolType("Elementary");
        testSubject.setIsActive(true);
        testSubject = subjectRepository.save(testSubject);
    }

    @Test
    void getSortFields_ShouldReturnConfiguredFields() throws Exception {
        mockMvc.perform(get("/subjects/sort-fields"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(7)))
                .andExpect(jsonPath("$.data[0].key").value("id"));
    }

    @Test
    void getPaginate_ShouldReturnFilteredItems() throws Exception {
        Subject subject2 = new Subject();
        subject2.setSubjectCode("PHYS101");
        subject2.setSubjectTitle("Physics");
        subject2.setSubjectCategory(testCategory);
        subject2.setSchoolType("High School");
        subject2.setIsActive(true);
        subjectRepository.save(subject2);

        mockMvc.perform(get("/subjects/paginate")
                        .param("page", "1")
                        .param("pageSize", "2")
                        .param("sortBy", "subjectCode")
                        .param("sortOrder", "asc")
                        .param("searchValue", "MATH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(2))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].subjectCode", is("MATH101")))
                .andExpect(jsonPath("$.data.totalItems").value(1));
    }

    @Test
    void getAll_ShouldReturnAllSubjects() throws Exception {
        Subject subject2 = new Subject();
        subject2.setSubjectCode("PHYS101");
        subject2.setSubjectTitle("Physics");
        subject2.setSubjectCategory(testCategory);
        subject2.setIsActive(true);
        subjectRepository.save(subject2);

        mockMvc.perform(get("/subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void getById_ShouldReturnSubject() throws Exception {
        mockMvc.perform(get("/subjects/" + testSubject.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testSubject.getId()))
                .andExpect(jsonPath("$.data.subjectCode").value("MATH101"))
                .andExpect(jsonPath("$.data.subjectTitle").value("Mathematics"));
    }

    @Test
    void getById_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/subjects/9999"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void create_ShouldPersistSubject() throws Exception {
        SubjectCreateDto dto = new SubjectCreateDto();
        dto.setSubjectCode("CHEM101");
        dto.setSubjectTitle("Chemistry");
        dto.setSubjectCategoryId(testCategory.getId());
        dto.setSchoolType("High School");
        dto.setIsActive(true);

        mockMvc.perform(post("/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.subjectCode").value("CHEM101"))
                .andExpect(jsonPath("$.data.subjectTitle").value("Chemistry"));
    }

    @Test
    void create_DuplicateCode_ShouldReturnBadRequest() throws Exception {
        SubjectCreateDto dto = new SubjectCreateDto();
        dto.setSubjectCode("MATH101"); // Duplicate
        dto.setSubjectTitle("Different Title");
        dto.setSubjectCategoryId(testCategory.getId());

        mockMvc.perform(post("/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_InvalidInput_ShouldReturnBadRequest() throws Exception {
        SubjectCreateDto dto = new SubjectCreateDto();
        // Missing required fields
        dto.setSubjectCode("");

        mockMvc.perform(post("/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_ShouldModifySubject() throws Exception {
        SubjectUpdateDto dto = new SubjectUpdateDto();
        dto.setSubjectTitle("Advanced Mathematics");
        dto.setSchoolType("High School");

        mockMvc.perform(put("/subjects/" + testSubject.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.subjectTitle").value("Advanced Mathematics"))
                .andExpect(jsonPath("$.data.schoolType").value("High School"));
    }

    @Test
    void update_NotFound_ShouldReturnNotFound() throws Exception {
        SubjectUpdateDto dto = new SubjectUpdateDto();
        dto.setSubjectTitle("Non Existing");

        mockMvc.perform(put("/subjects/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_ShouldRemoveSubject() throws Exception {
        mockMvc.perform(delete("/subjects/" + testSubject.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/subjects"))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void delete_NotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/subjects/9999"))
                .andExpect(status().isNotFound());
    }
}

