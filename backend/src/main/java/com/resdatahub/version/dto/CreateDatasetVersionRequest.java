package com.resdatahub.version.dto;

import com.resdatahub.dataset.entity.Dataset;

import jakarta.validation.constraints.NotBlank;

public record CreateDatasetVersionRequest(
        @NotBlank(message = "Version number is required")
        String versionNumber,

        @NotBlank(message = "Dataset version title is required")
        String title,

        @NotBlank(message = "Dataset version description is required")
        String description,

        @NotBlank(message = "Dataset version change note is required")
        String changeNote
) {
}
