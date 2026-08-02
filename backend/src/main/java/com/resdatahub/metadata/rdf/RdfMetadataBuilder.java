package com.resdatahub.metadata.rdf;

import com.resdatahub.creator.entity.DatasetCreator;
import com.resdatahub.creator.repository.DatasetCreatorRepository;
import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.file.repository.DatasetFileRepository;
import com.resdatahub.keyword.repository.DatasetKeywordRepository;
import com.resdatahub.license.entity.License;
import com.resdatahub.organization.entity.Organization;
import com.resdatahub.version.entity.DatasetVersion;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class RdfMetadataBuilder {

    public static final String DCAT = "http://www.w3.org/ns/dcat#";
    public static final String DCT = "http://purl.org/dc/terms/";
    public static final String FOAF = "http://xmlns.com/foaf/0.1/";
    public static final String SPDX = "http://spdx.org/rdf/terms#";
    public static final String RESDATAHUB = "https://resdatahub.example/vocab#";

    private final DatasetCreatorRepository datasetCreatorRepository;
    private final DatasetKeywordRepository datasetKeywordRepository;
    private final DatasetFileRepository datasetFileRepository;
    private final String publicBaseUrl;

    public RdfMetadataBuilder(
            DatasetCreatorRepository datasetCreatorRepository,
            DatasetKeywordRepository datasetKeywordRepository,
            DatasetFileRepository datasetFileRepository,
            @Value("${resdatahub.public-base-url}") String publicBaseUrl
    ) {
        this.datasetCreatorRepository = datasetCreatorRepository;
        this.datasetKeywordRepository = datasetKeywordRepository;
        this.datasetFileRepository = datasetFileRepository;
        this.publicBaseUrl = trimTrailingSlashes(publicBaseUrl);
    }

    public void setPrefixes(Model model) {
        model.setNsPrefix("dcat", DCAT);
        model.setNsPrefix("dct", DCT);
        model.setNsPrefix("foaf", FOAF);
        model.setNsPrefix("spdx", SPDX);
        model.setNsPrefix("resdatahub", RESDATAHUB);
    }

    public Resource addDatasetVersion(Model model, DatasetVersion version) {
        Dataset dataset = version.getDataset();
        Organization organization = dataset.getOrganization();
        Resource datasetResource = model.createResource(datasetUri(dataset.getId()));
        Resource versionResource = model.createResource(versionUri(dataset.getId(), version.getId()));
        Resource organizationResource = addOrganization(model, organization);

        datasetResource
                .addProperty(RDF.type, model.createResource(DCAT + "Dataset"))
                .addProperty(property(model, DCT, "identifier"), dataset.getId().toString())
                .addProperty(property(model, DCT, "publisher"), organizationResource)
                .addProperty(property(model, DCAT, "version"), version.getVersionNumber())
                .addProperty(property(model, DCT, "hasVersion"), versionResource);

        versionResource
                .addProperty(RDF.type, model.createResource(DCAT + "Dataset"))
                .addProperty(property(model, DCT, "identifier"), version.getId().toString())
                .addProperty(property(model, DCT, "title"), version.getTitle())
                .addProperty(property(model, DCT, "description"), version.getDescription())
                .addProperty(property(model, DCT, "publisher"), organizationResource)
                .addProperty(property(model, DCAT, "version"), version.getVersionNumber());

        addInstant(model, versionResource, property(model, DCT, "issued"), version.getPublishedAt());
        addInstant(model, versionResource, property(model, DCT, "modified"), version.getUpdatedAt());
        addLicense(model, versionResource, version.getLicense());
        addCreators(model, versionResource, version.getId());
        addKeywords(model, versionResource, version.getId());
        addFiles(model, versionResource, dataset.getId(), version.getId());

        return versionResource;
    }

    public Resource addCatalogPublisher(Model model) {
        Resource publisher = model.createResource("%s/id/organization/resdatahub".formatted(publicBaseUrl));
        publisher
                .addProperty(RDF.type, model.createResource(FOAF + "Organization"))
                .addProperty(property(model, FOAF, "name"), "ResDataHub");
        return publisher;
    }

    public String catalogUri() {
        return "%s/id/catalog".formatted(publicBaseUrl);
    }

    private Resource addOrganization(Model model, Organization organization) {
        Resource organizationResource = model.createResource(organizationUri(organization.getId()));
        organizationResource
                .addProperty(RDF.type, model.createResource(FOAF + "Organization"))
                .addProperty(property(model, FOAF, "name"), organization.getName());

        addOptionalLiteral(organizationResource, property(model, FOAF, "nick"), organization.getShortName());
        addOptionalResource(model, organizationResource, property(model, FOAF, "homepage"), organization.getWebsite());

        return organizationResource;
    }

    private void addCreators(Model model, Resource versionResource, UUID versionId) {
        List<DatasetCreator> creators = datasetCreatorRepository.findByDatasetVersionIdOrderByPositionAsc(versionId);

        for (DatasetCreator creator : creators) {
            Resource creatorResource = createCreatorResource(model, creator);
            versionResource.addProperty(property(model, DCT, "creator"), creatorResource);
        }
    }

    private Resource createCreatorResource(Model model, DatasetCreator creator) {
        String creatorUri = creator.getOrcid() == null || creator.getOrcid().isBlank()
                ? creatorUri(creator.getId())
                : "https://orcid.org/" + creator.getOrcid();

        Resource creatorResource = model.createResource(creatorUri);
        creatorResource
                .addProperty(RDF.type, model.createResource(FOAF + "Person"))
                .addProperty(property(model, FOAF, "givenName"), creator.getGivenName())
                .addProperty(property(model, FOAF, "familyName"), creator.getFamilyName());

        addOptionalLiteral(creatorResource, property(model, RESDATAHUB, "affiliation"), creator.getAffiliation());
        if (creator.getOrcid() != null && !creator.getOrcid().isBlank()) {
            addOptionalResource(model, creatorResource, property(model, RESDATAHUB, "orcid"), creatorUri);
        }

        return creatorResource;
    }

    private void addKeywords(Model model, Resource versionResource, UUID versionId) {
        datasetKeywordRepository.findByDatasetVersionIdOrderByValueAsc(versionId)
                .forEach(keyword -> versionResource.addProperty(property(model, DCAT, "keyword"), keyword.getValue()));
    }

    private void addLicense(Model model, Resource versionResource, License license) {
        if (license != null && license.getUri() != null && !license.getUri().isBlank()) {
            versionResource.addProperty(property(model, DCT, "license"), model.createResource(license.getUri()));
        }
    }

    private void addFiles(Model model, Resource versionResource, UUID datasetId, UUID versionId) {
        datasetFileRepository.findByDatasetVersionIdOrderByCreatedAtAsc(versionId)
                .forEach(file -> {
                    Resource distribution = model.createResource(fileUri(file.getId()));
                    distribution
                            .addProperty(RDF.type, model.createResource(DCAT + "Distribution"))
                            .addProperty(property(model, DCT, "identifier"), file.getId().toString())
                            .addProperty(property(model, DCT, "title"), file.getOriginalFilename())
                            .addProperty(property(model, DCAT, "downloadURL"), model.createResource(buildDownloadUrl(datasetId, versionId, file.getId())))
                            .addProperty(property(model, DCAT, "mediaType"), file.getContentType())
                            .addLiteral(property(model, DCAT, "byteSize"), file.getFileSize())
                            .addProperty(property(model, SPDX, "checksumValue"), file.getSha256())
                            .addProperty(property(model, RESDATAHUB, "filename"), file.getOriginalFilename())
                            .addProperty(property(model, RESDATAHUB, "fileCategory"), file.getCategory().name());

                    versionResource.addProperty(property(model, DCAT, "distribution"), distribution);
                });
    }

    public void addInstant(Model model, Resource resource, Property property, Instant value) {
        if (value != null) {
            resource.addLiteral(property, model.createTypedLiteral(value.toString(), XSDDatatype.XSDdateTime));
        }
    }

    private void addOptionalLiteral(Resource resource, Property property, String value) {
        if (value != null && !value.isBlank()) {
            resource.addProperty(property, value);
        }
    }

    private void addOptionalResource(Model model, Resource resource, Property property, String value) {
        if (value != null && !value.isBlank()) {
            resource.addProperty(property, model.createResource(value));
        }
    }

    public Property property(Model model, String namespace, String localName) {
        return model.createProperty(namespace, localName);
    }

    private String datasetUri(UUID datasetId) {
        return "%s/id/dataset/%s".formatted(publicBaseUrl, datasetId);
    }

    private String versionUri(UUID datasetId, UUID versionId) {
        return "%s/id/dataset/%s/version/%s".formatted(publicBaseUrl, datasetId, versionId);
    }

    private String organizationUri(UUID organizationId) {
        return "%s/id/organization/%s".formatted(publicBaseUrl, organizationId);
    }

    private String creatorUri(UUID creatorId) {
        return "%s/id/creator/%s".formatted(publicBaseUrl, creatorId);
    }

    private String fileUri(UUID fileId) {
        return "%s/id/file/%s".formatted(publicBaseUrl, fileId);
    }

    private String buildDownloadUrl(UUID datasetId, UUID versionId, UUID fileId) {
        return "%s/api/datasets/%s/versions/%s/files/%s/download".formatted(
                publicBaseUrl,
                datasetId,
                versionId,
                fileId
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
