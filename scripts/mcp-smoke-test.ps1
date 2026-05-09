param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$ApiKey = "local-dev-key"
)

$headers = @{ "Content-Type" = "application/json"; "X-API-Key" = $ApiKey }

$initialize = @{
  jsonrpc = "2.0"
  id = 1
  method = "initialize"
  params = @{
    protocolVersion = "2024-11-05"
    capabilities = @{}
    clientInfo = @{ name = "local-smoke-test"; version = "1.0.0" }
  }
} | ConvertTo-Json -Depth 8

Write-Host "Initializing MCP session..."
Invoke-WebRequest "$BaseUrl/mcp" -Method POST -Headers $headers -Body $initialize | Select-Object -ExpandProperty Content

$toolsList = @{ jsonrpc = "2.0"; id = 2; method = "tools/list"; params = @{} } | ConvertTo-Json -Depth 8
Write-Host "`nListing MCP tools..."
Invoke-WebRequest "$BaseUrl/mcp" -Method POST -Headers $headers -Body $toolsList | Select-Object -ExpandProperty Content
