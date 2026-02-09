package com.imaudiopaas.tts.api;

import com.imaudiopaas.tts.model.ProviderConfig;
import com.imaudiopaas.tts.repository.ProviderConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/debug")
@RequiredArgsConstructor
public class DebugController {

    private final ProviderConfigRepository providerConfigRepository;

    @GetMapping("/providers")
    public List<String> getValidProviders() {
        // Only return providers that have at least one active configuration
        return providerConfigRepository.findByIsActiveTrue().stream()
                .map(ProviderConfig::getProviderType)
                .distinct()
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
