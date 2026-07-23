# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Loan Management System: a Spring Boot 3.5.14 / Java 21 multi-module Maven monorepo implementing eight microservices on Spring Cloud 2025.0.0 (Eureka discovery + Spring Cloud Gateway + OpenFeign).

## Build & run commands

- Build everything: `./mvnw clean install`
- Build one module (with its dependencies): `./mvnw -pl loan-service -am clean install`
- Run all tests: `./mvnw test`
- Run tests for one module: `./mvnw -pl auth-service test`
- Run a single test class: `./mvnw -pl auth-service test -Dtest=AuthApplicationTests`
- Run a service locally: `./mvnw -pl <module> spring-boot:run` — start `discovery-server` first, then the others (each registers with Eureka on boot; `api-gateway` also expects `discovery-server` to be healthy)
- Full stack via Docker: `docker compose up --build` — builds each module's `Dockerfile`. Containers connect to your **local** Postgres and Redis via `host.docker.internal`, not containerized DBs. `docker/init-db.sql` only creates the six empty databases (`auth_db`, `customer_db`, `loans_db`, `payment_db`, `loan_product_db`, `accounting_db`); tables are created at runtime by Hibernate (`spring.jpa.hibernate.ddl-auto=update`). Note: `loans_db` (plural) is deliberate — your local Postgres already had an unrelated `loan_db` (singular) owned by a different project.

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
| loan-product-service | 8085 | `com.example.loanproduct` | `loan_product_db` |
| accounting-service | 8086 | `com.example.accounting` | `accounting_db` |

Shared dependency versions/BOMs (jjwt 0.12.6, springdoc 2.6.0, spring-cloud-dependencies 2025.0.0) are managed centrally in the root `pom.xml`; each module has its own `pom.xml` with a `parent` pointing at the root `loan-system` aggregator (packaging `pom`).

### Request flow & auth model

All external traffic enters through `api-gateway`, which routes by path prefix to the backend service (`/api/auth/**`, `/api/customers/**`, `/api/loans/**`, `/api/payments/**`, `/api/loan-products/**`, `/api/gl-accounts/**`, `/api/journal-templates/**`, `/api/accounting-schemes/**`, `/api/financial-periods/**`, `/api/journal-entries/**`, `/api/trial-balance/**`) via Eureka service discovery (`lb://<service-name>`). `AuthenticationFilter` (a `GlobalFilter` in api-gateway) is the **only** place that verifies JWTs: it parses the `Authorization: Bearer` header with jjwt, rejects with 401 if missing/invalid, and on success injects `X-User-Name` / `X-User-Role` headers before forwarding downstream. `/api/auth/login`, `/api/auth/register`, and Swagger/docs paths bypass this filter.

Downstream services (customer, loan, payment, loan-product, accounting) also independently verify the JWT's signature and expiry — each has its own `JwtAuthenticationFilter` (parses `Authorization: Bearer` with jjwt using the shared secret, sets the `SecurityContext` from the token's claims if valid, otherwise proceeds unauthenticated) wired into `SecurityConfig`. Their `SecurityConfig` URL matcher is still `anyRequest().permitAll()` — authorization is enforced at the method level via `@PreAuthorize` on specific endpoints, not at the URL-matcher level — but a request that reaches a backend service directly (same Docker network, misconfigured port exposure), bypassing the gateway entirely, can no longer forge `X-User-Role: ADMIN` by just setting a header; it needs a validly-signed token. This is defense-in-depth on top of the gateway's own check, not a replacement for it — the gateway still rejects missing/invalid tokens before they reach any backend service at all.

`auth-service` owns the shared JWT secret (`jwt.secret`) and issues/validates tokens (`JwtService`); the identical secret is now duplicated in seven places: `api-gateway`, `customer-service`, `loan-service`, `payment-service`, `loan-product-service`, and `accounting-service`'s config, all copied from `auth-service`. There's no central secret management — if you rotate the secret, update all seven files.

### Service-to-service calls (Feign)

- `loan-service` → `customer-service`: `CustomerClient` (`GET /api/customers/{id}`)
- `payment-service` → `loan-service`: `LoanClient` (`GET /api/loans/{id}`)

Feign clients call by Eureka service name (`@FeignClient(name = "customer-service")`), not hardcoded host:port. `customer-service` declares `@EnableFeignClients` but currently has no outgoing Feign clients of its own.

### Per-service structure

Each business service (`auth`, `customer`, `loan`, `payment`, `loan-product`, `accounting`) follows the same layering: `controller` → `service` (interface) / `service.impl` (implementation) → `repository` (Spring Data JPA) → `entity`, plus `dto`, `exception` (`AppException` + `GlobalExceptionHandler`), and `config` (`SecurityConfig`, `OpenApiConfig`). `customer-service` additionally has an `aop` package (`RateLimitAspect`, `LoggingAspect`) backing a custom `@RateLimit` annotation, backed by Redis (`INCR`+`EXPIRE`), throwing `TooManyRequestsException` on limit breach.

`loan-product-service` owns `loan_products` (product catalog: amount/term ranges, default interest rate, repayment method/frequency, grace period) plus five child tables scoped to a product — `loan_product_interest_rates`, `loan_product_fees`, `loan_product_terms`, `loan_product_rules` (structured eligibility conditions: field/operator/value), `loan_product_documents` (a required-document checklist, no file storage). Unlike every other entity in this monorepo, `LoanProduct.id` is a `UUID` (`GenerationType.UUID`), not a `Long` identity — that was an explicit schema choice for this table only; its child tables still use `Long` identity ids and carry a `loan_product_id` FK. It has no Feign clients and nothing else calls it yet.

`accounting-service` implements the double-entry books: `gl_accounts` (chart of accounts, self-referencing `parent_id` hierarchy), `journal_templates` + `journal_template_lines` (per-`TransactionType` blueprints of symbolic debit/credit "account roles"), `accounting_schemes` (binds a template's role + currency to a real `gl_account`), `financial_periods` (OPEN/CLOSED date ranges that every posting is scoped to), and `journal_entries` + `journal_entry_lines` (the actual DRAFT → POSTED → REVERSED double-entry postings, validated balanced on both create and post). `TransactionType` (DISBURSEMENT, PRINCIPAL_PAYMENT, INTEREST_PAYMENT, etc.) is the only vocabulary this service shares with loan-service/payment-service — it has no notion of a loan product and no Feign clients; upstream services are expected to call `POST /api/journal-entries` with an opaque `referenceType`/`referenceId` pointing back at their own domain object. General ledger (`GET /api/gl-accounts/{id}/ledger`) and trial balance (`GET /api/trial-balance`) are read-only reports computed from `journal_entry_lines`, not separately stored.

### Orphaned code

The `src/` directory at the repository root (outside all module directories) is pre-microservices leftover code — it predates the split into services (see git history: `first commit` → `change to microservice`). It is not listed in the root `pom.xml`'s `<modules>` and is not built. Its package is `com.example.loan` but the actual content is customer-domain code (the ancestor of `customer-service`). Safe to ignore or delete; don't confuse it with the real `loan-service` module.
