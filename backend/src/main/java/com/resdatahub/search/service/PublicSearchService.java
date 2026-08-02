package com.resdatahub.search.service;

import com.resdatahub.creator.entity.DatasetCreator;
import com.resdatahub.creator.repository.DatasetCreatorRepository;
import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.keyword.entity.DatasetKeyword;
import com.resdatahub.keyword.repository.DatasetKeywordRepository;
import com.resdatahub.license.entity.License;
import com.resdatahub.organization.entity.Organization;
import com.resdatahub.search.dto.PublicSearchItemResponse;
import com.resdatahub.search.dto.PublicSearchResponse;
import com.resdatahub.search.dto.SearchSort;
import com.resdatahub.version.entity.DatasetVersion;
import com.resdatahub.version.entity.DatasetVersionStatus;
import com.resdatahub.version.repository.DatasetVersionRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PublicSearchService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final DatasetVersionRepository datasetVersionRepository;
    private final DatasetCreatorRepository datasetCreatorRepository;
    private final DatasetKeywordRepository datasetKeywordRepository;
    private final String publicBaseUrl;

    public PublicSearchService(
            DatasetVersionRepository datasetVersionRepository,
            DatasetCreatorRepository datasetCreatorRepository,
            DatasetKeywordRepository datasetKeywordRepository,
            @Value("${resdatahub.public-base-url}") String publicBaseUrl
    ) {
        this.datasetVersionRepository = datasetVersionRepository;
        this.datasetCreatorRepository = datasetCreatorRepository;
        this.datasetKeywordRepository = datasetKeywordRepository;
        this.publicBaseUrl = trimTrailingSlashes(publicBaseUrl);
    }

    @Transactional(readOnly = true)
    public PublicSearchResponse search(
            String q,
            Integer page,
            Integer size,
            UUID organizationId,
            String keyword,
            String licenseCode,
            SearchSort searchSort
    ) {
        int safePage = page == null || page < 0 ? DEFAULT_PAGE : page;
        int safeSize = normalizeSize(size);
        SearchSort safeSort = searchSort == null ? SearchSort.NEWEST : searchSort;
        Pageable pageable = PageRequest.of(safePage, safeSize, toSort(safeSort));

        Page<UUID> versionIdPage = datasetVersionRepository.searchPublishedVersionIds(
                DatasetVersionStatus.PUBLISHED,
                toLikeQuery(q),
                organizationId,
                toExactLowercase(keyword),
                toExactLowercase(licenseCode),
                pageable
        );

        List<PublicSearchItemResponse> items = getItems(versionIdPage.getContent());

        return new PublicSearchResponse(
                items,
                versionIdPage.getNumber(),
                versionIdPage.getSize(),
                versionIdPage.getTotalElements(),
                versionIdPage.getTotalPages()
        );
    }

    private List<PublicSearchItemResponse> getItems(List<UUID> versionIds) {
        if (versionIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, DatasetVersion> versionsById = datasetVersionRepository
                .findAllByIdInWithDatasetOrganizationAndLicense(versionIds)
                .stream()
                .collect(Collectors.toMap(DatasetVersion::getId, Function.identity()));

        Map<UUID, List<DatasetCreator>> creatorsByVersionId = getCreatorsByVersionId(versionIds);
        Map<UUID, List<DatasetKeyword>> keywordsByVersionId = getKeywordsByVersionId(versionIds);

        return versionIds.stream()
                .map(versionsById::get)
                .map(version -> toItemResponse(
                        version,
                        creatorsByVersionId.getOrDefault(version.getId(), List.of()),
                        keywordsByVersionId.getOrDefault(version.getId(), List.of())
                ))
                .toList();
    }

    private Map<UUID, List<DatasetCreator>> getCreatorsByVersionId(Collection<UUID> versionIds) {
        return datasetCreatorRepository.findByDatasetVersionIdsOrderByPositionAsc(versionIds)
                .stream()
                .collect(Collectors.groupingBy(
                        creator -> creator.getDatasetVersion().getId(),
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                creators -> creators.stream()
                                        .sorted(Comparator.comparing(DatasetCreator::getPosition))
                                        .toList()
                        )
                ));
    }

    private Map<UUID, List<DatasetKeyword>> getKeywordsByVersionId(Collection<UUID> versionIds) {
        return datasetKeywordRepository.findByDatasetVersionIdsOrderByValueAsc(versionIds)
                .stream()
                .collect(Collectors.groupingBy(
                        keyword -> keyword.getDatasetVersion().getId(),
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                keywords -> keywords.stream()
                                        .sorted(Comparator.comparing(DatasetKeyword::getValue, String.CASE_INSENSITIVE_ORDER))
                                        .toList()
                        )
                ));
    }

    private PublicSearchItemResponse toItemResponse(
            DatasetVersion version,
            List<DatasetCreator> creators,
            List<DatasetKeyword> keywords
    ) {
        Dataset dataset = version.getDataset();
        Organization organization = dataset.getOrganization();
        License license = version.getLicense();

        return new PublicSearchItemResponse(
                dataset.getId(),
                version.getId(),
                version.getVersionNumber(),
                version.getTitle(),
                version.getDescription(),
                new PublicSearchItemResponse.OrganizationSummary(
                        organization.getId(),
                        organization.getName(),
                        organization.getShortName()
                ),
                creators.stream()
                        .map(creator -> new PublicSearchItemResponse.CreatorSummary(
                                creator.getGivenName(),
                                creator.getFamilyName(),
                                creator.getPosition()
                        ))
                        .toList(),
                keywords.stream()
                        .map(DatasetKeyword::getValue)
                        .toList(),
                license == null ? null : license.getCode(),
                version.getPublishedAt(),
                buildLandingPageUrl(dataset.getId(), version.getId())
        );
    }

    private Sort toSort(SearchSort searchSort) {
        return switch (searchSort) {
            case NEWEST -> Sort.by(Sort.Direction.DESC, "publishedAt");
            case OLDEST -> Sort.by(Sort.Direction.ASC, "publishedAt");
            case TITLE_ASC -> Sort.by(Sort.Direction.ASC, "title");
            case TITLE_DESC -> Sort.by(Sort.Direction.DESC, "title");
        };
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }

        if (size < 1) {
            return DEFAULT_SIZE;
        }

        return Math.min(size, MAX_SIZE);
    }

    private String toLikeQuery(String value) {
        String normalizedValue = toExactLowercase(value);
        if (normalizedValue == null) {
            return null;
        }

        return "%" + normalizedValue + "%";
    }

    private String toExactLowercase(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String buildLandingPageUrl(UUID datasetId, UUID versionId) {
        return "%s/api/public/datasets/%s/versions/%s".formatted(
                publicBaseUrl,
                datasetId,
                versionId
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
