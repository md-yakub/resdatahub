# ResDataHub

ResDataHub is a full-stack FAIR research data repository for publishing, managing, discovering, and semantically querying research datasets. It supports dataset versioning, metadata management, FAIR-compliant publication workflows, and Knowledge Graph exploration.

## Features

- Dataset versioning with immutable published releases
- Draft-to-publication workflow
- Organization, creator, keyword, and license management
- Secure file storage with MinIO (S3-compatible)
- Citation generation (APA, BibTeX, RIS, Plain Text)
- FAIR metadata export (RDF, Turtle, JSON-LD, RDF/XML)
- DCAT catalog generation for metadata harvesting
- Metadata validation for published datasets
- Apache Jena Knowledge Graph with SPARQL endpoint
- Public data portal and management dashboard

## Tech Stack

### Backend
- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Flyway
- Apache Jena
- Bean Validation

### Frontend
- Next.js
- React
- TypeScript
- Tailwind CSS

### Storage
- PostgreSQL
- MinIO (S3-compatible Object Storage)

## Project Structure

```text
backend/          Spring Boot backend
frontend/         Next.js frontend
infrastructure/   Infrastructure configuration
docs/             Project documentation
sample-data/      Sample datasets
```

## Architecture

```text
                Next.js Frontend
                       │
                 REST APIs
                       │
              Spring Boot Backend
         ┌──────────┴──────────┐
         │                     │
   PostgreSQL              MinIO Storage
         │
         ▼
 FAIR Metadata (DCAT / RDF)
         │
         ▼
 Apache Jena Knowledge Graph
         │
         ▼
    SPARQL Endpoint
```

## Main Capabilities

- Manage research datasets and metadata
- Version and publish datasets
- Upload and manage research files
- Generate machine-readable FAIR metadata
- Export RDF using DCAT vocabulary
- Generate academic citations
- Explore published datasets through SPARQL
- Search and browse datasets via a public portal

## Getting Started

### Infrastructure

```bash
docker compose -f infrastructure/docker-compose.yml up -d
```

### Backend

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```
## License

This project is developed as a personal portfolio project for learning and demonstrating modern Research Data Management (RDM), FAIR principles, Semantic Web technologies, and full-stack software engineering.