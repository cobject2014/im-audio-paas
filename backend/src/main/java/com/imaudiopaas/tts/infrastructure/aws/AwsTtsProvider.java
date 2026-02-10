package com.imaudiopaas.tts.infrastructure.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imaudiopaas.tts.core.TtsProvider;
import com.imaudiopaas.tts.core.domain.AudioFormat;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.core.domain.TtsResponse;
import com.imaudiopaas.tts.exception.TtsException;
import com.imaudiopaas.tts.model.ProviderConfig;
import com.imaudiopaas.tts.core.ParameterMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
import software.amazon.awssdk.services.polly.model.TextType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.imaudiopaas.tts.core.domain.ProviderType; // Added import

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsTtsProvider implements TtsProvider {

    private final ObjectMapper objectMapper;
    private final Map<String, PollyClient> clientCache = new ConcurrentHashMap<>();

    @Override
    public ProviderType getType() {
        return ProviderType.AWS;
    }

    @Override
    public TtsResponse synthesize(TtsRequest request, ProviderConfig config) {
        try {
            String regionName = parseRegion(config.getMetadata());
            Region region = regionName != null ? Region.of(regionName) : Region.US_EAST_1;
            
            String cacheKey = config.getAccessKey() + ":" + region.id();
            
            PollyClient polly = clientCache.computeIfAbsent(cacheKey, k -> {
                AwsBasicCredentials credentials = AwsBasicCredentials.create(config.getAccessKey(), config.getSecretKey());
                return PollyClient.builder()
                        .credentialsProvider(StaticCredentialsProvider.create(credentials))
                        .region(region)
                        .build();
            });

            String processedText = ParameterMapper.toAwsSsml(request.getText(), request.getExtraBody());
            TextType textType = processedText.contains("<speak>") ? TextType.SSML : TextType.TEXT;

            SynthesizeSpeechRequest.Builder requestBuilder = SynthesizeSpeechRequest.builder()
                    .text(processedText)
                    .textType(textType)
                    .outputFormat(mapFormat(request.getFormat()));

             if (request.getVoiceId() != null && !request.getVoiceId().isEmpty()) {
                 requestBuilder.voiceId(request.getVoiceId());
             }

            // Map extra params like Engine (standard/neural) or LanguageCode if needed
            // For now keeping it simple based on existing implementation

            ResponseInputStream<SynthesizeSpeechResponse> response = polly.synthesizeSpeech(requestBuilder.build());

            return TtsResponse.builder()
                    .audioStream(response)
                    .format(request.getFormat())
                    // AWS Polly returns a stream, content length might not be available upfront easily
                    .contentLength(0) 
                    .build();

        } catch (Exception e) {
            log.error("AWS Polly TTS failed", e);
            throw new TtsException("AWS Polly TTS failed", e);
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
            log.warn("Failed to parse metadata for AWS Region", e);
        }
        return null;
    }

    private OutputFormat mapFormat(AudioFormat format) {
        if (format == null) return OutputFormat.MP3;
        switch (format) {
            case PCM: return OutputFormat.PCM;
            default: return OutputFormat.MP3;
        }
    }
}
