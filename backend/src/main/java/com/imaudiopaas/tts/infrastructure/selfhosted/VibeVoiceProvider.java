package com.imaudiopaas.tts.infrastructure.selfhosted;

import com.imaudiopaas.tts.core.TtsProvider;
import com.imaudiopaas.tts.core.domain.AudioFormat;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.core.domain.TtsResponse;
import com.imaudiopaas.tts.model.ProviderConfig;
import com.imaudiopaas.tts.core.domain.ProviderType;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class VibeVoiceProvider implements TtsProvider {

    private final RestTemplate restTemplate;

    @Override
    public ProviderType getType() {
        return ProviderType.VIBEVOICE;
    }

    @Override
    public TtsResponse synthesize(TtsRequest request, ProviderConfig config) {
        log.info("Synthesizing with VibeVoice: {}", config.getName());

        String url = config.getBaseUrl() + "/tts";
        
        // Simple JSON Payload
        Map<String, Object> body = new HashMap<>();
        body.put("text", request.getText());
        body.put("voice_id", request.getVoiceId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (config.getAccessKey() != null) {
            headers.set("Authorization", "Bearer " + config.getAccessKey());
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            byte[] responseBytes = restTemplate.postForObject(url, entity, byte[].class);
            
            if (responseBytes == null || responseBytes.length == 0) {
                throw new RuntimeException("Empty response from VibeVoice provider");
            }

            InputStream audioStream = new ByteArrayInputStream(responseBytes);
            
            return TtsResponse.builder()
                    .audioStream(audioStream)
                    .format(AudioFormat.WAV) // Assuming default
                    .contentLength((long) responseBytes.length)
                    .build();

        } catch (Exception e) {
            log.error("Failed to call VibeVoice TTS", e);
            throw new RuntimeException("VibeVoice synthesis failed", e);
        }
    }
}
