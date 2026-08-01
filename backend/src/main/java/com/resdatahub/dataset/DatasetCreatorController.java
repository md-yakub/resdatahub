package com.resdatahub.dataset;

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
@RequestMapping("/api/datasets/{datasetId}/versions/{versionId}/creators")
public class DatasetCreatorController {

    private final DatasetCreatorService datasetCreatorService;

    public DatasetCreatorController(DatasetCreatorService datasetCreatorService) {
        this.datasetCreatorService = datasetCreatorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create dataset creator",
            description = "Adds a creator to a DRAFT dataset version.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Dataset creator created.",
                            content = @Content(schema = @Schema(implementation = DatasetCreatorResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Dataset or version not found.", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Version is immutable or creator position already exists.", content = @Content)
            }
    )
    public DatasetCreatorResponse createCreator(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId,
            @Valid @RequestBody CreateDatasetCreatorRequest request
    ) {
        return datasetCreatorService.createCreator(datasetId, versionId, request);
    }

    @GetMapping
    @Operation(
            summary = "List dataset creators",
            description = "Returns all creators for a dataset version.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dataset creators returned.",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DatasetCreatorResponse.class)))
                    ),
                    @ApiResponse(responseCode = "404", description = "Dataset or version not found.", content = @Content)
            }
    )
    public List<DatasetCreatorResponse> getCreators(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId
    ) {
        return datasetCreatorService.getCreators(datasetId, versionId);
    }

    @GetMapping("/{creatorId}")
    @Operation(
            summary = "Get dataset creator",
            description = "Returns a creator by id for a dataset version.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dataset creator returned.",
                            content = @Content(schema = @Schema(implementation = DatasetCreatorResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Dataset, version, or creator not found.", content = @Content)
            }
    )
    public DatasetCreatorResponse getCreator(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId,
            @PathVariable UUID creatorId
    ) {
        return datasetCreatorService.getCreator(datasetId, versionId, creatorId);
    }

    @PatchMapping("/{creatorId}")
    @Operation(
            summary = "Update dataset creator",
            description = "Updates a creator on a DRAFT dataset version.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dataset creator updated.",
                            content = @Content(schema = @Schema(implementation = DatasetCreatorResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Dataset, version, or creator not found.", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Version is immutable or creator position already exists.", content = @Content)
            }
    )
    public DatasetCreatorResponse updateCreator(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId,
            @PathVariable UUID creatorId,
            @Valid @RequestBody UpdateDatasetCreatorRequest request
    ) {
        return datasetCreatorService.updateCreator(datasetId, versionId, creatorId, request);
    }

    @DeleteMapping("/{creatorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete dataset creator",
            description = "Deletes a creator from a DRAFT dataset version.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Dataset creator deleted.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Dataset, version, or creator not found.", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Version is immutable.", content = @Content)
            }
    )
    public void deleteCreator(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId,
            @PathVariable UUID creatorId
    ) {
        datasetCreatorService.deleteCreator(datasetId, versionId, creatorId);
    }
}
