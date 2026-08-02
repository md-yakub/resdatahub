package com.resdatahub.dataset.dto;

import java.util.UUID;

public record DatasetOrganizationSummary(
        UUID id,
        String name,
        String shortName
) {
}
