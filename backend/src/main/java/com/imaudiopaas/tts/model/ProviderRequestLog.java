package com.imaudiopaas.tts.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "provider_request_logs")
public class ProviderRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_name", nullable = false)
    private String providerName;

    @Column(name = "request_time", nullable = false)
    private LocalDateTime requestTime;

    @Column(name = "is_success", nullable = false)
    private boolean isSuccess;

    @Column(name = "error_message", length = 2048)
    private String errorMessage;

    @Column(name = "latency_ms")
    private Long latencyMs;
}
