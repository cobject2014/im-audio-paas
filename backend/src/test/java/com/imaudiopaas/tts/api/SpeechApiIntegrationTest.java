package com.imaudiopaas.tts.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imaudiopaas.tts.api.dto.OpenAiSpeechRequest;
import com.imaudiopaas.tts.config.SecurityConfig;
import com.imaudiopaas.tts.config.security.ApiTokenFilter;
import com.imaudiopaas.tts.core.domain.AudioFormat;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.core.domain.TtsResponse;
import com.imaudiopaas.tts.service.ProviderRoutingService;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OpenAiSpeechController.class)
@Import({SecurityConfig.class, ApiTokenFilter.class})
public class SpeechApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProviderRoutingService routingService;

    @Test
    void whenValidRequest_thenReturnsAudio() throws Exception {
        // Given
        OpenAiSpeechRequest requestDto = new OpenAiSpeechRequest();
        requestDto.setInput("Hello world");
        requestDto.setVoice("aliyun-xiaoyun");
        requestDto.setModel("tts-1");

        TtsResponse mockResponse = TtsResponse.builder()
                .audioStream(new ByteArrayInputStream(new byte[] {1, 2, 3}))
                .format(AudioFormat.MP3)
                .contentLength(3)
                .build();

        when(routingService.routeAndSynthesize(any(TtsRequest.class))).thenReturn(mockResponse);

        // When/Then
        mockMvc.perform(post("/v1/audio/speech")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("audio/mpeg")))
                .andExpect(content().bytes(new byte[] {1, 2, 3}));
    }

    @Test
    void whenMissingText_thenReturnsBadRequest() throws Exception {
        TtsRequest request = TtsRequest.builder()
                .voiceId("aliyun-xiaoyun")
                .build(); // Text is missing

        mockMvc.perform(post("/v1/audio/speech")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
