package com.resdatahub.version.controller;

import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.version.dto.CreateDatasetVersionRequest;
import com.resdatahub.version.dto.DatasetVersionResponse;
import com.resdatahub.version.dto.UpdateDatasetVersionLicenseRequest;
import com.resdatahub.version.dto.UpdateDatasetVersionRequest;
import com.resdatahub.version.service.DatasetVersionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/datasets/{datasetId}/versions")
public class DatasetVersionController {

    private final DatasetVersionService datasetVersionService;

    public DatasetVersionController(DatasetVersionService datasetVersionService) {
        this.datasetVersionService = datasetVersionService;
    }

    @GetMapping
    @Operation(
            summary = "List dataset versions",
            description = "Returns all versions for a dataset.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dataset versions returned.",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DatasetVersionResponse.class)))
                    ),
                    @ApiResponse(responseCode = "404", description = "Dataset not found.", content = @Content)
            }
    )
    public List<DatasetVersionResponse> getVersions(@PathVariable UUID datasetId) {
        return datasetVersionService.getVersions(datasetId);
    }

    @GetMapping("/{versionId}")
    @Operation(
            summary = "Get dataset version",
            description = "Returns a dataset version by id.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dataset version returned.",
                            content = @Content(schema = @Schema(implementation = DatasetVersionResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Dataset or version not found.", content = @Content)
            }
    )
    public DatasetVersionResponse getVersion(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId
    ) {
        return datasetVersionService.getVersion(datasetId, versionId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create dataset version",
            description = "Creates a new DRAFT version for a dataset.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Dataset version created.",
                            content = @Content(schema = @Schema(implementation = DatasetVersionResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Dataset not found.", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Version number already exists.", content = @Content)
            }
    )
    public DatasetVersionResponse createVersion(
            @PathVariable UUID datasetId,
            @Valid @RequestBody CreateDatasetVersionRequest request
    ) {
        return datasetVersionService.createVersion(datasetId, request);
    }

    @PatchMapping("/{versionId}")
    @Operation(
            summary = "Update dataset version",
            description = "Updates a DRAFT dataset version.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dataset version updated.",
                            content = @Content(schema = @Schema(implementation = DatasetVersionResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Dataset or version not found.", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Version is not editable or version number already exists.", content = @Content)
            }
    )
    public DatasetVersionResponse updateVersion(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId,
            @Valid @RequestBody UpdateDatasetVersionRequest request
    ) {
        return datasetVersionService.updateVersion(datasetId, versionId, request);
    }

    @PatchMapping("/{versionId}/license")
    @Operation(
            summary = "Update dataset version license",
            description = "Sets the license for a DRAFT dataset version.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dataset version license updated.",
                            content = @Content(schema = @Schema(implementation = DatasetVersionResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Dataset, version, or license not found.", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Version is immutable.", content = @Content)
            }
    )
    public DatasetVersionResponse updateLicense(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId,
            @Valid @RequestBody UpdateDatasetVersionLicenseRequest request
    ) {
        return datasetVersionService.updateLicense(datasetId, versionId, request);
    }

    @PostMapping("/{versionId}/publish")
    @Operation(
            summary = "Publish dataset version",
            description = "Publishes a complete DRAFT dataset version and makes it immutable.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dataset version published.",
                            content = @Content(schema = @Schema(implementation = DatasetVersionResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Dataset or version not found.", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Version is not publishable.", content = @Content)
            }
    )
    public DatasetVersionResponse publishVersion(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId
    ) {
        return datasetVersionService.publishVersion(datasetId, versionId);
    }
}
