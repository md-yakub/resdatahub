package com.resdatahub.metadata.service;

import com.resdatahub.metadata.dto.CatalogInfoResponse;
import com.resdatahub.metadata.dto.MetadataFormat;
import com.resdatahub.metadata.rdf.RdfMetadataBuilder;
import com.resdatahub.version.entity.DatasetVersion;
import com.resdatahub.version.entity.DatasetVersionStatus;
import com.resdatahub.version.repository.DatasetVersionRepository;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CatalogMetadataService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 100;
    private static final int MAX_SIZE = 500;

    private final DatasetVersionRepository datasetVersionRepository;
    private final RdfMetadataBuilder rdfMetadataBuilder;
    private final String publicBaseUrl;
    private final String title;
    private final String description;
    private final String publisherName;
    private final String publisherUri;
    private final String homepage;
    private final String language;
    private final Instant issued;
    private final String contact;

    public CatalogMetadataService(
            DatasetVersionRepository datasetVersionRepository,
            RdfMetadataBuilder rdfMetadataBuilder,
            @Value("${resdatahub.public-base-url}") String publicBaseUrl,
            @Value("${resdatahub.catalog.title}") String title,
            @Value("${resdatahub.catalog.description}") String description,
            @Value("${resdatahub.catalog.publisher-name}") String publisherName,
            @Value("${resdatahub.catalog.publisher-uri:}") String publisherUri,
            @Value("${resdatahub.catalog.homepage}") String homepage,
            @Value("${resdatahub.catalog.language}") String language,
            @Value("${resdatahub.catalog.issued}") String issued,
            @Value("${resdatahub.catalog.contact:}") String contact
    ) {
        this.datasetVersionRepository = datasetVersionRepository;
        this.rdfMetadataBuilder = rdfMetadataBuilder;
        this.publicBaseUrl = trimTrailingSlashes(publicBaseUrl);
        this.title = title;
        this.description = description;
        this.publisherName = publisherName;
        this.publisherUri = publisherUri;
        this.homepage = homepage;
        this.language = language;
        this.issued = Instant.parse(issued);
        this.contact = contact;
    }

    @Transactional(readOnly = true)
    public CatalogMetadataResult exportCatalog(MetadataFormat format, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(
                normalizePage(page),
                normalizeSize(size),
                Sort.by(Sort.Direction.DESC, "publishedAt")
        );

        Page<UUID> versionIdPage = datasetVersionRepository.findIdsByStatus(
                DatasetVersionStatus.PUBLISHED,
                pageable
        );
        List<DatasetVersion> versions = getVersionsInPageOrder(versionIdPage.getContent());

        Model model = buildCatalogModel(versions);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        RDFDataMgr.write(outputStream, model, format.getRdfFormat());

        return new CatalogMetadataResult(
                outputStream.toString(StandardCharsets.UTF_8),
                versionIdPage.getTotalElements(),
                versionIdPage.getNumber(),
                versionIdPage.getSize()
        );
    }

    public CatalogInfoResponse getCatalogInfo() {
        return new CatalogInfoResponse(
                title,
                "DCAT",
                "/api/public/catalog/metadata",
                List.of(
                        MetadataFormat.TURTLE.name(),
                        MetadataFormat.JSON_LD.name(),
                        MetadataFormat.RDF_XML.name()
                ),
                publicBaseUrl,
                blankToNull(contact)
        );
    }

    private List<DatasetVersion> getVersionsInPageOrder(List<UUID> versionIds) {
        if (versionIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, DatasetVersion> versionsById = datasetVersionRepository
                .findAllByIdInWithDatasetOrganizationAndLicense(versionIds)
                .stream()
                .collect(Collectors.toMap(DatasetVersion::getId, Function.identity()));

        return versionIds.stream()
                .map(versionsById::get)
                .toList();
    }

    private Model buildCatalogModel(List<DatasetVersion> versions) {
        Model model = ModelFactory.createDefaultModel();
        rdfMetadataBuilder.setPrefixes(model);

        Resource catalog = model.createResource(rdfMetadataBuilder.catalogUri());
        catalog
                .addProperty(RDF.type, model.createResource(RdfMetadataBuilder.DCAT + "Catalog"))
                .addProperty(rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCT, "title"), title)
                .addProperty(rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCT, "description"), description)
                .addProperty(
                        rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCT, "publisher"),
                        rdfMetadataBuilder.addCatalogPublisher(model, publisherUri, publisherName)
                )
                .addProperty(rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCT, "language"), language);

        addHomepage(model, catalog);

        addCatalogDates(model, catalog, versions);

        for (DatasetVersion version : versions) {
            Resource versionResource = rdfMetadataBuilder.addDatasetVersion(model, version);
            catalog.addProperty(rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCAT, "dataset"), versionResource);
        }

        return model;
    }

    private void addCatalogDates(Model model, Resource catalog, List<DatasetVersion> versions) {
        Instant modified = versions.stream()
                .map(DatasetVersion::getUpdatedAt)
                .max(Comparator.naturalOrder())
                .orElse(issued);

        rdfMetadataBuilder.addInstant(model, catalog, rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCT, "issued"), issued);
        rdfMetadataBuilder.addInstant(model, catalog, rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCT, "modified"), modified);
    }

    private void addHomepage(Model model, Resource catalog) {
        if (homepage != null && !homepage.isBlank()) {
            catalog.addProperty(
                    rdfMetadataBuilder.property(model, RdfMetadataBuilder.FOAF, "homepage"),
                    model.createResource(homepage)
            );
        }
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 0) {
            return DEFAULT_PAGE;
        }

        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }

        return Math.min(size, MAX_SIZE);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value;
    }

    private String trimTrailingSlashes(String value) {
        String trimmedValue = value.trim();

        while (trimmedValue.endsWith("/")) {
            trimmedValue = trimmedValue.substring(0, trimmedValue.length() - 1);
        }

        return trimmedValue;
    }

    public record CatalogMetadataResult(
            String metadata,
            long totalElements,
            int page,
            int size
    ) {
    }
}
