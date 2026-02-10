package com.imaudiopaas.tts.infrastructure.aliyun;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imaudiopaas.tts.core.domain.ProviderType;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.exception.TtsException;
import com.imaudiopaas.tts.model.ProviderConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AliyunCosyVoiceProviderTest {

    private AliyunCosyVoiceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new AliyunCosyVoiceProvider(new ObjectMapper());
    }

    @Test
    void getType_ShouldReturnAliyunCosyVoice() {
        assertEquals(ProviderType.ALIYUN_COSYVOICE, provider.getType());
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
                .voiceId("longxiaochun")
                .build();
        
        // Then
        assertThrows(TtsException.class, () -> provider.synthesize(request, config));
    }
}
