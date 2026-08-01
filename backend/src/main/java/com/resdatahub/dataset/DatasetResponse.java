package com.resdatahub.dataset;

import java.time.Instant;
import java.util.UUID;

public record DatasetResponse(
        UUID id,
        String title,
        String description,
        UUID organizationId,
        String organizationName,
        DatasetStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
