package com.resdatahub.dataset;

public record UpdateDatasetRequest(
        String title,
        String description,
        DatasetStatus status
) {
}
