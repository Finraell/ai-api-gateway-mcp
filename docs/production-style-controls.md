# Production-style controls

This repo is intentionally production-style rather than production-certified. The implementation demonstrates the controls that a real backend/platform engineer would discuss in a design review.

## Implemented in this repo

| Area | Implementation |
|---|---|
| Management isolation | Application traffic runs on `8080`; actuator/Prometheus runs on `8081` in Docker. The host binding uses `127.0.0.1:8081:8081`, while Prometheus scrapes `backend:8081` on the private Compose network. |
| Authentication | Docker demo uses server-side nginx API-key injection. Optional Bearer JWT HMAC verification is included to demonstrate a JWT path without adding an external identity provider. |
| Rate limiting | In-memory per-client/path rate limiting with `X-RateLimit-*` response headers and Prometheus counters. Replace with Redis/gateway/service-mesh limits for real production. |
| Correlation IDs | `X-Correlation-ID` is accepted or generated, returned in responses, and stored in MDC for logs and audit events. |
| Audit persistence | `audit_events` stores event type, aggregate id, correlation id, principal, JSON payload, and timestamp. |
| Database migrations | Flyway creates and versions the schema. Docker profile uses `ddl-auto=validate`, so Hibernate validates instead of mutating the schema. |
| Structured logging | Application event messages are serialized as JSON payloads. |
| Observability | Micrometer provides JVM/HTTP metrics plus custom risk and MCP metrics. Prometheus scrapes them from the management port. |
| Grafana dashboard | Optional Docker Compose profile provisions a Prometheus datasource and an AI API Gateway dashboard for local demos and README screenshots. |
| Container hardening | Backend and frontend run as non-root users; secrets are not passed as build args; API key is injected at runtime by nginx. |
| CI security | GitHub Actions runs tests, Docker builds, and Trivy scans. |
| Postgres integration | A Testcontainers integration test validates the risk-decision flow against real PostgreSQL. |
| MCP compatibility | PowerShell smoke script exercises the stateless `/mcp` endpoint with JSON-RPC calls. |

## Still required for true production

- OAuth2/OIDC resource-server validation against a real issuer
- managed secrets from Vault, AWS Secrets Manager, GCP Secret Manager, Azure Key Vault, etc.
- Redis/gateway-backed rate limiting
- TLS termination and certificate lifecycle management
- OpenTelemetry traces and distributed trace propagation
- centralized JSON log aggregation
- vulnerability baseline policy that can fail builds
- load testing and SLO dashboards
- threat model and compliance review
- Kubernetes/network policies for management and data-plane separation


## Optional Grafana profile

Grafana is included as an optional Compose profile because it improves reviewer screenshots without making the base demo heavier. Run it with:

```bash
docker compose --profile dashboards up --build
```

Grafana is reachable at `http://localhost:3001` and is provisioned from files in `ops/grafana`. The datasource points to Prometheus on the private Compose network (`http://prometheus:9090`).

For real production, Grafana access should be protected with SSO/OIDC, network restrictions, and managed secrets. The local profile uses `.env` only to keep the demo runnable.

## Secrets and TLS approach

The repository intentionally uses `.env` for local Docker Compose because requiring Vault, AWS Secrets Manager, Azure Key Vault, GCP Secret Manager, or TLS certificates would make the project difficult for recruiters to run.

Production deployment should replace `.env` with a managed secret source and terminate TLS before traffic reaches the application container, for example at an ingress controller, API gateway, load balancer, nginx/Envoy reverse proxy, or service mesh.
