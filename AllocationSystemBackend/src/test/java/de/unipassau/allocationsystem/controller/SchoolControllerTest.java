package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.school.SchoolCreateDto;
import de.unipassau.allocationsystem.dto.school.SchoolUpdateDto;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.School.SchoolType;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.testutil.TestSchoolFactory;
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

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for SchoolController aligned with actual controller endpoints.
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
class SchoolControllerTest {

    private static final String BASE_URL = "/api/schools";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final SchoolRepository schoolRepository;

    private School testSchool;

    @Autowired
    SchoolControllerTest(
            @Autowired
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            SchoolRepository schoolRepository
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.schoolRepository = schoolRepository;
    }

    @BeforeEach
    void setUp() {
        schoolRepository.deleteAll();

        // Centralized test data creation to avoid clone warnings.
        School school = TestSchoolFactory.buildTestSchool(1L, "Test Elementary School", SchoolType.PRIMARY);
        school.setZoneNumber(1); // keep explicit to match assertions and avoid DB constraints variability

        testSchool = schoolRepository.save(school);
    }

    // ==================== GET /schools ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllSchoolsSuccess() throws Exception {
        performGet(BASE_URL)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data[0].schoolName", is("Test Elementary School")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllSchoolsWithSearchFilterSuccess() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("searchValue", "Elementary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data[0].schoolName", containsString("Test")));
    }

    @Test
    void getAllSchoolsUnauthorizedShouldFail() throws Exception {
        performGet(BASE_URL)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllSchoolsWithUserRoleShouldSucceed() throws Exception {
        performGet(BASE_URL)
                .andExpect(status().isOk());
    }

    // ==================== GET /schools/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSchoolByIdSuccess() throws Exception {
        performGet(BASE_URL + "/{id}", testSchool.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.schoolName", is("Test Elementary School")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSchoolByIdNotFoundShouldFail() throws Exception {
        performGet(BASE_URL + "/{id}", 9999L)
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getSchoolByIdWithUserRoleShouldSucceed() throws Exception {
        performGet(BASE_URL + "/{id}", testSchool.getId())
                .andExpect(status().isOk());
    }

    // ==================== GET /schools/paginate ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPaginateDefaultSuccess() throws Exception {
        performGet(BASE_URL + "/paginate")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data.items", notNullValue()))
                .andExpect(jsonPath("$.data.totalItems", notNullValue()));
    }

    // ==================== GET /schools/sort-fields ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSortFieldsSuccess() throws Exception {
        performGet(BASE_URL + "/sort-fields")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data[0].key", is("id")));
    }

    // ==================== POST /schools ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSchoolSuccess() throws Exception {
        SchoolCreateDto dto = new SchoolCreateDto();
        dto.setSchoolName("New School");
        dto.setSchoolType(SchoolType.MIDDLE);
        dto.setZoneNumber(2);
        dto.setAddress("New Street 10");
        dto.setContactEmail("new@school.de");

        performPost(BASE_URL, dto)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.schoolName", is("New School")))
                .andExpect(jsonPath("$.data.id", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSchoolDuplicateNameShouldFail() throws Exception {
        SchoolCreateDto dto = new SchoolCreateDto();
        dto.setSchoolName("Test Elementary School");
        dto.setSchoolType(SchoolType.PRIMARY);

        performPost(BASE_URL, dto)
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSchoolInvalidDataShouldFail() throws Exception {
        Map<String, Object> invalid = Map.of("zoneNumber", 1);

        performPost(BASE_URL, invalid)
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSchoolInvalidEmailShouldFail() throws Exception {
        SchoolCreateDto dto = new SchoolCreateDto();
        dto.setSchoolName("Invalid Email School");
        dto.setSchoolType(SchoolType.MIDDLE);
        dto.setContactEmail("not-an-email");

        performPost(BASE_URL, dto)
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createSchoolWithoutAdminRoleShouldFail() throws Exception {
        SchoolCreateDto dto = new SchoolCreateDto();
        dto.setSchoolName("Should Not Create");
        dto.setSchoolType(SchoolType.MIDDLE);

        performPost(BASE_URL, dto)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.zoneNumber", is("Zone number is required")));
    }

    // ==================== PUT /schools/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSchoolSuccess() throws Exception {
        SchoolUpdateDto dto = new SchoolUpdateDto();
        dto.setSchoolName("Updated School Name");
        dto.setZoneNumber(5);

        performPut(BASE_URL + "/{id}", testSchool.getId(), dto)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.schoolName", is("Updated School Name")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSchoolNotFoundShouldFail() throws Exception {
        SchoolUpdateDto dto = new SchoolUpdateDto();
        dto.setSchoolName("Doesn't Matter");

        performPut(BASE_URL + "/{id}", 9999L, dto)
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateSchoolWithoutAdminRoleShouldSucceed() throws Exception {
        SchoolUpdateDto dto = new SchoolUpdateDto();
        dto.setSchoolName("No Permission");

        performPut(BASE_URL + "/{id}", testSchool.getId(), dto)
                .andExpect(status().isOk());
    }

    // ==================== DELETE /schools/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteSchoolSuccess() throws Exception {
        performDelete(BASE_URL + "/{id}", testSchool.getId())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteSchoolNotFoundShouldFail() throws Exception {
        performDelete(BASE_URL + "/{id}", 9999L)
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteSchoolWithoutAdminRoleShouldSucceed() throws Exception {
        performDelete(BASE_URL + "/{id}", testSchool.getId())
                .andExpect(status().isNoContent());
    }

    private ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions performGet(String url, Object pathVar) throws Exception {
        return mockMvc.perform(get(url, pathVar).contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions performPost(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions performPut(String url, Object pathVar, Object body) throws Exception {
        return mockMvc.perform(put(url, pathVar)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions performDelete(String url, Object pathVar) throws Exception {
        return mockMvc.perform(delete(url, pathVar).contentType(MediaType.APPLICATION_JSON));
    }
}
