package com.resdatahub.dataset;

public record UpdateDatasetVersionRequest(
        String versionNumber,
        String title,
        String description,
        String changeNote
) {
}
