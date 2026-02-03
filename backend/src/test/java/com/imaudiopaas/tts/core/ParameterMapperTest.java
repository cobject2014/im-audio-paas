package com.imaudiopaas.tts.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ParameterMapperTest {

    @Test
    void testAwsSsmlMapping() {
        // Given
        String text = "Hello World";
        Map<String, Object> extra = new HashMap<>();
        extra.put("emotion", "excited");

        // When
        String ssml = ParameterMapper.toAwsSsml(text, extra);

        // Then
        assertTrue(ssml.contains("<speak>"));
        assertTrue(ssml.contains("<amazon:emotion name=\"excited\""));
        assertTrue(ssml.contains("Hello World"));
        assertTrue(ssml.contains("</amazon:emotion>"));
        assertTrue(ssml.contains("</speak>"));
    }

    @Test
    void testAwsSsmlMapping_NoEmotion() {
        // Given
        String text = "Hello World";
        Map<String, Object> extra = new HashMap<>();

        // When
        String ssml = ParameterMapper.toAwsSsml(text, extra);

        // Then
        // Expect either raw text or just speak tags? 
        // Usually providers handle raw text, but if we assume logic wrapper:
        assertEquals("Hello World", ssml); 
        // Or if we decide to always wrap in speak:
        // assertEquals("<speak>Hello World</speak>", ssml);
        // Let's assume defaults behavior: return null or original if no SSML needed?
    }

    @Test
    void testAliyunMapping() {
        // Given
        Map<String, Object> extra = new HashMap<>();
        extra.put("emotion", "sad");

        // When
        // Assuming Aliyun mostly takes a JSON payload or request builder
        // Maybe we just normalize it to consistent keys
        Map<String, Object> aliyunParams = ParameterMapper.toAliyunParams(extra);

        // Then
        assertEquals("sad", aliyunParams.get("emotion"));
    }
}
