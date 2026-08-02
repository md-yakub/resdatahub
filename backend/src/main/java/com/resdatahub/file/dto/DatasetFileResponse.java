package com.resdatahub.file.dto;

import com.resdatahub.file.entity.DatasetFileCategory;

import java.time.Instant;
import java.util.UUID;

public record DatasetFileResponse(
        UUID id,
        UUID datasetVersionId,
        String originalFilename,
        String contentType,
        long fileSize,
        String sha256,
        DatasetFileCategory category,
        Instant createdAt
) {
}
