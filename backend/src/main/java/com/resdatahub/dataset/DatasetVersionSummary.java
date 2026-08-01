package com.resdatahub.dataset;

import java.time.Instant;
import java.util.UUID;

public record DatasetVersionSummary(
        UUID id,
        String versionNumber,
        String title,
        DatasetVersionStatus status,
        Instant publishedAt
) {
}
