package com.imaudiopaas.tts.repository;

import com.imaudiopaas.tts.model.ProviderRequestLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderRequestLogRepository extends JpaRepository<ProviderRequestLog, Long> {

    @Query("SELECT l.providerName, COUNT(l), " +
            "SUM(CASE WHEN l.isSuccess = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN l.isSuccess = false THEN 1 ELSE 0 END), " +
            "AVG(l.latencyMs) " +
            "FROM ProviderRequestLog l GROUP BY l.providerName")
    List<Object[]> findProviderStatisticsRaw();
}
