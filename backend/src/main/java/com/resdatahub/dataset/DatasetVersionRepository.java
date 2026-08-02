package com.resdatahub.dataset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

    @Query(
            value = """
                    select v.id
                    from DatasetVersion v
                    join v.dataset d
                    join d.organization o
                    left join v.license l
                    where v.status = :status
                      and (:organizationId is null or o.id = :organizationId)
                      and (:keyword is null or exists (
                          select 1
                          from DatasetKeyword keywordFilter
                          where keywordFilter.datasetVersion = v
                            and lower(keywordFilter.value) = :keyword
                      ))
                      and (:licenseCode is null or lower(l.code) = :licenseCode)
                      and (:query is null or (
                          lower(v.title) like :query
                          or lower(v.description) like :query
                          or lower(o.name) like :query
                          or lower(o.shortName) like :query
                          or exists (
                              select 1
                              from DatasetCreator creatorSearch
                              where creatorSearch.datasetVersion = v
                                and (
                                    lower(creatorSearch.givenName) like :query
                                    or lower(creatorSearch.familyName) like :query
                                    or lower(creatorSearch.affiliation) like :query
                                )
                          )
                          or exists (
                              select 1
                              from DatasetKeyword keywordSearch
                              where keywordSearch.datasetVersion = v
                                and lower(keywordSearch.value) like :query
                          )
                      ))
                    """,
            countQuery = """
                    select count(v.id)
                    from DatasetVersion v
                    join v.dataset d
                    join d.organization o
                    left join v.license l
                    where v.status = :status
                      and (:organizationId is null or o.id = :organizationId)
                      and (:keyword is null or exists (
                          select 1
                          from DatasetKeyword keywordFilter
                          where keywordFilter.datasetVersion = v
                            and lower(keywordFilter.value) = :keyword
                      ))
                      and (:licenseCode is null or lower(l.code) = :licenseCode)
                      and (:query is null or (
                          lower(v.title) like :query
                          or lower(v.description) like :query
                          or lower(o.name) like :query
                          or lower(o.shortName) like :query
                          or exists (
                              select 1
                              from DatasetCreator creatorSearch
                              where creatorSearch.datasetVersion = v
                                and (
                                    lower(creatorSearch.givenName) like :query
                                    or lower(creatorSearch.familyName) like :query
                                    or lower(creatorSearch.affiliation) like :query
                                )
                          )
                          or exists (
                              select 1
                              from DatasetKeyword keywordSearch
                              where keywordSearch.datasetVersion = v
                                and lower(keywordSearch.value) like :query
                          )
                      ))
                    """
    )
    Page<UUID> searchPublishedVersionIds(
            @Param("status") DatasetVersionStatus status,
            @Param("query") String query,
            @Param("organizationId") UUID organizationId,
            @Param("keyword") String keyword,
            @Param("licenseCode") String licenseCode,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"dataset", "dataset.organization", "license"})
    @Query("select v from DatasetVersion v where v.id in :ids")
    List<DatasetVersion> findAllByIdInWithDatasetOrganizationAndLicense(@Param("ids") Collection<UUID> ids);

    @Query(
            value = "select v.id from DatasetVersion v where v.status = :status",
            countQuery = "select count(v.id) from DatasetVersion v where v.status = :status"
    )
    Page<UUID> findIdsByStatus(@Param("status") DatasetVersionStatus status, Pageable pageable);
}
