CREATE TABLE licenses (
    id UUID PRIMARY KEY,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    uri VARCHAR(2048) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uk_licenses_code ON licenses (code);

INSERT INTO licenses (id, code, name, uri, active, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'CC-BY-4.0', 'Creative Commons Attribution 4.0 International', 'https://creativecommons.org/licenses/by/4.0/', true, NOW(), NOW()),
    ('22222222-2222-2222-2222-222222222222', 'CC0-1.0', 'Creative Commons Zero 1.0 Universal', 'https://creativecommons.org/publicdomain/zero/1.0/', true, NOW(), NOW()),
    ('33333333-3333-3333-3333-333333333333', 'CC-BY-SA-4.0', 'Creative Commons Attribution-ShareAlike 4.0 International', 'https://creativecommons.org/licenses/by-sa/4.0/', true, NOW(), NOW()),
    ('44444444-4444-4444-4444-444444444444', 'OTHER', 'Other license', 'https://example.org/licenses/other', true, NOW(), NOW());

ALTER TABLE dataset_versions
    ADD COLUMN license_id UUID,
    ADD CONSTRAINT fk_dataset_versions_license
        FOREIGN KEY (license_id)
        REFERENCES licenses (id);

CREATE INDEX idx_dataset_versions_license_id ON dataset_versions (license_id);

CREATE TABLE dataset_keywords (
    id UUID PRIMARY KEY,
    dataset_version_id UUID NOT NULL,
    value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_dataset_keywords_dataset_version
        FOREIGN KEY (dataset_version_id)
        REFERENCES dataset_versions (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_dataset_keywords_dataset_version_id
    ON dataset_keywords (dataset_version_id);

CREATE UNIQUE INDEX uk_dataset_keywords_version_value
    ON dataset_keywords (dataset_version_id, LOWER(value));
