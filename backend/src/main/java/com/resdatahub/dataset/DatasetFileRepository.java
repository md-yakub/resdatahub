package com.resdatahub.dataset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DatasetFileRepository extends JpaRepository<DatasetFile, UUID> {

    List<DatasetFile> findByDatasetVersionIdOrderByCreatedAtDesc(UUID datasetVersionId);

    List<DatasetFile> findByDatasetVersionIdOrderByCreatedAtAsc(UUID datasetVersionId);

    Optional<DatasetFile> findByIdAndDatasetVersionId(UUID id, UUID datasetVersionId);

    long countByDatasetVersionId(UUID datasetVersionId);
}
