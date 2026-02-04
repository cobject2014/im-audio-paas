package com.imaudiopaas.tts.service;

import com.imaudiopaas.tts.api.dto.ProviderConfigDto;
import com.imaudiopaas.tts.model.ProviderConfig;
import com.imaudiopaas.tts.repository.ProviderConfigRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProviderConfigService {

    private final ProviderConfigRepository providerConfigRepository;

    public List<ProviderConfigDto> getAllConfigs() {
        return providerConfigRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProviderConfigDto getConfig(UUID id) {
        return providerConfigRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
    }

    @Transactional
    public ProviderConfigDto createConfig(ProviderConfigDto dto) {
        ProviderConfig entity = new ProviderConfig();
        updateEntityFromDto(entity, dto);
        ProviderConfig saved = providerConfigRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public ProviderConfigDto updateConfig(UUID id, ProviderConfigDto dto) {
        ProviderConfig entity = providerConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider config not found with id: " + id));

        updateEntityFromDto(entity, dto);
        ProviderConfig saved = providerConfigRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public void deleteConfig(UUID id) {
        providerConfigRepository.deleteById(id);
    }

    private void updateEntityFromDto(ProviderConfig entity, ProviderConfigDto dto) {
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getProviderType() != null) entity.setProviderType(dto.getProviderType());
        if (dto.getBaseUrl() != null) entity.setBaseUrl(dto.getBaseUrl());
        if (dto.getAccessKey() != null) entity.setAccessKey(dto.getAccessKey());
        if (dto.getSecretKey() != null) entity.setSecretKey(dto.getSecretKey());
        if (dto.getMetadata() != null) entity.setMetadata(dto.getMetadata());
        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());
    }

    private ProviderConfigDto toDto(ProviderConfig entity) {
        return ProviderConfigDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .providerType(entity.getProviderType())
                .baseUrl(entity.getBaseUrl())
                .isActive(entity.getIsActive())
                .accessKey(mask(entity.getAccessKey()))
                .metadata(entity.getMetadata())
                .build();
    }

    private String mask(String input) {
        if (input == null || input.length() < 4) return "****";
        return input.substring(0, 2) + "****" + input.substring(input.length() - 2);
    }
}
