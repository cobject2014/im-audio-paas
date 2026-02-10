package com.imaudiopaas.tts.api;

import com.imaudiopaas.tts.api.dto.OpenAiSpeechRequest;
import com.imaudiopaas.tts.core.domain.AudioFormat;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.core.domain.TtsResponse;
import com.imaudiopaas.tts.service.ProviderRoutingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/audio/speech")
@RequiredArgsConstructor
public class OpenAiSpeechController {

    private final ProviderRoutingService routingService;

    @PostMapping(produces = "audio/mpeg") // Default, but overridden by response
    public ResponseEntity<Resource> generateSpeech(@Valid @RequestBody OpenAiSpeechRequest request) {
        
        // Map DTO to Domain Request
        TtsRequest domainRequest = TtsRequest.builder()
                .text(request.getInput())
                .model(request.getModel())
                .voiceId(request.getVoice())
                .speed(request.getSpeed() != null ? request.getSpeed() : 1.0f)
                .format(mapFormat(request.getResponseFormat()))
                .build();

        // Execute
        TtsResponse response = routingService.routeAndSynthesize(domainRequest);

        // Build Response
        InputStreamResource resource = new InputStreamResource(response.getAudioStream());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.getContentType()))
                .contentLength(response.getContentLength() > 0 ? response.getContentLength() : -1) // Chunked if unknown
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"speech." + request.getResponseFormat() + "\"")
                .body(resource);
    }

    private AudioFormat mapFormat(String format) {
        if (format == null) return AudioFormat.MP3;
        switch (format.toLowerCase()) {
            case "opus": return AudioFormat.OPUS;
            case "aac": return AudioFormat.AAC;
            case "flac": return AudioFormat.FLAC;
            case "wav": return AudioFormat.WAV;
            case "pcm": return AudioFormat.PCM;
            default: return AudioFormat.MP3;
        }
    }
}
