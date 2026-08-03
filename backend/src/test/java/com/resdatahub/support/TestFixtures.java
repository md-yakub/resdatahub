package com.resdatahub.support;

import com.resdatahub.creator.entity.DatasetCreator;
import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.file.entity.DatasetFile;
import com.resdatahub.file.entity.DatasetFileCategory;
import com.resdatahub.keyword.entity.DatasetKeyword;
import com.resdatahub.license.entity.License;
import com.resdatahub.organization.entity.Organization;
import com.resdatahub.version.entity.DatasetVersion;
import com.resdatahub.version.entity.DatasetVersionStatus;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

public final class TestFixtures {

    public static final Instant CREATED_AT = Instant.parse("2026-01-10T10:00:00Z");
    public static final Instant UPDATED_AT = Instant.parse("2026-01-11T10:00:00Z");
    public static final Instant PUBLISHED_AT = Instant.parse("2026-02-01T10:00:00Z");

    private TestFixtures() {
    }

    public static Organization organization(UUID id) {
        Organization organization = new Organization();
        set(organization, "id", id);
        organization.setName("Alfred Wegener Institute");
        organization.setShortName("AWI");
        organization.setDescription("Polar research institute");
        organization.setWebsite("https://www.awi.de");
        setTimestamps(organization);
        return organization;
    }

    public static Dataset dataset(UUID id, Organization organization) {
        Dataset dataset = new Dataset();
        set(dataset, "id", id);
        dataset.setOrganization(organization);
        setTimestamps(dataset);
        return dataset;
    }

    public static DatasetVersion version(
            UUID id,
            Dataset dataset,
            String versionNumber,
            DatasetVersionStatus status
    ) {
        DatasetVersion version = new DatasetVersion();
        set(version, "id", id);
        version.setDataset(dataset);
        version.setVersionNumber(versionNumber);
        version.setTitle("Arctic Sea Ice Thickness Measurements 2025");
        version.setDescription("Measurements collected during the 2025 Arctic campaign.");
        version.setChangeNote("Initial version");
        version.setStatus(status);
        version.setLicense(license(UUID.fromString("00000000-0000-0000-0000-000000000010")));
        version.setPublishedAt(status == DatasetVersionStatus.PUBLISHED ? PUBLISHED_AT : null);
        setTimestamps(version);
        return version;
    }

    public static DatasetCreator creator(UUID id, DatasetVersion version, int position) {
        DatasetCreator creator = new DatasetCreator();
        set(creator, "id", id);
        creator.setDatasetVersion(version);
        creator.setGivenName("Anna");
        creator.setFamilyName("Muller");
        creator.setAffiliation("AWI");
        creator.setOrcid("0000-0000-0000-0000");
        creator.setPosition(position);
        setTimestamps(creator);
        return creator;
    }

    public static DatasetKeyword keyword(UUID id, DatasetVersion version, String value) {
        DatasetKeyword keyword = new DatasetKeyword();
        set(keyword, "id", id);
        keyword.setDatasetVersion(version);
        keyword.setValue(value);
        set(keyword, "createdAt", CREATED_AT);
        return keyword;
    }

    public static License license(UUID id) {
        License license = new License();
        set(license, "id", id);
        set(license, "code", "CC-BY-4.0");
        set(license, "name", "Creative Commons Attribution 4.0 International");
        set(license, "uri", "https://creativecommons.org/licenses/by/4.0/");
        set(license, "active", true);
        setTimestamps(license);
        return license;
    }

    public static DatasetFile file(UUID id, DatasetVersion version) {
        DatasetFile file = new DatasetFile();
        set(file, "id", id);
        file.setDatasetVersion(version);
        file.setOriginalFilename("sea-ice.csv");
        file.setStorageKey("datasets/%s/files/%s".formatted(version.getId(), id));
        file.setContentType("text/csv");
        file.setFileSize(1024L);
        file.setSha256("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        file.setCategory(DatasetFileCategory.RAW);
        set(file, "createdAt", CREATED_AT);
        return file;
    }

    public static void set(Object target, String fieldName, Object value) {
        ReflectionTestUtils.setField(target, fieldName, value);
    }

    private static void setTimestamps(Object target) {
        set(target, "createdAt", CREATED_AT);
        set(target, "updatedAt", UPDATED_AT);
    }
}
