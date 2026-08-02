package com.resdatahub.dataset;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/catalog")
public class CatalogMetadataController {

    private final CatalogMetadataService catalogMetadataService;

    public CatalogMetadataController(CatalogMetadataService catalogMetadataService) {
        this.catalogMetadataService = catalogMetadataService;
    }

    @GetMapping(
            value = "/metadata",
            produces = {"text/turtle", "application/ld+json", "application/rdf+xml"}
    )
    @Operation(
            summary = "Export public catalog metadata",
            description = "Returns a paginated DCAT catalog containing published dataset versions.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Catalog RDF metadata returned."),
                    @ApiResponse(responseCode = "400", description = "Invalid request.", content = @Content)
            }
    )
    public ResponseEntity<String> exportCatalogMetadata(
            @Parameter(description = "RDF serialization format.") @RequestParam(defaultValue = "TURTLE") MetadataFormat format,
            @Parameter(description = "Zero-based page number.") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size. Maximum is 500.") @RequestParam(defaultValue = "100") Integer size
    ) {
        CatalogMetadataService.CatalogMetadataResult result = catalogMetadataService.exportCatalog(
                format,
                page,
                size
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(format.getContentType()))
                .header("X-Total-Elements", String.valueOf(result.totalElements()))
                .header("X-Page", String.valueOf(result.page()))
                .header("X-Size", String.valueOf(result.size()))
                .body(result.metadata());
    }
}
