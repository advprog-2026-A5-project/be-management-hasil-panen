# Hasil Panen Monitoring Deployment Notes

This monitoring stack is intended for deployment use, not localhost-only scraping.

## 1) Configure Prometheus target

Edit `monitoring/prometheus.yml` and replace:

- `<HASIL_PANEN_HOST>:8082`

with your deployed Hasil Panen host (EC2 public DNS or IP), for example:

- `ec2-12-34-56-78.ap-southeast-1.compute.amazonaws.com:8082`

## 2) EC2 security group inbound rules

For demo, you may allow `0.0.0.0/0`; safer is only trusted source IP/CIDR.

- Hasil Panen EC2:
  - allow TCP `8082` from Prometheus server IP/CIDR (or `0.0.0.0/0` for demo)
- Monitoring EC2:
  - allow TCP `9090` from your IP/CIDR (or `0.0.0.0/0` for demo)
  - allow TCP `3000` from your IP/CIDR (or `0.0.0.0/0` for demo)

## 3) Start monitoring stack

```bash
docker compose -f monitoring/docker-compose.monitoring.yml up -d
```

## 4) Verify

- Prometheus targets page:
  - `http://<MONITORING_HOST>:9090/targets`
- Grafana:
  - `http://<MONITORING_HOST>:3000`
  - demo login: `admin/admin` (change for non-demo usage)

In Grafana, set Prometheus data source URL to:

- `http://prometheus:9090`

## 5) Example PromQL queries (Hasil Panen)

- Request rate:
  - `sum(rate(http_server_requests_seconds_count{job="hasil-panen-service"}[1m]))`
- JVM memory used:
  - `sum(jvm_memory_used_bytes{job="hasil-panen-service"})`
- HTTP 500 rate:
  - `sum(rate(http_server_requests_seconds_count{job="hasil-panen-service",status="500"}[1m]))`

## Same-host note

If Prometheus and Hasil Panen run on the same EC2 host, `localhost:8082` can be used as the target.
