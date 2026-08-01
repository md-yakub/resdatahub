package com.resdatahub.organization;

import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {

    public Organization toEntity(CreateOrganizationRequest request) {
        Organization organization = new Organization();
        organization.setName(request.name());
        organization.setShortName(request.shortName());
        organization.setDescription(request.description());
        organization.setWebsite(request.website());
        return organization;
    }

    public OrganizationResponse toResponse(Organization organization) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getShortName(),
                organization.getDescription(),
                organization.getWebsite(),
                organization.getCreatedAt(),
                organization.getUpdatedAt()
        );
    }
}
