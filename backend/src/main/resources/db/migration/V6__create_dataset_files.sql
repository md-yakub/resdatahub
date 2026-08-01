CREATE TABLE dataset_files (
    id UUID PRIMARY KEY,
    dataset_version_id UUID NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    storage_key VARCHAR(1024) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    sha256 CHAR(64) NOT NULL,
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_dataset_files_dataset_version
        FOREIGN KEY (dataset_version_id)
        REFERENCES dataset_versions (id)
        ON DELETE CASCADE,
    CONSTRAINT ck_dataset_files_file_size_positive
        CHECK (file_size > 0)
);

CREATE UNIQUE INDEX uk_dataset_files_storage_key
    ON dataset_files (storage_key);

CREATE INDEX idx_dataset_files_dataset_version_id
    ON dataset_files (dataset_version_id);

CREATE INDEX idx_dataset_files_sha256
    ON dataset_files (sha256);
