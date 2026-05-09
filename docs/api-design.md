# API Design

## Risk scoring endpoint

`POST /api/v1/risk/score`

Accepts transaction/customer/device signals and returns:

- decision id,
- score from 0 to 100,
- `APPROVE`, `REVIEW`, or `DECLINE`,
- explainable signals,
- recommendation,
- model/rules version.

## Idempotency

Clients may pass `Idempotency-Key` to safely retry requests.

In this demo, idempotency is in-memory for simplicity. In production, this should be Redis or a database-backed idempotency table.

## Tool execution

`POST /api/v1/tools/execute`

The tool API intentionally resembles Spring AI-style tool calling and compatibility REST execution:

```json
{
  "toolName": "risk.explain.policy",
  "arguments": {
    "topic": "thresholds"
  }
}
```

This keeps the service ready for migration to full Spring AI MCP server support.
