package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintCreateDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintUpdateDto;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.ZoneConstraint;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.ZoneConstraintRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for ZoneConstraintController.
 * Tests all REST endpoints with security, validation, and error scenarios.
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ZoneConstraintControllerTest {

    private static final String BASE_URL = "/api/zone-constraints";
    private static final String TYPE_CODE = "TEST-INT-01";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final ZoneConstraintRepository zoneConstraintRepository;
    private final InternshipTypeRepository internshipTypeRepository;

    private InternshipType testInternshipType;
    private ZoneConstraint testConstraint;

        @Autowired
        ZoneConstraintControllerTest(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            ZoneConstraintRepository zoneConstraintRepository,
            InternshipTypeRepository internshipTypeRepository
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.zoneConstraintRepository = zoneConstraintRepository;
        this.internshipTypeRepository = internshipTypeRepository;
    }

    @BeforeEach
    void setUp() {
        zoneConstraintRepository.deleteAll();
        testInternshipType = internshipTypeRepository.save(buildInternshipType());
        testConstraint = zoneConstraintRepository.save(buildConstraint(testInternshipType));
    }

    private InternshipType buildInternshipType() {
        InternshipType it = new InternshipType();
        it.setInternshipCode(TYPE_CODE);
        it.setFullName("Test Internship Type");
        it.setIsSubjectSpecific(false);
        it.setSemester(1);
        return it;
    }

    private ZoneConstraint buildConstraint(InternshipType internshipType) {
        ZoneConstraint c = new ZoneConstraint();
        c.setZoneNumber(1);
        c.setInternshipType(internshipType);
        c.setIsAllowed(true);
        c.setDescription("Test constraint for zone 1");
        return c;
    }

    private ResultActions getAll() throws Exception {
        return mockMvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions getPaginated(String searchValue) throws Exception {
        var req = get(BASE_URL + "/paginate")
                .param("page", "1")
                .param("pageSize", "10")
                .param("sortBy", "id")
                .param("sortOrder", "asc");
        if (searchValue != null) {
            req = req.param("searchValue", searchValue);
        }
        return mockMvc.perform(req.accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions getById(long id) throws Exception {
        return mockMvc.perform(get(BASE_URL + "/{id}", id).accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions create(ZoneConstraintCreateDto dto) throws Exception {
        return mockMvc.perform(post(BASE_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    private ResultActions update(long id, ZoneConstraintUpdateDto dto) throws Exception {
        return mockMvc.perform(put(BASE_URL + "/{id}", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    private ResultActions remove(long id) throws Exception {
        return mockMvc.perform(delete(BASE_URL + "/{id}", id).with(csrf()));
    }

    // -------------------- GET ALL --------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllZoneConstraintsSuccess() throws Exception {
        getAll()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Zone constraints retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].zoneNumber", is(1)))
                .andExpect(jsonPath("$.data[0].isAllowed", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllZoneConstraintsPaginated() throws Exception {
        getPaginated(null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].zoneNumber", is(1)))
                .andExpect(jsonPath("$.data.totalItems", is(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllZoneConstraintsWithSearch() throws Exception {
        getPaginated("1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].zoneNumber", is(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSortFieldsSuccess() throws Exception {
        mockMvc.perform(get(BASE_URL + "/sort-fields"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getAllZoneConstraintsUnauthorized() throws Exception {
        getAll().andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllZoneConstraintsUserRole() throws Exception {
        getAll()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    // -------------------- GET BY ID --------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void getZoneConstraintByIdSuccess() throws Exception {
        getById(testConstraint.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Zone constraint retrieved successfully")))
                .andExpect(jsonPath("$.data.id", is(testConstraint.getId().intValue())))
                .andExpect(jsonPath("$.data.zoneNumber", is(1)))
                .andExpect(jsonPath("$.data.internshipTypeCode", is(TYPE_CODE)))
                .andExpect(jsonPath("$.data.isAllowed", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getZoneConstraintByIdNotFound() throws Exception {
        getById(99999L).andExpect(status().isNotFound());
    }

    @Test
    void getZoneConstraintByIdUnauthorized() throws Exception {
        getById(testConstraint.getId()).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getZoneConstraintByIdUserRole() throws Exception {
        getById(testConstraint.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    // -------------------- CREATE --------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void createZoneConstraintSuccess() throws Exception {
        ZoneConstraintCreateDto dto = new ZoneConstraintCreateDto();
        dto.setZoneNumber(2);
        dto.setInternshipTypeId(testInternshipType.getId());
        dto.setIsAllowed(false);
        dto.setDescription("Zone 2 constraint");

        create(dto)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Zone constraint created successfully")))
                .andExpect(jsonPath("$.data.zoneNumber", is(2)))
                .andExpect(jsonPath("$.data.isAllowed", is(false)))
                .andExpect(jsonPath("$.data.description", is("Zone 2 constraint")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createZoneConstraintMissingZoneNumber() throws Exception {
        ZoneConstraintCreateDto dto = new ZoneConstraintCreateDto();
        dto.setInternshipTypeId(testInternshipType.getId());
        dto.setIsAllowed(true);

        create(dto).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createZoneConstraintNegativeZoneNumber() throws Exception {
        ZoneConstraintCreateDto dto = new ZoneConstraintCreateDto();
        dto.setZoneNumber(-1);
        dto.setInternshipTypeId(testInternshipType.getId());
        dto.setIsAllowed(true);

        create(dto).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createZoneConstraintMissingInternshipTypeId() throws Exception {
        ZoneConstraintCreateDto dto = new ZoneConstraintCreateDto();
        dto.setZoneNumber(2);
        dto.setIsAllowed(true);

        create(dto).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createZoneConstraintMissingIsAllowed() throws Exception {
        ZoneConstraintCreateDto dto = new ZoneConstraintCreateDto();
        dto.setZoneNumber(2);
        dto.setInternshipTypeId(testInternshipType.getId());

        create(dto).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createZoneConstraintDuplicateConstraint() throws Exception {
        ZoneConstraintCreateDto dto = new ZoneConstraintCreateDto();
        dto.setZoneNumber(1);
        dto.setInternshipTypeId(testInternshipType.getId());
        dto.setIsAllowed(false);

        create(dto).andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createZoneConstraintInvalidInternshipType() throws Exception {
        ZoneConstraintCreateDto dto = new ZoneConstraintCreateDto();
        dto.setZoneNumber(2);
        dto.setInternshipTypeId(99999L);
        dto.setIsAllowed(true);

        create(dto).andExpect(status().isNotFound());
    }

    @Test
    void createZoneConstraintUnauthorized() throws Exception {
        ZoneConstraintCreateDto dto = new ZoneConstraintCreateDto();
        dto.setZoneNumber(2);
        dto.setInternshipTypeId(testInternshipType.getId());
        dto.setIsAllowed(true);

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createZoneConstraintUserRole() throws Exception {
        ZoneConstraintCreateDto dto = new ZoneConstraintCreateDto();
        dto.setZoneNumber(2);
        dto.setInternshipTypeId(testInternshipType.getId());
        dto.setIsAllowed(true);

        create(dto)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)));
    }

    // -------------------- UPDATE --------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateZoneConstraintSuccess() throws Exception {
        ZoneConstraintUpdateDto dto = new ZoneConstraintUpdateDto();
        dto.setIsAllowed(false);
        dto.setDescription("Updated description");

        update(testConstraint.getId(), dto)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Zone constraint updated successfully")))
                .andExpect(jsonPath("$.data.isAllowed", is(false)))
                .andExpect(jsonPath("$.data.description", is("Updated description")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateZoneConstraintPartialUpdate() throws Exception {
        ZoneConstraintUpdateDto dto = new ZoneConstraintUpdateDto();
        dto.setZoneNumber(5);

        update(testConstraint.getId(), dto)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.zoneNumber", is(5)))
                .andExpect(jsonPath("$.data.isAllowed", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateZoneConstraintNotFound() throws Exception {
        ZoneConstraintUpdateDto dto = new ZoneConstraintUpdateDto();
        dto.setIsAllowed(false);

        update(99999L, dto).andExpect(status().isNotFound());
    }

    @Test
    void updateZoneConstraintUnauthorized() throws Exception {
        ZoneConstraintUpdateDto dto = new ZoneConstraintUpdateDto();
        dto.setIsAllowed(false);

        mockMvc.perform(put(BASE_URL + "/{id}", testConstraint.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateZoneConstraintUserRole() throws Exception {
        ZoneConstraintUpdateDto dto = new ZoneConstraintUpdateDto();
        dto.setIsAllowed(false);

        update(testConstraint.getId(), dto)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    // -------------------- DELETE --------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteZoneConstraintSuccess() throws Exception {
        remove(testConstraint.getId()).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteZoneConstraintNotFound() throws Exception {
        remove(99999L).andExpect(status().isNotFound());
    }

    @Test
    void deleteZoneConstraintUnauthorized() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", testConstraint.getId()).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteZoneConstraintUserRole() throws Exception {
        remove(testConstraint.getId()).andExpect(status().isNoContent());
    }
}
