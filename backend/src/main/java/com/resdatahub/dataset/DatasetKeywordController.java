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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/datasets/{datasetId}/versions/{versionId}/keywords")
public class DatasetKeywordController {

    private final DatasetKeywordService datasetKeywordService;

    public DatasetKeywordController(DatasetKeywordService datasetKeywordService) {
        this.datasetKeywordService = datasetKeywordService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create dataset keyword",
            description = "Adds a keyword to a DRAFT dataset version.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Keyword created.",
                            content = @Content(schema = @Schema(implementation = DatasetKeywordResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Dataset or version not found.", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Version is immutable or keyword already exists.", content = @Content)
            }
    )
    public DatasetKeywordResponse createKeyword(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId,
            @Valid @RequestBody CreateDatasetKeywordRequest request
    ) {
        return datasetKeywordService.createKeyword(datasetId, versionId, request);
    }

    @GetMapping
    @Operation(
            summary = "List dataset keywords",
            description = "Returns keywords for a dataset version.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Keywords returned.",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DatasetKeywordResponse.class)))
                    ),
                    @ApiResponse(responseCode = "404", description = "Dataset or version not found.", content = @Content)
            }
    )
    public List<DatasetKeywordResponse> getKeywords(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId
    ) {
        return datasetKeywordService.getKeywords(datasetId, versionId);
    }

    @DeleteMapping("/{keywordId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete dataset keyword",
            description = "Deletes a keyword from a DRAFT dataset version.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Keyword deleted.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Dataset, version, or keyword not found.", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Version is immutable.", content = @Content)
            }
    )
    public void deleteKeyword(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId,
            @PathVariable UUID keywordId
    ) {
        datasetKeywordService.deleteKeyword(datasetId, versionId, keywordId);
    }
}
