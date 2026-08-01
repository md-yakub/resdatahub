package com.resdatahub.dataset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DatasetCreatorRepository extends JpaRepository<DatasetCreator, UUID> {

    List<DatasetCreator> findByDatasetVersionIdOrderByPositionAsc(UUID datasetVersionId);

    Optional<DatasetCreator> findByIdAndDatasetVersionId(UUID id, UUID datasetVersionId);

    long countByDatasetVersionId(UUID datasetVersionId);

    boolean existsByDatasetVersionIdAndPosition(UUID datasetVersionId, Integer position);

    boolean existsByDatasetVersionIdAndPositionAndIdNot(
            UUID datasetVersionId,
            Integer position,
            UUID id
    );
}
