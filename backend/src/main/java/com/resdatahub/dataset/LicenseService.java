package com.resdatahub.dataset;

import com.resdatahub.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;

    public LicenseService(LicenseRepository licenseRepository) {
        this.licenseRepository = licenseRepository;
    }

    @Transactional(readOnly = true)
    public List<LicenseResponse> getLicenses() {
        return licenseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LicenseResponse getLicense(UUID id) {
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("License not found"));

        return toResponse(license);
    }

    private LicenseResponse toResponse(License license) {
        return new LicenseResponse(
                license.getId(),
                license.getCode(),
                license.getName(),
                license.getUri(),
                license.isActive(),
                license.getCreatedAt(),
                license.getUpdatedAt()
        );
    }
}
