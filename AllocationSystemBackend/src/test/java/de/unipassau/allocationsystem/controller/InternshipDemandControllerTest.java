package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.entity.*;
import de.unipassau.allocationsystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
class InternshipDemandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InternshipDemandRepository internshipDemandRepository;

    @Autowired
    private InternshipTypeRepository internshipTypeRepository;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private SubjectCategoryRepository subjectCategoryRepository;

    private AcademicYear testYear;
    private InternshipType it1;
    private InternshipType it2;
    private InternshipType it3;
    private Subject testSubject;

    @BeforeEach
    void setUp() {
        internshipDemandRepository.deleteAll();
        internshipTypeRepository.deleteAll();
        academicYearRepository.deleteAll();
        subjectRepository.deleteAll();
        subjectCategoryRepository.deleteAll();

        // create academic year
        testYear = new AcademicYear();
        testYear.setYearName("2024/2025");
        testYear.setBudgetAnnouncementDate(LocalDateTime.now());
        testYear.setElementarySchoolHours(100);
        testYear.setMiddleSchoolHours(100);
        testYear.setTotalCreditHours(200);
        testYear = academicYearRepository.save(testYear);

        // create internship types
        it1 = new InternshipType();
        it1.setInternshipCode("IT1");
        it1.setFullName("Type 1");
        it1.setSemester(1);
        it1 = internshipTypeRepository.save(it1);

        it2 = new InternshipType();
        it2.setInternshipCode("IT2");
        it2.setFullName("Type 2");
        it2.setSemester(2);
        it2 = internshipTypeRepository.save(it2);

        it3 = new InternshipType();
        it3.setInternshipCode("IT3");
        it3.setFullName("Type 3");
        it3.setSemester(1);
        it3 = internshipTypeRepository.save(it3);

        SubjectCategory cat = new SubjectCategory();
        cat.setCategoryTitle("General");
        cat = subjectCategoryRepository.save(cat);

        testSubject = new Subject();
        testSubject.setSubjectCode("MATH");
        testSubject.setSubjectTitle("Mathematics");
        testSubject.setSubjectCategory(cat);
        testSubject.setIsActive(true);
        testSubject = subjectRepository.save(testSubject);

        InternshipDemand d1 = new InternshipDemand();
        d1.setAcademicYear(testYear);
        d1.setInternshipType(it1);
        d1.setSchoolType(School.SchoolType.PRIMARY);
        d1.setSubject(testSubject);
        d1.setRequiredTeachers(7);
        d1.setIsForecasted(false);
        internshipDemandRepository.save(d1);

        InternshipDemand d2 = new InternshipDemand();
        d2.setAcademicYear(testYear);
        d2.setInternshipType(it2);
        d2.setSchoolType(School.SchoolType.PRIMARY);
        d2.setSubject(testSubject);
        d2.setRequiredTeachers(3);
        d2.setIsForecasted(false);
        internshipDemandRepository.save(d2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSortFields_Success() throws Exception {
        mockMvc.perform(get("/api/internship-demands/sort-fields")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", not(empty())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPaginate_Success() throws Exception {
        mockMvc.perform(get("/api/internship-demands/paginate")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", not(empty())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_Success() throws Exception {
        mockMvc.perform(get("/api/internship-demands")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", not(empty())))
                .andExpect(jsonPath("$.data[0].id", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listFilter_Success() throws Exception {
        mockMvc.perform(get("/api/internship-demands/list-filter")
                        .param("academic_year_id", String.valueOf(testYear.getId()))
                        .param("school_type", "PRIMARY")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id")
                        .param("direction", "ASC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", not(empty())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void aggregateByYear_Success() throws Exception {
        mockMvc.perform(get("/api/internship-demands/aggregate")
                        .param("academic_year_id", String.valueOf(testYear.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", isA(Iterable.class)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].internshipTypeId", notNullValue()))
                .andExpect(jsonPath("$.data[0].totalRequiredTeachers", notNullValue()));
    }

    @Test
    void create_Unauthorized_ShouldFail() throws Exception {
        var createPayload = java.util.Map.<String, Object>of(
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
}