# Masla
A fully asynchronous Java API gateway, validated with millions of concurrent C-end users, supporting multiple access protocols including HTTP, HTTPS, and HTTP/2.

# Dependency

running only jdk, not dependency other compensate

# Feature
- Service Discovery
  Masla API Gateway currently only supports Nacos as the service registry. It performs service discovery via Nacos to automatically detect dynamic changes in services.

- Traffic Control
  Masla provides built-in support for traffic control. You can configure rate limiting for each API using a token bucket algorithm, which is single-node rate limiting.

- Traffic Circuit Breaking
  Masla API Gateway has a default circuit breaking mechanism. When the failure rate reaches 50%, circuit breaking is triggered. It supports intelligent auto-upgrade and auto-recovery mechanisms.

- Black/White List
  Service-level support including matching by path, client IP, and header parameters.

Global blacklist support.

- Warm-up / Slow Start
  Supports a slow start mechanism for newly added service instances, similar to TCP slow start, where traffic is gradually increased. Once the warm-up window ends, normal traffic distribution resumes.

- Load Balancing
  Masla provides Round-Robin as the default load balancing strategy.

- Health Check
  Requires services to support /healthcheck endpoint. With health check support, zero-downtime deployment can be achieved.


# Architect


# Startup

