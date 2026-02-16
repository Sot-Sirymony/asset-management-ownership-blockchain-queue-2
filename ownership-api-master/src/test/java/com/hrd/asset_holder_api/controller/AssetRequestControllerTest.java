package com.hrd.asset_holder_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrd.asset_holder_api.configuration.BeanConfig;
import com.hrd.asset_holder_api.configuration.SecurityConfig;
import com.hrd.asset_holder_api.jwt.JwtAuthEntrypoint;
import com.hrd.asset_holder_api.jwt.JwtAuthFilter;
import com.hrd.asset_holder_api.jwt.JwtUtil;
import com.hrd.asset_holder_api.model.entity.AssetRequest;
import com.hrd.asset_holder_api.model.request.AssetRequestRes;
import com.hrd.asset_holder_api.service.AppUserService;
import com.hrd.asset_holder_api.service.AssetRequestService;
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

@WebMvcTest(controllers = AssetReqeustController.class)
@Import({SecurityConfig.class, BeanConfig.class, JwtAuthFilter.class, JwtAuthEntrypoint.class})
class AssetRequestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AssetRequestService assetRequestService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private AppUserService appUserService;

    @BeforeEach
    void setupJwt() {
        SecurityTestSupport.mockJwtAsUser(jwtUtil, appUserService);
    }

    @Test
    void getUserAssetRequests_asUser_returnsOk() throws Exception {
        when(assetRequestService.getUserAssetRequest()).thenReturn(List.of(new AssetRequest()));

        mockMvc.perform(get("/api/v1/user/assetRequest")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload").isArray());
    }

    @Test
    void createUserAssetRequest_asUser_returnsOk() throws Exception {
        AssetRequestRes req = new AssetRequestRes();
        when(assetRequestService.createUserAssetRequest(any(AssetRequestRes.class))).thenReturn(req);

        mockMvc.perform(post("/api/v1/user/createAssetRequest")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload").exists());
    }

    @Test
    void adminEndpoint_requiresAdminAuthority() throws Exception {
        // authenticated as USER -> should be 403 on /admin/**
        mockMvc.perform(get("/api/v1/admin/assetRequest")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN))
                .andExpect(status().isForbidden());
    }
}
