package com.imaudiopaas.tts.service;

import com.imaudiopaas.tts.core.TtsProvider;
import com.imaudiopaas.tts.core.domain.ProviderType;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.core.domain.TtsResponse;
import com.imaudiopaas.tts.exception.TtsException;
import com.imaudiopaas.tts.model.ProviderConfig;
import com.imaudiopaas.tts.repository.ProviderConfigRepository;
import com.imaudiopaas.tts.repository.VoiceDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoutingLogicTest {

    @Mock
    private ProviderConfigRepository configRepo;
    @Mock
    private VoiceDefinitionRepository voiceRepo;
    @Mock
    private TtsProvider aliyunProvider;
    @Mock
    private TtsProvider cosyVoiceProvider;

    private ProviderRoutingService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(aliyunProvider.getType()).thenReturn(ProviderType.ALIYUN);
        when(cosyVoiceProvider.getType()).thenReturn(ProviderType.ALIYUN_COSYVOICE);
        
        service = new ProviderRoutingService(
                java.util.Arrays.asList(aliyunProvider, cosyVoiceProvider),
                configRepo,
                voiceRepo
        );
    }

    @Test
    void testAliyunSmartDefault() {
        // Setup config
        ProviderConfig config = new ProviderConfig();
        when(configRepo.findFirstByProviderTypeAndIsActiveTrue(ProviderType.ALIYUN))
                .thenReturn(Optional.of(config));

        // Setup mock response
        when(aliyunProvider.synthesize(any(), any())).thenReturn(TtsResponse.builder().build());

        // Test Request
        TtsRequest request = TtsRequest.builder()
                .voiceId("aliyun") // User input
                .text("test")
                .build();

        service.routeAndSynthesize(request);

        // Capture what was passed to provider
        ArgumentCaptor<TtsRequest> captor = ArgumentCaptor.forClass(TtsRequest.class);
        verify(aliyunProvider).synthesize(captor.capture(), any());

        assertEquals("xiaoyun", captor.getValue().getVoiceId(), "Should map 'aliyun' to 'xiaoyun'");
    }
    
    @Test
    void testAliyunCaseInsensitiveSmartDefault() {
        // Setup config
        ProviderConfig config = new ProviderConfig();
        when(configRepo.findFirstByProviderTypeAndIsActiveTrue(ProviderType.ALIYUN))
                .thenReturn(Optional.of(config));
        when(aliyunProvider.synthesize(any(), any())).thenReturn(TtsResponse.builder().build());

        TtsRequest request = TtsRequest.builder()
                .voiceId("ALIYUN")
                .text("test")
                .build();

        service.routeAndSynthesize(request);

        ArgumentCaptor<TtsRequest> captor = ArgumentCaptor.forClass(TtsRequest.class);
        verify(aliyunProvider).synthesize(captor.capture(), any());

        assertEquals("xiaoyun", captor.getValue().getVoiceId(), "Should map 'ALIYUN' to 'xiaoyun'");
    }

    @Test
    void testCosyVoiceRouting() {
        // Setup config for CosyVoice
        ProviderConfig config = new ProviderConfig();
        when(configRepo.findFirstByProviderTypeAndIsActiveTrue(ProviderType.ALIYUN_COSYVOICE))
                .thenReturn(Optional.of(config));

        when(cosyVoiceProvider.synthesize(any(), any())).thenReturn(TtsResponse.builder().build());

        // Test Request with a CosyVoice ID
        TtsRequest request = TtsRequest.builder()
                .voiceId("longxiaochun")
                .text("hello")
                .build();

        service.routeAndSynthesize(request);

        // Verify it went to the CosyVoice provider
        ArgumentCaptor<TtsRequest> captor = ArgumentCaptor.forClass(TtsRequest.class);
        verify(cosyVoiceProvider).synthesize(captor.capture(), any());
        
        // Ensure standard provider was NOT called
        verify(aliyunProvider, never()).synthesize(any(), any());

        assertEquals("longxiaochun", captor.getValue().getVoiceId(), "Voice ID should remain unchanged");
    }
}
