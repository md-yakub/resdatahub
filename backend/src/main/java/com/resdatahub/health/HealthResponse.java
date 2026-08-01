package com.resdatahub.health;

public record HealthResponse(
        String status,
        String application,
        String database
) {
}
