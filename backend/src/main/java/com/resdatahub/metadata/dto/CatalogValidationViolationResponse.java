package com.resdatahub.metadata.dto;

public record CatalogValidationViolationResponse(
        String resource,
        String property,
        ValidationSeverity severity,
        String message
) {
}
