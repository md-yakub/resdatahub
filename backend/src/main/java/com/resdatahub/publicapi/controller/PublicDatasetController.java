package com.resdatahub.publicapi.controller;

import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.publicapi.dto.PublicDatasetResponse;
import com.resdatahub.publicapi.service.PublicDatasetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/datasets")
public class PublicDatasetController {

    private final PublicDatasetService publicDatasetService;

    public PublicDatasetController(PublicDatasetService publicDatasetService) {
        this.publicDatasetService = publicDatasetService;
    }

    @GetMapping("/{datasetId}/versions/{versionId}")
    @Operation(
            summary = "Get public published dataset version",
            description = "Returns the complete public record for a published dataset version.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Published dataset version returned.",
                            content = @Content(schema = @Schema(implementation = PublicDatasetResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Dataset, version, or published version not found.",
                            content = @Content
                    )
            }
    )
    public PublicDatasetResponse getPublishedDatasetVersion(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId
    ) {
        return publicDatasetService.getPublishedDatasetVersion(datasetId, versionId);
    }
}
