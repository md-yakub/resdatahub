package com.resdatahub.dataset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DatasetVersionRepository extends JpaRepository<DatasetVersion, UUID> {

    List<DatasetVersion> findByDatasetIdOrderByCreatedAtDesc(UUID datasetId);

    Optional<DatasetVersion> findByIdAndDatasetId(UUID id, UUID datasetId);

    Optional<DatasetVersion> findTopByDatasetIdOrderByCreatedAtDesc(UUID datasetId);

    boolean existsByDatasetIdAndVersionNumberIgnoreCase(UUID datasetId, String versionNumber);

    boolean existsByDatasetIdAndVersionNumberIgnoreCaseAndIdNot(
            UUID datasetId,
            String versionNumber,
            UUID id
    );
}
