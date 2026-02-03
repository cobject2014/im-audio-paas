package com.imaudiopaas.tts.infrastructure.selfhosted;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.imaudiopaas.tts.core.domain.AudioFormat;
import com.imaudiopaas.tts.core.domain.ProviderType;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.core.domain.TtsResponse;
import com.imaudiopaas.tts.model.ProviderConfig;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class QwenTtsProviderTest {

    private QwenTtsProvider provider;
    private MockRestServiceServer mockServer;
    private ProviderConfig config;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        
        config = new ProviderConfig();
        config.setName("qwen-local");
        config.setProviderType(ProviderType.QWEN);
        config.setBaseUrl("http://localhost:8001");
        
        provider = new QwenTtsProvider(restTemplate);
    }

    @Test
    void synthesize_Successful() throws IOException {
        // Given
        TtsRequest request = TtsRequest.builder()
                .text("Hello Qwen")
                .voiceId("qwen-voice-1")
                .build();
        
        byte[] audioData = new byte[]{5, 6, 7, 8};

        mockServer.expect(requestTo("http://localhost:8001/api/tts"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hello Qwen")))
                .andRespond(withSuccess(audioData, MediaType.APPLICATION_OCTET_STREAM));

        // When
        TtsResponse response = provider.synthesize(request, config);

        // Then
        mockServer.verify();
        assertNotNull(response);
        assertEquals(AudioFormat.WAV, response.getFormat());
        assertEquals(4, response.getContentLength());
    }
}
