package com.resdatahub.dataset;

import java.time.Instant;
import java.util.UUID;

public record DatasetKeywordResponse(
        UUID id,
        UUID datasetVersionId,
        String value,
        Instant createdAt
) {
}
