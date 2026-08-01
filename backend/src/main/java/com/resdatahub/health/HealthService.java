package com.resdatahub.health;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    private final JdbcTemplate jdbcTemplate;

    public HealthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public HealthResponse getHealth() {
        verifyDatabaseConnection();
        return new HealthResponse("UP", "ResDataHub", "UP");
    }

    private void verifyDatabaseConnection() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

        if (result == null || result != 1) {
            throw new IllegalStateException("Database health check failed");
        }
    }
}
