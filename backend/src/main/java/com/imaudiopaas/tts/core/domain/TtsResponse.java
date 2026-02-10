package com.imaudiopaas.tts.core.domain;

import java.io.InputStream;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TtsResponse {
    private InputStream audioStream;
    private AudioFormat format;
    private long contentLength;

    public String getContentType() {
        return format.getContentType();
    }
}
