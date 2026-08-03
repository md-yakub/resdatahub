package com.resdatahub.metadata.service;

import com.resdatahub.creator.repository.DatasetCreatorRepository;
import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.file.repository.DatasetFileRepository;
import com.resdatahub.keyword.repository.DatasetKeywordRepository;
import com.resdatahub.metadata.rdf.RdfMetadataBuilder;
import com.resdatahub.organization.entity.Organization;
import com.resdatahub.support.TestFixtures;
import com.resdatahub.version.entity.DatasetVersion;
import com.resdatahub.version.entity.DatasetVersionStatus;
import com.resdatahub.version.repository.DatasetVersionRepository;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogMetadataServiceTest {

    @Mock
    private DatasetVersionRepository datasetVersionRepository;

    @Mock
    private DatasetCreatorRepository datasetCreatorRepository;

    @Mock
    private DatasetKeywordRepository datasetKeywordRepository;

    @Mock
    private DatasetFileRepository datasetFileRepository;

    @Test
    void knowledgeGraphCatalogModelUsesLatestPublishedVersionsOnly() {
        RdfMetadataBuilder rdfMetadataBuilder = new RdfMetadataBuilder(
                datasetCreatorRepository,
                datasetKeywordRepository,
                datasetFileRepository,
                "http://localhost:8080"
        );
        CatalogMetadataService service = new CatalogMetadataService(
                datasetVersionRepository,
                rdfMetadataBuilder,
                "http://localhost:8080",
                "ResDataHub Catalog",
                "Research dataset catalog",
                "ResDataHub",
                "",
                "http://localhost:8080",
                "en",
                "2026-01-01T00:00:00Z",
                ""
        );
        UUID datasetId = UUID.fromString("60000000-0000-0000-0000-000000000001");
        UUID versionId = UUID.fromString("60000000-0000-0000-0000-000000000002");
        Organization organization = TestFixtures.organization(UUID.fromString("60000000-0000-0000-0000-000000000003"));
        Dataset dataset = TestFixtures.dataset(datasetId, organization);
        DatasetVersion latestPublishedVersion = TestFixtures.version(
                versionId,
                dataset,
                "1.1",
                DatasetVersionStatus.PUBLISHED
        );
        when(datasetVersionRepository.findLatestPublishedVersionPerDataset(DatasetVersionStatus.PUBLISHED))
                .thenReturn(List.of(latestPublishedVersion));
        when(datasetCreatorRepository.findByDatasetVersionIdOrderByPositionAsc(versionId)).thenReturn(List.of());
        when(datasetKeywordRepository.findByDatasetVersionIdOrderByValueAsc(versionId)).thenReturn(List.of());
        when(datasetFileRepository.findByDatasetVersionIdOrderByCreatedAtAsc(versionId)).thenReturn(List.of());

        Model model = service.buildLatestPublishedCatalogModel();
        Resource versionResource = model.createResource(
                "http://localhost:8080/id/dataset/%s/version/%s".formatted(datasetId, versionId)
        );

        assertThat(model.contains(
                model.createResource("http://localhost:8080/id/catalog"),
                model.createProperty(RdfMetadataBuilder.DCAT, "dataset"),
                versionResource
        )).isTrue();
        verify(datasetVersionRepository).findLatestPublishedVersionPerDataset(DatasetVersionStatus.PUBLISHED);
    }
}
