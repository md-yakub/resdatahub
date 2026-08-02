package com.resdatahub.dataset.repository;

import com.resdatahub.dataset.entity.Dataset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DatasetRepository extends JpaRepository<Dataset, UUID> {
}
