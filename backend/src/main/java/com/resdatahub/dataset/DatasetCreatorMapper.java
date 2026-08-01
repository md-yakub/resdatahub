package com.resdatahub.dataset;

import org.springframework.stereotype.Component;

@Component
public class DatasetCreatorMapper {

    public DatasetCreator toEntity(
            CreateDatasetCreatorRequest request,
            DatasetVersion datasetVersion
    ) {
        DatasetCreator creator = new DatasetCreator();
        creator.setDatasetVersion(datasetVersion);
        creator.setGivenName(request.givenName());
        creator.setFamilyName(request.familyName());
        creator.setAffiliation(request.affiliation());
        creator.setOrcid(request.orcid());
        creator.setPosition(request.position());
        return creator;
    }

    public DatasetCreatorResponse toResponse(DatasetCreator creator) {
        return new DatasetCreatorResponse(
                creator.getId(),
                creator.getDatasetVersion().getId(),
                creator.getGivenName(),
                creator.getFamilyName(),
                creator.getAffiliation(),
                creator.getOrcid(),
                creator.getPosition(),
                creator.getCreatedAt(),
                creator.getUpdatedAt()
        );
    }
}
