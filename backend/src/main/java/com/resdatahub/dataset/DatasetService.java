package com.resdatahub.dataset;

import com.resdatahub.exception.ResourceNotFoundException;
import com.resdatahub.organization.Organization;
import com.resdatahub.organization.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final DatasetVersionRepository datasetVersionRepository;
    private final OrganizationRepository organizationRepository;
    private final DatasetMapper datasetMapper;
    private final DatasetVersionMapper datasetVersionMapper;

    public DatasetService(
            DatasetRepository datasetRepository,
            DatasetVersionRepository datasetVersionRepository,
            OrganizationRepository organizationRepository,
            DatasetMapper datasetMapper,
            DatasetVersionMapper datasetVersionMapper
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetVersionRepository = datasetVersionRepository;
        this.organizationRepository = organizationRepository;
        this.datasetMapper = datasetMapper;
        this.datasetVersionMapper = datasetVersionMapper;
    }

    @Transactional
    public DatasetResponse createDataset(CreateDatasetRequest request) {
        Organization organization = findOrganization(request.organizationId());
        Dataset dataset = datasetMapper.toEntity(request, organization);
        Dataset savedDataset = datasetRepository.save(dataset);

        DatasetVersion firstVersion = datasetVersionMapper.toFirstVersion(
                savedDataset,
                request.title(),
                request.description()
        );
        DatasetVersion savedVersion = datasetVersionRepository.save(firstVersion);

        return datasetMapper.toResponse(savedDataset, savedVersion);
    }

    @Transactional(readOnly = true)
    public List<DatasetResponse> getDatasets() {
        return datasetRepository.findAll()
                .stream()
                .map(this::toResponseWithLatestVersion)
                .toList();
    }

    @Transactional(readOnly = true)
    public DatasetResponse getDataset(UUID id) {
        Dataset dataset = findDataset(id);
        return toResponseWithLatestVersion(dataset);
    }

    @Transactional(readOnly = true)
    public DatasetResponse updateDataset(UUID id, UpdateDatasetRequest request) {
        Dataset dataset = findDataset(id);
        return toResponseWithLatestVersion(dataset);
    }

    @Transactional
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

    private DatasetResponse toResponseWithLatestVersion(Dataset dataset) {
        DatasetVersion latestVersion = datasetVersionRepository
                .findTopByDatasetIdOrderByCreatedAtDesc(dataset.getId())
                .orElse(null);

        return datasetMapper.toResponse(dataset, latestVersion);
    }
}
