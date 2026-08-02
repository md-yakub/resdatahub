package com.resdatahub.file.mapper;

import com.resdatahub.file.dto.DatasetFileResponse;
import com.resdatahub.file.entity.DatasetFile;

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
