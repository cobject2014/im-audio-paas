package com.imaudiopaas.tts.core.event;

import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProviderRequestEvent extends ApplicationEvent {
    private final String providerName;
    private final boolean isSuccess;
    private final Long latencyMs;
    private final String errorMessage;
    private final LocalDateTime requestTime;

    public ProviderRequestEvent(Object source, String providerName, boolean isSuccess, Long latencyMs, String errorMessage) {
        super(source);
        this.providerName = providerName;
        this.isSuccess = isSuccess;
        this.latencyMs = latencyMs;
        this.errorMessage = errorMessage;
        this.requestTime = LocalDateTime.now();
    }
}
