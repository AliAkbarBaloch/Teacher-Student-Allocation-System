package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingCreateDto;
import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.CreditHourTracking;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Teacher;
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

/**
 * Integration tests for the {@link CreditHourTrackingController}.
 * <p>
 * This test class validates CRUD operations, pagination, and authorization
 * for credit hour tracking endpoints.
 * </p>
 */
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
        teacher.setEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);
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
    void list_Unauthorized_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/credit-hour-tracking")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_Success() throws Exception {
        mockMvc.perform(get("/api/credit-hour-tracking/{id}", saved.getId())
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

        mockMvc.perform(put("/api/credit-hour-tracking/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notes", is("updated")))
                .andExpect(jsonPath("$.data.creditBalance", is(95.0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_Success() throws Exception {
        mockMvc.perform(get("/api/credit-hour-tracking")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", not(empty())))
                .andExpect(jsonPath("$.data[0].id", is(saved.getId().intValue())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void paginate_Success() throws Exception {
        mockMvc.perform(get("/api/credit-hour-tracking/paginate")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", not(empty())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_Success() throws Exception {
        // create a different academic year to avoid duplicate (teacher+year) constraint
        AcademicYear newYear = new AcademicYear();
        newYear.setYearName("2026/27");
        newYear.setTotalCreditHours(100);
        newYear.setElementarySchoolHours(10);
        newYear.setMiddleSchoolHours(20);
        newYear.setBudgetAnnouncementDate(LocalDateTime.now());
        newYear.setCreatedAt(LocalDateTime.now());
        newYear = yearRepo.save(newYear);

        CreditHourTrackingCreateDto dto = new CreditHourTrackingCreateDto();
        dto.setTeacherId(saved.getTeacher().getId());
        dto.setAcademicYearId(newYear.getId()); // use the new year id
        dto.setAssignmentsCount(2);
        dto.setCreditHoursAllocated(20.0);
        dto.setCreditBalance(80.0);
        dto.setNotes("created");

        mockMvc.perform(post("/api/credit-hour-tracking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.notes", is("created")))
                .andExpect(jsonPath("$.data.creditBalance", is(80.0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_Success() throws Exception {
        mockMvc.perform(delete("/api/credit-hour-tracking/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}