package com.resdatahub.dataset;

import java.util.UUID;

public record DatasetOrganizationSummary(
        UUID id,
        String name,
        String shortName
) {
}
