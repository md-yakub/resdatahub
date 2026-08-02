package com.resdatahub.version.dto;

import com.resdatahub.version.entity.DatasetVersionStatus;

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
