# Grafana provisioning

This folder provisions Grafana for the optional Docker Compose dashboard profile.

- `provisioning/datasources/prometheus.yml` creates a Prometheus datasource with UID `prometheus`.
- `provisioning/dashboards/dashboards.yml` loads dashboard JSON files from `/var/lib/grafana/dashboards`.
- `dashboards/ai-api-gateway-overview.json` contains the starter recruiter/demo dashboard.

Run it with:

```bash
docker compose --profile dashboards up --build
```

Then open `http://localhost:3001` and log in with `GRAFANA_ADMIN_USER` / `GRAFANA_ADMIN_PASSWORD` from `.env`.
