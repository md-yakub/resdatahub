package com.resdatahub.dataset;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/datasets")
public class CitationController {

    private final CitationService citationService;

    public CitationController(CitationService citationService) {
        this.citationService = citationService;
    }

    @GetMapping("/{datasetId}/versions/{versionId}/citation")
    @Operation(
            summary = "Get dataset citation",
            description = "Returns a citation for a published dataset version in the requested format.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Citation returned.",
                            content = @Content(schema = @Schema(implementation = CitationResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid citation format.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Published dataset version not found.", content = @Content)
            }
    )
    public CitationResponse getCitation(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId,
            @RequestParam CitationFormat format
    ) {
        return citationService.getCitation(datasetId, versionId, format);
    }
}
