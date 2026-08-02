package com.resdatahub.keyword.service;

import com.resdatahub.common.exception.ConflictException;
import com.resdatahub.common.exception.ResourceNotFoundException;
import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.dataset.repository.DatasetRepository;
import com.resdatahub.keyword.dto.CreateDatasetKeywordRequest;
import com.resdatahub.keyword.dto.DatasetKeywordResponse;
import com.resdatahub.keyword.entity.DatasetKeyword;
import com.resdatahub.keyword.repository.DatasetKeywordRepository;
import com.resdatahub.version.entity.DatasetVersion;
import com.resdatahub.version.entity.DatasetVersionStatus;
import com.resdatahub.version.repository.DatasetVersionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DatasetKeywordService {

    private final DatasetRepository datasetRepository;
    private final DatasetVersionRepository datasetVersionRepository;
    private final DatasetKeywordRepository datasetKeywordRepository;

    public DatasetKeywordService(
            DatasetRepository datasetRepository,
            DatasetVersionRepository datasetVersionRepository,
            DatasetKeywordRepository datasetKeywordRepository
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetVersionRepository = datasetVersionRepository;
        this.datasetKeywordRepository = datasetKeywordRepository;
    }

    @Transactional
    public DatasetKeywordResponse createKeyword(
            UUID datasetId,
            UUID versionId,
            CreateDatasetKeywordRequest request
    ) {
        DatasetVersion version = findVersion(datasetId, versionId);
        ensureDraft(version);

        String value = request.value().trim();
        if (datasetKeywordRepository.existsByDatasetVersionIdAndValueIgnoreCase(versionId, value)) {
            throw new ConflictException("Keyword already exists");
        }

        DatasetKeyword keyword = new DatasetKeyword();
        keyword.setDatasetVersion(version);
        keyword.setValue(value);

        DatasetKeyword savedKeyword = datasetKeywordRepository.save(keyword);
        return toResponse(savedKeyword);
    }

    @Transactional(readOnly = true)
    public List<DatasetKeywordResponse> getKeywords(UUID datasetId, UUID versionId) {
        findVersion(datasetId, versionId);

        return datasetKeywordRepository.findByDatasetVersionIdOrderByValueAsc(versionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteKeyword(UUID datasetId, UUID versionId, UUID keywordId) {
        DatasetVersion version = findVersion(datasetId, versionId);
        ensureDraft(version);

        DatasetKeyword keyword = datasetKeywordRepository
                .findByIdAndDatasetVersionId(keywordId, versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Keyword not found"));

        datasetKeywordRepository.delete(keyword);
    }

    private DatasetVersion findVersion(UUID datasetId, UUID versionId) {
        if (!datasetRepository.existsById(datasetId)) {
            throw new ResourceNotFoundException("Dataset not found");
        }

        return datasetVersionRepository.findByIdAndDatasetId(versionId, datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset version not found"));
    }

    private void ensureDraft(DatasetVersion version) {
        if (version.getStatus() != DatasetVersionStatus.DRAFT) {
            throw new ConflictException("Only DRAFT dataset versions can be changed");
        }
    }

    private DatasetKeywordResponse toResponse(DatasetKeyword keyword) {
        return new DatasetKeywordResponse(
                keyword.getId(),
                keyword.getDatasetVersion().getId(),
                keyword.getValue(),
                keyword.getCreatedAt()
        );
    }
}
