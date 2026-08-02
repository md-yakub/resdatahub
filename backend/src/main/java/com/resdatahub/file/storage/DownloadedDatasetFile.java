package com.resdatahub.file.storage;

import java.io.InputStream;

public record DownloadedDatasetFile(
        InputStream inputStream,
        String contentType,
        long fileSize
) {
}
