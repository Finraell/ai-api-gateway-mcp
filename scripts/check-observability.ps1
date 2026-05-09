Write-Host "Checking Prometheus target..."
Invoke-WebRequest http://localhost:9090/api/v1/targets | Select-Object -ExpandProperty Content

Write-Host "`nChecking business metrics..."
$queries = @(
  'up',
  'sum by (decision) (risk_decisions_total)',
  'avg by (decision) (risk_decision_score)',
  'sum by (signal) (risk_decision_signals_total)',
  'sum by (tool,status) (mcp_tool_calls_total)',
  'sum by (path,result) (gateway_rate_limit_requests_total)'
)
foreach ($q in $queries) {
  Write-Host "`nPROMQL: $q"
  $encoded = [System.Uri]::EscapeDataString($q)
  Invoke-WebRequest "http://localhost:9090/api/v1/query?query=$encoded" | Select-Object -ExpandProperty Content
}
