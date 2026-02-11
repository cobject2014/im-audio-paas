package com.imaudiopaas.tts.service;

import com.imaudiopaas.tts.core.TtsProvider;
import com.imaudiopaas.tts.core.domain.ProviderType;
import com.imaudiopaas.tts.core.event.ProviderRequestEvent;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.core.domain.TtsResponse;
import com.imaudiopaas.tts.exception.TtsException;
import com.imaudiopaas.tts.model.ProviderConfig;
import com.imaudiopaas.tts.model.VoiceDefinition;
import com.imaudiopaas.tts.repository.ProviderConfigRepository;
import com.imaudiopaas.tts.repository.VoiceDefinitionRepository;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProviderRoutingService {

    private final Map<ProviderType, TtsProvider> providers = new EnumMap<>(ProviderType.class);
    private final ProviderConfigRepository providerConfigRepository;
    private final VoiceDefinitionRepository voiceDefinitionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProviderRoutingService(
            List<TtsProvider> providersList,
            ProviderConfigRepository providerConfigRepository,
            VoiceDefinitionRepository voiceDefinitionRepository,
            ApplicationEventPublisher eventPublisher) {
        this.providerConfigRepository = providerConfigRepository;
        this.voiceDefinitionRepository = voiceDefinitionRepository;
        this.eventPublisher = eventPublisher;
        providersList.forEach(p -> providers.put(p.getType(), p));
    }

    public TtsResponse routeAndSynthesize(TtsRequest request) {
        log.info("Routing TTS request for voice: {}", request.getVoiceId());
        
        // 1. Resolve Voice Definition
        Optional<VoiceDefinition> voiceDef = voiceDefinitionRepository.findById(request.getVoiceId());
        
        ProviderType type;
        String nativeVoiceId;

        if (voiceDef.isPresent()) {
            type = voiceDef.get().getProviderType();
            nativeVoiceId = voiceDef.get().getNativeVoiceId();
            log.debug("Resolved mapped voice {} to provider {} with native ID {}", 
                    request.getVoiceId(), type, nativeVoiceId);
        } else {
            // Heuristic resolution if not mapped
            // 1. Check if explicit model/provider is passed (Debug/Direct mode)
            if (request.getModel() != null && !request.getModel().isEmpty()) {
                try {
                    type = ProviderType.valueOf(request.getModel().toUpperCase());
                    log.debug("Using explicit provider from model param: {}", type);
                } catch (IllegalArgumentException e) {
                    // Not a valid provider enum, fall back to inference
                    type = inferProvider(request.getVoiceId());
                }
            } else {
                type = inferProvider(request.getVoiceId());
            }

            nativeVoiceId = request.getVoiceId();
            
            // Smart default for Aliyun (if user just types "aliyun")
            if (type == ProviderType.ALIYUN && "aliyun".equalsIgnoreCase(nativeVoiceId)) {
                nativeVoiceId = "xiaoyun"; 
            }
            
            log.debug("Inferred provider {} for unmapped voice {}", type, request.getVoiceId());
        }

        // 2. Fetch Active Configuration
        final ProviderType finalType = type;
        ProviderConfig config = providerConfigRepository.findFirstByProviderTypeAndIsActiveTrue(finalType)
                .orElseThrow(() -> new TtsException("No active provider configuration found for type: " + finalType));

        // 3. Select Implementation
        TtsProvider provider = providers.get(type);
        if (provider == null) {
            throw new TtsException("No implementation found for provider type: " + type);
        }

        // 4. Update request with native ID and invoke
        // Note: effectively modifying the request object here. 
        // If deep copy needed, we should build a new one. But TtsRequest is @Data.
        request.setVoiceId(nativeVoiceId);
        
        long startTime = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = null;
        try {
            TtsResponse response = provider.synthesize(request, config);
            success = true;
            return response;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            eventPublisher.publishEvent(new ProviderRequestEvent(
                    this,
                    config.getName(),
                    success,
                    duration,
                    errorMessage
            ));
        }
    }
    
    private ProviderType inferProvider(String voiceId) {
        if (voiceId == null) throw new IllegalArgumentException("Voice ID cannot be null");
        String lower = voiceId.toLowerCase();
        
        // CosyVoice Heuristics
        if (lower.startsWith("long") || lower.startsWith("loong") || lower.endsWith("_v2") || lower.startsWith("libai")) {
            return ProviderType.ALIYUN_COSYVOICE;
        }

        if (lower.startsWith("aliyun") || lower.contains("xiaoyun")) return ProviderType.ALIYUN;
        if (lower.startsWith("aws") || lower.equals("joanna")) return ProviderType.AWS; // Joanna is AWS
        if (lower.startsWith("tencent")) return ProviderType.TENCENT;
        
        // Fallback default: Aliyun (matches MVP priority) or Error
        throw new IllegalArgumentException("Cannot infer provider for voice ID: " + voiceId);
    }
}
