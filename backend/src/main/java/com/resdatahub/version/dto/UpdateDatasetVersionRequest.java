package com.resdatahub.version.dto;

public record UpdateDatasetVersionRequest(
        String versionNumber,
        String title,
        String description,
        String changeNote
) {
}
