package com.resdatahub.knowledgegraph.dto;

import java.util.List;

public record SparqlQueryResponse(
        List<String> variables,
        List<SparqlResultRow> rows,
        int rowCount,
        boolean truncated
) {
}
