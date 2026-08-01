package com.resdatahub.organization;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record CreateOrganizationRequest(
        @NotBlank(message = "Organization name is required")
        String name,

        String shortName,

        String description,

        @URL(message = "Website must be a valid URL")
        String website
) {
}
