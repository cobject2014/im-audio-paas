package com.imaudiopaas.tts.core.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TtsRequest {
    @NotBlank
    private String text;

    /**
     * The voice ID to use.
     * Can be a mapped ID (e.g., "aliyun-xiaoyun") or a provider-specific ID.
     */
    private String voiceId;

    /**
     * The model to use. Can be provider specific or generic like "tts-1".
     */
    private String model;

    /**
     * Speed of the generated audio. 1.0 is normal.
     */
    @Builder.Default
    private Float speed = 1.0f;

    /**
     * The requested audio format.
     */
    @Builder.Default
    private AudioFormat format = AudioFormat.MP3;

    /**
     * Whether to stream the response.
     */
    @Builder.Default
    private boolean stream = false;

    /**
     * Extended parameters for specific providers (emotion, tone).
     * Mapped from JSON field `extra_body`.
     */
    private java.util.Map<String, Object> extraBody;
}
