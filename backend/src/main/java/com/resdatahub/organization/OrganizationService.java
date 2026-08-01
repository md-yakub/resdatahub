package com.resdatahub.organization;

import com.resdatahub.exception.ConflictException;
import com.resdatahub.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    public OrganizationService(
            OrganizationRepository organizationRepository,
            OrganizationMapper organizationMapper
    ) {
        this.organizationRepository = organizationRepository;
        this.organizationMapper = organizationMapper;
    }

    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        if (organizationRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Organization name already exists");
        }

        Organization organization = organizationMapper.toEntity(request);
        Organization savedOrganization = organizationRepository.save(organization);
        return organizationMapper.toResponse(savedOrganization);
    }

    public List<OrganizationResponse> getOrganizations() {
        return organizationRepository.findAll()
                .stream()
                .map(organizationMapper::toResponse)
                .toList();
    }

    public OrganizationResponse getOrganization(UUID id) {
        Organization organization = findOrganization(id);
        return organizationMapper.toResponse(organization);
    }

    public OrganizationResponse updateOrganization(UUID id, UpdateOrganizationRequest request) {
        Organization organization = findOrganization(id);

        if (request.name() != null) {
            updateName(organization, request.name());
        }

        if (request.shortName() != null) {
            organization.setShortName(request.shortName());
        }

        if (request.description() != null) {
            organization.setDescription(request.description());
        }

        if (request.website() != null) {
            organization.setWebsite(request.website());
        }

        Organization savedOrganization = organizationRepository.save(organization);
        return organizationMapper.toResponse(savedOrganization);
    }

    public void deleteOrganization(UUID id) {
        Organization organization = findOrganization(id);
        organizationRepository.delete(organization);
    }

    private Organization findOrganization(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }

    private void updateName(Organization organization, String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Organization name is required");
        }

        if (organizationRepository.existsByNameIgnoreCaseAndIdNot(name, organization.getId())) {
            throw new ConflictException("Organization name already exists");
        }

        organization.setName(name);
    }
}
