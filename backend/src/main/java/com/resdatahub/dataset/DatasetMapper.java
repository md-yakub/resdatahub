package com.resdatahub.dataset;

import com.resdatahub.organization.Organization;
import org.springframework.stereotype.Component;

@Component
public class DatasetMapper {

    public Dataset toEntity(CreateDatasetRequest request, Organization organization) {
        Dataset dataset = new Dataset();
        dataset.setOrganization(organization);
        return dataset;
    }

    public DatasetResponse toResponse(Dataset dataset, DatasetVersion latestVersion) {
        Organization organization = dataset.getOrganization();

        return new DatasetResponse(
                dataset.getId(),
                new DatasetOrganizationSummary(
                        organization.getId(),
                        organization.getName(),
                        organization.getShortName()
                ),
                toLatestVersionSummary(latestVersion),
                dataset.getCreatedAt(),
                dataset.getUpdatedAt()
        );
    }

    private DatasetVersionSummary toLatestVersionSummary(DatasetVersion latestVersion) {
        if (latestVersion == null) {
            return null;
        }

        return new DatasetVersionSummary(
                latestVersion.getId(),
                latestVersion.getVersionNumber(),
                latestVersion.getTitle(),
                latestVersion.getStatus(),
                latestVersion.getPublishedAt()
        );
    }
}
