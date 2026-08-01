package com.resdatahub.dataset;

import com.resdatahub.exception.ResourceNotFoundException;
import com.resdatahub.organization.Organization;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PublicDatasetService {

    private final DatasetRepository datasetRepository;
    private final DatasetVersionRepository datasetVersionRepository;
    private final DatasetCreatorRepository datasetCreatorRepository;
    private final DatasetKeywordRepository datasetKeywordRepository;
    private final DatasetFileRepository datasetFileRepository;

    public PublicDatasetService(
            DatasetRepository datasetRepository,
            DatasetVersionRepository datasetVersionRepository,
            DatasetCreatorRepository datasetCreatorRepository,
            DatasetKeywordRepository datasetKeywordRepository,
            DatasetFileRepository datasetFileRepository
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetVersionRepository = datasetVersionRepository;
        this.datasetCreatorRepository = datasetCreatorRepository;
        this.datasetKeywordRepository = datasetKeywordRepository;
        this.datasetFileRepository = datasetFileRepository;
    }

    @Transactional(readOnly = true)
    public PublicDatasetResponse getPublishedDatasetVersion(UUID datasetId, UUID versionId) {
        findDataset(datasetId);
        DatasetVersion version = datasetVersionRepository.findByIdAndDatasetId(versionId, datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Published dataset version not found"));

        if (version.getStatus() != DatasetVersionStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Published dataset version not found");
        }

        return toResponse(version);
    }

    private Dataset findDataset(UUID datasetId) {
        return datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found"));
    }

    private PublicDatasetResponse toResponse(DatasetVersion version) {
        Dataset dataset = version.getDataset();
        Organization organization = dataset.getOrganization();

        return new PublicDatasetResponse(
                dataset.getId(),
                new PublicDatasetResponse.PublicOrganizationResponse(
                        organization.getId(),
                        organization.getName(),
                        organization.getShortName(),
                        organization.getWebsite()
                ),
                new PublicDatasetResponse.PublicDatasetVersionResponse(
                        version.getId(),
                        version.getVersionNumber(),
                        version.getTitle(),
                        version.getDescription(),
                        version.getChangeNote(),
                        version.getStatus(),
                        version.getPublishedAt(),
                        version.getCreatedAt(),
                        version.getUpdatedAt()
                ),
                getCreators(version.getId()),
                getKeywords(version.getId()),
                toLicenseResponse(version.getLicense()),
                getFiles(dataset.getId(), version.getId())
        );
    }

    private List<PublicDatasetResponse.PublicCreatorResponse> getCreators(UUID versionId) {
        return datasetCreatorRepository.findByDatasetVersionIdOrderByPositionAsc(versionId)
                .stream()
                .map(creator -> new PublicDatasetResponse.PublicCreatorResponse(
                        creator.getId(),
                        creator.getGivenName(),
                        creator.getFamilyName(),
                        creator.getAffiliation(),
                        creator.getOrcid(),
                        creator.getPosition()
                ))
                .toList();
    }

    private List<PublicDatasetResponse.PublicKeywordResponse> getKeywords(UUID versionId) {
        return datasetKeywordRepository.findByDatasetVersionIdOrderByValueAsc(versionId)
                .stream()
                .map(keyword -> new PublicDatasetResponse.PublicKeywordResponse(
                        keyword.getId(),
                        keyword.getValue()
                ))
                .toList();
    }

    private PublicDatasetResponse.PublicLicenseResponse toLicenseResponse(License license) {
        if (license == null) {
            return null;
        }

        return new PublicDatasetResponse.PublicLicenseResponse(
                license.getId(),
                license.getCode(),
                license.getName(),
                license.getUri()
        );
    }

    private List<PublicDatasetResponse.PublicFileResponse> getFiles(UUID datasetId, UUID versionId) {
        return datasetFileRepository.findByDatasetVersionIdOrderByCreatedAtAsc(versionId)
                .stream()
                .map(file -> new PublicDatasetResponse.PublicFileResponse(
                        file.getId(),
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getFileSize(),
                        file.getSha256(),
                        file.getCategory(),
                        file.getCreatedAt(),
                        buildDownloadUrl(datasetId, versionId, file.getId())
                ))
                .toList();
    }

    private String buildDownloadUrl(UUID datasetId, UUID versionId, UUID fileId) {
        return "/api/datasets/%s/versions/%s/files/%s/download".formatted(
                datasetId,
                versionId,
                fileId
        );
    }
}
