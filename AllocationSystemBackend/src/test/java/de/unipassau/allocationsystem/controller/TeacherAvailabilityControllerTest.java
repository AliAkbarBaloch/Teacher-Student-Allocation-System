package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityCreateDto;
import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityUpdateDto;
import de.unipassau.allocationsystem.entity.*;
import de.unipassau.allocationsystem.entity.School.SchoolType;
import de.unipassau.allocationsystem.entity.Teacher.EmploymentStatus;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TeacherAvailabilityController.
 * Removed unnecessary role-only tests; kept core CRUD + paginate tests.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
class TeacherAvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TeacherAvailabilityRepository teacherAvailabilityRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private InternshipTypeRepository internshipTypeRepository;

    private School testSchool;
    private Teacher testTeacher;
    private AcademicYear testYear;
    private InternshipType testInternshipType;
    private TeacherAvailability testAvailability;

    @BeforeEach
    void setUp() {
        // clear repositories to avoid conflicts between tests
        teacherAvailabilityRepository.deleteAll();
        teacherRepository.deleteAll();
        schoolRepository.deleteAll();
        academicYearRepository.deleteAll();
        internshipTypeRepository.deleteAll();

        // create school
        testSchool = new School();
        testSchool.setSchoolName("Test School");
        testSchool.setSchoolType(SchoolType.PRIMARY);
        testSchool.setZoneNumber(1);
        testSchool.setAddress("Test Street 1");
        testSchool.setContactEmail("school@test.de");
        testSchool.setIsActive(true);
        testSchool = schoolRepository.save(testSchool);

        // create teacher -- include required validation fields
        testTeacher = new Teacher();
        testTeacher.setFirstName("John");
        testTeacher.setLastName("Doe");
        testTeacher.setEmail("john.doe@example.com"); // required
        testTeacher.setEmploymentStatus(EmploymentStatus.FULL_TIME);
        testTeacher.setSchool(testSchool);
        testTeacher.setIsActive(true);
        testTeacher = teacherRepository.save(testTeacher);

        // create academic year with required fields to satisfy validation
        testYear = new AcademicYear();
        testYear.setYearName("2024/2025");
        testYear.setBudgetAnnouncementDate(LocalDateTime.now()); // required
        testYear.setElementarySchoolHours(180); // required
        testYear.setMiddleSchoolHours(200); // required
        testYear.setTotalCreditHours(380); // required
        testYear = academicYearRepository.save(testYear);

        // create internship type
        testInternshipType = new InternshipType();
        testInternshipType.setInternshipCode("Practice");
        testInternshipType.setFullName("Practice Internship");
        testInternshipType = internshipTypeRepository.save(testInternshipType);

        // create an availability entry
        testAvailability = new TeacherAvailability();
        testAvailability.setTeacher(testTeacher);
        testAvailability.setAcademicYear(testYear);
        testAvailability.setInternshipType(testInternshipType);
        testAvailability.setIsAvailable(true);
        testAvailability.setPreferenceRank(1);
        testAvailability = teacherAvailabilityRepository.save(testAvailability);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPaginate_Success() throws Exception {
        mockMvc.perform(get("/api/teacher-availability/paginate")
                        .param("teacherId", String.valueOf(testTeacher.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_Success() throws Exception {
        mockMvc.perform(get("/api/teacher-availability/{teacherId}/{availabilityId}",
                        testTeacher.getId(), testAvailability.getAvailabilityId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.teacherId").value(testTeacher.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_NotFound_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/teacher-availability/{teacherId}/{availabilityId}",
                        testTeacher.getId(), 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAvailability_Duplicate_ShouldFail() throws Exception {
        TeacherAvailabilityCreateDto dto = new TeacherAvailabilityCreateDto();
        dto.setTeacherId(testTeacher.getId());
        dto.setYearId(testYear.getId());
        dto.setInternshipTypeId(testInternshipType.getId());
        dto.setIsAvailable(true);

        mockMvc.perform(post("/api/teacher-availability/{teacherId}", testTeacher.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAvailability_Invalid_ShouldFail() throws Exception {
        // invalid: teacherId mismatch between path and body
        TeacherAvailabilityCreateDto dto = new TeacherAvailabilityCreateDto();
        dto.setTeacherId(testTeacher.getId() + 1);
        dto.setYearId(testYear.getId());
        dto.setInternshipTypeId(testInternshipType.getId());
        dto.setIsAvailable(true);

        mockMvc.perform(post("/api/teacher-availability/{teacherId}", testTeacher.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAvailability_Success() throws Exception {
        TeacherAvailabilityUpdateDto dto = new TeacherAvailabilityUpdateDto();
        dto.setIsAvailable(false);
        dto.setPreferenceRank(null);
        dto.setNotes("Now unavailable");

        mockMvc.perform(put("/api/teacher-availability/{teacherId}/{availabilityId}",
                        testTeacher.getId(), testAvailability.getAvailabilityId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isAvailable").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAvailability_NotFound_ShouldFail() throws Exception {
        TeacherAvailabilityUpdateDto dto = new TeacherAvailabilityUpdateDto();
        dto.setIsAvailable(true);

        mockMvc.perform(put("/api/teacher-availability/{teacherId}/{availabilityId}",
                        testTeacher.getId(), 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAvailability_Success() throws Exception {
        mockMvc.perform(delete("/api/teacher-availability/{teacherId}/{availabilityId}",
                        testTeacher.getId(), testAvailability.getAvailabilityId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAvailability_NotFound_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/teacher-availability/{teacherId}/{availabilityId}",
                        testTeacher.getId(), 99999L))
                .andExpect(status().isNotFound());
    }
}
