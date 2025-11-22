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

@SpringBootTest
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
    private Subject testSubject;

    @BeforeEach
    void setUp() {
        internshipDemandRepository.deleteAll();
        internshipTypeRepository.deleteAll();
        academicYearRepository.deleteAll();
        subjectRepository.deleteAll();

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
        it1 = internshipTypeRepository.save(it1);

        it2 = new InternshipType();
        it2.setInternshipCode("IT2");
        it2.setFullName("Type 2");
        it2 = internshipTypeRepository.save(it2);

        // create subject category
        SubjectCategory cat = new SubjectCategory();
        cat.setCategoryTitle("General");
        cat = subjectCategoryRepository.save(cat);

        // create subject
        testSubject = new Subject();
        testSubject.setSubjectCode("MATH");
        testSubject.setSubjectTitle("Mathematics");
        testSubject.setSubjectCategory(cat);
        testSubject.setIsActive(true);
        testSubject = subjectRepository.save(testSubject);

        // create two demand rows
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
    void aggregateByYear_Success() throws Exception {
        mockMvc.perform(get("/api/internship-demand/aggregate")
                        .param("year_id", String.valueOf(testYear.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", isA(Iterable.class)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].internshipTypeId", notNullValue()))
                .andExpect(jsonPath("$.data[0].totalRequiredTeachers", notNullValue()));
    }

        @Test
        @WithMockUser(roles = "ADMIN")
        void create_Get_Update_Delete_Success() throws Exception {
        var createPayload = java.util.Map.<String, Object>of(
            "yearId", testYear.getId(),
            "internshipTypeId", it1.getId(),
            "schoolType", "PRIMARY",
            "subjectId", testSubject.getId(),
            "requiredTeachers", 12,
            "studentCount", 120,
            "isForecasted", true
        );

        var createResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/internship-demand")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPayload)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.requiredTeachers").value(12))
            .andReturn();

        String resp = createResult.getResponse().getContentAsString();
        var root = objectMapper.readTree(resp);
        Long createdId = root.path("data").path("id").asLong();

        mockMvc.perform(get("/api/internship-demand/" + createdId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(createdId));

        var updatePayload = java.util.Map.<String, Object>of(
            "requiredTeachers", 20
        );
        mockMvc.perform(MockMvcRequestBuilders.put("/api/internship-demand/" + createdId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePayload)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.requiredTeachers").value(20));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/internship-demand/" + createdId))
            .andExpect(status().isNoContent());
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

        mockMvc.perform(MockMvcRequestBuilders.post("/api/internship-demand")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPayload)))
            .andExpect(status().isUnauthorized());
        }
}
