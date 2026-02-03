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

class VibeVoiceProviderTest {

    private VibeVoiceProvider provider;
    private MockRestServiceServer mockServer;
    private ProviderConfig config;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        
        config = new ProviderConfig();
        config.setName("vibevoice-local");
        config.setProviderType(ProviderType.VIBEVOICE);
        config.setBaseUrl("http://localhost:8000");
        config.setAccessKey("test-api-key");
        
        provider = new VibeVoiceProvider(restTemplate);
    }

    @Test
    void synthesize_Successful() throws IOException {
        // Given
        TtsRequest request = TtsRequest.builder()
                .text("Hello Vibe")
                .voiceId("en_us_vibe")
                .build();
        
        byte[] audioData = new byte[]{1, 2, 3, 4};

        mockServer.expect(requestTo("http://localhost:8000/tts"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hello Vibe")))
                .andRespond(withSuccess(audioData, MediaType.APPLICATION_OCTET_STREAM));

        // When
        TtsResponse response = provider.synthesize(request, config);

        // Then
        mockServer.verify();
        assertNotNull(response);
        assertEquals(AudioFormat.WAV, response.getFormat()); // Assuming VibeVoice returns WAV
        assertEquals(4, response.getContentLength());
    }
}
