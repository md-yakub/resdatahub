package com.resdatahub.metadata.service;

import com.resdatahub.creator.entity.DatasetCreator;
import com.resdatahub.creator.repository.DatasetCreatorRepository;
import com.resdatahub.file.entity.DatasetFile;
import com.resdatahub.file.repository.DatasetFileRepository;
import com.resdatahub.keyword.entity.DatasetKeyword;
import com.resdatahub.keyword.repository.DatasetKeywordRepository;
import com.resdatahub.metadata.dto.CatalogValidationResponse;
import com.resdatahub.metadata.dto.CatalogValidationViolationResponse;
import com.resdatahub.metadata.dto.ValidationSeverity;
import com.resdatahub.organization.entity.Organization;
import com.resdatahub.version.entity.DatasetVersion;
import com.resdatahub.version.entity.DatasetVersionStatus;
import com.resdatahub.version.repository.DatasetVersionRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CatalogValidationService {

    private static final String PROFILE = "ResDataHub DCAT harvesting profile";
    private static final Pattern SHA256_PATTERN = Pattern.compile("^[0-9a-fA-F]{64}$");

    private final DatasetVersionRepository datasetVersionRepository;
    private final DatasetCreatorRepository datasetCreatorRepository;
    private final DatasetKeywordRepository datasetKeywordRepository;
    private final DatasetFileRepository datasetFileRepository;
    private final String catalogTitle;
    private final String catalogDescription;
    private final String catalogPublisherName;
    private final String catalogHomepage;
    private final String catalogLanguage;
    private final String publicBaseUrl;

    public CatalogValidationService(
            DatasetVersionRepository datasetVersionRepository,
            DatasetCreatorRepository datasetCreatorRepository,
            DatasetKeywordRepository datasetKeywordRepository,
            DatasetFileRepository datasetFileRepository,
            @Value("${resdatahub.catalog.title}") String catalogTitle,
            @Value("${resdatahub.catalog.description}") String catalogDescription,
            @Value("${resdatahub.catalog.publisher-name}") String catalogPublisherName,
            @Value("${resdatahub.catalog.homepage}") String catalogHomepage,
            @Value("${resdatahub.catalog.language}") String catalogLanguage,
            @Value("${resdatahub.public-base-url}") String publicBaseUrl
    ) {
        this.datasetVersionRepository = datasetVersionRepository;
        this.datasetCreatorRepository = datasetCreatorRepository;
        this.datasetKeywordRepository = datasetKeywordRepository;
        this.datasetFileRepository = datasetFileRepository;
        this.catalogTitle = catalogTitle;
        this.catalogDescription = catalogDescription;
        this.catalogPublisherName = catalogPublisherName;
        this.catalogHomepage = catalogHomepage;
        this.catalogLanguage = catalogLanguage;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Transactional(readOnly = true)
    public CatalogValidationResponse validateCatalog() {
        List<CatalogValidationViolationResponse> violations = new java.util.ArrayList<>();
        validateCatalogFields(violations);

        List<DatasetVersion> versions = datasetVersionRepository
                .findLatestPublishedVersionPerDataset(DatasetVersionStatus.PUBLISHED);
        List<UUID> versionIds = versions.stream()
                .map(DatasetVersion::getId)
                .toList();

        Map<UUID, List<DatasetFile>> filesByVersionId = getFilesByVersionId(versionIds);
        Map<UUID, List<DatasetCreator>> creatorsByVersionId = getCreatorsByVersionId(versionIds);
        Map<UUID, List<DatasetKeyword>> keywordsByVersionId = getKeywordsByVersionId(versionIds);

        for (DatasetVersion version : versions) {
            validateDatasetVersion(version, violations);
            validateCreators(version, creatorsByVersionId.getOrDefault(version.getId(), List.of()), violations);
            validateKeywords(version, keywordsByVersionId.getOrDefault(version.getId(), List.of()), violations);
            validateFiles(version, filesByVersionId.getOrDefault(version.getId(), List.of()), violations);
        }

        boolean conforms = violations.stream()
                .noneMatch(violation -> violation.severity() == ValidationSeverity.ERROR);

        return new CatalogValidationResponse(
                PROFILE,
                conforms,
                versions.size(),
                violations
        );
    }

    private void validateCatalogFields(List<CatalogValidationViolationResponse> violations) {
        addErrorIfBlank(violations, "catalog", "dct:title", catalogTitle, "Catalog title must exist");
        addErrorIfBlank(violations, "catalog", "dct:description", catalogDescription, "Catalog description must exist");
        addErrorIfBlank(violations, "catalog", "dct:publisher/foaf:name", catalogPublisherName, "Catalog publisher name must exist");
        addErrorIfBlank(violations, "catalog", "foaf:homepage", catalogHomepage, "Catalog homepage must exist");
        addErrorIfBlank(violations, "catalog", "dct:language", catalogLanguage, "Catalog language must exist");
    }

    private void validateDatasetVersion(
            DatasetVersion version,
            List<CatalogValidationViolationResponse> violations
    ) {
        String resource = datasetResource(version);
        addErrorIfBlank(violations, resource, "dct:title", version.getTitle(), "Dataset title must exist");
        addErrorIfBlank(violations, resource, "dct:description", version.getDescription(), "Dataset description must exist");

        if (version.getDataset() == null || version.getDataset().getOrganization() == null) {
            addViolation(violations, resource, "dct:publisher", ValidationSeverity.ERROR, "Dataset organization/publisher must exist");
            return;
        }

        Organization organization = version.getDataset().getOrganization();
        addErrorIfBlank(violations, resource, "dct:publisher/foaf:name", organization.getName(), "Dataset organization/publisher must exist");

        if (version.getLicense() == null) {
            addViolation(violations, resource, "dct:license", ValidationSeverity.ERROR, "Dataset license must exist");
        }
    }

    private void validateCreators(
            DatasetVersion version,
            List<DatasetCreator> creators,
            List<CatalogValidationViolationResponse> violations
    ) {
        if (creators.isEmpty()) {
            addViolation(
                    violations,
                    datasetResource(version),
                    "dct:creator",
                    ValidationSeverity.WARNING,
                    "Published dataset should have at least one creator"
            );
        }
    }

    private void validateKeywords(
            DatasetVersion version,
            List<DatasetKeyword> keywords,
            List<CatalogValidationViolationResponse> violations
    ) {
        if (keywords.isEmpty()) {
            addViolation(
                    violations,
                    datasetResource(version),
                    "dcat:keyword",
                    ValidationSeverity.WARNING,
                    "Published dataset should have at least one keyword"
            );
        }
    }

    private void validateFiles(
            DatasetVersion version,
            List<DatasetFile> files,
            List<CatalogValidationViolationResponse> violations
    ) {
        if (files.isEmpty()) {
            addViolation(
                    violations,
                    datasetResource(version),
                    "dcat:distribution",
                    ValidationSeverity.ERROR,
                    "Published dataset must have at least one distribution/file"
            );
            return;
        }

        for (DatasetFile file : files) {
            validateFile(version, file, violations);
        }
    }

    private void validateFile(
            DatasetVersion version,
            DatasetFile file,
            List<CatalogValidationViolationResponse> violations
    ) {
        String resource = fileResource(file);
        addErrorIfBlank(violations, resource, "dcat:downloadURL", buildDownloadUrl(version, file), "Distribution download URL must exist");
        addErrorIfBlank(violations, resource, "dct:title", file.getOriginalFilename(), "Distribution original filename must exist");
        addErrorIfBlank(violations, resource, "dcat:mediaType", file.getContentType(), "Distribution content type must exist");

        if (file.getFileSize() <= 0) {
            addViolation(violations, resource, "dcat:byteSize", ValidationSeverity.ERROR, "Distribution file size must be greater than zero");
        }

        if (!StringUtils.hasText(file.getSha256()) || !SHA256_PATTERN.matcher(file.getSha256()).matches()) {
            addViolation(
                    violations,
                    resource,
                    "spdx:checksumValue",
                    ValidationSeverity.ERROR,
                    "Distribution SHA-256 must exist and contain 64 hexadecimal characters"
            );
        }
    }

    private Map<UUID, List<DatasetCreator>> getCreatorsByVersionId(Collection<UUID> versionIds) {
        if (versionIds.isEmpty()) {
            return Map.of();
        }

        return datasetCreatorRepository.findByDatasetVersionIdsOrderByPositionAsc(versionIds)
                .stream()
                .collect(Collectors.groupingBy(creator -> creator.getDatasetVersion().getId()));
    }

    private Map<UUID, List<DatasetKeyword>> getKeywordsByVersionId(Collection<UUID> versionIds) {
        if (versionIds.isEmpty()) {
            return Map.of();
        }

        return datasetKeywordRepository.findByDatasetVersionIdsOrderByValueAsc(versionIds)
                .stream()
                .collect(Collectors.groupingBy(keyword -> keyword.getDatasetVersion().getId()));
    }

    private Map<UUID, List<DatasetFile>> getFilesByVersionId(Collection<UUID> versionIds) {
        if (versionIds.isEmpty()) {
            return Map.of();
        }

        return datasetFileRepository.findByDatasetVersionIdsOrderByCreatedAtAsc(versionIds)
                .stream()
                .collect(Collectors.groupingBy(file -> file.getDatasetVersion().getId()));
    }

    private void addErrorIfBlank(
            List<CatalogValidationViolationResponse> violations,
            String resource,
            String property,
            String value,
            String message
    ) {
        if (!StringUtils.hasText(value)) {
            addViolation(violations, resource, property, ValidationSeverity.ERROR, message);
        }
    }

    private void addViolation(
            List<CatalogValidationViolationResponse> violations,
            String resource,
            String property,
            ValidationSeverity severity,
            String message
    ) {
        violations.add(new CatalogValidationViolationResponse(resource, property, severity, message));
    }

    private String datasetResource(DatasetVersion version) {
        return "datasetVersion:%s".formatted(version.getId());
    }

    private String fileResource(DatasetFile file) {
        return "file:%s".formatted(file.getId());
    }

    private String buildDownloadUrl(DatasetVersion version, DatasetFile file) {
        if (!StringUtils.hasText(publicBaseUrl)
                || version.getDataset() == null
                || version.getDataset().getId() == null
                || version.getId() == null
                || file.getId() == null) {
            return "";
        }

        return "%s/api/datasets/%s/versions/%s/files/%s/download".formatted(
                trimTrailingSlashes(publicBaseUrl),
                version.getDataset().getId(),
                version.getId(),
                file.getId()
        );
    }

    private String trimTrailingSlashes(String value) {
        String trimmedValue = value.trim();

        while (trimmedValue.endsWith("/")) {
            trimmedValue = trimmedValue.substring(0, trimmedValue.length() - 1);
        }

        return trimmedValue;
    }
}
