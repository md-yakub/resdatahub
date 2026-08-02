package com.resdatahub.search.dto;

import java.util.List;

public record PublicSearchResponse(
        List<PublicSearchItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
