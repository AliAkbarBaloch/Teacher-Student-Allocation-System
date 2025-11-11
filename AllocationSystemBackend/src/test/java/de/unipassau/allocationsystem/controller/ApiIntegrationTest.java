package de.unipassau.allocationsystem.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
// Disable security filters for MockMvc in tests so we can exercise endpoints without authentication
@AutoConfigureMockMvc(addFilters = false)
class ApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void listRooms_returns200() throws Exception {
        mvc.perform(get("/api/v1/rooms").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void getStudent_notFound_returns404() throws Exception {
        mvc.perform(get("/api/v1/students/not-found").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getStudent_found_returns200() throws Exception {
        mvc.perform(get("/api/v1/students/stu-123").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("stu-123"));
    }

    @Test
    @WithMockUser
    void createAllocation_valid_returns201() throws Exception {
        String body = "{\"studentId\":\"stu-123\",\"roomId\":\"room-A\"}";
        mvc.perform(post("/api/v1/allocations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.studentId").value("stu-123"));
    }

    @Test
    @WithMockUser
    void createAllocation_invalid_returns400() throws Exception {
        String body = "{\"studentId\":\"\",\"roomId\":\"\"}";
        mvc.perform(post("/api/v1/allocations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }
}
