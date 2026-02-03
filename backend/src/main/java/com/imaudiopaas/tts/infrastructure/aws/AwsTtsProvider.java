package com.imaudiopaas.tts.infrastructure.aws;

import com.imaudiopaas.tts.core.TtsProvider;
import com.imaudiopaas.tts.core.domain.AudioFormat;
import com.imaudiopaas.tts.core.domain.ProviderType;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.core.domain.TtsResponse;
import com.imaudiopaas.tts.exception.TtsException;
import com.imaudiopaas.tts.model.ProviderConfig;
import com.imaudiopaas.tts.core.ParameterMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
import software.amazon.awssdk.services.polly.model.VoiceId;
import software.amazon.awssdk.services.polly.model.TextType;

@Slf4j
@Component
public class AwsTtsProvider implements TtsProvider {

    @Override
    public TtsResponse synthesize(TtsRequest request, ProviderConfig config) {
        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(config.getAccessKey(), config.getSecretKey());
            
            // Note: In real prod, clients should be cached/pooled rather than created per request
            PollyClient polly = PollyClient.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.US_EAST_1) // Should be parsed from metadata
                    .build();

            String processedText = ParameterMapper.toAwsSsml(request.getText(), request.getExtraBody());
            TextType textType = processedText.contains("<speak>") ? TextType.SSML : TextType.TEXT;

            SynthesizeSpeechRequest speechRequest = SynthesizeSpeechRequest.builder()
                    .text(processedText)
                    .textType(textType)
                    .voiceId(request.getVoiceId()) // Assumes ID matches AWS VoiceId string
                    .outputFormat(mapFormat(request.getFormat()))
                    .build();

            ResponseInputStream<SynthesizeSpeechResponse> response = polly.synthesizeSpeech(speechRequest);

            return TtsResponse.builder()
                    .audioStream(response)
                    .format(request.getFormat())
                    // Content length might be unknown in stream, but response.response() has metadata?
                    // response.response() returns SynthesizeSpeechResponse which doesn't always have length for stream?
                    // Actually usually it's streamed. we might not know content-length.
                    .contentLength(0) 
                    .build();

        } catch (Exception e) {
            log.error("AWS Polly TTS failed", e);
            throw new TtsException("AWS Polly TTS failed", e);
        }
    }

    private OutputFormat mapFormat(AudioFormat format) {
        switch (format) {
            case PCM: return OutputFormat.PCM;
            default: return OutputFormat.MP3;
        }
    }

    @Override
    public ProviderType getType() {
        return ProviderType.AWS;
    }
}
