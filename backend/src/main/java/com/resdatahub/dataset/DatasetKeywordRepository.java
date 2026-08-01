package com.resdatahub.dataset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DatasetKeywordRepository extends JpaRepository<DatasetKeyword, UUID> {

    List<DatasetKeyword> findByDatasetVersionIdOrderByValueAsc(UUID datasetVersionId);

    Optional<DatasetKeyword> findByIdAndDatasetVersionId(UUID id, UUID datasetVersionId);

    boolean existsByDatasetVersionIdAndValueIgnoreCase(UUID datasetVersionId, String value);
}
