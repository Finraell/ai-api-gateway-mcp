$ApiKey = if ($env:GATEWAY_API_KEY) { $env:GATEWAY_API_KEY } else { "local-dev-key" }
$Body = @{
  jsonrpc = "2.0"
  id = "tools-list-1"
  method = "tools/list"
  params = @{}
} | ConvertTo-Json -Depth 10

Invoke-RestMethod `
  -Uri "http://localhost:8080/mcp" `
  -Method Post `
  -Headers @{ "X-API-Key" = $ApiKey; "Content-Type" = "application/json" } `
  -Body $Body
