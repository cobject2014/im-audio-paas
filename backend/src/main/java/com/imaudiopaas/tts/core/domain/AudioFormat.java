package com.imaudiopaas.tts.core.domain;

public enum AudioFormat {
    MP3("audio/mpeg"),
    OPUS("audio/opus"),
    AAC("audio/aac"),
    FLAC("audio/flac"),
    PCM("audio/pcm"),
    WAV("audio/wav");

    private final String contentType;

    AudioFormat(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}
