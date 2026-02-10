package com.imaudiopaas.tts.infrastructure.tencent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imaudiopaas.tts.core.domain.ProviderType;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.exception.TtsException;
import com.imaudiopaas.tts.model.ProviderConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TencentTtsProviderTest {

    private TencentTtsProvider provider;

    @BeforeEach
    void setUp() {
        provider = new TencentTtsProvider(new ObjectMapper());
    }

    @Test
    void getType_ShouldReturnTencent() {
        assertEquals(ProviderType.TENCENT, provider.getType());
    }

    @Test
    void whenSynthesizeCalled_withInvalidCreds_thenShouldFail() {
        // Given
        ProviderConfig config = new ProviderConfig();
        config.setAccessKey("invalidId");
        config.setSecretKey("invalidKey");
        
        TtsRequest request = TtsRequest.builder()
                .text("hello")
                .voiceId("101001")
                .build();

        // Then
        assertThrows(TtsException.class, () -> provider.synthesize(request, config));
    }
}
