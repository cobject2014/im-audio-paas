package com.imaudiopaas.tts.infrastructure.aliyun;

import com.alibaba.nls.client.AccessToken;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.OutputFormatEnum;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imaudiopaas.tts.core.TtsProvider;
import com.imaudiopaas.tts.core.domain.AudioFormat;
import com.imaudiopaas.tts.exception.TtsException;
import com.imaudiopaas.tts.model.ProviderConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AliyunBaseProvider implements TtsProvider {
    
    protected final ObjectMapper objectMapper;
    // Cache NlsClient per AccessKey to reuse connections.
    protected static final Map<String, ClientWrapper> clientCache = new ConcurrentHashMap<>();

    protected AliyunBaseProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected static class ClientWrapper {
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

    protected NlsClient getClient(ProviderConfig config) {
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
        return wrapper.client;
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

    protected String parseAppKey(String metadataJson) {
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

    protected OutputFormatEnum mapToOutputFormat(AudioFormat format) {
        if (format == null) return OutputFormatEnum.MP3;
        switch (format) {
            case WAV: return OutputFormatEnum.WAV;
            case PCM: return OutputFormatEnum.PCM;
            default: return OutputFormatEnum.MP3;
        }
    }
    
    protected int toInt(Object obj) {
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
