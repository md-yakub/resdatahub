package com.resdatahub.citation.service;

import com.resdatahub.citation.dto.CitationFormat;
import com.resdatahub.citation.dto.CitationResponse;
import com.resdatahub.creator.entity.DatasetCreator;
import com.resdatahub.creator.repository.DatasetCreatorRepository;
import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.dataset.repository.DatasetRepository;
import com.resdatahub.organization.entity.Organization;
import com.resdatahub.support.TestFixtures;
import com.resdatahub.version.entity.DatasetVersion;
import com.resdatahub.version.entity.DatasetVersionStatus;
import com.resdatahub.version.repository.DatasetVersionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitationServiceTest {

    private static final UUID DATASET_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID VERSION_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");

    @Mock
    private DatasetRepository datasetRepository;

    @Mock
    private DatasetVersionRepository datasetVersionRepository;

    @Mock
    private DatasetCreatorRepository datasetCreatorRepository;

    private CitationService citationService;
    private Dataset dataset;
    private DatasetVersion version;

    @BeforeEach
    void setUp() {
        citationService = new CitationService(
                datasetRepository,
                datasetVersionRepository,
                datasetCreatorRepository,
                "http://localhost:8080/"
        );

        Organization organization = TestFixtures.organization(UUID.fromString("20000000-0000-0000-0000-000000000003"));
        dataset = TestFixtures.dataset(DATASET_ID, organization);
        version = TestFixtures.version(VERSION_ID, dataset, "1.0", DatasetVersionStatus.PUBLISHED);
    }

    @ParameterizedTest
    @EnumSource(CitationFormat.class)
    void citationContainsCorePublishedDatasetInformation(CitationFormat format) {
        mockPublishedVersionWithCreators();

        CitationResponse response = citationService.getCitation(DATASET_ID, VERSION_ID, format);

        assertThat(response.format()).isEqualTo(format);
        assertThat(response.citation()).contains("Arctic Sea Ice Thickness Measurements 2025");
        assertThat(response.citation()).contains("2026");
        assertThat(response.citation()).contains("Alfred Wegener Institute");
        assertThat(response.citation()).contains("1.0");
    }

    @Test
    void apaCitationIncludesInitialedCreatorAndLandingPageUrl() {
        mockPublishedVersionWithCreators();

        CitationResponse response = citationService.getCitation(DATASET_ID, VERSION_ID, CitationFormat.APA);

        assertThat(response.citation())
                .contains("Muller, A.")
                .contains("http://localhost:8080/api/public/datasets/%s/versions/%s".formatted(DATASET_ID, VERSION_ID));
    }

    @Test
    void bibtexCitationReturnsDatasetEntry() {
        mockPublishedVersionWithCreators();

        CitationResponse response = citationService.getCitation(DATASET_ID, VERSION_ID, CitationFormat.BIBTEX);

        assertThat(response.citation())
                .contains("@dataset{resdatahub-20000000-20000000")
                .contains("author = {Muller, Anna}")
                .contains("url = {http://localhost:8080/api/public/datasets/%s/versions/%s}".formatted(DATASET_ID, VERSION_ID));
    }

    @Test
    void risCitationReturnsLineBasedDatasetCitation() {
        mockPublishedVersionWithCreators();

        CitationResponse response = citationService.getCitation(DATASET_ID, VERSION_ID, CitationFormat.RIS);

        assertThat(response.citation())
                .contains("TY  - DATA")
                .contains("AU  - Muller, Anna")
                .contains("ER  -");
    }

    @Test
    void plainTextCitationContainsHumanReadableReference() {
        mockPublishedVersionWithCreators();

        CitationResponse response = citationService.getCitation(DATASET_ID, VERSION_ID, CitationFormat.TEXT);

        assertThat(response.citation())
                .isEqualTo("Muller, A. (2026). Arctic Sea Ice Thickness Measurements 2025 (Version 1.0). Alfred Wegener Institute.");
    }

    private void mockPublishedVersionWithCreators() {
        DatasetCreator creator = TestFixtures.creator(
                UUID.fromString("20000000-0000-0000-0000-000000000004"),
                version,
                1
        );

        when(datasetRepository.findById(DATASET_ID)).thenReturn(Optional.of(dataset));
        when(datasetVersionRepository.findByIdAndDatasetId(VERSION_ID, DATASET_ID)).thenReturn(Optional.of(version));
        when(datasetCreatorRepository.findByDatasetVersionIdOrderByPositionAsc(VERSION_ID)).thenReturn(List.of(creator));
    }
}
