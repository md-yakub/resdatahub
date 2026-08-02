package com.resdatahub.metadata.service;

import com.resdatahub.common.exception.ResourceNotFoundException;
import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.dataset.repository.DatasetRepository;
import com.resdatahub.metadata.dto.MetadataFormat;
import com.resdatahub.metadata.rdf.RdfMetadataBuilder;
import com.resdatahub.version.entity.DatasetVersion;
import com.resdatahub.version.entity.DatasetVersionStatus;
import com.resdatahub.version.repository.DatasetVersionRepository;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class MetadataExportService {

    private final DatasetRepository datasetRepository;
    private final DatasetVersionRepository datasetVersionRepository;
    private final RdfMetadataBuilder rdfMetadataBuilder;

    public MetadataExportService(
            DatasetRepository datasetRepository,
            DatasetVersionRepository datasetVersionRepository,
            RdfMetadataBuilder rdfMetadataBuilder
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetVersionRepository = datasetVersionRepository;
        this.rdfMetadataBuilder = rdfMetadataBuilder;
    }

    @Transactional(readOnly = true)
    public String exportMetadata(UUID datasetId, UUID versionId, MetadataFormat format) {
        findDataset(datasetId);
        DatasetVersion version = datasetVersionRepository.findByIdAndDatasetId(versionId, datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Published dataset version not found"));

        if (version.getStatus() != DatasetVersionStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Published dataset version not found");
        }

        Model model = buildModel(version);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        RDFDataMgr.write(outputStream, model, format.getRdfFormat());
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    private Dataset findDataset(UUID datasetId) {
        return datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found"));
    }

    private Model buildModel(DatasetVersion version) {
        Model model = ModelFactory.createDefaultModel();
        rdfMetadataBuilder.setPrefixes(model);
        rdfMetadataBuilder.addDatasetVersion(model, version);

        return model;
    }
}
