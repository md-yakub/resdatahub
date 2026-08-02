package com.resdatahub.creator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CreateDatasetCreatorRequest(
        @NotBlank(message = "Creator given name is required")
        String givenName,

        @NotBlank(message = "Creator family name is required")
        String familyName,

        String affiliation,

        @Pattern(
                regexp = "\\d{4}-\\d{4}-\\d{4}-\\d{4}",
                message = "ORCID must use the format 0000-0000-0000-0000"
        )
        String orcid,

        @NotNull(message = "Creator position is required")
        @Positive(message = "Creator position must be positive")
        Integer position
) {
}
