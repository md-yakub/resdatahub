package com.resdatahub.publicapi.dto;

import com.resdatahub.file.entity.DatasetFileCategory;
import com.resdatahub.version.entity.DatasetVersionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicDatasetResponse(
        UUID datasetId,
        PublicOrganizationResponse organization,
        PublicDatasetVersionResponse version,
        List<PublicCreatorResponse> creators,
        List<PublicKeywordResponse> keywords,
        PublicLicenseResponse license,
        List<PublicFileResponse> files
) {

    public record PublicOrganizationResponse(
            UUID id,
            String name,
            String shortName,
            String website
    ) {
    }

    public record PublicDatasetVersionResponse(
            UUID versionId,
            String versionNumber,
            String title,
            String description,
            String changeNote,
            DatasetVersionStatus status,
            Instant publishedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record PublicCreatorResponse(
            UUID id,
            String givenName,
            String familyName,
            String affiliation,
            String orcid,
            Integer position
    ) {
    }

    public record PublicKeywordResponse(
            UUID id,
            String value
    ) {
    }

    public record PublicLicenseResponse(
            UUID id,
            String code,
            String name,
            String uri
    ) {
    }

    public record PublicFileResponse(
            UUID id,
            String originalFilename,
            String contentType,
            long fileSize,
            String sha256,
            DatasetFileCategory category,
            Instant createdAt,
            String downloadUrl
    ) {
    }
}
