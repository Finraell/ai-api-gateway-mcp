# Architecture

## Goal

This service demonstrates how a financial-platform backend can expose deterministic risk decisions while also making those decisions available to AI agents through a Spring AI MCP server plus compatibility tool registry.

## Main components

- `RiskDecisionController`: direct REST API for transaction scoring and decision lookup.
- `RiskScoringEngine`: pure deterministic rules engine. Easy to unit test and replace with ML later.
- `RiskDecisionService`: orchestration layer for persistence, metrics, and event publishing.
- `ToolRegistry`: internal registry of auditable tools.
- `AgentGatewayService`: lightweight facade that chooses tools from prompt/context.
- `RiskEventPublisher`: Kafka publishing hook for downstream fraud/compliance workflows.

## Design principle

The AI layer never bypasses the backend platform. It calls the same audited tools as any other client.

This matters because production AI systems need:

- traceability,
- validation,
- permission checks,
- replayability,
- explainability,
- operational metrics.

## Docker Compose runtime

The full demo can run through Docker Compose with four default services:

- `frontend`: React static assets served by nginx on `localhost:3000`
- `backend`: Spring Boot API on `localhost:8080`
- `postgres`: durable local database for decisions
- `prometheus`: metrics scraper for `/actuator/prometheus`; the endpoint is unauthenticated in the local Docker demo so Prometheus/Grafana can scrape it, while business APIs remain protected by `X-API-Key`.

The frontend container does not bake the API key into browser JavaScript. The browser calls same-origin `/api`, nginx forwards the request to `backend:8080`, and nginx injects the `X-API-Key` header from the runtime `GATEWAY_API_KEY` environment variable.

Kafka is intentionally behind the optional Compose profile `events`; the default one-command demo stays lightweight and logs `risk_event_publish_skipped reason=events_disabled`.


## Production-style controls

See [`production-style-controls.md`](production-style-controls.md) for the security, observability, database, CI, and Docker controls included in the project.


## Optional Grafana dashboard

Grafana is provided as an optional Docker Compose profile. It reads from Prometheus using provisioned files in `ops/grafana` and displays business metrics such as risk decisions by outcome, top risk signals, MCP tool calls, HTTP traffic, and rate-limit decisions. It is optional so the default demo stays lightweight and runnable without dashboard credentials.
