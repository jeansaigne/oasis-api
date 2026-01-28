# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

```bash
# Start PostgreSQL database (required for local development)
docker-compose up -d

# Run the application
./mvnw spring-boot:run

# Run all tests (uses H2 in-memory database)
./mvnw test

# Run a specific test class
./mvnw test -Dtest=GameControllerTest

# Run a specific test method
./mvnw test -Dtest=GameControllerTest#'should create a new game'

# Build the project
./mvnw clean package

# Build Docker image
docker build -t oasis-api .
```

## Architecture

This is a Spring Boot 4.0 REST API written in Kotlin for managing board games. It follows a standard layered architecture:

- **Controller** (`controller/`): REST endpoints with OpenAPI/Swagger annotations. All game endpoints are under `/api/games`.
- **Service** (`service/`): Business logic with `@Transactional` support.
- **Repository** (`repository/`): Spring Data JPA repositories extending `JpaRepository`.
- **Entity** (`entity/`): JPA entities mapped to PostgreSQL tables.
- **DTO** (`dto/`): Request/Response objects for API serialization. `GameResponse` has a companion `from()` factory method for entity conversion.
- **Exception** (`exception/`): Custom exceptions with `GlobalExceptionHandler` for centralized error handling.
- **Config** (`config/`): CORS and OpenAPI configuration.

## Key Technical Details

- **Java 24** and **Kotlin 2.2** with strict JSR-305 null-safety
- **Kotlin JPA plugin** configured with `all-open` for Entity/MappedSuperclass/Embeddable annotations
- **Tests** use `@ActiveProfiles("test")` which loads `application-test.yml` with H2 database
- **Jackson** uses `tools.jackson.module:jackson-module-kotlin` (not the older `com.fasterxml.jackson` package)
- **Swagger UI** available at `/swagger-ui.html` via springdoc-openapi

## Environment Variables

The application uses spring-dotenv for local `.env` file support. Required variables:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (defaults provided for local dev)
- `PORT` (defaults to 8080)