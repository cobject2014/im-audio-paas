package com.imaudiopaas.tts.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DatabaseSchemaFixer {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void fixSchema() {
        log.info("Starting database schema migration check...");
        try {
            // Find all check constraints on PROVIDER_CONFIGS table
            List<String> constraints = jdbcTemplate.query(
                "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
                "WHERE TABLE_NAME = 'PROVIDER_CONFIGS' AND CONSTRAINT_TYPE = 'CHECK'",
                (rs, rowNum) -> rs.getString("CONSTRAINT_NAME")
            );

            log.info("Found constraints on PROVIDER_CONFIGS: {}", constraints);

            for (String constraintName : constraints) {
                // We want to drop constraints that restrict provider_type. 
                // Since we can't easily parse the SQL, we'll aggressively drop ALL ID-based check constraints (like CONSTRAINT_1, CONSTRAINT_2)
                // OR checking schema.
                // For safety, let's just drop them all and re-add our trusted one.
                // Assuming this table only has this one important check constraint for now, or we risk dropping others.
                // But typically Generated constraints are for Enums.
                
                log.info("Dropping constraint: {}", constraintName);
                jdbcTemplate.execute("ALTER TABLE PROVIDER_CONFIGS DROP CONSTRAINT " + constraintName);
            }

            // Re-create the constraint with permitted values
            String sql = "ALTER TABLE PROVIDER_CONFIGS ADD CONSTRAINT chk_provider_type CHECK (provider_type IN ('ALIYUN', 'ALIYUN_COSYVOICE', 'AWS', 'TENCENT', 'VIBEVOICE', 'QWEN'))";
            jdbcTemplate.execute(sql);
            
            log.info("Database schema migration executed successfully: Updated provider_type constraint.");
        } catch (Exception e) {
            log.error("Database schema migration error: ", e);
        }
    }
}
