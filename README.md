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

## Local Development

Docker Compose uses `infrastructure/.env` for PostgreSQL container settings. Spring Boot does not read that file.

Copy the local Spring Boot configuration example:

```powershell
Copy-Item backend\src\main\resources\application-local.properties.example backend\src\main\resources\application-local.properties
```

Replace the password in `backend/src/main/resources/application-local.properties`.

Start the application with the local profile:

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

## Start the Backend

For local development, start the backend with the `local` profile:

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

## Health Endpoints

- `GET /api/health`
- `GET /actuator/health`
