package com.resdatahub.knowledgegraph.dto;

import jakarta.validation.constraints.NotBlank;

public record SparqlQueryRequest(
        @NotBlank(message = "SPARQL query is required")
        String query
) {
}
