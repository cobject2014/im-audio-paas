package com.imaudiopaas.tts.api;

import com.imaudiopaas.tts.core.domain.ProviderType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/debug")
public class DebugController {

    @GetMapping("/providers")
    public List<String> getValidProviders() {
        return Arrays.stream(ProviderType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
