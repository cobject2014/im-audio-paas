package com.imaudiopaas.tts.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imaudiopaas.tts.api.dto.ProviderConfigDto;
import com.imaudiopaas.tts.config.SecurityConfig;
import com.imaudiopaas.tts.core.domain.ProviderType;
import com.imaudiopaas.tts.service.ProviderConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminProviderController.class)
@Import(SecurityConfig.class)
public class AdminProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProviderConfigService providerConfigService;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenGetAllProviders_thenReturnsList() throws Exception {
        UUID id = UUID.randomUUID();
        ProviderConfigDto dto = ProviderConfigDto.builder()
                .id(id)
                .name("test-aliyun")
                .providerType(ProviderType.ALIYUN)
                .build();

        when(providerConfigService.getAllConfigs()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/admin/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("test-aliyun"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenCreateProvider_thenReturnsCreated() throws Exception {
        ProviderConfigDto dto = ProviderConfigDto.builder()
                .name("new-aws")
                .providerType(ProviderType.AWS)
                .accessKey("key")
                .secretKey("secret")
                .build();

        ProviderConfigDto savedDto = ProviderConfigDto.builder()
                .id(UUID.randomUUID())
                .name("new-aws")
                .providerType(ProviderType.AWS)
                .build();

        when(providerConfigService.createConfig(any(ProviderConfigDto.class))).thenReturn(savedDto);

        mockMvc.perform(post("/admin/providers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void whenUnauthenticated_thenReturns401() throws Exception {
        mockMvc.perform(get("/admin/providers"))
                .andExpect(status().isUnauthorized());
    }
}
