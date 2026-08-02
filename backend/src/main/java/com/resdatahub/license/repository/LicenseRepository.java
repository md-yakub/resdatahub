package com.resdatahub.license.repository;

import com.resdatahub.license.entity.License;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LicenseRepository extends JpaRepository<License, UUID> {

    Optional<License> findByIdAndActiveTrue(UUID id);
}
