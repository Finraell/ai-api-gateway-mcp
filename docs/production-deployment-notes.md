# Production deployment notes

This project is designed to be production-style and locally runnable. The local Docker Compose setup uses `.env` files and HTTP ports so reviewers can run the system quickly.

## Secrets

Local demo:

- `.env` provides `GATEWAY_API_KEY`, `POSTGRES_PASSWORD`, optional JWT HMAC secret, and optional Grafana admin password.
- `.env` is ignored by Git; only `.env.example` should be committed.

Production direction:

- Use Vault, AWS Secrets Manager, Azure Key Vault, GCP Secret Manager, Kubernetes Secrets, or a platform-managed equivalent.
- Inject secrets at runtime, not during Docker image build.
- Rotate secrets and define ownership/retention policies.

## TLS

Local demo:

- HTTP is used for `localhost` convenience.

Production direction:

- Terminate TLS at an ingress controller, API gateway, load balancer, nginx/Envoy reverse proxy, or service mesh.
- Keep management endpoints private.
- Use mTLS/service identity where platform requirements justify it.

## Identity and authorization

Local demo:

- Server-side nginx API-key injection keeps the browser bundle from exposing the secret.
- Optional HMAC JWT verifier demonstrates a bearer-token path without requiring an external IdP.

Production direction:

- Replace demo HMAC JWT verification with OAuth2/OIDC issuer validation.
- Use scoped claims/roles for admin, analyst, and service-to-service access.

## Observability

Local demo:

- Micrometer exports JVM/HTTP/business metrics.
- Prometheus scrapes the backend management port on the Compose network.
- Optional Grafana provisions a starter dashboard.

Production direction:

- Add OpenTelemetry traces.
- Forward structured JSON logs to a central log system.
- Define SLOs and alerting rules.
