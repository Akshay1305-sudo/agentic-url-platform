# Agentic URL Platform — Spring Boot Microservices

Prototype for the attached Agentic Software Engineering System assignment. It combines a URL shortener and click analytics with a governed workflow orchestrator.

## Stack
- Java 17, Spring Boot 3.3.5, Maven multi-module
- URL Service (8081), Analytics Service (8082), Orchestrator Service (8083)
- PostgreSQL 16, Docker Compose, Actuator health endpoints

## Run with Docker
Prerequisites: Docker Desktop / Docker Engine with Compose.

```bash
git clone <your-repository-url>
cd agentic-url-platform
docker compose up --build
```

The first startup builds the three services and initializes PostgreSQL. Health checks:
- `http://localhost:8081/actuator/health`
- `http://localhost:8082/actuator/health`
- `http://localhost:8083/actuator/health`

## Try URL APIs
Create a URL:
```bash
curl -i -X POST http://localhost:8081/api/urls -H 'Content-Type: application/json' -d '{"url":"https://spring.io","expiresInDays":30}'
```
Use returned `code`:
```bash
curl -i http://localhost:8081/api/urls/CODE/redirect
curl http://localhost:8082/api/analytics/CODE
curl http://localhost:8081/api/urls/CODE
curl -i -X DELETE http://localhost:8081/api/urls/CODE
```
`expiresInDays` is optional; omitted or `0` means no expiry. Redirect events are sent to analytics best-effort so analytics downtime does not prevent redirecting.

## Try the agentic workflow
Start one of the required scenarios (`greenfield`, `brownfield`, `ambiguous`):
```bash
curl -s -X POST http://localhost:8083/api/workflows -H 'Content-Type: application/json' -d '{"scenario":"ambiguous","requirement":"Add custom aliases and click analytics to the URL shortener"}'
```
The response includes a workflow ID, task dependency graph, task states and audit events. Tasks with no dependencies can progress independently; the release task waits for the tests and docs gates. Approve only after review:
```bash
curl -s -X POST http://localhost:8083/api/workflows/WORKFLOW_ID/approval -H 'Content-Type: application/json' -d '{"approved":true,"comment":"Reviewed tests and risk controls"}'
```
Reject with `approved:false`, retrieve status with `GET /api/workflows/{id}`, stop with `POST /api/workflows/{id}/safe-stop`, and inspect metrics at `GET /api/workflows/{id}/metrics`.

## Architecture and orchestration model
- Explicit DAG: `understand -> design -> implementation -> tests -> release`; `design -> docs -> release`.
- The release gate requires both test validation and documentation completion and always pauses for human approval.
- Each task carries status, dependencies, attempts and output; workflow events preserve audit lineage.
- Retry is bounded to two attempts; exhausted tasks safe-stop rather than advancing to release.
- Metrics include success rate, retry count, rollback count and end-to-end latency. Rollback count is currently a placeholder because this prototype does not mutate source code or deploy releases.
- Requirement changes should start a new workflow in this baseline. Full dependency-aware invalidation/replanning is a planned extension.

## Required scenarios
1. **Greenfield:** create a new endpoint or service; inspect the normalized requirement, task graph, test plan and approval gate.
2. **Brownfield:** request a change to an existing URL API; use the graph to identify design, implementation, test and documentation tasks before approval.
3. **Ambiguous:** submit a vague requirement; the prototype records assumptions and a validation plan. A production agent should ask clarifying questions before generating code.

## Security and production hardening
- This is an interview prototype, not production-hardened software.
- The orchestrator uses deterministic task agents to make the demo repeatable; no LLM provider is wired in by default.
- Workflow state is in memory and is lost on restart. Add persistent workflow storage, authentication/authorization, audit retention, distributed tracing and rate limiting before production.
- URL input validates HTTP(S) syntax but production must add SSRF protections, DNS/IP validation and redirect policy. Do not use this service to fetch user URLs server-side.
- Redirect analytics calls are synchronous best-effort in this demo; production should use an outbox or message broker (Kafka/RabbitMQ) for durable event delivery.
- Add Flyway migrations, secret management, TLS, API gateway, CI pipeline, integration tests, load tests and alerting for a production release.

## Build locally
```bash
mvn clean verify
```
Requires Maven 3.9+ and JDK 17+. This environment may not have Maven installed, so the build has not been verified here; Docker builds compile each module.
