package com.up.asset_holder_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.up.asset_holder_api.configuration.BeanConfig;
import com.up.asset_holder_api.configuration.SecurityConfig;
import com.up.asset_holder_api.jwt.JwtAuthEntrypoint;
import com.up.asset_holder_api.jwt.JwtAuthFilter;
import com.up.asset_holder_api.jwt.JwtUtil;
import com.up.asset_holder_api.model.request.AssetTrasferRequest;
import com.up.asset_holder_api.service.AppUserService;
import com.up.asset_holder_api.service.AssetService;
import com.up.asset_holder_api.testsupport.SecurityTestSupport;
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

@WebMvcTest(controllers = AssetController.class)
@Import({SecurityConfig.class, BeanConfig.class, JwtAuthFilter.class, JwtAuthEntrypoint.class})
class AssetControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AssetService assetService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private AppUserService appUserService;

    @BeforeEach
    void setupJwtAsUser() {
        SecurityTestSupport.mockJwtAsUser(jwtUtil, appUserService);
    }

    @Test
    void getAllAssets_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/v1/user/getAllAsset"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllAssets_asUser_returnsOk() throws Exception {
        // AssetService#getAllAsset returns JsonNode in this project
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.putArray("items");
        when(assetService.getAllAsset()).thenReturn(payload);

        mockMvc.perform(get("/api/v1/user/getAllAsset")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.payload.items").isArray());
    }

    @Test
    void getAllHistory_asUser_returnsOk() throws Exception {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.putArray("history");
        when(assetService.getAllAssetHistroy()).thenReturn(payload);

        mockMvc.perform(get("/api/v1/getAllHistory")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.history").isArray());
    }

    @Test
    void transferAsset_requiresAdmin() throws Exception {
        AssetTrasferRequest req = AssetTrasferRequest.builder().newAssignTo(2).build();

        // User token should be forbidden for /api/v1/admin/**
        mockMvc.perform(put("/api/v1/admin/transferAsset/{id}", "asset1")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void transferAsset_asAdmin_returnsOk() throws Exception {
        // Switch mocked JWT to ADMIN for this test
        SecurityTestSupport.mockJwtAsAdmin(jwtUtil, appUserService);

        when(assetService.trasfterAsset(eq("asset1"), any(AssetTrasferRequest.class))).thenReturn(true);

        AssetTrasferRequest req = AssetTrasferRequest.builder().newAssignTo(2).build();

        mockMvc.perform(put("/api/v1/admin/transferAsset/{id}", "asset1")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload").value(true));
    }

    @Test
    void getHistoryById_asAdmin_returnsOk() throws Exception {
        SecurityTestSupport.mockJwtAsAdmin(jwtUtil, appUserService);

        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("assetId", "asset1");
        when(assetService.getHistoryById(eq("asset1"))).thenReturn(payload);

        // Note: controller mapping uses /admin/getHistoryById/{id} BUT method expects request param "id".
        mockMvc.perform(get("/api/v1/admin/getHistoryById/{ignored}", "asset1")
                        .param("id", "asset1")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.assetId").value("asset1"));
    }
}
