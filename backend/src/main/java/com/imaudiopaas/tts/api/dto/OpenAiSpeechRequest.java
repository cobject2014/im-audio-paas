package com.imaudiopaas.tts.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OpenAiSpeechRequest {
    
    @NotBlank(message = "Model is required (e.g., tts-1)")
    private String model;

    @NotBlank(message = "Input text is required")
    private String input;

    @NotBlank(message = "Voice is required")
    private String voice;

    @JsonProperty("response_format")
    private String responseFormat = "mp3";

    private Float speed = 1.0f;
}
