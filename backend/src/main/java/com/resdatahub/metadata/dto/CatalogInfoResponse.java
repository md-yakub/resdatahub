package com.resdatahub.metadata.dto;

import java.util.List;

public record CatalogInfoResponse(
        String title,
        String profile,
        String metadataEndpoint,
        List<String> supportedFormats,
        String publicBaseUrl,
        String contact
) {
}
