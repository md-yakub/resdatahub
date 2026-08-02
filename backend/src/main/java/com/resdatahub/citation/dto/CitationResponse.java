package com.resdatahub.citation.dto;

public record CitationResponse(
        CitationFormat format,
        String citation
) {
}
