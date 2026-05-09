param(
    [string]$GrafanaUrl = "http://localhost:3001"
)

Write-Host "Checking Grafana health at $GrafanaUrl/api/health"
try {
    $response = Invoke-RestMethod -Uri "$GrafanaUrl/api/health" -Method Get -TimeoutSec 5
    $response | ConvertTo-Json -Depth 5
    Write-Host "Grafana responded successfully."
} catch {
    Write-Error "Grafana check failed. Start it with: docker compose --profile dashboards up --build"
    throw
}
