package com.resdatahub.creator.repository;

import com.resdatahub.creator.entity.DatasetCreator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DatasetCreatorRepository extends JpaRepository<DatasetCreator, UUID> {

    List<DatasetCreator> findByDatasetVersionIdOrderByPositionAsc(UUID datasetVersionId);

    @Query("""
            select creator
            from DatasetCreator creator
            where creator.datasetVersion.id in :datasetVersionIds
            order by creator.datasetVersion.id asc, creator.position asc
            """)
    List<DatasetCreator> findByDatasetVersionIdsOrderByPositionAsc(
            @Param("datasetVersionIds") Collection<UUID> datasetVersionIds
    );

    Optional<DatasetCreator> findByIdAndDatasetVersionId(UUID id, UUID datasetVersionId);

    long countByDatasetVersionId(UUID datasetVersionId);

    boolean existsByDatasetVersionIdAndPosition(UUID datasetVersionId, Integer position);

    boolean existsByDatasetVersionIdAndPositionAndIdNot(
            UUID datasetVersionId,
            Integer position,
            UUID id
    );
}
