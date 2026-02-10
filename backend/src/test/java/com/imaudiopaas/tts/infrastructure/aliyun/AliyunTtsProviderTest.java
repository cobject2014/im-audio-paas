package com.imaudiopaas.tts.infrastructure.aliyun;

import com.imaudiopaas.tts.exception.TtsException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.model.ProviderConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imaudiopaas.tts.core.domain.ProviderType;

class AliyunTtsProviderTest {

    private AliyunTtsProvider provider;

    @BeforeEach
    void setUp() {
        provider = new AliyunTtsProvider(new ObjectMapper());
    }

    @Test
    void getType_ShouldReturnAliyun() {
        assertEquals(ProviderType.ALIYUN, provider.getType());
    }

    @Test
    void whenSynthesizeCalled_withMissingAppKey_thenShouldThrow() {
        // Given
        ProviderConfig config = new ProviderConfig();
        config.setAccessKey("testKey");
        config.setSecretKey("testSecret");
        // No metadata with appKey
        
        TtsRequest request = TtsRequest.builder()
                .text("hello")
                .voiceId("xiaoyun")
                .build();
        
        // Then
        assertThrows(TtsException.class, () -> provider.synthesize(request, config));
    }
}
