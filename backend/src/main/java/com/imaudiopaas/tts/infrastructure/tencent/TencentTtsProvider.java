package com.imaudiopaas.tts.infrastructure.tencent;

import com.imaudiopaas.tts.core.TtsProvider;
import com.imaudiopaas.tts.core.domain.AudioFormat;
import com.imaudiopaas.tts.core.domain.ProviderType;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.core.domain.TtsResponse;
import com.imaudiopaas.tts.exception.TtsException;
import com.imaudiopaas.tts.model.ProviderConfig;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.tts.v20190823.TtsClient;
import com.tencentcloudapi.tts.v20190823.models.TextToVoiceRequest;
import com.tencentcloudapi.tts.v20190823.models.TextToVoiceResponse;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TencentTtsProvider implements TtsProvider {

    private static final String REGION = "ap-shanghai"; // Should be configurable via metadata

    @Override
    public TtsResponse synthesize(TtsRequest request, ProviderConfig config) {
        try {
            Credential cred = new Credential(config.getAccessKey(), config.getSecretKey());
            TtsClient client = new TtsClient(cred, REGION);

            TextToVoiceRequest req = new TextToVoiceRequest();
            req.setText(request.getText());
            req.setSessionId(UUID.randomUUID().toString());
            req.setModelType(1L);
            // Need to parse VoiceId to Long? Or pass string? Tencent API uses Long VoiceType often.
            // Assumption: VoiceId in request is numeric string like "101001"
            req.setVoiceType(Long.parseLong(request.getVoiceId()));
            req.setCodec(mapFormat(request.getFormat()));

            TextToVoiceResponse resp = client.TextToVoice(req);
            
            byte[] audioBytes = Base64.getDecoder().decode(resp.getAudio());

            return TtsResponse.builder()
                    .audioStream(new ByteArrayInputStream(audioBytes))
                    .format(request.getFormat())
                    .contentLength(audioBytes.length)
                    .build();

        } catch (Exception e) {
            log.error("Tencent TTS failed", e);
            throw new TtsException("Tencent TTS failed", e);
        }
    }

    private String mapFormat(AudioFormat format) {
        switch (format) {
            case WAV: return "wav";
            case PCM: return "pcm";
            default: return "mp3";
        }
    }

    @Override
    public ProviderType getType() {
        return ProviderType.TENCENT;
    }
}
