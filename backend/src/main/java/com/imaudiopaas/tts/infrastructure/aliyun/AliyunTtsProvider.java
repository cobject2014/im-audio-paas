package com.imaudiopaas.tts.infrastructure.aliyun;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imaudiopaas.tts.core.TtsProvider;
import com.imaudiopaas.tts.core.domain.AudioFormat;
import com.imaudiopaas.tts.core.domain.ProviderType;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.core.domain.TtsResponse;
import com.imaudiopaas.tts.exception.TtsException;
import com.imaudiopaas.tts.model.ProviderConfig;
import com.imaudiopaas.tts.core.ParameterMapper;
import java.io.ByteArrayInputStream;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AliyunTtsProvider implements TtsProvider {

    private final ObjectMapper objectMapper;
    private static final String REGION_ID = "cn-shanghai"; // Default, could be config
    private static final String DOMAIN = "nls-gateway.cn-shanghai.aliyuncs.com";
    private static final String API_VERSION = "2019-08-23";

    @Override
    public TtsResponse synthesize(TtsRequest request, ProviderConfig config) {
        try {
            // 1. Parse AppKey from metadata
            String appKey = parseAppKey(config.getMetadata());
            
            // 2. Build Client
            DefaultProfile profile = DefaultProfile.getProfile(REGION_ID, config.getAccessKey(), config.getSecretKey());
            IAcsClient client = new DefaultAcsClient(profile);

            // 3. Build Request
            CommonRequest apiRequest = new CommonRequest();
            apiRequest.setSysMethod(MethodType.POST);
            apiRequest.setSysDomain(DOMAIN);
            apiRequest.setSysVersion(API_VERSION);
            apiRequest.setSysAction("RunTts");
            
            apiRequest.putBodyParameter("AppKey", appKey);
            apiRequest.putBodyParameter("Text", request.getText());
            apiRequest.putBodyParameter("Voice", request.getVoiceId());
            apiRequest.putBodyParameter("Format", mapFormat(request.getFormat()));
            apiRequest.putBodyParameter("SampleRate", "16000");

            // Map extra parameters (e.g., emotion)
            Map<String, Object> extraParams = ParameterMapper.toAliyunParams(request.getExtraBody());
            extraParams.forEach((k, v) -> apiRequest.putBodyParameter(k, String.valueOf(v)));

            // 4. Call API
            CommonResponse response = client.getCommonResponse(apiRequest);

            // 5. Handle Response
            if (response.getHttpStatus() != 200) {
                 throw new TtsException("Aliyun API returned status: " + response.getHttpStatus());
            }

            String contentType = response.getHttpResponse().getHeaderValue("Content-Type");
            if (contentType != null && contentType.contains("json")) {
                 // Even if 200, it might be a logical error in JSON
                 if (response.getData() != null) {
                     JsonNode errorNode = objectMapper.readTree(response.getData());
                     throw new TtsException("Aliyun Error: " + errorNode.toString());
                 }
            }
            
            byte[] bytes = response.getHttpResponse().getHttpContent();
            if (bytes == null) {
                // Check if 'data' string has content if httpContent is null (unlikely for binary but possible)
               if (response.getData() != null) {
                   // This is risky as it might be default encoding
                   bytes = response.getData().getBytes();
               } else {
                   throw new TtsException("Failed to retrieve binary content from Aliyun response");
               }
            }

            return TtsResponse.builder()
                    .audioStream(new ByteArrayInputStream(bytes))
                    .format(request.getFormat())
                    .contentLength(bytes.length)
                    .build();

        } catch (Exception e) {
            log.error("Aliyun TTS failed", e);
            throw new TtsException("Aliyun TTS failed: " + e.getMessage(), e);
        }
    }

    private String mapFormat(AudioFormat format) {
        switch (format) {
            case WAV: return "wav";
            case PCM: return "pcm";
            default: return "mp3";
        }
    }

    private String parseAppKey(String metadata) {
        try {
            if (metadata == null) return null;
            JsonNode node = objectMapper.readTree(metadata);
            if (node.has("appKey")) {
                return node.get("appKey").asText();
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to parse metadata", e);
            return null;
        }
    }

    @Override
    public ProviderType getType() {
        return ProviderType.ALIYUN;
    }
}
