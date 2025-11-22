package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingUpdateDto;
import de.unipassau.allocationsystem.entity.*;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.CreditHourTrackingRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.repository.SchoolRepository;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
class CreditHourTrackingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CreditHourTrackingRepository creditRepo;

    @Autowired
    private TeacherRepository teacherRepo;

    @Autowired
    private SchoolRepository schoolRepo;

    @Autowired
    private AcademicYearRepository yearRepo;

    private CreditHourTracking saved;

    @BeforeEach
    void setUp() {
        creditRepo.deleteAll();
        teacherRepo.deleteAll();
        schoolRepo.deleteAll();
        yearRepo.deleteAll();

        School school = new School();
        school.setSchoolName("Test School");
        school.setSchoolType(School.SchoolType.PRIMARY);
        school.setZoneNumber(1);
        school = schoolRepo.save(school);

        Teacher teacher = new Teacher();
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        teacher.setSchool(school);
        teacher.setEmail("john.doe@example.test");
        teacher.setEmploymentStatus(Teacher.EmploymentStatus.FULL_TIME);
        teacher = teacherRepo.save(teacher);

        AcademicYear year = new AcademicYear();
        year.setYearName("2025/26");
        year.setTotalCreditHours(100);
        year.setElementarySchoolHours(10);
        year.setMiddleSchoolHours(20);
        year.setBudgetAnnouncementDate(LocalDateTime.now());
        year.setCreatedAt(LocalDateTime.now());
        year = yearRepo.save(year);

        CreditHourTracking c = new CreditHourTracking();
        c.setTeacher(teacher);
        c.setAcademicYear(year);
        c.setAssignmentsCount(1);
        c.setCreditHoursAllocated(10.0);
        c.setCreditBalance(90.0);
        c.setNotes("initial");
        saved = creditRepo.save(c);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_ForYear_Success() throws Exception {
        mockMvc.perform(get("/api/credit-hour-tracking/credit-tracking")
                .param("year_id", saved.getAcademicYear().getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", notNullValue(Object.class)))
                .andExpect(jsonPath("$.items[0].id", notNullValue(Object.class)));
    }

    @Test
    void list_Unauthorized_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/credit-hour-tracking/credit-tracking")
                .param("year_id", saved.getAcademicYear().getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_Success() throws Exception {
        mockMvc.perform(get("/api/credit-hour-tracking/credit-tracking/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.data.notes", is("initial")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_Success() throws Exception {
        CreditHourTrackingUpdateDto dto = new CreditHourTrackingUpdateDto();
        dto.setNotes("updated");
        dto.setCreditHoursAllocated(5.0);
        dto.setCreditBalance(95.0);

        mockMvc.perform(put("/api/credit-hour-tracking/credit-tracking/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notes", is("updated")))
                .andExpect(jsonPath("$.data.creditBalance", is(95.0)));
    }
}
