package com.resdatahub.dataset.service;

import com.resdatahub.common.exception.ConflictException;
import com.resdatahub.creator.repository.DatasetCreatorRepository;
import com.resdatahub.dataset.dto.CreateDatasetRequest;
import com.resdatahub.dataset.dto.DatasetResponse;
import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.dataset.mapper.DatasetMapper;
import com.resdatahub.dataset.repository.DatasetRepository;
import com.resdatahub.file.repository.DatasetFileRepository;
import com.resdatahub.license.repository.LicenseRepository;
import com.resdatahub.organization.entity.Organization;
import com.resdatahub.organization.repository.OrganizationRepository;
import com.resdatahub.support.TestFixtures;
import com.resdatahub.version.dto.CreateDatasetVersionRequest;
import com.resdatahub.version.dto.DatasetVersionResponse;
import com.resdatahub.version.dto.UpdateDatasetVersionRequest;
import com.resdatahub.version.entity.DatasetVersion;
import com.resdatahub.version.entity.DatasetVersionStatus;
import com.resdatahub.version.mapper.DatasetVersionMapper;
import com.resdatahub.version.repository.DatasetVersionRepository;
import com.resdatahub.version.service.DatasetVersionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatasetWorkflowServiceTest {

    private static final UUID ORGANIZATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID DATASET_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID VERSION_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Mock
    private DatasetRepository datasetRepository;

    @Mock
    private DatasetVersionRepository datasetVersionRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private DatasetCreatorRepository datasetCreatorRepository;

    @Mock
    private DatasetFileRepository datasetFileRepository;

    @Mock
    private LicenseRepository licenseRepository;

    private DatasetService datasetService;
    private DatasetVersionService datasetVersionService;
    private Organization organization;
    private Dataset dataset;

    @BeforeEach
    void setUp() {
        DatasetMapper datasetMapper = new DatasetMapper();
        DatasetVersionMapper datasetVersionMapper = new DatasetVersionMapper();
        datasetService = new DatasetService(
                datasetRepository,
                datasetVersionRepository,
                organizationRepository,
                datasetMapper,
                datasetVersionMapper
        );
        datasetVersionService = new DatasetVersionService(
                datasetRepository,
                datasetVersionRepository,
                datasetCreatorRepository,
                datasetFileRepository,
                licenseRepository,
                datasetVersionMapper
        );

        organization = TestFixtures.organization(ORGANIZATION_ID);
        dataset = TestFixtures.dataset(DATASET_ID, organization);
    }

    @Test
    void createDatasetCreatesFirstDraftVersionNumberedOnePointZero() {
        when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(organization));
        when(datasetRepository.save(any(Dataset.class))).thenReturn(dataset);
        when(datasetVersionRepository.save(any(DatasetVersion.class))).thenAnswer(invocation -> {
            DatasetVersion version = invocation.getArgument(0);
            TestFixtures.set(version, "id", VERSION_ID);
            version.setStatus(DatasetVersionStatus.DRAFT);
            TestFixtures.set(version, "createdAt", TestFixtures.CREATED_AT);
            TestFixtures.set(version, "updatedAt", TestFixtures.UPDATED_AT);
            return version;
        });

        DatasetResponse response = datasetService.createDataset(new CreateDatasetRequest(
                "Arctic Sea Ice Thickness Measurements 2025",
                "Measurements collected during the 2025 Arctic campaign.",
                ORGANIZATION_ID
        ));

        assertThat(response.id()).isEqualTo(DATASET_ID);
        assertThat(response.latestVersion()).isNotNull();
        assertThat(response.latestVersion().id()).isEqualTo(VERSION_ID);
        assertThat(response.latestVersion().versionNumber()).isEqualTo("1.0");
        assertThat(response.latestVersion().status()).isEqualTo(DatasetVersionStatus.DRAFT);
    }

    @Test
    void publishDraftChangesStatusToPublishedAndSetsPublishedAt() {
        DatasetVersion draft = TestFixtures.version(VERSION_ID, dataset, "1.0", DatasetVersionStatus.DRAFT);
        draft.setPublishedAt(null);
        when(datasetVersionRepository.findByIdAndDatasetId(VERSION_ID, DATASET_ID)).thenReturn(Optional.of(draft));
        when(datasetCreatorRepository.countByDatasetVersionId(VERSION_ID)).thenReturn(1L);
        when(datasetFileRepository.countByDatasetVersionId(VERSION_ID)).thenReturn(1L);
        when(datasetVersionRepository.save(draft)).thenAnswer(invocation -> invocation.getArgument(0));

        DatasetVersionResponse response = datasetVersionService.publishVersion(DATASET_ID, VERSION_ID);

        assertThat(response.status()).isEqualTo(DatasetVersionStatus.PUBLISHED);
        assertThat(response.publishedAt()).isNotNull();
        assertThat(response.publishedAt()).isAfter(Instant.parse("2025-01-01T00:00:00Z"));
    }

    @Test
    void updatePublishedVersionIsRejected() {
        DatasetVersion published = TestFixtures.version(VERSION_ID, dataset, "1.0", DatasetVersionStatus.PUBLISHED);
        when(datasetVersionRepository.findByIdAndDatasetId(VERSION_ID, DATASET_ID)).thenReturn(Optional.of(published));

        assertThatThrownBy(() -> datasetVersionService.updateVersion(
                DATASET_ID,
                VERSION_ID,
                new UpdateDatasetVersionRequest(null, "Changed title", null, null)
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Only DRAFT dataset versions can be edited");

        verify(datasetVersionRepository, never()).save(any(DatasetVersion.class));
    }

    @Test
    void createNewVersionFromPublishedDatasetCreatesDraftVersionWithRequestedIncrement() {
        UUID nextVersionId = UUID.fromString("00000000-0000-0000-0000-000000000004");
        when(datasetRepository.findById(DATASET_ID)).thenReturn(Optional.of(dataset));
        when(datasetVersionRepository.existsByDatasetIdAndVersionNumberIgnoreCase(DATASET_ID, "1.1"))
                .thenReturn(false);
        when(datasetVersionRepository.save(any(DatasetVersion.class))).thenAnswer(invocation -> {
            DatasetVersion version = invocation.getArgument(0);
            TestFixtures.set(version, "id", nextVersionId);
            version.setStatus(DatasetVersionStatus.DRAFT);
            TestFixtures.set(version, "createdAt", TestFixtures.CREATED_AT);
            TestFixtures.set(version, "updatedAt", TestFixtures.UPDATED_AT);
            return version;
        });

        DatasetVersionResponse response = datasetVersionService.createVersion(
                DATASET_ID,
                new CreateDatasetVersionRequest(
                        "1.1",
                        "Updated sea ice measurements",
                        "Expanded quality-controlled dataset.",
                        "Added validated 2025 observations."
                )
        );

        assertThat(response.id()).isEqualTo(nextVersionId);
        assertThat(response.versionNumber()).isEqualTo("1.1");
        assertThat(response.status()).isEqualTo(DatasetVersionStatus.DRAFT);
    }

    @Test
    void getDatasetUsesLatestVersionReturnedByRepository() {
        DatasetVersion olderPublished = TestFixtures.version(
                UUID.fromString("00000000-0000-0000-0000-000000000005"),
                dataset,
                "1.0",
                DatasetVersionStatus.PUBLISHED
        );
        DatasetVersion latestPublished = TestFixtures.version(
                UUID.fromString("00000000-0000-0000-0000-000000000006"),
                dataset,
                "1.1",
                DatasetVersionStatus.PUBLISHED
        );

        when(datasetRepository.findById(DATASET_ID)).thenReturn(Optional.of(dataset));
        when(datasetVersionRepository.findTopByDatasetIdOrderByCreatedAtDesc(DATASET_ID))
                .thenReturn(Optional.of(latestPublished));

        DatasetResponse response = datasetService.getDataset(DATASET_ID);

        assertThat(response.latestVersion()).isNotNull();
        assertThat(response.latestVersion().id()).isEqualTo(latestPublished.getId());
        assertThat(response.latestVersion().versionNumber()).isEqualTo("1.1");
        assertThat(response.latestVersion().id()).isNotEqualTo(olderPublished.getId());
    }

    @Test
    void getVersionsReturnsRepositoryOrderForDatasetVersionHistory() {
        DatasetVersion versionOne = TestFixtures.version(
                UUID.fromString("00000000-0000-0000-0000-000000000007"),
                dataset,
                "1.0",
                DatasetVersionStatus.PUBLISHED
        );
        DatasetVersion versionTwo = TestFixtures.version(
                UUID.fromString("00000000-0000-0000-0000-000000000008"),
                dataset,
                "1.1",
                DatasetVersionStatus.DRAFT
        );

        when(datasetRepository.findById(DATASET_ID)).thenReturn(Optional.of(dataset));
        when(datasetVersionRepository.findByDatasetIdOrderByCreatedAtDesc(DATASET_ID))
                .thenReturn(List.of(versionTwo, versionOne));

        List<DatasetVersionResponse> response = datasetVersionService.getVersions(DATASET_ID);

        assertThat(response)
                .extracting(DatasetVersionResponse::versionNumber)
                .containsExactly("1.1", "1.0");
    }
}
