package com.resdatahub.dataset;

import java.io.InputStream;

public record DownloadedDatasetFile(
        InputStream inputStream,
        String contentType,
        long fileSize
) {
}
