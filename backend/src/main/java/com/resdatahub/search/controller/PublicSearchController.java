package com.resdatahub.search.controller;

import com.resdatahub.search.dto.PublicSearchResponse;
import com.resdatahub.search.dto.SearchSort;
import com.resdatahub.search.service.PublicSearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/search")
public class PublicSearchController {

    private final PublicSearchService publicSearchService;

    public PublicSearchController(PublicSearchService publicSearchService) {
        this.publicSearchService = publicSearchService;
    }

    @GetMapping
    @Operation(
            summary = "Search public datasets",
            description = "Searches published dataset versions and returns public search results.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Search results returned.",
                            content = @Content(schema = @Schema(implementation = PublicSearchResponse.class))
                    )
            }
    )
    public PublicSearchResponse search(
            @Parameter(description = "Search text.") @RequestParam(required = false) String q,
            @Parameter(description = "Zero-based page number.") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size. Maximum is 100.") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Filter by organization id.") @RequestParam(required = false) UUID organizationId,
            @Parameter(description = "Filter by exact keyword, case-insensitive.") @RequestParam(required = false) String keyword,
            @Parameter(description = "Filter by license code, case-insensitive.") @RequestParam(required = false) String licenseCode,
            @Parameter(description = "Sort order.") @RequestParam(defaultValue = "NEWEST") SearchSort sort
    ) {
        return publicSearchService.search(
                q,
                page,
                size,
                organizationId,
                keyword,
                licenseCode,
                sort
        );
    }
}
