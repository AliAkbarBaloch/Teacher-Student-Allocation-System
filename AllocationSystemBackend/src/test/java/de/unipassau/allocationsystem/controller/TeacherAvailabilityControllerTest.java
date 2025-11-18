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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TeacherAvailabilityController.
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
        // Clean up in correct order (foreign key dependencies)
        teacherAvailabilityRepository.deleteAll();
        teacherRepository.deleteAll();
        schoolRepository.deleteAll();
        // Note: Don't delete academic_years and internship_types as they might be seeded

        // Create test school
        testSchool = new School();
        testSchool.setSchoolName("Test School");
        testSchool.setSchoolType(SchoolType.PRIMARY);
        testSchool.setZoneNumber(1);
        testSchool.setIsActive(true);
        testSchool = schoolRepository.save(testSchool);

        // Create test teacher
        testTeacher = new Teacher();
        testTeacher.setSchool(testSchool);
        testTeacher.setFirstName("John");
        testTeacher.setLastName("Doe");
        testTeacher.setEmail("john.doe@availability.test");
        testTeacher.setPhone("+49841123456");
        testTeacher.setIsPartTime(false);
        testTeacher.setEmploymentStatus(EmploymentStatus.FULL_TIME);
        testTeacher.setIsActive(true);
        testTeacher = teacherRepository.save(testTeacher);

        // Get or create test academic year
        testYear = academicYearRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    AcademicYear year = new AcademicYear();
                    year.setYearName("2024/2025");
                    year.setTotalCreditHours(500);
                    year.setElementarySchoolHours(200);
                    year.setMiddleSchoolHours(300);
                    year.setBudgetAnnouncementDate(java.time.LocalDateTime.now());
                    return academicYearRepository.save(year);
                });

        // Get or create test internship type
        testInternshipType = internshipTypeRepository.findByInternshipCode("SFP")
                .orElseGet(() -> {
                    InternshipType type = new InternshipType();
                    type.setInternshipCode("SFP");
                    type.setFullName("Schulisches Fachpraktikum");
                    type.setTiming("Block");
                    type.setPeriodType("Continuous");
                    type.setSemester("Winter");
                    type.setIsSubjectSpecific(true);
                    type.setPriorityOrder(1);
                    return internshipTypeRepository.save(type);
                });

        // Create test availability
        testAvailability = new TeacherAvailability();
        testAvailability.setTeacher(testTeacher);
        testAvailability.setAcademicYear(testYear);
        testAvailability.setInternshipType(testInternshipType);
        testAvailability.setIsAvailable(true);
        testAvailability.setPreferenceRank(1);
        testAvailability.setNotes("Available all semester");
        testAvailability = teacherAvailabilityRepository.save(testAvailability);
    }

    // ==================== GET /teachers/{teacherId}/availability ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeacherAvailability_Success() throws Exception {
        mockMvc.perform(get("/api/teachers/{teacherId}/availability", testTeacher.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Teacher availability retrieved successfully (paginated)"))
                .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.items[0].availabilityId").exists())
                .andExpect(jsonPath("$.data.items[0].teacherFirstName").value("John"))
                .andExpect(jsonPath("$.data.items[0].teacherLastName").value("Doe"))
                .andExpect(jsonPath("$.data.items[0].isAvailable").value(true))
                .andExpect(jsonPath("$.data.totalItems").exists())
                .andExpect(jsonPath("$.data.totalPages").exists())
                .andExpect(jsonPath("$.data.page").exists())
                .andExpect(jsonPath("$.data.pageSize").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeacherAvailability_WithYearFilter_Success() throws Exception {
        mockMvc.perform(get("/api/teachers/{teacherId}/availability", testTeacher.getId())
                .param("yearId", testYear.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeacherAvailability_WithInternshipTypeFilter_Success() throws Exception {
        mockMvc.perform(get("/api/teachers/{teacherId}/availability", testTeacher.getId())
                .param("internshipTypeId", testInternshipType.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeacherAvailability_WithAllFilters_Success() throws Exception {
        mockMvc.perform(get("/api/teachers/{teacherId}/availability", testTeacher.getId())
                .param("yearId", testYear.getId().toString())
                .param("internshipTypeId", testInternshipType.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeacherAvailability_TeacherNotFound_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/teachers/{teacherId}/availability", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTeacherAvailability_WithUserRole_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/teachers/{teacherId}/availability", testTeacher.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTeacherAvailability_WithoutAuth_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/teachers/{teacherId}/availability", testTeacher.getId()))
                .andExpect(status().isUnauthorized());
    }

    // ==================== GET /teachers/{teacherId}/availability/{availabilityId} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAvailabilityById_Success() throws Exception {
        mockMvc.perform(get("/api/teachers/{teacherId}/availability/{availabilityId}",
                testTeacher.getId(), testAvailability.getAvailabilityId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Availability entry retrieved successfully"))
                .andExpect(jsonPath("$.data.availabilityId").value(testAvailability.getAvailabilityId()))
                .andExpect(jsonPath("$.data.teacherId").value(testTeacher.getId()))
                .andExpect(jsonPath("$.data.teacherFirstName").value("John"))
                .andExpect(jsonPath("$.data.teacherLastName").value("Doe"))
                .andExpect(jsonPath("$.data.yearName").value(testYear.getYearName()))
                .andExpect(jsonPath("$.data.internshipTypeName").value(testInternshipType.getFullName()))
                .andExpect(jsonPath("$.data.isAvailable").value(true))
                .andExpect(jsonPath("$.data.preferenceRank").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAvailabilityById_NotFound_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/teachers/{teacherId}/availability/{availabilityId}",
                testTeacher.getId(), 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAvailabilityById_WrongTeacher_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/teachers/{teacherId}/availability/{availabilityId}",
                99999L, testAvailability.getAvailabilityId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAvailabilityById_WithUserRole_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/teachers/{teacherId}/availability/{availabilityId}",
                testTeacher.getId(), testAvailability.getAvailabilityId()))
                .andExpect(status().isForbidden());
    }

    // ==================== POST /teachers/{teacherId}/availability ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAvailability_Success() throws Exception {
        // Create another internship type for this test
        InternshipType anotherType = internshipTypeRepository.findByInternshipCode("ZSP")
                .orElseGet(() -> {
                    InternshipType type = new InternshipType();
                    type.setInternshipCode("ZSP");
                    type.setFullName("Zusätzliches Schulpraktikum");
                    type.setTiming("Block");
                    type.setPeriodType("Continuous");
                    type.setSemester("Summer");
                    type.setIsSubjectSpecific(true);
                    type.setPriorityOrder(2);
                    return internshipTypeRepository.save(type);
                });

        TeacherAvailabilityCreateDto dto = new TeacherAvailabilityCreateDto();
        dto.setTeacherId(testTeacher.getId());
        dto.setYearId(testYear.getId());
        dto.setInternshipTypeId(anotherType.getId());
        dto.setIsAvailable(true);
        dto.setPreferenceRank(2);
        dto.setNotes("Available for ZSP");

        mockMvc.perform(post("/api/teachers/{teacherId}/availability", testTeacher.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Availability entry created successfully"))
                .andExpect(jsonPath("$.data.teacherId").value(testTeacher.getId()))
                .andExpect(jsonPath("$.data.isAvailable").value(true))
                .andExpect(jsonPath("$.data.preferenceRank").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAvailability_TeacherIdMismatch_ShouldFail() throws Exception {
        TeacherAvailabilityCreateDto dto = new TeacherAvailabilityCreateDto();
        dto.setTeacherId(999L); // Different from path parameter
        dto.setYearId(testYear.getId());
        dto.setInternshipTypeId(testInternshipType.getId());
        dto.setIsAvailable(true);

        mockMvc.perform(post("/api/teachers/{teacherId}/availability", testTeacher.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAvailability_DuplicateEntry_ShouldFail() throws Exception {
        TeacherAvailabilityCreateDto dto = new TeacherAvailabilityCreateDto();
        dto.setTeacherId(testTeacher.getId());
        dto.setYearId(testYear.getId());
        dto.setInternshipTypeId(testInternshipType.getId()); // Same combination as testAvailability
        dto.setIsAvailable(false);

        mockMvc.perform(post("/api/teachers/{teacherId}/availability", testTeacher.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAvailability_InactiveTeacher_ShouldFail() throws Exception {
        testTeacher.setIsActive(false);
        teacherRepository.save(testTeacher);

        TeacherAvailabilityCreateDto dto = new TeacherAvailabilityCreateDto();
        dto.setTeacherId(testTeacher.getId());
        dto.setYearId(testYear.getId());
        dto.setInternshipTypeId(testInternshipType.getId());
        dto.setIsAvailable(true);

        mockMvc.perform(post("/api/teachers/{teacherId}/availability", testTeacher.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAvailability_PreferenceRankWithNotAvailable_ShouldFail() throws Exception {
        TeacherAvailabilityCreateDto dto = new TeacherAvailabilityCreateDto();
        dto.setTeacherId(testTeacher.getId());
        dto.setYearId(testYear.getId());
        dto.setInternshipTypeId(testInternshipType.getId());
        dto.setIsAvailable(false); // Not available
        dto.setPreferenceRank(1); // But has preference rank (should be null)

        mockMvc.perform(post("/api/teachers/{teacherId}/availability", testTeacher.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAvailability_MissingRequiredFields_ShouldFail() throws Exception {
        TeacherAvailabilityCreateDto dto = new TeacherAvailabilityCreateDto();
        // Missing required fields

        mockMvc.perform(post("/api/teachers/{teacherId}/availability", testTeacher.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createAvailability_WithUserRole_ShouldFail() throws Exception {
        TeacherAvailabilityCreateDto dto = new TeacherAvailabilityCreateDto();
        dto.setTeacherId(testTeacher.getId());
        dto.setYearId(testYear.getId());
        dto.setInternshipTypeId(testInternshipType.getId());
        dto.setIsAvailable(true);

        mockMvc.perform(post("/api/teachers/{teacherId}/availability", testTeacher.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ==================== PUT /teachers/{teacherId}/availability/{availabilityId} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAvailability_Success() throws Exception {
        TeacherAvailabilityUpdateDto dto = new TeacherAvailabilityUpdateDto();
        dto.setIsAvailable(false);
        dto.setPreferenceRank(null); // Must be null when not available
        dto.setNotes("No longer available");

        mockMvc.perform(put("/api/teachers/{teacherId}/availability/{availabilityId}",
                testTeacher.getId(), testAvailability.getAvailabilityId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Availability entry updated successfully"))
                .andExpect(jsonPath("$.data.isAvailable").value(false))
                .andExpect(jsonPath("$.data.notes").value("No longer available"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAvailability_PartialUpdate_Success() throws Exception {
        TeacherAvailabilityUpdateDto dto = new TeacherAvailabilityUpdateDto();
        dto.setPreferenceRank(5); // Only update preference rank

        mockMvc.perform(put("/api/teachers/{teacherId}/availability/{availabilityId}",
                testTeacher.getId(), testAvailability.getAvailabilityId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.preferenceRank").value(5));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAvailability_NotFound_ShouldFail() throws Exception {
        TeacherAvailabilityUpdateDto dto = new TeacherAvailabilityUpdateDto();
        dto.setNotes("Updated");

        mockMvc.perform(put("/api/teachers/{teacherId}/availability/{availabilityId}",
                testTeacher.getId(), 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateAvailability_WithUserRole_ShouldFail() throws Exception {
        TeacherAvailabilityUpdateDto dto = new TeacherAvailabilityUpdateDto();
        dto.setNotes("Updated");

        mockMvc.perform(put("/api/teachers/{teacherId}/availability/{availabilityId}",
                testTeacher.getId(), testAvailability.getAvailabilityId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ==================== DELETE /teachers/{teacherId}/availability/{availabilityId} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAvailability_Success() throws Exception {
        mockMvc.perform(delete("/api/teachers/{teacherId}/availability/{availabilityId}",
                testTeacher.getId(), testAvailability.getAvailabilityId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        assert teacherAvailabilityRepository.findById(testAvailability.getAvailabilityId()).isEmpty();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAvailability_NotFound_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/teachers/{teacherId}/availability/{availabilityId}",
                testTeacher.getId(), 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAvailability_WrongTeacher_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/teachers/{teacherId}/availability/{availabilityId}",
                99999L, testAvailability.getAvailabilityId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteAvailability_WithUserRole_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/teachers/{teacherId}/availability/{availabilityId}",
                testTeacher.getId(), testAvailability.getAvailabilityId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteAvailability_WithoutAuth_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/teachers/{teacherId}/availability/{availabilityId}",
                testTeacher.getId(), testAvailability.getAvailabilityId()))
                .andExpect(status().isUnauthorized());
    }
}
