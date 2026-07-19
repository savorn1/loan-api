# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Loan Management System: a Spring Boot 3.5.14 / Java 21 multi-module Maven monorepo implementing six microservices on Spring Cloud 2025.0.0 (Eureka discovery + Spring Cloud Gateway + OpenFeign).

## Build & run commands

- Build everything: `./mvnw clean install`
- Build one module (with its dependencies): `./mvnw -pl loan-service -am clean install`
- Run all tests: `./mvnw test`
- Run tests for one module: `./mvnw -pl auth-service test`
- Run a single test class: `./mvnw -pl auth-service test -Dtest=AuthApplicationTests`
- Run a service locally: `./mvnw -pl <module> spring-boot:run` — start `discovery-server` first, then the others (each registers with Eureka on boot; `api-gateway` also expects `discovery-server` to be healthy)
- Full stack via Docker: `docker compose up --build` — builds each module's `Dockerfile`. Containers connect to your **local** Postgres and Redis via `host.docker.internal`, not containerized DBs. `docker/init-db.sql` only creates the four empty databases (`auth_db`, `customer_db`, `loans_db`, `payment_db`); tables are created at runtime by Hibernate (`spring.jpa.hibernate.ddl-auto=update`). Note: `loans_db` (plural) is deliberate — your local Postgres already had an unrelated `loan_db` (singular) owned by a different project.

There is no lint command configured beyond the Maven build/compile step.

## Architecture

### Module layout

| Module | Port | Package | Datastore |
|---|---|---|---|
| discovery-server | 8761 | `com.example.discovery` | — (Eureka server) |
| api-gateway | 8080 | `com.example.gateway` | — (reactive/WebFlux) |
| auth-service | 8081 | `com.example.auth` | `auth_db` |
| customer-service | 8082 | `com.example.customer` | `customer_db` + Redis |
| loan-service | 8083 | `com.example.loan` | `loans_db` |
| payment-service | 8084 | `com.example.payment` | `payment_db` |

Shared dependency versions/BOMs (jjwt 0.12.6, springdoc 2.6.0, spring-cloud-dependencies 2025.0.0) are managed centrally in the root `pom.xml`; each module has its own `pom.xml` with a `parent` pointing at the root `loan-system` aggregator (packaging `pom`).

### Request flow & auth model

All external traffic enters through `api-gateway`, which routes by path prefix to the backend service (`/api/auth/**`, `/api/customers/**`, `/api/loans/**`, `/api/payments/**`) via Eureka service discovery (`lb://<service-name>`). `AuthenticationFilter` (a `GlobalFilter` in api-gateway) is the **only** place that verifies JWTs: it parses the `Authorization: Bearer` header with jjwt, rejects with 401 if missing/invalid, and on success injects `X-User-Name` / `X-User-Role` headers before forwarding downstream. `/api/auth/login`, `/api/auth/register`, and Swagger/docs paths bypass this filter.

Downstream services (customer, loan, payment) do **not** re-validate JWTs — their `SecurityConfig` (where present) is `anyRequest().permitAll()`, trusting that only the gateway can reach them. `payment-service` doesn't even depend on `spring-boot-starter-security`. This is a "trust the perimeter" model, not defense-in-depth — keep that in mind when adding new endpoints or reasoning about auth bugs (a bug in the gateway filter affects every service at once, and none of the backend services independently protect themselves).

`auth-service` owns the shared JWT secret (`jwt.secret`) and issues/validates tokens (`JwtService`); the identical secret is duplicated in `api-gateway`'s `application.yml`. There's no central secret management — if you rotate the secret, update both files.

### Service-to-service calls (Feign)

- `loan-service` → `customer-service`: `CustomerClient` (`GET /api/customers/{id}`)
- `payment-service` → `loan-service`: `LoanClient` (`GET /api/loans/{id}`)

Feign clients call by Eureka service name (`@FeignClient(name = "customer-service")`), not hardcoded host:port. `customer-service` declares `@EnableFeignClients` but currently has no outgoing Feign clients of its own.

### Per-service structure

Each business service (`auth`, `customer`, `loan`, `payment`) follows the same layering: `controller` → `service` (interface) / `service.impl` (implementation) → `repository` (Spring Data JPA) → `entity`, plus `dto`, `exception` (`AppException` + `GlobalExceptionHandler`), and `config` (`SecurityConfig`, `OpenApiConfig`). `customer-service` additionally has an `aop` package (`RateLimitAspect`, `LoggingAspect`) backing a custom `@RateLimit` annotation, backed by Redis (`INCR`+`EXPIRE`), throwing `TooManyRequestsException` on limit breach.

### Orphaned code

The `src/` directory at the repository root (outside all module directories) is pre-microservices leftover code — it predates the split into services (see git history: `first commit` → `change to microservice`). It is not listed in the root `pom.xml`'s `<modules>` and is not built. Its package is `com.example.loan` but the actual content is customer-domain code (the ancestor of `customer-service`). Safe to ignore or delete; don't confuse it with the real `loan-service` module.
