package com.resdatahub.dataset;

import java.util.List;

public record PublicSearchResponse(
        List<PublicSearchItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
