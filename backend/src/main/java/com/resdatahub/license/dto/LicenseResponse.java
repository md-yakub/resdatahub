package com.resdatahub.license.dto;

import java.time.Instant;
import java.util.UUID;

public record LicenseResponse(
        UUID id,
        String code,
        String name,
        String uri,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
