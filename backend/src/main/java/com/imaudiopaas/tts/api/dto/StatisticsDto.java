package com.imaudiopaas.tts.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatisticsDto {
    private String providerName;
    private long totalRequests;
    private long successCount;
    private long failureCount;
    private Double successRate;
    private Double avgLatencyMs;
}
