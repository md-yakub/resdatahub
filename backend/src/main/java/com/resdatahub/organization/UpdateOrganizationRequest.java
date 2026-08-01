package com.resdatahub.organization;

import org.hibernate.validator.constraints.URL;

public record UpdateOrganizationRequest(
        String name,

        String shortName,

        String description,

        @URL(message = "Website must be a valid URL")
        String website
) {
}
