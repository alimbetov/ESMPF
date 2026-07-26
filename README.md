# ESMPF

Equipment Service Management Platform Foundation 1.0.

## Current baseline

- Java 21 and Spring Boot 3.5
- Spring Modulith application boundaries
- Spring Data JPA and MapStruct application services
- 48 persistent entities grouped into 10 modules
- tenant-aware repositories and optimistic locking
- explicit business lifecycle transitions
- PostgreSQL Liquibase formatted SQL baseline
- composite tenant foreign keys, unique/check constraints and query indexes
- PostgreSQL JSONB mappings
- atomic document sequence, public-token consumption and queue claiming
- H2 service-layer tests
- PostgreSQL 16 Testcontainers migration, validation and concurrency proofs

Controllers, security adapters and external workers remain separate implementation stages.

Cross-module references are stored as UUID values instead of JPA object graphs. Database-level composite foreign keys preserve tenant integrity without introducing accidental JPA cascading across module boundaries.
