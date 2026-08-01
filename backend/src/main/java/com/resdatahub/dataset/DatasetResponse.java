package com.resdatahub.dataset;

import java.time.Instant;
import java.util.UUID;

public record DatasetResponse(
        UUID id,
        DatasetOrganizationSummary organization,
        DatasetVersionSummary latestVersion,
        Instant createdAt,
        Instant updatedAt
) {
}
