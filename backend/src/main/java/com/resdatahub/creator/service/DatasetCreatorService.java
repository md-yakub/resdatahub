package com.resdatahub.creator.service;

import com.resdatahub.common.exception.ConflictException;
import com.resdatahub.common.exception.ResourceNotFoundException;
import com.resdatahub.creator.dto.CreateDatasetCreatorRequest;
import com.resdatahub.creator.dto.DatasetCreatorResponse;
import com.resdatahub.creator.dto.UpdateDatasetCreatorRequest;
import com.resdatahub.creator.entity.DatasetCreator;
import com.resdatahub.creator.mapper.DatasetCreatorMapper;
import com.resdatahub.creator.repository.DatasetCreatorRepository;
import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.dataset.repository.DatasetRepository;
import com.resdatahub.version.entity.DatasetVersion;
import com.resdatahub.version.entity.DatasetVersionStatus;
import com.resdatahub.version.repository.DatasetVersionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class DatasetCreatorService {

    private final DatasetRepository datasetRepository;
    private final DatasetVersionRepository datasetVersionRepository;
    private final DatasetCreatorRepository datasetCreatorRepository;
    private final DatasetCreatorMapper datasetCreatorMapper;

    public DatasetCreatorService(
            DatasetRepository datasetRepository,
            DatasetVersionRepository datasetVersionRepository,
            DatasetCreatorRepository datasetCreatorRepository,
            DatasetCreatorMapper datasetCreatorMapper
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetVersionRepository = datasetVersionRepository;
        this.datasetCreatorRepository = datasetCreatorRepository;
        this.datasetCreatorMapper = datasetCreatorMapper;
    }

    @Transactional
    public DatasetCreatorResponse createCreator(
            UUID datasetId,
            UUID versionId,
            CreateDatasetCreatorRequest request
    ) {
        DatasetVersion version = findVersion(datasetId, versionId);
        ensureDraft(version);
        ensurePositionAvailable(versionId, request.position());

        DatasetCreator creator = datasetCreatorMapper.toEntity(request, version);
        DatasetCreator savedCreator = datasetCreatorRepository.save(creator);
        return datasetCreatorMapper.toResponse(savedCreator);
    }

    @Transactional(readOnly = true)
    public List<DatasetCreatorResponse> getCreators(UUID datasetId, UUID versionId) {
        findVersion(datasetId, versionId);

        return datasetCreatorRepository.findByDatasetVersionIdOrderByPositionAsc(versionId)
                .stream()
                .map(datasetCreatorMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DatasetCreatorResponse getCreator(
            UUID datasetId,
            UUID versionId,
            UUID creatorId
    ) {
        findVersion(datasetId, versionId);
        DatasetCreator creator = findCreator(versionId, creatorId);
        return datasetCreatorMapper.toResponse(creator);
    }

    @Transactional
    public DatasetCreatorResponse updateCreator(
            UUID datasetId,
            UUID versionId,
            UUID creatorId,
            UpdateDatasetCreatorRequest request
    ) {
        DatasetVersion version = findVersion(datasetId, versionId);
        ensureDraft(version);

        DatasetCreator creator = findCreator(versionId, creatorId);

        if (request.givenName() != null) {
            updateGivenName(creator, request.givenName());
        }

        if (request.familyName() != null) {
            updateFamilyName(creator, request.familyName());
        }

        if (request.affiliation() != null) {
            creator.setAffiliation(request.affiliation());
        }

        if (request.orcid() != null) {
            creator.setOrcid(request.orcid());
        }

        if (request.position() != null) {
            updatePosition(versionId, creator, request.position());
        }

        DatasetCreator savedCreator = datasetCreatorRepository.save(creator);
        return datasetCreatorMapper.toResponse(savedCreator);
    }

    @Transactional
    public void deleteCreator(UUID datasetId, UUID versionId, UUID creatorId) {
        DatasetVersion version = findVersion(datasetId, versionId);
        ensureDraft(version);

        DatasetCreator creator = findCreator(versionId, creatorId);
        datasetCreatorRepository.delete(creator);
    }

    private DatasetVersion findVersion(UUID datasetId, UUID versionId) {
        if (!datasetRepository.existsById(datasetId)) {
            throw new ResourceNotFoundException("Dataset not found");
        }

        return datasetVersionRepository.findByIdAndDatasetId(versionId, datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset version not found"));
    }

    private DatasetCreator findCreator(UUID versionId, UUID creatorId) {
        return datasetCreatorRepository.findByIdAndDatasetVersionId(creatorId, versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset creator not found"));
    }

    private void ensureDraft(DatasetVersion version) {
        if (version.getStatus() != DatasetVersionStatus.DRAFT) {
            throw new ConflictException("Only DRAFT dataset versions can be changed");
        }
    }

    private void ensurePositionAvailable(UUID versionId, Integer position) {
        if (datasetCreatorRepository.existsByDatasetVersionIdAndPosition(versionId, position)) {
            throw new ConflictException("Creator position already exists");
        }
    }

    private void updatePosition(UUID versionId, DatasetCreator creator, Integer position) {
        boolean duplicatePosition = datasetCreatorRepository
                .existsByDatasetVersionIdAndPositionAndIdNot(
                        versionId,
                        position,
                        creator.getId()
                );

        if (duplicatePosition) {
            throw new ConflictException("Creator position already exists");
        }

        creator.setPosition(position);
    }

    private void updateGivenName(DatasetCreator creator, String givenName) {
        if (!StringUtils.hasText(givenName)) {
            throw new IllegalArgumentException("Creator given name is required");
        }

        creator.setGivenName(givenName);
    }

    private void updateFamilyName(DatasetCreator creator, String familyName) {
        if (!StringUtils.hasText(familyName)) {
            throw new IllegalArgumentException("Creator family name is required");
        }

        creator.setFamilyName(familyName);
    }
}
