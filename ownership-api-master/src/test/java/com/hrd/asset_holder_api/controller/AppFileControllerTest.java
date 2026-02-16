package com.hrd.asset_holder_api.controller;

import com.hrd.asset_holder_api.configuration.BeanConfig;
import com.hrd.asset_holder_api.configuration.SecurityConfig;
import com.hrd.asset_holder_api.jwt.JwtAuthEntrypoint;
import com.hrd.asset_holder_api.jwt.JwtAuthFilter;
import com.hrd.asset_holder_api.jwt.JwtUtil;
import com.hrd.asset_holder_api.service.AppFileService;
import com.hrd.asset_holder_api.service.AppUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppFileController.class)
@Import({SecurityConfig.class, BeanConfig.class, JwtAuthFilter.class, JwtAuthEntrypoint.class})
class AppFileControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private AppFileService appFileService;

    // Needed by SecurityConfig / JwtAuthFilter (even though /api/v1/files/** is permitAll)
    @MockBean private JwtUtil jwtUtil;
    @MockBean private AppUserService appUserService;

    @Test
    void uploadFile_returnsOk() throws Exception {
        when(appFileService.saveFile(any())).thenReturn("test.png");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                "hello".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/files").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.fileName").value("test.png"));
    }

    @Test
    void getFile_returnsOk() throws Exception {
        Resource resource = new ByteArrayResource("dummy".getBytes()) {
            @Override
            public String getFilename() {
                return "test.pdf";
            }
        };

        when(appFileService.getFileByFileName(eq("test.pdf"))).thenReturn(resource);

        mockMvc.perform(get("/api/v1/files").param("fileName", "test.pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline; filename=\"test.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
