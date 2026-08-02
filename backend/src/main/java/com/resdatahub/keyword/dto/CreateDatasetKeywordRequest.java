package com.resdatahub.keyword.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDatasetKeywordRequest(
        @NotBlank(message = "Keyword value is required")
        String value
) {
}
