package com.resdatahub.dataset.dto;

import com.resdatahub.version.dto.DatasetVersionSummary;

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
