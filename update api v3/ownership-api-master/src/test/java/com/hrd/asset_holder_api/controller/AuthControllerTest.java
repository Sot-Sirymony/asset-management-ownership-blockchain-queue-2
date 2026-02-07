package com.hrd.asset_holder_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrd.asset_holder_api.configuration.BeanConfig;
import com.hrd.asset_holder_api.configuration.SecurityConfig;
import com.hrd.asset_holder_api.jwt.JwtAuthEntrypoint;
import com.hrd.asset_holder_api.jwt.JwtAuthFilter;
import com.hrd.asset_holder_api.jwt.JwtUtil;
import com.hrd.asset_holder_api.model.response.LoginReq;
import com.hrd.asset_holder_api.model.response.LoginRes;
import com.hrd.asset_holder_api.service.AppUserService;
import com.hrd.asset_holder_api.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, BeanConfig.class, JwtAuthFilter.class, JwtAuthEntrypoint.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // Needed by SecurityConfig / JwtAuthFilter
    @MockBean private JwtUtil jwtUtil;
    @MockBean private AppUserService appUserService;

    @Test
    void loginSuccess_shouldReturnToken() throws Exception {
        LoginReq req = LoginReq.builder().username("admin").password("adminpw").build();

        LoginRes res = new LoginRes();
        res.setToken("fake-jwt-token");
        when(authService.login(any(LoginReq.class))).thenReturn(res);

        mockMvc.perform(post("/rest/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.payload.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.httpStatus").value("OK"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
