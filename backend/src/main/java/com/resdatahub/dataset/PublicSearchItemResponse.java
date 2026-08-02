package com.resdatahub.dataset;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicSearchItemResponse(
        UUID datasetId,
        UUID versionId,
        String versionNumber,
        String title,
        String description,
        OrganizationSummary organization,
        List<CreatorSummary> creators,
        List<String> keywords,
        String licenseCode,
        Instant publishedAt,
        String landingPageUrl
) {

    public record OrganizationSummary(
            UUID id,
            String name,
            String shortName
    ) {
    }

    public record CreatorSummary(
            String givenName,
            String familyName,
            Integer position
    ) {
    }
}
