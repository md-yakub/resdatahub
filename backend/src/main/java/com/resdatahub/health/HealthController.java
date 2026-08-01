package com.resdatahub.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    @Operation(
            summary = "Get API health",
            description = "Checks whether the ResDataHub API and database connection are available.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The API and database are available.",
                            content = @Content(schema = @Schema(implementation = HealthResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "The API is running but the health check failed.",
                            content = @Content
                    )
            }
    )
    public HealthResponse getHealth() {
        return healthService.getHealth();
    }
}
