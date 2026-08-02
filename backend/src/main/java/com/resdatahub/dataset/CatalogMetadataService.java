package com.resdatahub.dataset;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
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

    public CatalogMetadataService(
            DatasetVersionRepository datasetVersionRepository,
            RdfMetadataBuilder rdfMetadataBuilder
    ) {
        this.datasetVersionRepository = datasetVersionRepository;
        this.rdfMetadataBuilder = rdfMetadataBuilder;
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
                .addProperty(rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCT, "title"), "ResDataHub Catalog")
                .addProperty(
                        rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCT, "description"),
                        "Catalog of published ResDataHub dataset versions."
                )
                .addProperty(
                        rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCT, "publisher"),
                        rdfMetadataBuilder.addCatalogPublisher(model)
                );

        addCatalogDates(model, catalog, versions);

        for (DatasetVersion version : versions) {
            Resource versionResource = rdfMetadataBuilder.addDatasetVersion(model, version);
            catalog.addProperty(rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCAT, "dataset"), versionResource);
        }

        return model;
    }

    private void addCatalogDates(Model model, Resource catalog, List<DatasetVersion> versions) {
        Instant issued = versions.stream()
                .map(DatasetVersion::getPublishedAt)
                .min(Comparator.naturalOrder())
                .orElse(Instant.now());
        Instant modified = versions.stream()
                .map(DatasetVersion::getUpdatedAt)
                .max(Comparator.naturalOrder())
                .orElse(issued);

        rdfMetadataBuilder.addInstant(model, catalog, rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCT, "issued"), issued);
        rdfMetadataBuilder.addInstant(model, catalog, rdfMetadataBuilder.property(model, RdfMetadataBuilder.DCT, "modified"), modified);
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

    public record CatalogMetadataResult(
            String metadata,
            long totalElements,
            int page,
            int size
    ) {
    }
}
