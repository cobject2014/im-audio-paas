package com.imaudiopaas.tts.core;

import java.util.HashMap;
import java.util.Map;

public class ParameterMapper {

    /**
     * Converts text and extra parameters into AWS SSML format if necessary.
     * Currently supports 'emotion'.
     *
     * @param text The input text.
     * @param extra The extra parameters map.
     * @return SSML string if transformation occurred, otherwise original text.
     */
    public static String toAwsSsml(String text, Map<String, Object> extra) {
        if (extra == null || extra.isEmpty()) {
            return text;
        }

        if (extra.containsKey("emotion")) {
            String emotion = (String) extra.get("emotion");
            // Defaulting intensity to medium for now, could be parameterized too
            return String.format("<speak><amazon:emotion name=\"%s\" intensity=\"medium\">%s</amazon:emotion></speak>", 
                    emotion, text);
        }

        return text;
    }

    /**
     * Extracts and normalizes parameters for Aliyun TTS.
     *
     * @param extra The extra parameters map.
     * @return A map suitable for Aliyun API.
     */
    public static Map<String, Object> toAliyunParams(Map<String, Object> extra) {
        Map<String, Object> params = new HashMap<>();
        if (extra == null) {
            return params;
        }

        // Pass through known Aliyun supported parameters
        if (extra.containsKey("emotion")) {
            params.put("emotion", extra.get("emotion"));
        }
        
        // Add other mappings as needed (e.g. intensity -> intensity)
        
        return params;
    }
}
