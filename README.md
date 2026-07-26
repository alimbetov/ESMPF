# ESMPF

Equipment Service Management Platform Foundation 1.0.

## Foundation baseline

- Java 21
- Spring Boot 3.5
- Spring Data JPA
- H2 development profile
- PostgreSQL-ready dependency
- Liquibase-ready configuration
- 48 persistent entities grouped into 10 modules
- no controllers, repositories or application services yet

The baseline intentionally stores cross-module references as UUID values instead of JPA object graphs. This keeps aggregate boundaries explicit and prevents accidental cascading across modules.
