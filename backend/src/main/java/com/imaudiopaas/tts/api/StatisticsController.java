package com.imaudiopaas.tts.api;

import com.imaudiopaas.tts.api.dto.StatisticsDto;
import com.imaudiopaas.tts.service.StatisticsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping
    public List<StatisticsDto> getStatistics() {
        return statisticsService.getProviderStatistics();
    }
}
