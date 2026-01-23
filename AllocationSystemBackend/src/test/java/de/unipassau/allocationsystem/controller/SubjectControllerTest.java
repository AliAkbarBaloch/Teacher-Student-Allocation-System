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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link SubjectController}.
 * <p>
 * This test class validates CRUD operations, pagination, sorting, and validation
 * for subject endpoints.
 * </p>
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
class SubjectControllerTest {

    private static final String BASE_URL = "/api/subjects";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final SubjectRepository subjectRepository;
    private final SubjectCategoryRepository subjectCategoryRepository;

    private SubjectCategory testCategory;
    private Subject testSubject;

    @Autowired
    SubjectControllerTest(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            SubjectRepository subjectRepository,
            SubjectCategoryRepository subjectCategoryRepository
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.subjectRepository = subjectRepository;
        this.subjectCategoryRepository = subjectCategoryRepository;
    }

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
    void getSortFieldsShouldReturnConfiguredFields() throws Exception {
        performGet(BASE_URL + "/sort-fields")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(7)))
                .andExpect(jsonPath("$.data[0].key").value("id"));
    }

    @Test
    void getPaginateShouldReturnFilteredItems() throws Exception {
        subjectRepository.save(buildSubject("PHYS101", "Physics", testCategory, "High School", true));

        mockMvc.perform(get(BASE_URL + "/paginate")
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
    void getAllShouldReturnAllSubjects() throws Exception {
        subjectRepository.save(buildSubject("PHYS101", "Physics", testCategory, null, true));

        performGet(BASE_URL)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void getByIdShouldReturnSubject() throws Exception {
        performGet(BASE_URL + "/" + testSubject.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testSubject.getId()))
                .andExpect(jsonPath("$.data.subjectCode").value("MATH101"))
                .andExpect(jsonPath("$.data.subjectTitle").value("Mathematics"));
    }

    @Test
    void getByIdNotFoundShouldReturn404() throws Exception {
        performGet(BASE_URL + "/9999")
                .andExpect(status().isNotFound());
    }

    @Test
    void createShouldPersistSubject() throws Exception {
        SubjectCreateDto dto = new SubjectCreateDto();
        dto.setSubjectCode("CHEM101");
        dto.setSubjectTitle("Chemistry");
        dto.setSubjectCategoryId(testCategory.getId());
        dto.setSchoolType("High School");
        dto.setIsActive(true);

        performPost(BASE_URL, dto)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.subjectCode").value("CHEM101"))
                .andExpect(jsonPath("$.data.subjectTitle").value("Chemistry"));
    }

    @Test
    void createDuplicateCodeShouldReturnConflict() throws Exception {
        SubjectCreateDto dto = new SubjectCreateDto();
        dto.setSubjectCode("MATH101");
        dto.setSubjectTitle("Different Title");
        dto.setSubjectCategoryId(testCategory.getId());

        performPost(BASE_URL, dto)
                .andExpect(status().isConflict());
    }

    @Test
    void createInvalidInputShouldReturnBadRequest() throws Exception {
        SubjectCreateDto dto = new SubjectCreateDto();
        dto.setSubjectCode("");

        performPost(BASE_URL, dto)
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateShouldModifySubject() throws Exception {
        SubjectUpdateDto dto = new SubjectUpdateDto();
        dto.setSubjectTitle("Advanced Mathematics");
        dto.setSchoolType("High School");

        performPut(BASE_URL + "/" + testSubject.getId(), dto)
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.subjectTitle").value("Advanced Mathematics"))
                .andExpect(jsonPath("$.data.schoolType").value("High School"));
    }

    @Test
    void updateNotFoundShouldReturnNotFound() throws Exception {
        SubjectUpdateDto dto = new SubjectUpdateDto();
        dto.setSubjectTitle("Non Existing");

        performPut(BASE_URL + "/9999", dto)
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteShouldRemoveSubject() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + testSubject.getId()))
                .andExpect(status().isNoContent());

        performGet(BASE_URL)
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void deleteNotFoundShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/9999"))
                .andExpect(status().isNotFound());
    }

    private Subject buildSubject(
            String code,
            String title,
            SubjectCategory category,
            String schoolType,
            boolean active
    ) {
        Subject s = new Subject();
        s.setSubjectCode(code);
        s.setSubjectTitle(title);
        s.setSubjectCategory(category);
        if (schoolType != null) {
            s.setSchoolType(schoolType);
        }
        s.setIsActive(active);
        return s;
    }

    private ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url));
    }

    private ResultActions performPost(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions performPut(String url, Object body) throws Exception {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }
}
