package com.resdatahub.dataset;

import com.resdatahub.exception.ResourceNotFoundException;
import com.resdatahub.organization.Organization;
import com.resdatahub.organization.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final OrganizationRepository organizationRepository;
    private final DatasetMapper datasetMapper;

    public DatasetService(
            DatasetRepository datasetRepository,
            OrganizationRepository organizationRepository,
            DatasetMapper datasetMapper
    ) {
        this.datasetRepository = datasetRepository;
        this.organizationRepository = organizationRepository;
        this.datasetMapper = datasetMapper;
    }

    public DatasetResponse createDataset(CreateDatasetRequest request) {
        Organization organization = findOrganization(request.organizationId());
        Dataset dataset = datasetMapper.toEntity(request, organization);
        Dataset savedDataset = datasetRepository.save(dataset);
        return datasetMapper.toResponse(savedDataset);
    }

    public List<DatasetResponse> getDatasets() {
        return datasetRepository.findAll()
                .stream()
                .map(datasetMapper::toResponse)
                .toList();
    }

    public DatasetResponse getDataset(UUID id) {
        Dataset dataset = findDataset(id);
        return datasetMapper.toResponse(dataset);
    }

    public DatasetResponse updateDataset(UUID id, UpdateDatasetRequest request) {
        Dataset dataset = findDataset(id);

        if (request.title() != null) {
            updateTitle(dataset, request.title());
        }

        if (request.description() != null) {
            dataset.setDescription(request.description());
        }

        if (request.status() != null) {
            dataset.setStatus(request.status());
        }

        Dataset savedDataset = datasetRepository.save(dataset);
        return datasetMapper.toResponse(savedDataset);
    }

    public void deleteDataset(UUID id) {
        Dataset dataset = findDataset(id);
        datasetRepository.delete(dataset);
    }

    private Dataset findDataset(UUID id) {
        return datasetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found"));
    }

    private Organization findOrganization(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }

    private void updateTitle(Dataset dataset, String title) {
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Dataset title is required");
        }

        dataset.setTitle(title);
    }
}
