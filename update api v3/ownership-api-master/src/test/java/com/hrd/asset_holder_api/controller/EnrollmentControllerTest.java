package com.hrd.asset_holder_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrd.asset_holder_api.configuration.BeanConfig;
import com.hrd.asset_holder_api.configuration.SecurityConfig;
import com.hrd.asset_holder_api.jwt.JwtAuthEntrypoint;
import com.hrd.asset_holder_api.jwt.JwtAuthFilter;
import com.hrd.asset_holder_api.jwt.JwtUtil;
import com.hrd.asset_holder_api.model.request.UserRegister;
import com.hrd.asset_holder_api.model.request.UserRequest;
import com.hrd.asset_holder_api.model.response.UserResponse;
import com.hrd.asset_holder_api.service.AppUserService;
import com.hrd.asset_holder_api.service.EnrollmentService;
import com.hrd.asset_holder_api.testsupport.SecurityTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EnrollmentController.class)
@Import({SecurityConfig.class, BeanConfig.class, JwtAuthFilter.class, JwtAuthEntrypoint.class})
class EnrollmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private EnrollmentService enrollmentService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private AppUserService appUserService;

    @BeforeEach
    void setupJwt() {
        SecurityTestSupport.mockJwtAsAdmin(jwtUtil, appUserService);
    }

    @Test
    void registerUser_asAdmin_returnsOk() throws Exception {
        UserRegister req = new UserRegister();
        when(enrollmentService.registerUser(any(UserRegister.class))).thenReturn(req);

        mockMvc.perform(post("/api/v1/admin/register_user")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload").exists());
    }

    @Test
    void getAllUser_asAdmin_returnsOk() throws Exception {
        when(enrollmentService.getAllUser(anyInt(), anyInt()))
                .thenReturn(List.of(new UserResponse(), new UserResponse()));

        mockMvc.perform(get("/api/v1/admin/getAllUser")
                        .param("size", "1")
                        .param("page", "10")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload").isArray());
    }

    @Test
    void updateUser_asAdmin_returnsOk() throws Exception {
        UserRequest req = new UserRequest();
        when(enrollmentService.updateUser(eq(1), any(UserRequest.class))).thenReturn(req);

        mockMvc.perform(put("/api/v1/admin/updateUser/{id}", 1)
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload").exists());
    }

    @Test
    void userEndpoint_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/v1/getProfile"))
                .andExpect(status().isUnauthorized());
    }
}
