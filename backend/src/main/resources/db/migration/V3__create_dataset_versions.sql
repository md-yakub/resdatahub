CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE dataset_versions (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    version_number VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    change_note TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_dataset_versions_dataset
        FOREIGN KEY (dataset_id)
        REFERENCES datasets (id)
        ON DELETE CASCADE
);

INSERT INTO dataset_versions (
    id,
    dataset_id,
    version_number,
    title,
    description,
    change_note,
    status,
    created_at,
    updated_at,
    published_at
)
SELECT
    gen_random_uuid(),
    id,
    '1.0',
    title,
    COALESCE(description, ''),
    'Initial version',
    CASE status
        WHEN 'PUBLISHED' THEN 'PUBLISHED'
        WHEN 'ARCHIVED' THEN 'WITHDRAWN'
        ELSE 'DRAFT'
    END,
    created_at,
    updated_at,
    CASE
        WHEN status = 'PUBLISHED' THEN updated_at
        ELSE NULL
    END
FROM datasets;

ALTER TABLE datasets
    DROP COLUMN title,
    DROP COLUMN description,
    DROP COLUMN status;

CREATE INDEX idx_dataset_versions_dataset_id ON dataset_versions (dataset_id);

CREATE UNIQUE INDEX uk_dataset_versions_dataset_version_number
    ON dataset_versions (dataset_id, LOWER(version_number));
