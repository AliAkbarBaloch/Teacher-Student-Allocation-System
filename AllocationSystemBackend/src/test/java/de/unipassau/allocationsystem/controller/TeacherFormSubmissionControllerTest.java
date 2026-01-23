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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private static final String BASE_URL = "/api/teacher-form-submissions";
    private static final String TOKEN_EXISTING = "test-token-123";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final TeacherFormSubmissionRepository submissionRepo;
    private final TeacherRepository teacherRepo;
    private final AcademicYearRepository yearRepo;
    private final SchoolRepository schoolRepo;

    private Teacher teacher;
    private AcademicYear academicYear;
    private TeacherFormSubmission testSubmission;

    TeacherFormSubmissionControllerTest(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            TeacherFormSubmissionRepository submissionRepo,
            TeacherRepository teacherRepo,
            AcademicYearRepository yearRepo,
            SchoolRepository schoolRepo
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.submissionRepo = submissionRepo;
        this.teacherRepo = teacherRepo;
        this.yearRepo = yearRepo;
        this.schoolRepo = schoolRepo;
    }

    @BeforeEach
    void setUp() {
        submissionRepo.deleteAll();
        School school = schoolRepo.save(buildSchool());
        teacher = teacherRepo.save(buildTeacher(school));
        academicYear = yearRepo.save(buildAcademicYear());
        testSubmission = submissionRepo.save(buildSubmission(teacher, academicYear, TOKEN_EXISTING));
    }

    private School buildSchool() {
        School school = new School();
        school.setSchoolName("Test School");
        school.setSchoolType(School.SchoolType.MIDDLE);
        school.setZoneNumber(1);
        school.setAddress("123 Test St");
        school.setContactPhone("+49123456789");
        school.setContactEmail("test@school.com");
        school.setIsActive(true);
        return school;
    }

    private Teacher buildTeacher(School school) {
        Teacher t = new Teacher();
        t.setSchool(school);
        t.setFirstName("John");
        t.setLastName("Doe");
        t.setEmail("john.doe+" + System.nanoTime() + "@test.com");
        t.setIsPartTime(false);
        t.setEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);
        return t;
    }

    private AcademicYear buildAcademicYear() {
        AcademicYear y = new AcademicYear();
        y.setYearName("2024/2025-" + System.nanoTime());
        y.setTotalCreditHours(100);
        y.setElementarySchoolHours(20);
        y.setMiddleSchoolHours(25);
        y.setBudgetAnnouncementDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        y.setIsLocked(false);
        return y;
    }

    private TeacherFormSubmission buildSubmission(Teacher t, AcademicYear y, String token) {
        TeacherFormSubmission s = new TeacherFormSubmission();
        s.setTeacher(t);
        s.setAcademicYear(y);
        s.setFormToken(token);
        s.setSubmittedAt(LocalDateTime.now());
        s.setIsProcessed(false);
        return s;
    }

    // ==================== GET ALL ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllAsAdmin() throws Exception {
        performGet(BASE_URL)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].formToken").value(TOKEN_EXISTING));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllFilteredByTeacherId() throws Exception {
        performGetWithParam("teacherId", teacher.getId().toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].teacherId").value(teacher.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllFilteredByYearId() throws Exception {
        performGetWithParam("yearId", academicYear.getId().toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].yearId").value(academicYear.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllFilteredByProcessedStatus() throws Exception {
        performGetWithParam("isProcessed", "false")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].isProcessed").value(false));
    }

    @Test
    void getAllWithoutAuthShouldFail() throws Exception {
        performGet(BASE_URL)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllAsUserShouldFail() throws Exception {
        performGet(BASE_URL)
                .andExpect(status().isForbidden());
    }

    // ==================== GET BY ID ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getByIdAsAdmin() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", testSubmission.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testSubmission.getId()))
                .andExpect(jsonPath("$.data.formToken").value(TOKEN_EXISTING))
                .andExpect(jsonPath("$.data.teacherFirstName").value("John"))
                .andExpect(jsonPath("$.data.yearName").value(containsString("2024/2025")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getByIdNotFoundShouldReturn404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Form submission not found with ID: 99999"));
    }

    @Test
    void getByIdWithoutAuthShouldFail() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", testSubmission.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getByIdAsUserShouldFail() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", testSubmission.getId()))
                .andExpect(status().isForbidden());
    }

    // ==================== CREATE ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAsAdminShouldSucceed() throws Exception {
        TeacherFormSubmissionCreateDto dto = createDto("new-token-456");
        performPost(dto)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.formToken").value("new-token-456"))
                .andExpect(jsonPath("$.data.isProcessed").value(false))
                .andExpect(jsonPath("$.data.id").value(notNullValue()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createWithMissingFieldsShouldFail() throws Exception {
        performPost(new TeacherFormSubmissionCreateDto())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createWithNonExistentTeacherShouldFail() throws Exception {
        TeacherFormSubmissionCreateDto dto = createDto("token-789");
        dto.setTeacherId(99999L);

        performPost(dto)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Teacher not found")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createWithNonExistentYearShouldFail() throws Exception {
        TeacherFormSubmissionCreateDto dto = createDto("token-789");
        dto.setYearId(99999L);

        performPost(dto)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Academic year not found")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createWithDuplicateTokenShouldFail() throws Exception {
        performPost(createDto(TOKEN_EXISTING))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Form token already exists")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createForLockedYearShouldFail() throws Exception {
        academicYear.setIsLocked(true);
        yearRepo.save(academicYear);

        performPost(createDto("token-999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(containsString("Cannot create submission for locked academic year")));
    }

    @Test
    void createWithoutAuthShouldFail() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto("token-789"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createAsUserShouldFail() throws Exception {
        performPost(createDto("token-789"))
                .andExpect(status().isForbidden());
    }

    // ==================== UPDATE STATUS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatusAsAdminShouldSucceed() throws Exception {
        performPatchStatus(testSubmission.getId(), true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testSubmission.getId()))
                .andExpect(jsonPath("$.data.isProcessed").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatusForNonExistentSubmissionShouldFail() throws Exception {
        performPatchStatus(99999L, true)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Form submission not found")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatusWithMissingFieldsShouldFail() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}/status", testSubmission.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TeacherFormSubmissionStatusUpdateDto())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatusWithoutAuthShouldFail() throws Exception {
        TeacherFormSubmissionStatusUpdateDto dto = new TeacherFormSubmissionStatusUpdateDto();
        dto.setIsProcessed(true);

        mockMvc.perform(patch(BASE_URL + "/{id}/status", testSubmission.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateStatusAsUserShouldFail() throws Exception {
        performPatchStatus(testSubmission.getId(), true)
                .andExpect(status().isForbidden());
    }

    private ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performGetWithParam(String key, String value) throws Exception {
        return mockMvc.perform(get(BASE_URL).param(key, value).accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performPost(Object body) throws Exception {
        return mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions performPatchStatus(long id, boolean processed) throws Exception {
        TeacherFormSubmissionStatusUpdateDto dto = new TeacherFormSubmissionStatusUpdateDto();
        dto.setIsProcessed(processed);
        return mockMvc.perform(patch(BASE_URL + "/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    private TeacherFormSubmissionCreateDto createDto(String token) {
        TeacherFormSubmissionCreateDto dto = new TeacherFormSubmissionCreateDto();
        dto.setTeacherId(teacher.getId());
        dto.setYearId(academicYear.getId());
        dto.setFormToken(token);
        dto.setSubmittedAt(LocalDateTime.now());
        return dto;
    }
}
