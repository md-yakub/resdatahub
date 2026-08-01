package com.resdatahub.dataset;

import org.springframework.stereotype.Component;

@Component
public class DatasetVersionMapper {

    public DatasetVersion toFirstVersion(Dataset dataset, String title, String description) {
        DatasetVersion version = new DatasetVersion();
        version.setDataset(dataset);
        version.setVersionNumber("1.0");
        version.setTitle(title);
        version.setDescription(description == null ? "" : description);
        version.setChangeNote("Initial version");
        return version;
    }

    public DatasetVersion toEntity(CreateDatasetVersionRequest request, Dataset dataset) {
        DatasetVersion version = new DatasetVersion();
        version.setDataset(dataset);
        version.setVersionNumber(request.versionNumber());
        version.setTitle(request.title());
        version.setDescription(request.description());
        version.setChangeNote(request.changeNote());
        return version;
    }

    public DatasetVersionResponse toResponse(DatasetVersion version) {
        return new DatasetVersionResponse(
                version.getId(),
                version.getDataset().getId(),
                version.getVersionNumber(),
                version.getTitle(),
                version.getDescription(),
                version.getChangeNote(),
                version.getStatus(),
                version.getCreatedAt(),
                version.getUpdatedAt(),
                version.getPublishedAt()
        );
    }
}
