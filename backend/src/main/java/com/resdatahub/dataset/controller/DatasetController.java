package com.resdatahub.dataset.controller;

import com.resdatahub.dataset.dto.CreateDatasetRequest;
import com.resdatahub.dataset.dto.DatasetResponse;
import com.resdatahub.dataset.dto.UpdateDatasetRequest;
import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.dataset.service.DatasetService;
import com.resdatahub.organization.entity.Organization;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/datasets")
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create dataset",
            description = "Creates a new dataset in DRAFT status for an existing organization.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Dataset created.",
                            content = @Content(schema = @Schema(implementation = DatasetResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Organization not found.", content = @Content)
            }
    )
    public DatasetResponse createDataset(
            @Valid @RequestBody CreateDatasetRequest request
    ) {
        return datasetService.createDataset(request);
    }

    @GetMapping
    @Operation(
            summary = "List datasets",
            description = "Returns all datasets.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Datasets returned.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DatasetResponse.class)))
            )
    )
    public List<DatasetResponse> getDatasets() {
        return datasetService.getDatasets();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get dataset",
            description = "Returns a dataset by id.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dataset returned.",
                            content = @Content(schema = @Schema(implementation = DatasetResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Dataset not found.", content = @Content)
            }
    )
    public DatasetResponse getDataset(@PathVariable UUID id) {
        return datasetService.getDataset(id);
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update dataset",
            description = "Updates basic dataset fields.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dataset updated.",
                            content = @Content(schema = @Schema(implementation = DatasetResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Dataset not found.", content = @Content)
            }
    )
    public DatasetResponse updateDataset(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDatasetRequest request
    ) {
        return datasetService.updateDataset(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete dataset",
            description = "Deletes a dataset by id.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Dataset deleted.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Dataset not found.", content = @Content)
            }
    )
    public void deleteDataset(@PathVariable UUID id) {
        datasetService.deleteDataset(id);
    }
}
