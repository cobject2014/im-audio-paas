package com.imaudiopaas.tts.core;

import com.imaudiopaas.tts.core.domain.ProviderType;
import com.imaudiopaas.tts.core.domain.TtsRequest;
import com.imaudiopaas.tts.core.domain.TtsResponse;
import com.imaudiopaas.tts.model.ProviderConfig;

public interface TtsProvider {
    /**
     * Synthesize speech from text.
     *
     * @param request The TTS request containing text and options.
     * @param config The provider configuration to use (credentials, etc).
     * @return The response containing the audio stream.
     */
    TtsResponse synthesize(TtsRequest request, ProviderConfig config);

    /**
     * Get the type of this provider.
     *
     * @return The provider type.
     */
    ProviderType getType();
}
