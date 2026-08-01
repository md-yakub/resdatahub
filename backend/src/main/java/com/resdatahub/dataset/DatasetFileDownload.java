package com.resdatahub.dataset;

import org.springframework.core.io.InputStreamResource;

public record DatasetFileDownload(
        InputStreamResource resource,
        String filename,
        String contentType,
        long fileSize
) {
}
