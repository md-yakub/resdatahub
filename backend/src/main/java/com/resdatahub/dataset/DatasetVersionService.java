package com.resdatahub.dataset;

import com.resdatahub.exception.ConflictException;
import com.resdatahub.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class DatasetVersionService {

    private final DatasetRepository datasetRepository;
    private final DatasetVersionRepository datasetVersionRepository;
    private final DatasetVersionMapper datasetVersionMapper;

    public DatasetVersionService(
            DatasetRepository datasetRepository,
            DatasetVersionRepository datasetVersionRepository,
            DatasetVersionMapper datasetVersionMapper
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetVersionRepository = datasetVersionRepository;
        this.datasetVersionMapper = datasetVersionMapper;
    }

    @Transactional(readOnly = true)
    public List<DatasetVersionResponse> getVersions(UUID datasetId) {
        findDataset(datasetId);

        return datasetVersionRepository.findByDatasetIdOrderByCreatedAtDesc(datasetId)
                .stream()
                .map(datasetVersionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DatasetVersionResponse getVersion(UUID datasetId, UUID versionId) {
        DatasetVersion version = findVersion(datasetId, versionId);
        return datasetVersionMapper.toResponse(version);
    }

    @Transactional
    public DatasetVersionResponse createVersion(
            UUID datasetId,
            CreateDatasetVersionRequest request
    ) {
        Dataset dataset = findDataset(datasetId);

        if (datasetVersionRepository.existsByDatasetIdAndVersionNumberIgnoreCase(
                datasetId,
                request.versionNumber()
        )) {
            throw new ConflictException("Dataset version number already exists");
        }

        DatasetVersion version = datasetVersionMapper.toEntity(request, dataset);
        DatasetVersion savedVersion = datasetVersionRepository.save(version);
        return datasetVersionMapper.toResponse(savedVersion);
    }

    @Transactional
    public DatasetVersionResponse updateVersion(
            UUID datasetId,
            UUID versionId,
            UpdateDatasetVersionRequest request
    ) {
        DatasetVersion version = findVersion(datasetId, versionId);
        ensureDraft(version);

        if (request.versionNumber() != null) {
            updateVersionNumber(datasetId, version, request.versionNumber());
        }

        if (request.title() != null) {
            updateTitle(version, request.title());
        }

        if (request.description() != null) {
            version.setDescription(request.description());
        }

        if (request.changeNote() != null) {
            version.setChangeNote(request.changeNote());
        }

        DatasetVersion savedVersion = datasetVersionRepository.save(version);
        return datasetVersionMapper.toResponse(savedVersion);
    }

    private Dataset findDataset(UUID datasetId) {
        return datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found"));
    }

    private DatasetVersion findVersion(UUID datasetId, UUID versionId) {
        return datasetVersionRepository.findByIdAndDatasetId(versionId, datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset version not found"));
    }

    private void ensureDraft(DatasetVersion version) {
        if (version.getStatus() != DatasetVersionStatus.DRAFT) {
            throw new ConflictException("Only DRAFT dataset versions can be edited");
        }
    }

    private void updateVersionNumber(
            UUID datasetId,
            DatasetVersion version,
            String versionNumber
    ) {
        if (!StringUtils.hasText(versionNumber)) {
            throw new IllegalArgumentException("Version number is required");
        }

        boolean duplicateVersionNumber = datasetVersionRepository
                .existsByDatasetIdAndVersionNumberIgnoreCaseAndIdNot(
                        datasetId,
                        versionNumber,
                        version.getId()
                );

        if (duplicateVersionNumber) {
            throw new ConflictException("Dataset version number already exists");
        }

        version.setVersionNumber(versionNumber);
    }

    private void updateTitle(DatasetVersion version, String title) {
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Dataset version title is required");
        }

        version.setTitle(title);
    }
}
