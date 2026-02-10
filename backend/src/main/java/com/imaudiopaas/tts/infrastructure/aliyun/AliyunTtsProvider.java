package com.imaudiopaas.tts.infrastructure.aliyun;

import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizer;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizerListener;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imaudiopaas.tts.core.domain.ProviderType;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.core.domain.TtsResponse;
import com.imaudiopaas.tts.exception.TtsException;
import com.imaudiopaas.tts.model.ProviderConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@Primary
public class AliyunTtsProvider extends AliyunBaseProvider {

    public AliyunTtsProvider(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public ProviderType getType() {
        return ProviderType.ALIYUN;
    }

    @Override
    public TtsResponse synthesize(TtsRequest request, ProviderConfig config) {
        String appKey = parseAppKey(config.getMetadata());
        if (appKey == null || appKey.isEmpty()) {
            throw new TtsException("Aliyun AppKey is missing in provider config metadata");
        }

        NlsClient client = getClient(config);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        AtomicReference<String> errorMsg = new AtomicReference<>();
        
        SpeechSynthesizerListener listener = new SpeechSynthesizerListener() {
            @Override
            public void onMessage(ByteBuffer message) {
                try {
                    byte[] bytesArray = new byte[message.remaining()];
                    message.get(bytesArray, 0, bytesArray.length);
                    outputStream.write(bytesArray);
                } catch (IOException e) {
                    log.error("Error writing audio bytes", e);
                }
            }

            @Override
            public void onComplete(SpeechSynthesizerResponse response) {
                log.debug("TTS Complete. taskId: {}", response.getTaskId());
            }

            @Override
            public void onFail(SpeechSynthesizerResponse response) {
                String msg = String.format("TTS Failed: status=%d, text=%s, task_id=%s", 
                    response.getStatus(), response.getStatusText(), response.getTaskId());
                log.error(msg);
                errorMsg.set(msg);
            }
        };

        SpeechSynthesizer synthesizer = null;
        try {
            synthesizer = new SpeechSynthesizer(client, listener);
            synthesizer.setAppKey(appKey);
            synthesizer.setText(request.getText());
            synthesizer.setFormat(mapToOutputFormat(request.getFormat()));
            synthesizer.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K); // Default to 16k
            
            // Set voice if specifically requested (skipping generic defaults to allow Console override)
            String voiceId = request.getVoiceId();
            if (voiceId != null && !voiceId.isEmpty() && 
                !voiceId.equalsIgnoreCase("aliyun") && 
                !voiceId.equalsIgnoreCase("default")) {
                synthesizer.setVoice(voiceId);
            }

            Map<String, Object> extra = request.getExtraBody();
            if (extra != null) {
                if (extra.containsKey("volume")) synthesizer.setVolume(toInt(extra.get("volume")));
                if (extra.containsKey("speech_rate")) synthesizer.setSpeechRate(toInt(extra.get("speech_rate")));
                if (extra.containsKey("pitch_rate")) synthesizer.setPitchRate(toInt(extra.get("pitch_rate")));
            }

            log.info("Sending request to Aliyun Standard TTS. AppKey: [{}], Voice: [{}], Text: [{}...]",
                    appKey, request.getVoiceId(), 
                    request.getText() != null && request.getText().length() > 20 ? request.getText().substring(0, 20) : request.getText());

            synthesizer.start();
            synthesizer.waitForComplete();
            
        } catch (Exception e) {
            throw new TtsException("Error during Aliyun TTS synthesis execution", e);
        } finally {
            if (synthesizer != null) synthesizer.close();
        }

        if (errorMsg.get() != null) {
            throw new TtsException(errorMsg.get());
        }

        byte[] audioData = outputStream.toByteArray();
        if (audioData.length == 0) {
            throw new TtsException("Aliyun TTS returned empty audio data");
        }

        return TtsResponse.builder()
                .audioStream(new ByteArrayInputStream(audioData))
                .format(request.getFormat())
                .contentLength(audioData.length)
                .build();
    }
}
