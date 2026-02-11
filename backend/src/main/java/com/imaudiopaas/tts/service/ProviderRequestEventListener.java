package com.imaudiopaas.tts.service;

import com.imaudiopaas.tts.core.event.ProviderRequestEvent;
import com.imaudiopaas.tts.model.ProviderRequestLog;
import com.imaudiopaas.tts.repository.ProviderRequestLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProviderRequestEventListener {

    private final ProviderRequestLogRepository repository;

    @Async
    @EventListener
    public void handleProviderRequestEvent(ProviderRequestEvent event) {
        log.debug("Received ProviderRequestEvent for provider: {}", event.getProviderName());
        try {
            ProviderRequestLog logEntry = new ProviderRequestLog();
            logEntry.setProviderName(event.getProviderName());
            logEntry.setRequestTime(event.getRequestTime());
            logEntry.setSuccess(event.isSuccess());
            logEntry.setErrorMessage(event.getErrorMessage());
            logEntry.setLatencyMs(event.getLatencyMs());

            repository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to save provider request log", e);
        }
    }
}
