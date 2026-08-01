package com.resdatahub.organization;

import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name,
        String shortName,
        String description,
        String website,
        Instant createdAt,
        Instant updatedAt
) {
}
