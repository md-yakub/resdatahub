package com.resdatahub.license.controller;

import com.resdatahub.license.dto.LicenseResponse;
import com.resdatahub.license.entity.License;
import com.resdatahub.license.service.LicenseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/licenses")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @GetMapping
    @Operation(
            summary = "List licenses",
            description = "Returns available dataset licenses.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Licenses returned.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LicenseResponse.class)))
            )
    )
    public List<LicenseResponse> getLicenses() {
        return licenseService.getLicenses();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get license",
            description = "Returns a license by id.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "License returned.",
                            content = @Content(schema = @Schema(implementation = LicenseResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "License not found.", content = @Content)
            }
    )
    public LicenseResponse getLicense(@PathVariable UUID id) {
        return licenseService.getLicense(id);
    }
}
