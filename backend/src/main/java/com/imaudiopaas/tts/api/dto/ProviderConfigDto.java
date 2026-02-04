package com.imaudiopaas.tts.api.dto;

import com.imaudiopaas.tts.core.domain.ProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class ProviderConfigDto {
    private UUID id;
    
    @NotBlank
    private String name;
    
    @NotNull
    private ProviderType providerType;
    
    // Optional because some providers might not need it, but generally they do.
    private String baseUrl;
    
    private String accessKey;
    
    // Secret key should be write-only in API responses (masked), but readable in request?
    // For now, simple DTO.
    private String secretKey;

    // JSON string for additional config (region, appKey, etc.)
    private String metadata;
    
    private Boolean isActive;
}
