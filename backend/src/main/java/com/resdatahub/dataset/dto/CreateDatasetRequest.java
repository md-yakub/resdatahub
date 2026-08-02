package com.resdatahub.dataset.dto;

import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.organization.entity.Organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateDatasetRequest(
        @NotBlank(message = "Dataset title is required")
        String title,

        String description,

        @NotNull(message = "Organization id is required")
        UUID organizationId
) {
}
