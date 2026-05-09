Write-Host "Checking backend Prometheus endpoint..."
Invoke-WebRequest http://localhost:8080/actuator/prometheus | Select-Object -ExpandProperty StatusCode

Write-Host "Checking Prometheus targets UI..."
Write-Host "Open: http://localhost:9090/targets"

Write-Host "Checking Prometheus API target status..."
Invoke-RestMethod "http://localhost:9090/api/v1/targets" | ConvertTo-Json -Depth 8
