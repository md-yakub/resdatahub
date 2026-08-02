package com.resdatahub.version.dto;

import com.resdatahub.license.dto.LicenseResponse;
import com.resdatahub.version.entity.DatasetVersionStatus;

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
