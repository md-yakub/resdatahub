package com.resdatahub.file.storage;

public record StoredDatasetFile(
        String storageKey,
        String contentType,
        long fileSize,
        String sha256
) {
}
