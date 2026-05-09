# Engineering Tradeoffs

## Deterministic rules first

This project uses deterministic rules rather than a black-box ML model. That is intentional: fraud, credit, compliance, and payments systems often need explainability before model sophistication.

## H2 locally, PostgreSQL in Docker

H2 makes the repo easy to run in 60 seconds. PostgreSQL is included for production-like local integration.

## In-memory idempotency

The idempotency implementation demonstrates the API contract but is not horizontally scalable. A production deployment should use Redis or a database table with TTL/expiration.

## Spring AI MCP server and compatibility REST tools

The repo includes Spring AI MCP server endpoints plus compatibility tool discovery and execution endpoints. The next step is to add a separate MCP client demo that connects to the server over SSE/Streamable HTTP.

## API-key security

API-key auth is intentionally simple for a portfolio project. Production systems should use OAuth2/JWT, mTLS, or service-mesh identity depending on environment.
