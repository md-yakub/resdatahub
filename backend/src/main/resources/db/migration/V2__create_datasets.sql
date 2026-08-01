CREATE TABLE datasets (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    organization_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_datasets_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations (id)
);

CREATE INDEX idx_datasets_organization_id ON datasets (organization_id);
