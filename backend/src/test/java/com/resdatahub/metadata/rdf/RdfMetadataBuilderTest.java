package com.resdatahub.metadata.rdf;

import com.resdatahub.creator.entity.DatasetCreator;
import com.resdatahub.creator.repository.DatasetCreatorRepository;
import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.file.entity.DatasetFile;
import com.resdatahub.file.repository.DatasetFileRepository;
import com.resdatahub.keyword.entity.DatasetKeyword;
import com.resdatahub.keyword.repository.DatasetKeywordRepository;
import com.resdatahub.organization.entity.Organization;
import com.resdatahub.support.TestFixtures;
import com.resdatahub.version.entity.DatasetVersion;
import com.resdatahub.version.entity.DatasetVersionStatus;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
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
class RdfMetadataBuilderTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final UUID ORGANIZATION_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID DATASET_ID = UUID.fromString("30000000-0000-0000-0000-000000000002");
    private static final UUID VERSION_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final UUID CREATOR_ID = UUID.fromString("30000000-0000-0000-0000-000000000004");
    private static final UUID KEYWORD_ID = UUID.fromString("30000000-0000-0000-0000-000000000005");
    private static final UUID FILE_ID = UUID.fromString("30000000-0000-0000-0000-000000000006");

    @Mock
    private DatasetCreatorRepository datasetCreatorRepository;

    @Mock
    private DatasetKeywordRepository datasetKeywordRepository;

    @Mock
    private DatasetFileRepository datasetFileRepository;

    private RdfMetadataBuilder rdfMetadataBuilder;
    private DatasetVersion version;

    @BeforeEach
    void setUp() {
        rdfMetadataBuilder = new RdfMetadataBuilder(
                datasetCreatorRepository,
                datasetKeywordRepository,
                datasetFileRepository,
                BASE_URL
        );

        Organization organization = TestFixtures.organization(ORGANIZATION_ID);
        Dataset dataset = TestFixtures.dataset(DATASET_ID, organization);
        version = TestFixtures.version(VERSION_ID, dataset, "1.0", DatasetVersionStatus.PUBLISHED);
    }

    @Test
    void generatedModelContainsExpectedDatasetCreatorPublisherLicenseAndKeywordTriples() {
        DatasetCreator creator = TestFixtures.creator(CREATOR_ID, version, 1);
        DatasetKeyword keyword = TestFixtures.keyword(KEYWORD_ID, version, "Arctic");
        DatasetFile file = TestFixtures.file(FILE_ID, version);
        when(datasetCreatorRepository.findByDatasetVersionIdOrderByPositionAsc(VERSION_ID)).thenReturn(List.of(creator));
        when(datasetKeywordRepository.findByDatasetVersionIdOrderByValueAsc(VERSION_ID)).thenReturn(List.of(keyword));
        when(datasetFileRepository.findByDatasetVersionIdOrderByCreatedAtAsc(VERSION_ID)).thenReturn(List.of(file));

        Model model = ModelFactory.createDefaultModel();
        rdfMetadataBuilder.setPrefixes(model);
        Resource versionResource = rdfMetadataBuilder.addDatasetVersion(model, version);

        Resource datasetResource = model.createResource("%s/id/dataset/%s".formatted(BASE_URL, DATASET_ID));
        Resource organizationResource = model.createResource("%s/id/organization/%s".formatted(BASE_URL, ORGANIZATION_ID));
        Resource creatorResource = model.createResource("https://orcid.org/0000-0000-0000-0000");
        Resource fileResource = model.createResource("%s/id/file/%s".formatted(BASE_URL, FILE_ID));

        assertThat(model.contains(datasetResource, RDF.type, model.createResource(RdfMetadataBuilder.DCAT + "Dataset"))).isTrue();
        assertThat(model.contains(
                versionResource,
                rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCT, "title"),
                "Arctic Sea Ice Thickness Measurements 2025"
        )).isTrue();
        assertThat(model.contains(versionResource, rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCT, "publisher"), organizationResource)).isTrue();
        assertThat(model.contains(versionResource, rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCT, "creator"), creatorResource)).isTrue();
        assertThat(model.contains(versionResource, rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCT, "license"), model.createResource("https://creativecommons.org/licenses/by/4.0/"))).isTrue();
        assertThat(model.contains(versionResource, rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCAT, "keyword"), "Arctic")).isTrue();
        assertThat(model.contains(versionResource, rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCAT, "distribution"), fileResource)).isTrue();
        assertThat(model.contains(organizationResource, rdfMetadataBuilder.property(model, RdfMetadataBuilder.FOAF, "name"), "Alfred Wegener Institute")).isTrue();
    }
}
