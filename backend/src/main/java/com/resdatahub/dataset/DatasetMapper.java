package com.resdatahub.dataset;

import com.resdatahub.organization.Organization;
import org.springframework.stereotype.Component;

@Component
public class DatasetMapper {

    public Dataset toEntity(CreateDatasetRequest request, Organization organization) {
        Dataset dataset = new Dataset();
        dataset.setTitle(request.title());
        dataset.setDescription(request.description());
        dataset.setOrganization(organization);
        return dataset;
    }

    public DatasetResponse toResponse(Dataset dataset) {
        Organization organization = dataset.getOrganization();

        return new DatasetResponse(
                dataset.getId(),
                dataset.getTitle(),
                dataset.getDescription(),
                organization.getId(),
                organization.getName(),
                dataset.getStatus(),
                dataset.getCreatedAt(),
                dataset.getUpdatedAt()
        );
    }
}
