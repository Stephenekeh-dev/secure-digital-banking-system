package com.steve.auth_service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseKeepAliveScheduler {

    private final JdbcTemplate jdbcTemplate;

    // Runs every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void keepDatabaseAlive() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.debug("Database keep-alive ping successful");
        } catch (Exception e) {
            log.warn("Database keep-alive ping failed: {}", e.getMessage());
        }
    }
}