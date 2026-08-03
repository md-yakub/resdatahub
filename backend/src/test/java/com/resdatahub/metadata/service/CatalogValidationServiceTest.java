package com.resdatahub.metadata.service;

import com.resdatahub.creator.repository.DatasetCreatorRepository;
import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.file.repository.DatasetFileRepository;
import com.resdatahub.keyword.repository.DatasetKeywordRepository;
import com.resdatahub.metadata.dto.CatalogValidationResponse;
import com.resdatahub.metadata.dto.CatalogValidationViolationResponse;
import com.resdatahub.metadata.dto.ValidationSeverity;
import com.resdatahub.organization.entity.Organization;
import com.resdatahub.support.TestFixtures;
import com.resdatahub.version.entity.DatasetVersion;
import com.resdatahub.version.entity.DatasetVersionStatus;
import com.resdatahub.version.repository.DatasetVersionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogValidationServiceTest {

    private static final UUID DATASET_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID VERSION_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");

    @Mock
    private DatasetVersionRepository datasetVersionRepository;

    @Mock
    private DatasetCreatorRepository datasetCreatorRepository;

    @Mock
    private DatasetKeywordRepository datasetKeywordRepository;

    @Mock
    private DatasetFileRepository datasetFileRepository;

    private CatalogValidationService validationService;
    private DatasetVersion version;

    @BeforeEach
    void setUp() {
        validationService = new CatalogValidationService(
                datasetVersionRepository,
                datasetCreatorRepository,
                datasetKeywordRepository,
                datasetFileRepository,
                "ResDataHub Catalog",
                "Research dataset catalog",
                "ResDataHub",
                "http://localhost:8080",
                "en",
                "http://localhost:8080"
        );

        Organization organization = TestFixtures.organization(UUID.fromString("10000000-0000-0000-0000-000000000003"));
        Dataset dataset = TestFixtures.dataset(DATASET_ID, organization);
        version = TestFixtures.version(VERSION_ID, dataset, "1.0", DatasetVersionStatus.PUBLISHED);
    }

    @Test
    void completePublishedMetadataPassesValidation() {
        when(datasetVersionRepository.findLatestPublishedVersionPerDataset(DatasetVersionStatus.PUBLISHED))
                .thenReturn(List.of(version));
        when(datasetCreatorRepository.findByDatasetVersionIdsOrderByPositionAsc(List.of(VERSION_ID)))
                .thenReturn(List.of(TestFixtures.creator(
                        UUID.fromString("10000000-0000-0000-0000-000000000004"),
                        version,
                        1
                )));
        when(datasetKeywordRepository.findByDatasetVersionIdsOrderByValueAsc(List.of(VERSION_ID)))
                .thenReturn(List.of(TestFixtures.keyword(
                        UUID.fromString("10000000-0000-0000-0000-000000000005"),
                        version,
                        "Arctic"
                )));
        when(datasetFileRepository.findByDatasetVersionIdsOrderByCreatedAtAsc(List.of(VERSION_ID)))
                .thenReturn(List.of(TestFixtures.file(
                        UUID.fromString("10000000-0000-0000-0000-000000000006"),
                        version
                )));

        CatalogValidationResponse response = validationService.validateCatalog();

        assertThat(response.conforms()).isTrue();
        assertThat(response.checkedDatasets()).isEqualTo(1);
        assertThat(response.violations()).isEmpty();
    }

    @Test
    void missingRequiredMetadataReturnsErrorsAndWarnings() {
        version.setTitle("");
        version.setDescription("");
        version.setLicense(null);
        when(datasetVersionRepository.findLatestPublishedVersionPerDataset(DatasetVersionStatus.PUBLISHED))
                .thenReturn(List.of(version));
        when(datasetCreatorRepository.findByDatasetVersionIdsOrderByPositionAsc(List.of(VERSION_ID)))
                .thenReturn(List.of());
        when(datasetKeywordRepository.findByDatasetVersionIdsOrderByValueAsc(List.of(VERSION_ID)))
                .thenReturn(List.of());
        when(datasetFileRepository.findByDatasetVersionIdsOrderByCreatedAtAsc(List.of(VERSION_ID)))
                .thenReturn(List.of());

        CatalogValidationResponse response = validationService.validateCatalog();

        assertThat(response.conforms()).isFalse();
        assertThat(response.checkedDatasets()).isEqualTo(1);
        assertThat(response.violations())
                .extracting(CatalogValidationViolationResponse::property)
                .contains(
                        "dct:title",
                        "dct:description",
                        "dct:license",
                        "dcat:distribution",
                        "dct:creator",
                        "dcat:keyword"
                );
        assertThat(response.violations())
                .extracting(CatalogValidationViolationResponse::severity)
                .contains(ValidationSeverity.ERROR, ValidationSeverity.WARNING);
    }
}
