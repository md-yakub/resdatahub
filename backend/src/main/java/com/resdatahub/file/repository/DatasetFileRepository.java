package com.resdatahub.file.repository;

import com.resdatahub.file.entity.DatasetFile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DatasetFileRepository extends JpaRepository<DatasetFile, UUID> {

    List<DatasetFile> findByDatasetVersionIdOrderByCreatedAtDesc(UUID datasetVersionId);

    List<DatasetFile> findByDatasetVersionIdOrderByCreatedAtAsc(UUID datasetVersionId);

    @Query("""
            select file
            from DatasetFile file
            where file.datasetVersion.id in :datasetVersionIds
            order by file.datasetVersion.id asc, file.createdAt asc
            """)
    List<DatasetFile> findByDatasetVersionIdsOrderByCreatedAtAsc(
            @Param("datasetVersionIds") Collection<UUID> datasetVersionIds
    );

    Optional<DatasetFile> findByIdAndDatasetVersionId(UUID id, UUID datasetVersionId);

    long countByDatasetVersionId(UUID datasetVersionId);
}
