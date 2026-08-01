package com.resdatahub.dataset;

import org.springframework.stereotype.Component;

@Component
public class DatasetFileMapper {

    public DatasetFileResponse toResponse(DatasetFile datasetFile) {
        return new DatasetFileResponse(
                datasetFile.getId(),
                datasetFile.getDatasetVersion().getId(),
                datasetFile.getOriginalFilename(),
                datasetFile.getContentType(),
                datasetFile.getFileSize(),
                datasetFile.getSha256(),
                datasetFile.getCategory(),
                datasetFile.getCreatedAt()
        );
    }
}
