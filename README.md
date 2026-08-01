# ResDataHub

ResDataHub is a FAIR research data repository for research institutes.

## Monorepo Structure

- `backend/` - Spring Boot backend application
- `frontend/` - Reserved for the future frontend
- `docs/` - Project documentation
- `sample-data/` - Sample data for development and demos
- `infrastructure/` - Docker Compose and infrastructure files

## Backend Stack

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Flyway
- Bean Validation
- Actuator
- PostgreSQL JDBC driver
- PostgreSQL 16 through Docker

## Start PostgreSQL

From the repository root:

```powershell
docker compose -f infrastructure/docker-compose.yml up -d
```

PostgreSQL is exposed on host port `5434`.

Database settings:

- Database: `resdatahub`
- Username: `resdatahub`
- Password: `resdatahub`

## Start the Backend

From the backend directory:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

## Health Endpoints

- `GET /api/health`
- `GET /actuator/health`
