package com.resdatahub.dataset;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateDatasetVersionLicenseRequest(
        @NotNull(message = "License id is required")
        UUID licenseId
) {
}
