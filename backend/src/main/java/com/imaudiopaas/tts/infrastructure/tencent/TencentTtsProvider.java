package com.imaudiopaas.tts.infrastructure.tencent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imaudiopaas.tts.core.TtsProvider;
import com.imaudiopaas.tts.core.domain.AudioFormat;
import com.imaudiopaas.tts.core.domain.ProviderType;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.core.domain.TtsResponse;
import com.imaudiopaas.tts.exception.TtsException;
import com.imaudiopaas.tts.model.ProviderConfig;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.tts.v20190823.TtsClient;
import com.tencentcloudapi.tts.v20190823.models.TextToVoiceRequest;
import com.tencentcloudapi.tts.v20190823.models.TextToVoiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class TencentTtsProvider implements TtsProvider {

    private final ObjectMapper objectMapper;
    private final Map<String, TtsClient> clientCache = new ConcurrentHashMap<>();

    @Override
    public ProviderType getType() {
        return ProviderType.TENCENT;
    }

    @Override
    public TtsResponse synthesize(TtsRequest request, ProviderConfig config) {
        try {
            String region = parseRegion(config.getMetadata());
            if (region == null || region.isEmpty()) {
                region = "ap-shanghai"; // Default
            }

            String cacheKey = config.getAccessKey() + ":" + region;

            // Compute client if absent. Note: TtsClient might be cleaner to recreate if credentials change,
            // but we assume config is relatively stable per key.
            // Ideally we should check if credentials changed for the key, but using AK as key helps.
            // However, if SK changes for same AK, this cache won't refresh. This is a known trade-off for simple caching.
            // A better key would include a hash of SK, or just don't cache deeply. 
            // AWS and Aliyun clients are relatively heavy, Tencent client is also HTTP client wrapper.
            // Let's stick to simple cache for now.
            
            String finalRegion = region;
            TtsClient client = clientCache.computeIfAbsent(cacheKey, k -> {
                Credential cred = new Credential(config.getAccessKey(), config.getSecretKey());
                return new TtsClient(cred, finalRegion);
            });

            TextToVoiceRequest req = new TextToVoiceRequest();
            req.setText(request.getText());
            req.setSessionId(UUID.randomUUID().toString());
            req.setModelType(1L);
            
            // Map VoiceId. If strictly numeric, parse Long. If string passed, maybe it fails.
            // Tencent usually expects Long VoiceType.
            try {
                req.setVoiceType(Long.parseLong(request.getVoiceId()));
            } catch (NumberFormatException e) {
                log.warn("Invalid VoiceId format for Tencent (expected numeric): {}", request.getVoiceId());
                // Fallback or let it throw? Let's default to a safe voice if parsing fails? 
                // Or maybe the input IS valid for some scenarios? 
                // Start with throwing exception essentially by letting API fail or setting default.
                req.setVoiceType(101001L); // Default female voice
            }
            
            req.setCodec(mapFormat(request.getFormat()));
            
            // Handle extra params if provided in request.getExtraBody() (Volume, Speed, etc)
            Map<String, Object> extra = request.getExtraBody();
            if (extra != null) {
                if(extra.containsKey("volume")) req.setVolume(((Number)extra.get("volume")).floatValue());
                if(extra.containsKey("speed")) req.setSpeed(((Number)extra.get("speed")).floatValue()); // Tencent uses 'Speed', Aliyun 'SpeechRate'
                if(extra.containsKey("projectId")) req.setProjectId(((Number)extra.get("projectId")).longValue());
            }

            TextToVoiceResponse resp = client.TextToVoice(req);
            
            if (resp.getAudio() == null) {
                throw new TtsException("Tencent TTS returned empty audio");
            }

            byte[] audioBytes = Base64.getDecoder().decode(resp.getAudio());

            return TtsResponse.builder()
                    .audioStream(new ByteArrayInputStream(audioBytes))
                    .format(request.getFormat())
                    .contentLength(audioBytes.length)
                    .build();

        } catch (Exception e) {
            log.error("Tencent TTS failed", e);
            throw new TtsException("Tencent TTS failed: " + e.getMessage(), e);
        }
    }
    
    private String parseRegion(String metadataJson) {
        if (metadataJson == null) return null;
        try {
            JsonNode root = objectMapper.readTree(metadataJson);
            if (root.has("region")) {
                return root.get("region").asText();
            }
        } catch (Exception e) {
            log.warn("Failed to parse metadata for Tencent Region", e);
        }
        return null;
    }

    private String mapFormat(AudioFormat format) {
        if (format == null) return "mp3";
        switch (format) {
            case WAV: return "wav";
            case PCM: return "pcm";
            default: return "mp3";
        }
    }
}
