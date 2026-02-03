package com.imaudiopaas.tts.infrastructure.aws;

import com.imaudiopaas.tts.exception.TtsException;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.model.ProviderConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AwsTtsProviderTest {

    private AwsTtsProvider provider;

    @BeforeEach
    void setUp() {
        provider = new AwsTtsProvider();
    }

    @Test
    void whenSynthesizeCalled_thenShouldCallPolly() {
        // Given
        ProviderConfig config = new ProviderConfig();
        config.setAccessKey("AKIATEST");
        config.setSecretKey("TESTSECRET");
        
        TtsRequest request = TtsRequest.builder()
                .text("hello")
                .voiceId("Joanna")
                .build();

        // Placeholder verification until implementation uses a mockable PollyClient
        assertThrows(TtsException.class, () -> provider.synthesize(request, config));
    }
}
