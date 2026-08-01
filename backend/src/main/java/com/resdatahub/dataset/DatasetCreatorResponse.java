package com.resdatahub.dataset;

import java.time.Instant;
import java.util.UUID;

public record DatasetCreatorResponse(
        UUID id,
        UUID datasetVersionId,
        String givenName,
        String familyName,
        String affiliation,
        String orcid,
        Integer position,
        Instant createdAt,
        Instant updatedAt
) {
}
