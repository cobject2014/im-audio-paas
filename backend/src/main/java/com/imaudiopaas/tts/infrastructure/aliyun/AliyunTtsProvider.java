package com.imaudiopaas.tts.infrastructure.aliyun;

import com.alibaba.nls.client.AccessToken;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.OutputFormatEnum;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizer;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizerListener;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizerResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imaudiopaas.tts.core.TtsProvider;
import com.imaudiopaas.tts.core.domain.AudioFormat;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.core.domain.TtsResponse;
import com.imaudiopaas.tts.exception.TtsException;
import com.imaudiopaas.tts.model.ProviderConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.imaudiopaas.tts.core.domain.ProviderType; // Added import

@Slf4j
@Component
@RequiredArgsConstructor
public class AliyunTtsProvider implements TtsProvider {

    private final ObjectMapper objectMapper;
    // Cache NlsClient per AccessKey to reuse connections.
    private final Map<String, ClientWrapper> clientCache = new ConcurrentHashMap<>();

    @Override
    public ProviderType getType() {
        return ProviderType.ALIYUN;
    }

    private static class ClientWrapper {
        final NlsClient client;
        final Instant expiry;
        final String token;

        ClientWrapper(NlsClient client, String token, long expireSeconds) {
            this.client = client;
            this.token = token;
            this.expiry = Instant.now().plusSeconds(expireSeconds - 60); // Buffer 60s
        }

        boolean isValid() {
            return Instant.now().isBefore(expiry);
        }
        
        void shutdown() {
            client.shutdown();
        }
    }

    @Override
    public TtsResponse synthesize(TtsRequest request, ProviderConfig config) {
        String appKey = parseAppKey(config.getMetadata());
        if (appKey == null || appKey.isEmpty()) {
            throw new TtsException("Aliyun AppKey is missing in provider config metadata");
        }

        String accessKeyId = config.getAccessKey();
        String accessKeySecret = config.getSecretKey();
        
        String cacheKey = accessKeyId; 

        ClientWrapper wrapper = clientCache.get(cacheKey);
        
        // Refresh client if missing or expired
        if (wrapper == null || !wrapper.isValid()) {
            synchronized (clientCache) {
                 // Double check
                 wrapper = clientCache.get(cacheKey);
                 if (wrapper == null || !wrapper.isValid()) {
                     if (wrapper != null) {
                         try { wrapper.shutdown(); } catch (Exception ignored) {}
                     }
                     try {
                         wrapper = createClient(accessKeyId, accessKeySecret);
                         clientCache.put(cacheKey, wrapper);
                     } catch (Exception e) {
                         throw new TtsException("Failed to create Aliyun NlsClient or get Token", e);
                     }
                 }
            }
        }

        NlsClient client = wrapper.client;
        
        // Prepare to capture output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        AtomicReference<String> errorMsg = new AtomicReference<>();
        
        SpeechSynthesizerListener listener = new SpeechSynthesizerListener() {
            @Override
            public void onMessage(ByteBuffer message) {
                try {
                    byte[] bytesArray = new byte[message.remaining()];
                    message.get(bytesArray, 0, bytesArray.length);
                    outputStream.write(bytesArray);
                } catch (IOException e) {
                    log.error("Error writing audio bytes", e);
                    // Can we stop early?
                }
            }

            @Override
            public void onComplete(SpeechSynthesizerResponse response) {
                log.debug("TTS Complete. taskId: {}", response.getTaskId());
            }

            @Override
            public void onFail(SpeechSynthesizerResponse response) {
                String msg = String.format("TTS Failed: status=%d, text=%s, task_id=%s", 
                    response.getStatus(), response.getStatusText(), response.getTaskId());
                log.error(msg);
                errorMsg.set(msg);
            }
        };

        SpeechSynthesizer synthesizer = null;
        try {
            synthesizer = new SpeechSynthesizer(client, listener);
            synthesizer.setAppKey(appKey);
            synthesizer.setText(request.getText());
            synthesizer.setFormat(mapToOutputFormat(request.getFormat()));
            synthesizer.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K); // Default to 16k
            
            if (request.getVoiceId() != null && !request.getVoiceId().isEmpty()) {
                synthesizer.setVoice(request.getVoiceId());
            }

            Map<String, Object> extra = request.getExtraBody();
            if (extra != null) {
                if (extra.containsKey("volume")) synthesizer.setVolume(toInt(extra.get("volume")));
                if (extra.containsKey("speech_rate")) synthesizer.setSpeechRate(toInt(extra.get("speech_rate")));
                if (extra.containsKey("pitch_rate")) synthesizer.setPitchRate(toInt(extra.get("pitch_rate")));
            }

            synthesizer.start();
            synthesizer.waitForComplete();
            
        } catch (Exception e) {
            throw new TtsException("Error during Aliyun TTS synthesis execution", e);
        } finally {
            if (synthesizer != null) synthesizer.close();
        }

        if (errorMsg.get() != null) {
            throw new TtsException(errorMsg.get());
        }

        byte[] audioData = outputStream.toByteArray();
        log.info("Aliyun TTS generated {} bytes for voice {}", audioData.length, request.getVoiceId());
        
        if (audioData.length == 0) {
            throw new TtsException("Aliyun TTS returned empty audio data");
        }

        return TtsResponse.builder()
                .audioStream(new ByteArrayInputStream(audioData))
                .format(request.getFormat())
                .contentLength(audioData.length)
                .build();
    }

    private ClientWrapper createClient(String ak, String sk) throws IOException {
        AccessToken accessToken = new AccessToken(ak, sk);
        accessToken.apply();
        String token = accessToken.getToken();
        long expireTime = accessToken.getExpireTime(); 
        
        long nowSeconds = System.currentTimeMillis() / 1000;
        long duration = expireTime - nowSeconds;
        
        NlsClient client = new NlsClient(token);
        return new ClientWrapper(client, token, duration > 0 ? duration : 3600);
    }

    private String parseAppKey(String metadataJson) {
        if (metadataJson == null) return null;
        try {
            JsonNode root = objectMapper.readTree(metadataJson);
            if (root.has("appKey")) {
                return root.get("appKey").asText();
            }
        } catch (Exception e) {
            log.warn("Failed to parse metadata for AppKey", e);
        }
        return null;
    }

    private OutputFormatEnum mapToOutputFormat(AudioFormat format) {
        if (format == null) return OutputFormatEnum.MP3;
        switch (format) {
            case WAV: return OutputFormatEnum.WAV;
            case PCM: return OutputFormatEnum.PCM;
            default: return OutputFormatEnum.MP3;
        }
    }
    
    private int toInt(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.parseInt(obj.toString());
        } catch (Exception e) {
            return 0;
        }
    }
}
