#!/usr/bin/env bash
set -euo pipefail

API_KEY="${API_KEY:-local-dev-key}"
BASE_URL="${BASE_URL:-http://localhost:8080}"

curl -s "${BASE_URL}/actuator/health" | jq .

curl -s -X POST "${BASE_URL}/api/v1/risk/score" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: ${API_KEY}" \
  -H "Idempotency-Key: smoke-test-001" \
  -d '{
    "customerId":"cust-1001",
    "transactionId":"tx-smoke-001",
    "transactionAmount":12500,
    "currency":"USD",
    "countryCode":"US",
    "merchantCategory":"WIRE_TRANSFER",
    "accountAgeDays":14,
    "failedLoginCount":5,
    "newDevice":true,
    "deviceTrustScore":35,
    "velocity30m":6,
    "previousChargebacks":1,
    "ipRiskScore":62
  }' | jq .
