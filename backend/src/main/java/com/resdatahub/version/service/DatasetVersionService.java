package com.resdatahub.version.service;

import com.resdatahub.common.exception.ConflictException;
import com.resdatahub.common.exception.ResourceNotFoundException;
import com.resdatahub.creator.repository.DatasetCreatorRepository;
import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.dataset.repository.DatasetRepository;
import com.resdatahub.file.repository.DatasetFileRepository;
import com.resdatahub.license.entity.License;
import com.resdatahub.license.repository.LicenseRepository;
import com.resdatahub.version.dto.CreateDatasetVersionRequest;
import com.resdatahub.version.dto.DatasetVersionResponse;
import com.resdatahub.version.dto.UpdateDatasetVersionLicenseRequest;
import com.resdatahub.version.dto.UpdateDatasetVersionRequest;
import com.resdatahub.version.entity.DatasetVersion;
import com.resdatahub.version.entity.DatasetVersionStatus;
import com.resdatahub.version.mapper.DatasetVersionMapper;
import com.resdatahub.version.repository.DatasetVersionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DatasetVersionService {

    private final DatasetRepository datasetRepository;
    private final DatasetVersionRepository datasetVersionRepository;
    private final DatasetCreatorRepository datasetCreatorRepository;
    private final DatasetFileRepository datasetFileRepository;
    private final LicenseRepository licenseRepository;
    private final DatasetVersionMapper datasetVersionMapper;

    public DatasetVersionService(
            DatasetRepository datasetRepository,
            DatasetVersionRepository datasetVersionRepository,
            DatasetCreatorRepository datasetCreatorRepository,
            DatasetFileRepository datasetFileRepository,
            LicenseRepository licenseRepository,
            DatasetVersionMapper datasetVersionMapper
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetVersionRepository = datasetVersionRepository;
        this.datasetCreatorRepository = datasetCreatorRepository;
        this.datasetFileRepository = datasetFileRepository;
        this.licenseRepository = licenseRepository;
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
    public DatasetVersionResponse updateLicense(
            UUID datasetId,
            UUID versionId,
            UpdateDatasetVersionLicenseRequest request
    ) {
        DatasetVersion version = findVersion(datasetId, versionId);
        ensureDraft(version);

        License license = licenseRepository.findByIdAndActiveTrue(request.licenseId())
                .orElseThrow(() -> new ResourceNotFoundException("License not found"));

        version.setLicense(license);
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

    @Transactional
    public DatasetVersionResponse publishVersion(UUID datasetId, UUID versionId) {
        DatasetVersion version = findVersion(datasetId, versionId);
        ensureDraftForPublication(version);
        validatePublishRequirements(version);

        version.setStatus(DatasetVersionStatus.PUBLISHED);
        version.setPublishedAt(Instant.now());

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

    private void ensureDraftForPublication(DatasetVersion version) {
        if (version.getStatus() != DatasetVersionStatus.DRAFT) {
            throw new ConflictException("Only DRAFT dataset versions can be published");
        }
    }

    private void validatePublishRequirements(DatasetVersion version) {
        List<String> missingRequirements = new ArrayList<>();

        if (!StringUtils.hasText(version.getTitle())) {
            missingRequirements.add("title");
        }

        if (!StringUtils.hasText(version.getDescription())) {
            missingRequirements.add("description");
        }

        if (datasetCreatorRepository.countByDatasetVersionId(version.getId()) == 0) {
            missingRequirements.add("at least one creator");
        }

        if (datasetFileRepository.countByDatasetVersionId(version.getId()) == 0) {
            missingRequirements.add("at least one uploaded file");
        }

        if (version.getLicense() == null) {
            missingRequirements.add("selected license");
        }

        if (!missingRequirements.isEmpty()) {
            throw new ConflictException(
                    "Dataset version cannot be published because it is missing: "
                            + String.join(", ", missingRequirements)
            );
        }
    }
}
