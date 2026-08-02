package com.resdatahub.dataset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DatasetKeywordRepository extends JpaRepository<DatasetKeyword, UUID> {

    List<DatasetKeyword> findByDatasetVersionIdOrderByValueAsc(UUID datasetVersionId);

    @Query("""
            select keyword
            from DatasetKeyword keyword
            where keyword.datasetVersion.id in :datasetVersionIds
            order by keyword.datasetVersion.id asc, lower(keyword.value) asc
            """)
    List<DatasetKeyword> findByDatasetVersionIdsOrderByValueAsc(
            @Param("datasetVersionIds") Collection<UUID> datasetVersionIds
    );

    Optional<DatasetKeyword> findByIdAndDatasetVersionId(UUID id, UUID datasetVersionId);

    boolean existsByDatasetVersionIdAndValueIgnoreCase(UUID datasetVersionId, String value);
}
