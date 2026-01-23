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
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final CreditHourTrackingRepository creditRepo;
    private final TeacherRepository teacherRepo;
    private final SchoolRepository schoolRepo;
    private final AcademicYearRepository yearRepo;

    private CreditHourTracking saved;

    @Autowired
    CreditHourTrackingControllerTest(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            CreditHourTrackingRepository creditRepo,
            TeacherRepository teacherRepo,
            SchoolRepository schoolRepo,
            AcademicYearRepository yearRepo
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.creditRepo = creditRepo;
        this.teacherRepo = teacherRepo;
        this.schoolRepo = schoolRepo;
        this.yearRepo = yearRepo;
    }

    @BeforeEach
    void setUp() {
        clearRepositories();
        saved = createAndPersistBaselineTracking();
    }

    private void clearRepositories() {
        creditRepo.deleteAll();
        teacherRepo.deleteAll();
        schoolRepo.deleteAll();
        yearRepo.deleteAll();
    }

    private CreditHourTracking createAndPersistBaselineTracking() {
        School school = createAndPersistSchool();
        Teacher teacher = createAndPersistTeacher(school);
        AcademicYear year = createAndPersistAcademicYear("2025/26");

        CreditHourTracking tracking = new CreditHourTracking();
        tracking.setTeacher(teacher);
        tracking.setAcademicYear(year);
        tracking.setAssignmentsCount(1);
        tracking.setCreditHoursAllocated(10.0);
        tracking.setCreditBalance(90.0);
        tracking.setNotes("initial");

        return creditRepo.save(tracking);
    }

    private School createAndPersistSchool() {
        School school = new School();
        school.setSchoolName("Test School");
        school.setSchoolType(School.SchoolType.PRIMARY);
        school.setZoneNumber(1);
        return schoolRepo.save(school);
    }

    private Teacher createAndPersistTeacher(School school) {
        Teacher teacher = new Teacher();
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        teacher.setSchool(school);
        teacher.setEmail("john.doe@example.test");
        teacher.setEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);
        return teacherRepo.save(teacher);
    }

    private AcademicYear createAndPersistAcademicYear(String yearName) {
        AcademicYear year = new AcademicYear();
        year.setYearName(yearName);
        year.setTotalCreditHours(100);
        year.setElementarySchoolHours(10);
        year.setMiddleSchoolHours(20);
        year.setBudgetAnnouncementDate(LocalDateTime.now());
        year.setCreatedAt(LocalDateTime.now());
        return yearRepo.save(year);
    }

    @Test
    void listUnauthorizedShouldFail() throws Exception {
        mockMvc.perform(get("/api/credit-hour-tracking")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getByIdSuccess() throws Exception {
        mockMvc.perform(get("/api/credit-hour-tracking/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.data.notes", is("initial")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSuccess() throws Exception {
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
    void getAllSuccess() throws Exception {
        mockMvc.perform(get("/api/credit-hour-tracking")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", not(empty())))
                .andExpect(jsonPath("$.data[0].id", is(saved.getId().intValue())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void paginateSuccess() throws Exception {
        mockMvc.perform(get("/api/credit-hour-tracking/paginate")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", not(empty())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSuccess() throws Exception {
        AcademicYear newYear = createAndPersistAcademicYear("2026/27");

        CreditHourTrackingCreateDto dto = new CreditHourTrackingCreateDto();
        dto.setTeacherId(saved.getTeacher().getId());
        dto.setAcademicYearId(newYear.getId());
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
    void deleteSuccess() throws Exception {
        mockMvc.perform(delete("/api/credit-hour-tracking/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
