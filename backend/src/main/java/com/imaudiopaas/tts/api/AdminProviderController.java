package com.imaudiopaas.tts.api;

import com.imaudiopaas.tts.api.dto.ProviderConfigDto;
import com.imaudiopaas.tts.service.ProviderConfigService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/providers")
@RequiredArgsConstructor
public class AdminProviderController {

    private final ProviderConfigService providerConfigService;

    @GetMapping
    public List<ProviderConfigDto> getAllProviders() {
        return providerConfigService.getAllConfigs();
    }

    @GetMapping("/{id}")
    public ProviderConfigDto getProvider(@PathVariable UUID id) {
        return providerConfigService.getConfig(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProviderConfigDto createProvider(@Valid @RequestBody ProviderConfigDto dto) {
        return providerConfigService.createConfig(dto);
    }

    @PutMapping("/{id}")
    public ProviderConfigDto updateProvider(@PathVariable UUID id, @Valid @RequestBody ProviderConfigDto dto) {
        return providerConfigService.updateConfig(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProvider(@PathVariable UUID id) {
        providerConfigService.deleteConfig(id);
    }
}
