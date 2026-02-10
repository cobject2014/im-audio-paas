package com.imaudiopaas.tts.infrastructure.aliyun;

import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisAudioFormat;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisResult;
import com.alibaba.dashscope.utils.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imaudiopaas.tts.core.TtsProvider;
import com.imaudiopaas.tts.core.domain.AudioFormat;
import com.imaudiopaas.tts.core.domain.ProviderType;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.core.domain.TtsResponse;
import com.imaudiopaas.tts.exception.TtsException;
import com.imaudiopaas.tts.model.ProviderConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

@Slf4j
@Component
public class AliyunCosyVoiceProvider implements TtsProvider {

    private final ObjectMapper objectMapper;

    public AliyunCosyVoiceProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ProviderType getType() {
        return ProviderType.ALIYUN_COSYVOICE;
    }

    @Override
    public TtsResponse synthesize(TtsRequest request, ProviderConfig config) {
        // 1. Get API Key
        String apiKey = config.getSecretKey();
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = parseFromMetadata(config.getMetadata(), "apiKey");
        }
        
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TtsException("DashScope API Key is missing. Please configure it in 'Access Key Secret' or metadata 'apiKey'.");
        }

        Constants.apiKey = apiKey;

        // 2. Prepare Parameters
        String model = "cosyvoice-v1";
        String voice = "longxiaochun";
        int sampleRate = 22050; // Default
        
        Map<String, Object> extra = request.getExtraBody();
        if (extra != null) {
            if (extra.containsKey("model")) model = extra.get("model").toString();
            if (extra.containsKey("sample_rate")) sampleRate = toInt(extra.get("sample_rate"));
        }
        
        if (request.getVoiceId() != null && !request.getVoiceId().isEmpty()) {
            voice = request.getVoiceId();
        }

        var builder = SpeechSynthesisParam.builder()
                .model(model)
                .voice(voice);

        // Map Format
        boolean isPcm = (request.getFormat() == AudioFormat.PCM);
        SpeechSynthesisAudioFormat dashFormat = mapFormat(isPcm, sampleRate);
        builder.format(dashFormat);
        
        if (extra != null) {
            if (extra.containsKey("volume")) builder.volume(toInt(extra.get("volume")));
            if (extra.containsKey("speech_rate")) builder.speechRate(Float.valueOf(extra.get("speech_rate").toString()));
            if (extra.containsKey("pitch_rate")) builder.pitchRate(Float.valueOf(extra.get("pitch_rate").toString()));
        }

        SpeechSynthesisParam param = builder.build();
        
        // 3. Synthesize
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(param, null);
        
        try {
            // call() blocks and returns full audio
            ByteBuffer audioBuffer = synthesizer.call(request.getText());
            
            if (audioBuffer == null || audioBuffer.remaining() == 0) {
                 throw new TtsException("CosyVoice returned empty audio");
            }

            byte[] audioData = new byte[audioBuffer.remaining()];
            audioBuffer.get(audioData);

            return TtsResponse.builder()
                    .audioStream(new ByteArrayInputStream(audioData))
                    .format(isPcm ? AudioFormat.PCM : AudioFormat.MP3)
                    .contentLength(audioData.length)
                    .build();

        } catch (Exception e) {
            log.error("CosyVoice synthesis failed", e);
            throw new TtsException("CosyVoice synthesis failed: " + e.getMessage(), e);
        }
    }

    private SpeechSynthesisAudioFormat mapFormat(boolean isPcm, int rate) {
        if (isPcm) {
            if (rate == 8000) return SpeechSynthesisAudioFormat.PCM_8000HZ_MONO_16BIT;
            if (rate == 16000) return SpeechSynthesisAudioFormat.PCM_16000HZ_MONO_16BIT;
            if (rate == 24000) return SpeechSynthesisAudioFormat.PCM_24000HZ_MONO_16BIT;
            if (rate == 48000) return SpeechSynthesisAudioFormat.PCM_48000HZ_MONO_16BIT;
            return SpeechSynthesisAudioFormat.PCM_22050HZ_MONO_16BIT;
        } else {
            if (rate == 8000) return SpeechSynthesisAudioFormat.MP3_8000HZ_MONO_128KBPS;
            if (rate == 16000) return SpeechSynthesisAudioFormat.MP3_16000HZ_MONO_128KBPS;
            if (rate == 24000) return SpeechSynthesisAudioFormat.MP3_24000HZ_MONO_256KBPS;
            if (rate == 48000) return SpeechSynthesisAudioFormat.MP3_48000HZ_MONO_256KBPS;
            return SpeechSynthesisAudioFormat.MP3_22050HZ_MONO_256KBPS;
        }
    }

    private String parseFromMetadata(String metadataJson, String key) {
        if (metadataJson == null) return null;
        try {
            JsonNode root = objectMapper.readTree(metadataJson);
            if (root.has(key)) {
                return root.get(key).asText();
            }
        } catch (Exception e) {
            log.warn("Failed to parse metadata", e);
        }
        return null;
    }

    private int toInt(Object obj) {
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            return Integer.parseInt(obj.toString());
        } catch (Exception e) {
            return 0;
        }
    }
}
