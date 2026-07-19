.PHONY: help env-check secret mvn-build \
	dev-build dev-up dev-down dev-restart dev-logs dev-ps dev-swagger \
	prod-build prod-up prod-down prod-restart prod-logs prod-ps prod-deploy prod-swagger

COMPOSE_DEV  = docker compose -f docker-compose.yml
COMPOSE_PROD = docker compose -f docker-compose.prod.yml --env-file .env

help:
	@echo "Dev targets (uses your local Postgres/Redis via host.docker.internal):"
	@echo "  make dev-build     - build images without starting anything"
	@echo "  make dev-up        - start the dev stack in the background"
	@echo "  make dev-down      - stop and remove the dev stack"
	@echo "  make dev-restart   - dev-down + dev-up"
	@echo "  make dev-logs      - tail logs for all dev services"
	@echo "  make dev-ps        - show dev container status"
	@echo "  make dev-swagger   - print the Swagger UI URL for the dev stack"
	@echo ""
	@echo "  make mvn-build     - compile loan-service and payment-service with Maven (no Docker)"
	@echo ""
	@echo "Prod targets (containerized Postgres/Redis + Caddy, needs .env):"
	@echo "  make secret        - print a freshly generated random secret (for .env)"
	@echo "  make prod-build    - build images without starting anything"
	@echo "  make prod-up       - start the prod stack in the background"
	@echo "  make prod-down     - stop and remove the prod stack (volumes are kept)"
	@echo "  make prod-restart  - prod-down + prod-up"
	@echo "  make prod-logs     - tail logs for all prod services"
	@echo "  make prod-ps       - show prod container status"
	@echo "  make prod-deploy   - git pull, build images, and (re)start the prod stack"
	@echo "  make prod-swagger  - print the aggregated Swagger UI URL for the prod stack"

env-check:
	@test -f .env || { \
		echo "Missing .env — copy .env.example to .env and fill in real values first."; \
		exit 1; \
	}

secret:
	@openssl rand -base64 48

# ── Dev ──────────────────────────────────────────────────────────────────────

dev-build:
	$(COMPOSE_DEV) build

dev-up:
	$(COMPOSE_DEV) up -d

dev-down:
	$(COMPOSE_DEV) down

dev-restart: dev-down dev-up

dev-logs:
	$(COMPOSE_DEV) logs -f --tail=200

dev-ps:
	$(COMPOSE_DEV) ps

dev-swagger:
	@echo "http://localhost:8090/swagger-ui.html"

mvn-build:
	./mvnw -pl loan-service,payment-service -am clean install

# ── Prod ─────────────────────────────────────────────────────────────────────

prod-build: env-check
	$(COMPOSE_PROD) build

prod-up: env-check
	$(COMPOSE_PROD) up -d

prod-down: env-check
	$(COMPOSE_PROD) down

prod-restart: prod-down prod-up

prod-logs: env-check
	$(COMPOSE_PROD) logs -f --tail=200

prod-ps: env-check
	$(COMPOSE_PROD) ps

prod-deploy: env-check
	git pull --ff-only
	$(COMPOSE_PROD) build
	$(COMPOSE_PROD) up -d

# Gateway aggregates /v3/api-docs from every service into one Swagger UI page.
# DOMAIN=:80 during local testing means "no real domain", so print plain http://localhost.
prod-swagger: env-check
	@domain=$$(grep -E '^DOMAIN=' .env | cut -d= -f2-); \
	if [ "$$domain" = ":80" ] || [ -z "$$domain" ]; then \
		echo "http://localhost/swagger-ui.html"; \
	else \
		echo "https://$$domain/swagger-ui.html"; \
	fi
