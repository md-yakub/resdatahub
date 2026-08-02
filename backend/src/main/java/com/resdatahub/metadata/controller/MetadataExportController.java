package com.resdatahub.metadata.controller;

import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.metadata.dto.MetadataFormat;
import com.resdatahub.metadata.service.MetadataExportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/datasets")
public class MetadataExportController {

    private final MetadataExportService metadataExportService;

    public MetadataExportController(MetadataExportService metadataExportService) {
        this.metadataExportService = metadataExportService;
    }

    @GetMapping(
            value = "/{datasetId}/versions/{versionId}/metadata",
            produces = {"text/turtle", "application/ld+json", "application/rdf+xml"}
    )
    @Operation(
            summary = "Export published dataset metadata",
            description = "Returns DCAT/RDF metadata for a published dataset version.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "RDF metadata returned."),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Dataset, version, or published version not found.",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<String> exportMetadata(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId,
            @Parameter(description = "RDF serialization format.") @RequestParam(defaultValue = "TURTLE") MetadataFormat format
    ) {
        String metadata = metadataExportService.exportMetadata(datasetId, versionId, format);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(format.getContentType()))
                .body(metadata);
    }
}
