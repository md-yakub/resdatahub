package com.resdatahub.keyword.dto;

import java.time.Instant;
import java.util.UUID;

public record DatasetKeywordResponse(
        UUID id,
        UUID datasetVersionId,
        String value,
        Instant createdAt
) {
}
