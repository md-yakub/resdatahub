package com.resdatahub.creator.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record UpdateDatasetCreatorRequest(
        String givenName,
        String familyName,
        String affiliation,

        @Pattern(
                regexp = "\\d{4}-\\d{4}-\\d{4}-\\d{4}",
                message = "ORCID must use the format 0000-0000-0000-0000"
        )
        String orcid,

        @Positive(message = "Creator position must be positive")
        Integer position
) {
}
