package com.resdatahub.metadata.dto;

import java.util.List;

public record CatalogValidationResponse(
        String profile,
        boolean conforms,
        long checkedDatasets,
        List<CatalogValidationViolationResponse> violations
) {
}
