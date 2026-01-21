package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionCreateDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionStatusUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherFormSubmission;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.repository.TeacherFormSubmissionRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link TeacherFormSubmissionController}.
 * <p>
 * This test class validates form submission CRUD operations, status updates,
 * and filtering for teacher form submissions.
 * </p>
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc
@Transactional
class TeacherFormSubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TeacherFormSubmissionRepository teacherFormSubmissionRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    private Teacher teacher;
    private AcademicYear academicYear;
    private TeacherFormSubmission testSubmission;

    @BeforeEach
    void setUp() {
        // Clean up
        teacherFormSubmissionRepository.deleteAll();

        // Create test school (required by Teacher)
        School school = new School();
        school.setSchoolName("Test School");
        school.setSchoolType(School.SchoolType.MIDDLE);
        school.setZoneNumber(1);
        school.setAddress("123 Test St");
        school.setContactPhone("+49123456789");
        school.setContactEmail("test@school.com");
        school.setIsActive(true);
        school = schoolRepository.save(school);

        // Create test teacher
        teacher = new Teacher();
        teacher.setSchool(school);
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        teacher.setEmail("john.doe@test.com");
        teacher.setIsPartTime(false);
        teacher.setEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);
        teacher = teacherRepository.save(teacher);

        // Create test academic year
        academicYear = new AcademicYear();
        academicYear.setYearName("2024/2025");
        academicYear.setTotalCreditHours(100);
        academicYear.setElementarySchoolHours(20);
        academicYear.setMiddleSchoolHours(25);
        academicYear.setBudgetAnnouncementDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        academicYear.setIsLocked(false);
        academicYear = academicYearRepository.save(academicYear);

        // Create test submission
        testSubmission = new TeacherFormSubmission();
        testSubmission.setTeacher(teacher);
        testSubmission.setAcademicYear(academicYear);
        testSubmission.setFormToken("test-token-123");
        testSubmission.setSubmittedAt(LocalDateTime.now());
        testSubmission.setIsProcessed(false);
        testSubmission = teacherFormSubmissionRepository.save(testSubmission);
    }

    // GET ALL Tests
    @Test
    @DisplayName("Should get all form submissions as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllFormSubmissionsAsAdmin() throws Exception {
        mockMvc.perform(get("/api/teacher-form-submissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].formToken").value("test-token-123"));
    }

    @Test
    @DisplayName("Should get form submissions filtered by teacher ID")
    @WithMockUser(roles = "ADMIN")
    void shouldGetFormSubmissionsFilteredByTeacherId() throws Exception {
        mockMvc.perform(get("/api/teacher-form-submissions")
                        .param("teacherId", teacher.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].teacherId").value(teacher.getId()));
    }

    @Test
    @DisplayName("Should get form submissions filtered by year ID")
    @WithMockUser(roles = "ADMIN")
    void shouldGetFormSubmissionsFilteredByYearId() throws Exception {
        mockMvc.perform(get("/api/teacher-form-submissions")
                        .param("yearId", academicYear.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].yearId").value(academicYear.getId()));
    }

    @Test
    @DisplayName("Should get form submissions filtered by processed status")
    @WithMockUser(roles = "ADMIN")
    void shouldGetFormSubmissionsFilteredByProcessedStatus() throws Exception {
        mockMvc.perform(get("/api/teacher-form-submissions")
                        .param("isProcessed", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].isProcessed").value(false));
    }

    @Test
    @DisplayName("Should fail to get form submissions without authentication")
    void shouldFailToGetFormSubmissionsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/teacher-form-submissions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail to get form submissions as USER role")
    @WithMockUser(roles = "USER")
    void shouldFailToGetFormSubmissionsAsUser() throws Exception {
        mockMvc.perform(get("/api/teacher-form-submissions"))
                .andExpect(status().isForbidden());
    }

    // GET BY ID Tests
    @Test
    @DisplayName("Should get form submission by ID as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldGetFormSubmissionByIdAsAdmin() throws Exception {
        mockMvc.perform(get("/api/teacher-form-submissions/{id}", testSubmission.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testSubmission.getId()))
                .andExpect(jsonPath("$.data.formToken").value("test-token-123"))
                .andExpect(jsonPath("$.data.teacherFirstName").value("John"))
                .andExpect(jsonPath("$.data.yearName").value("2024/2025"));
    }

    @Test
    @DisplayName("Should return 404 when form submission not found")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenFormSubmissionNotFound() throws Exception {
        mockMvc.perform(get("/api/teacher-form-submissions/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Form submission not found with ID: 99999"));
    }

    @Test
    @DisplayName("Should fail to get form submission by ID without authentication")
    void shouldFailToGetFormSubmissionByIdWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/teacher-form-submissions/{id}", testSubmission.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail to get form submission by ID as USER role")
    @WithMockUser(roles = "USER")
    void shouldFailToGetFormSubmissionByIdAsUser() throws Exception {
        mockMvc.perform(get("/api/teacher-form-submissions/{id}", testSubmission.getId()))
                .andExpect(status().isForbidden());
    }

    // CREATE Tests
    @Test
    @DisplayName("Should create form submission as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateFormSubmissionAsAdmin() throws Exception {
        TeacherFormSubmissionCreateDto createDto = new TeacherFormSubmissionCreateDto();
        createDto.setTeacherId(teacher.getId());
        createDto.setYearId(academicYear.getId());
        createDto.setFormToken("new-token-456");
        createDto.setSubmittedAt(LocalDateTime.now());

        mockMvc.perform(post("/api/teacher-form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.formToken").value("new-token-456"))
                .andExpect(jsonPath("$.data.isProcessed").value(false));
    }

    @Test
    @DisplayName("Should fail to create form submission with missing fields")
    @WithMockUser(roles = "ADMIN")
    void shouldFailToCreateFormSubmissionWithMissingFields() throws Exception {
        TeacherFormSubmissionCreateDto createDto = new TeacherFormSubmissionCreateDto();
        // Missing required fields

        mockMvc.perform(post("/api/teacher-form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail to create form submission with non-existent teacher")
    @WithMockUser(roles = "ADMIN")
    void shouldFailToCreateFormSubmissionWithNonExistentTeacher() throws Exception {
        TeacherFormSubmissionCreateDto createDto = new TeacherFormSubmissionCreateDto();
        createDto.setTeacherId(99999L);
        createDto.setYearId(academicYear.getId());
        createDto.setFormToken("token-789");
        createDto.setSubmittedAt(LocalDateTime.now());

        mockMvc.perform(post("/api/teacher-form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Teacher not found")));
    }

    @Test
    @DisplayName("Should fail to create form submission with non-existent academic year")
    @WithMockUser(roles = "ADMIN")
    void shouldFailToCreateFormSubmissionWithNonExistentYear() throws Exception {
        TeacherFormSubmissionCreateDto createDto = new TeacherFormSubmissionCreateDto();
        createDto.setTeacherId(teacher.getId());
        createDto.setYearId(99999L);
        createDto.setFormToken("token-789");
        createDto.setSubmittedAt(LocalDateTime.now());

        mockMvc.perform(post("/api/teacher-form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Academic year not found")));
    }

    @Test
    @DisplayName("Should fail to create form submission with duplicate token")
    @WithMockUser(roles = "ADMIN")
    void shouldFailToCreateFormSubmissionWithDuplicateToken() throws Exception {
        TeacherFormSubmissionCreateDto createDto = new TeacherFormSubmissionCreateDto();
        createDto.setTeacherId(teacher.getId());
        createDto.setYearId(academicYear.getId());
        createDto.setFormToken("test-token-123"); // Duplicate token
        createDto.setSubmittedAt(LocalDateTime.now());

        mockMvc.perform(post("/api/teacher-form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Form token already exists")));
    }

    @Test
    @DisplayName("Should fail to create form submission for locked year")
    @WithMockUser(roles = "ADMIN")
    void shouldFailToCreateFormSubmissionForLockedYear() throws Exception {
        // Lock the academic year
        academicYear.setIsLocked(true);
        academicYearRepository.save(academicYear);

        TeacherFormSubmissionCreateDto createDto = new TeacherFormSubmissionCreateDto();
        createDto.setTeacherId(teacher.getId());
        createDto.setYearId(academicYear.getId());
        createDto.setFormToken("token-999");
        createDto.setSubmittedAt(LocalDateTime.now());

        mockMvc.perform(post("/api/teacher-form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Cannot create submission for locked academic year")));
    }

    @Test
    @DisplayName("Should fail to create form submission without authentication")
    void shouldFailToCreateFormSubmissionWithoutAuth() throws Exception {
        TeacherFormSubmissionCreateDto createDto = new TeacherFormSubmissionCreateDto();
        createDto.setTeacherId(teacher.getId());
        createDto.setYearId(academicYear.getId());
        createDto.setFormToken("token-789");
        createDto.setSubmittedAt(LocalDateTime.now());

        mockMvc.perform(post("/api/teacher-form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail to create form submission as USER role")
    @WithMockUser(roles = "USER")
    void shouldFailToCreateFormSubmissionAsUser() throws Exception {
        TeacherFormSubmissionCreateDto createDto = new TeacherFormSubmissionCreateDto();
        createDto.setTeacherId(teacher.getId());
        createDto.setYearId(academicYear.getId());
        createDto.setFormToken("token-789");
        createDto.setSubmittedAt(LocalDateTime.now());

        mockMvc.perform(post("/api/teacher-form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    // UPDATE STATUS Tests
    @Test
    @DisplayName("Should update form submission status as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateFormSubmissionStatusAsAdmin() throws Exception {
        TeacherFormSubmissionStatusUpdateDto updateDto = new TeacherFormSubmissionStatusUpdateDto();
        updateDto.setIsProcessed(true);

        mockMvc.perform(patch("/api/teacher-form-submissions/{id}/status", testSubmission.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testSubmission.getId()))
                .andExpect(jsonPath("$.data.isProcessed").value(true));
    }

    @Test
    @DisplayName("Should fail to update status for non-existent submission")
    @WithMockUser(roles = "ADMIN")
    void shouldFailToUpdateStatusForNonExistentSubmission() throws Exception {
        TeacherFormSubmissionStatusUpdateDto updateDto = new TeacherFormSubmissionStatusUpdateDto();
        updateDto.setIsProcessed(true);

        mockMvc.perform(patch("/api/teacher-form-submissions/{id}/status", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Form submission not found")));
    }

    @Test
    @DisplayName("Should fail to update status with missing fields")
    @WithMockUser(roles = "ADMIN")
    void shouldFailToUpdateStatusWithMissingFields() throws Exception {
        TeacherFormSubmissionStatusUpdateDto updateDto = new TeacherFormSubmissionStatusUpdateDto();
        // Missing required field

        mockMvc.perform(patch("/api/teacher-form-submissions/{id}/status", testSubmission.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail to update status without authentication")
    void shouldFailToUpdateStatusWithoutAuth() throws Exception {
        TeacherFormSubmissionStatusUpdateDto updateDto = new TeacherFormSubmissionStatusUpdateDto();
        updateDto.setIsProcessed(true);

        mockMvc.perform(patch("/api/teacher-form-submissions/{id}/status", testSubmission.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail to update status as USER role")
    @WithMockUser(roles = "USER")
    void shouldFailToUpdateStatusAsUser() throws Exception {
        TeacherFormSubmissionStatusUpdateDto updateDto = new TeacherFormSubmissionStatusUpdateDto();
        updateDto.setIsProcessed(true);

        mockMvc.perform(patch("/api/teacher-form-submissions/{id}/status", testSubmission.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }
}
