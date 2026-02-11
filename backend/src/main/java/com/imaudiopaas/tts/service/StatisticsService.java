package com.imaudiopaas.tts.service;

import com.imaudiopaas.tts.api.dto.StatisticsDto;
import com.imaudiopaas.tts.repository.ProviderRequestLogRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ProviderRequestLogRepository repository;

    public List<StatisticsDto> getProviderStatistics() {
        List<Object[]> results = repository.findProviderStatisticsRaw();
        return results.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private StatisticsDto mapToDto(Object[] row) {
        String providerName = (String) row[0];
        long total = ((Number) row[1]).longValue();
        long success = row[2] == null ? 0 : ((Number) row[2]).longValue();
        long failure = row[3] == null ? 0 : ((Number) row[3]).longValue();
        Double avgLatency = row[4] == null ? 0.0 : ((Number) row[4]).doubleValue();

        double successRate = total == 0 ? 0.0 : (double) success / total;

        return StatisticsDto.builder()
                .providerName(providerName)
                .totalRequests(total)
                .successCount(success)
                .failureCount(failure)
                .avgLatencyMs(avgLatency)
                .successRate(successRate)
                .build();
    }
}
