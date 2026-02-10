package com.imaudiopaas.tts.infrastructure.selfhosted;

import com.imaudiopaas.tts.core.TtsProvider;
import com.imaudiopaas.tts.core.domain.AudioFormat;
import com.imaudiopaas.tts.core.domain.ProviderType;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.core.domain.TtsResponse;
import com.imaudiopaas.tts.model.ProviderConfig;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class QwenTtsProvider implements TtsProvider {

    private final RestTemplate restTemplate;

    @Override
    public ProviderType getType() {
        return ProviderType.QWEN;
    }

    @Override
    public TtsResponse synthesize(TtsRequest request, ProviderConfig config) {
        log.info("Synthesizing with Qwen: {}", config.getName());

        String url = config.getBaseUrl() + "/api/tts";
        
        // Simple JSON Payload
        Map<String, Object> body = new HashMap<>();
        body.put("input", request.getText()); // Qwen might use specific fields, assuming generic
        body.put("voice", request.getVoiceId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (config.getAccessKey() != null) {
            headers.set("Authorization", "Bearer " + config.getAccessKey());
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            byte[] responseBytes = restTemplate.postForObject(url, entity, byte[].class);
            
            if (responseBytes == null || responseBytes.length == 0) {
                throw new RuntimeException("Empty response from Qwen provider");
            }

            InputStream audioStream = new ByteArrayInputStream(responseBytes);
            
            return TtsResponse.builder()
                    .audioStream(audioStream)
                    .format(AudioFormat.WAV)
                    .contentLength((long) responseBytes.length)
                    .build();

        } catch (Exception e) {
            log.error("Failed to call Qwen TTS", e);
            throw new RuntimeException("Qwen synthesis failed", e);
        }
    }
}
