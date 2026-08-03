# ResDataHub

ResDataHub is a full-stack FAIR research data repository for publishing, managing, discovering, and semantically querying research datasets. It supports dataset versioning, metadata management, FAIR-compliant publication workflows, and Knowledge Graph exploration.

## Features

- Research dataset lifecycle management with draft and immutable published versions
- Dataset versioning and controlled publication workflow
- Organization, creator, keyword, license, and file management
- Secure file storage using MinIO (S3-compatible object storage)
- Full-text search and public dataset discovery
- Citation generation (APA, BibTeX, RIS, and Plain Text)
- FAIR metadata export in RDF (Turtle, JSON-LD, RDF/XML)
- DCAT catalog generation for metadata harvesting and interoperability
- Metadata validation against ResDataHub's FAIR publishing profile
- Apache Jena Knowledge Graph with a secure read-only SPARQL endpoint
- Interactive Knowledge Graph visualization with search, filtering, and relationship exploration
- Public data portal for dataset discovery and management dashboard for repository administration

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