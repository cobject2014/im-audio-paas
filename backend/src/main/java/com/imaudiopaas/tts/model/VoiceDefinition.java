package com.imaudiopaas.tts.model;

import com.imaudiopaas.tts.core.domain.ProviderType;
import com.imaudiopaas.tts.core.domain.VoiceGender;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Data;

@Data
@Entity
@Table(name = "voice_definitions")
public class VoiceDefinition {

    @Id
    private String id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false)
    private ProviderType providerType;

    @NotBlank
    @Column(name = "native_voice_id", nullable = false)
    private String nativeVoiceId;

    @NotBlank
    @Column(name = "display_name", nullable = false)
    private String displayName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoiceGender gender;

    @ElementCollection
    @CollectionTable(name = "voice_styles", joinColumns = @JoinColumn(name = "voice_id"))
    @Column(name = "style")
    private Set<String> styles;
}
