package com.resdatahub.dataset;

import java.time.Instant;
import java.util.UUID;

public record DatasetVersionResponse(
        UUID id,
        UUID datasetId,
        String versionNumber,
        String title,
        String description,
        String changeNote,
        DatasetVersionStatus status,
        LicenseResponse license,
        Instant createdAt,
        Instant updatedAt,
        Instant publishedAt
) {
}
