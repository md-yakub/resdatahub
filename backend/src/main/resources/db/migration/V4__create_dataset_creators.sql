CREATE TABLE dataset_creators (
    id UUID PRIMARY KEY,
    dataset_version_id UUID NOT NULL,
    given_name VARCHAR(255) NOT NULL,
    family_name VARCHAR(255) NOT NULL,
    affiliation VARCHAR(255),
    orcid VARCHAR(19),
    position INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_dataset_creators_dataset_version
        FOREIGN KEY (dataset_version_id)
        REFERENCES dataset_versions (id)
        ON DELETE CASCADE,
    CONSTRAINT ck_dataset_creators_position_positive
        CHECK (position > 0),
    CONSTRAINT ck_dataset_creators_orcid_format
        CHECK (orcid IS NULL OR orcid ~ '^[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}$')
);

CREATE INDEX idx_dataset_creators_dataset_version_id
    ON dataset_creators (dataset_version_id);

CREATE UNIQUE INDEX uk_dataset_creators_version_position
    ON dataset_creators (dataset_version_id, position);
