package com.resdatahub.dataset;

public record StoredDatasetFile(
        String storageKey,
        String contentType,
        long fileSize,
        String sha256
) {
}
