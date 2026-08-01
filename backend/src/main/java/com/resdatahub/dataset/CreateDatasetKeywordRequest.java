package com.resdatahub.dataset;

import jakarta.validation.constraints.NotBlank;

public record CreateDatasetKeywordRequest(
        @NotBlank(message = "Keyword value is required")
        String value
) {
}
