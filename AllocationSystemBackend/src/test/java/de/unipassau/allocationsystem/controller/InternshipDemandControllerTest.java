package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.InternshipDemand;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.InternshipDemandRepository;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link InternshipDemandController}.
 * <p>
 * This test class validates CRUD operations, pagination, filtering, and aggregation
 * for internship demand endpoints.
 * </p>
 */
@WebMvcTest(InternshipDemandController.class)
class InternshipDemandControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final InternshipDemandRepository internshipDemandRepository;
    private final InternshipTypeRepository internshipTypeRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SubjectRepository subjectRepository;
    private final SubjectCategoryRepository subjectCategoryRepository;

    private AcademicYear testYear;
    private InternshipType it1;
    private InternshipType it2;
    private Subject testSubject;

    InternshipDemandControllerTest(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            InternshipDemandRepository internshipDemandRepository,
            InternshipTypeRepository internshipTypeRepository,
            AcademicYearRepository academicYearRepository,
            SubjectRepository subjectRepository,
            SubjectCategoryRepository subjectCategoryRepository
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.internshipDemandRepository = internshipDemandRepository;
        this.internshipTypeRepository = internshipTypeRepository;
        this.academicYearRepository = academicYearRepository;
        this.subjectRepository = subjectRepository;
        this.subjectCategoryRepository = subjectCategoryRepository;
    }

    @BeforeEach
    void setUp() {
        clearRepositories();
        seedData();
    }

    private void clearRepositories() {
        internshipDemandRepository.deleteAll();
        internshipTypeRepository.deleteAll();
        academicYearRepository.deleteAll();
        subjectRepository.deleteAll();
        subjectCategoryRepository.deleteAll();
    }

    private void seedData() {
        testYear = createAndPersistAcademicYear("2024/2025");

        it1 = createAndPersistInternshipType("IT1", "Type 1", 1);
        it2 = createAndPersistInternshipType("IT2", "Type 2", 2);

        SubjectCategory cat = createAndPersistSubjectCategory("General");
        testSubject = createAndPersistSubject("MATH", "Mathematics", cat);

        createAndPersistDemand(testYear, it1, testSubject, 7);
        createAndPersistDemand(testYear, it2, testSubject, 3);
    }

    private AcademicYear createAndPersistAcademicYear(String yearName) {
        AcademicYear year = new AcademicYear();
        year.setYearName(yearName);
        year.setBudgetAnnouncementDate(LocalDateTime.now());
        year.setElementarySchoolHours(100);
        year.setMiddleSchoolHours(100);
        year.setTotalCreditHours(200);
        return academicYearRepository.save(year);
    }

    private InternshipType createAndPersistInternshipType(String code, String fullName, int semester) {
        InternshipType type = new InternshipType();
        type.setInternshipCode(code);
        type.setFullName(fullName);
        type.setSemester(semester);
        return internshipTypeRepository.save(type);
    }

    private SubjectCategory createAndPersistSubjectCategory(String title) {
        SubjectCategory cat = new SubjectCategory();
        cat.setCategoryTitle(title);
        return subjectCategoryRepository.save(cat);
    }

    private Subject createAndPersistSubject(String code, String title, SubjectCategory category) {
        Subject subject = new Subject();
        subject.setSubjectCode(code);
        subject.setSubjectTitle(title);
        subject.setSubjectCategory(category);
        subject.setIsActive(true);
        return subjectRepository.save(subject);
    }

    private void createAndPersistDemand(AcademicYear year, InternshipType type, Subject subject, int requiredTeachers) {
        InternshipDemand demand = new InternshipDemand();
        demand.setAcademicYear(year);
        demand.setInternshipType(type);
        demand.setSchoolType(School.SchoolType.PRIMARY);
        demand.setSubject(subject);
        demand.setRequiredTeachers(requiredTeachers);
        demand.setIsForecasted(false);
        internshipDemandRepository.save(demand);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSortFieldsSuccess() throws Exception {
        performGet("/api/internship-demands/sort-fields")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", not(empty())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPaginateSuccess() throws Exception {
        performGet("/api/internship-demands/paginate", Map.of("page", "1", "pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", not(empty())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllSuccess() throws Exception {
        performGet("/api/internship-demands")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", not(empty())))
                .andExpect(jsonPath("$.data[0].id", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listFilterSuccess() throws Exception {
        performGet("/api/internship-demands/list-filter", Map.of(
                "academic_year_id", String.valueOf(testYear.getId()),
                "school_type", "PRIMARY",
                "page", "0",
                "size", "10",
                "sort", "id",
                "direction", "ASC"
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", not(empty())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void aggregateByYearSuccess() throws Exception {
        performGet("/api/internship-demands/aggregate", Map.of(
                "academic_year_id", String.valueOf(testYear.getId())
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", isA(Iterable.class)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].internshipTypeId", notNullValue()))
                .andExpect(jsonPath("$.data[0].totalRequiredTeachers", notNullValue()));
    }

    @Test
    void createUnauthorizedShouldFail() throws Exception {
        Map<String, Object> createPayload = Map.of(
                "yearId", testYear.getId(),
                "internshipTypeId", it1.getId(),
                "schoolType", "PRIMARY",
                "subjectId", testSubject.getId(),
                "requiredTeachers", 5
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/internship-demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions performGet(String url, Map<String, String> params) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request =
                get(url).contentType(MediaType.APPLICATION_JSON);

        for (Map.Entry<String, String> e : params.entrySet()) {
            request = request.param(e.getKey(), e.getValue());
        }

        return mockMvc.perform(request);
    }
}
