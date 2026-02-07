package com.hrd.asset_holder_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hrd.asset_holder_api.configuration.BeanConfig;
import com.hrd.asset_holder_api.configuration.SecurityConfig;
import com.hrd.asset_holder_api.jwt.JwtAuthEntrypoint;
import com.hrd.asset_holder_api.jwt.JwtAuthFilter;
import com.hrd.asset_holder_api.jwt.JwtUtil;
import com.hrd.asset_holder_api.model.entity.ReportIssue;
import com.hrd.asset_holder_api.service.AppUserService;
import com.hrd.asset_holder_api.service.ReportIssueService;
import com.hrd.asset_holder_api.testsupport.SecurityTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportIssueController.class)
@Import({SecurityConfig.class, BeanConfig.class, JwtAuthFilter.class, JwtAuthEntrypoint.class})
class ReportIssueControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ReportIssueService reportIssueService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private AppUserService appUserService;

    @BeforeEach
    void setupJwt() {
        SecurityTestSupport.mockJwtAsUser(jwtUtil, appUserService);
    }

    @Test
    void createIssue_asUser_returnsOk() throws Exception {
        when(reportIssueService.createIssue(any(ReportIssue.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/user/createIssue")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReportIssue())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload").value(true));
    }

    @Test
    void getAllIssue_asUser_returnsOk() throws Exception {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("count", 1);
        when(reportIssueService.getAllIssue()).thenReturn(node);

        mockMvc.perform(get("/api/v1/user/getAllIssue")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.count").value(1));
    }

    @Test
    void deleteIssue_asUser_returnsOk() throws Exception {
        when(reportIssueService.deleteIssue(eq("1"))).thenReturn(true);

        mockMvc.perform(delete("/api/v1/user/deleteIssue/{id}", "1")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload").value(true));
    }
}
