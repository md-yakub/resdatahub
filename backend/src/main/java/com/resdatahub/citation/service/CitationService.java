package com.resdatahub.citation.service;

import com.resdatahub.citation.dto.CitationFormat;
import com.resdatahub.citation.dto.CitationResponse;
import com.resdatahub.common.exception.ResourceNotFoundException;
import com.resdatahub.creator.entity.DatasetCreator;
import com.resdatahub.creator.repository.DatasetCreatorRepository;
import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.dataset.repository.DatasetRepository;
import com.resdatahub.organization.entity.Organization;
import com.resdatahub.version.entity.DatasetVersion;
import com.resdatahub.version.entity.DatasetVersionStatus;
import com.resdatahub.version.repository.DatasetVersionRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CitationService {

    private final DatasetRepository datasetRepository;
    private final DatasetVersionRepository datasetVersionRepository;
    private final DatasetCreatorRepository datasetCreatorRepository;
    private final String publicBaseUrl;

    public CitationService(
            DatasetRepository datasetRepository,
            DatasetVersionRepository datasetVersionRepository,
            DatasetCreatorRepository datasetCreatorRepository,
            @Value("${resdatahub.public-base-url}") String publicBaseUrl
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetVersionRepository = datasetVersionRepository;
        this.datasetCreatorRepository = datasetCreatorRepository;
        this.publicBaseUrl = trimTrailingSlashes(publicBaseUrl);
    }

    @Transactional(readOnly = true)
    public CitationResponse getCitation(
            UUID datasetId,
            UUID versionId,
            CitationFormat format
    ) {
        findDataset(datasetId);
        DatasetVersion version = datasetVersionRepository.findByIdAndDatasetId(versionId, datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Published dataset version not found"));

        if (version.getStatus() != DatasetVersionStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Published dataset version not found");
        }

        List<DatasetCreator> creators = datasetCreatorRepository
                .findByDatasetVersionIdOrderByPositionAsc(versionId);
        String citation = createCitation(format, datasetId, versionId, version, creators);

        return new CitationResponse(format, citation);
    }

    private Dataset findDataset(UUID datasetId) {
        return datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found"));
    }

    private String createCitation(
            CitationFormat format,
            UUID datasetId,
            UUID versionId,
            DatasetVersion version,
            List<DatasetCreator> creators
    ) {
        return switch (format) {
            case TEXT -> createTextCitation(version, creators);
            case APA -> createApaCitation(version, creators);
            case BIBTEX -> createBibTeXCitation(datasetId, versionId, version, creators);
            case RIS -> createRisCitation(datasetId, versionId, version, creators);
        };
    }

    private String createTextCitation(DatasetVersion version, List<DatasetCreator> creators) {
        return "%s (%d). %s (Version %s). %s.".formatted(
                formatTextCreators(creators),
                getPublicationYear(version),
                version.getTitle(),
                version.getVersionNumber(),
                getOrganization(version).getName()
        );
    }

    private String createApaCitation(DatasetVersion version, List<DatasetCreator> creators) {
        return "%s (%d). %s (Version %s). %s. %s".formatted(
                formatApaCreators(creators),
                getPublicationYear(version),
                version.getTitle(),
                version.getVersionNumber(),
                getOrganization(version).getName(),
                buildLandingPageUrl(version.getDataset().getId(), version.getId())
        );
    }

    private String createBibTeXCitation(
            UUID datasetId,
            UUID versionId,
            DatasetVersion version,
            List<DatasetCreator> creators
    ) {
        String key = "resdatahub-%s-%s".formatted(
                datasetId.toString().substring(0, 8),
                versionId.toString().substring(0, 8)
        );

        return """
                @dataset{%s,
                  author = {%s},
                  title = {%s},
                  year = {%d},
                  version = {%s},
                  publisher = {%s},
                  url = {%s}
                }""".formatted(
                key,
                formatBibTeXCreators(creators),
                escapeBibTeX(version.getTitle()),
                getPublicationYear(version),
                escapeBibTeX(version.getVersionNumber()),
                escapeBibTeX(getOrganization(version).getName()),
                buildLandingPageUrl(datasetId, versionId)
        );
    }

    private String createRisCitation(
            UUID datasetId,
            UUID versionId,
            DatasetVersion version,
            List<DatasetCreator> creators
    ) {
        String authors = creators.stream()
                .map(creator -> "AU  - %s, %s".formatted(
                        creator.getFamilyName(),
                        creator.getGivenName()
                ))
                .collect(Collectors.joining("\n"));

        return """
                TY  - DATA
                %s
                PY  - %d
                TI  - %s
                ET  - Version %s
                PB  - %s
                UR  - %s
                ER  -""".formatted(
                authors,
                getPublicationYear(version),
                version.getTitle(),
                version.getVersionNumber(),
                getOrganization(version).getName(),
                buildLandingPageUrl(datasetId, versionId)
        );
    }

    private String formatTextCreators(List<DatasetCreator> creators) {
        return creators.stream()
                .map(creator -> "%s, %s".formatted(
                        creator.getFamilyName(),
                        getGivenNameInitial(creator)
                ))
                .collect(Collectors.joining("; "));
    }

    private String formatApaCreators(List<DatasetCreator> creators) {
        return creators.stream()
                .map(creator -> "%s, %s".formatted(
                        creator.getFamilyName(),
                        getGivenNameInitial(creator)
                ))
                .collect(Collectors.joining(", "));
    }

    private String formatBibTeXCreators(List<DatasetCreator> creators) {
        return creators.stream()
                .map(creator -> "%s, %s".formatted(
                        escapeBibTeX(creator.getFamilyName()),
                        escapeBibTeX(creator.getGivenName())
                ))
                .collect(Collectors.joining(" and "));
    }

    private int getPublicationYear(DatasetVersion version) {
        return version.getPublishedAt()
                .atZone(ZoneOffset.UTC)
                .getYear();
    }

    private Organization getOrganization(DatasetVersion version) {
        return version.getDataset().getOrganization();
    }

    private String buildLandingPageUrl(UUID datasetId, UUID versionId) {
        return "%s/api/public/datasets/%s/versions/%s".formatted(
                publicBaseUrl,
                datasetId,
                versionId
        );
    }

    private String escapeBibTeX(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("{", "\\{")
                .replace("}", "\\}");
    }

    private String getGivenNameInitial(DatasetCreator creator) {
        String givenName = creator.getGivenName();
        if (givenName == null || givenName.isBlank()) {
            return "";
        }

        return givenName.trim().substring(0, 1) + ".";
    }

    private String trimTrailingSlashes(String value) {
        String trimmedValue = value.trim();

        while (trimmedValue.endsWith("/")) {
            trimmedValue = trimmedValue.substring(0, trimmedValue.length() - 1);
        }

        return trimmedValue;
    }
}
