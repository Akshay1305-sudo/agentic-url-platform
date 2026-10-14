# API examples

## Create short URL
`POST /api/urls` JSON: `{"url":"https://example.com/path","expiresInDays":7}`

## Resolve metadata
`GET /api/urls/{code}`

## Redirect
`GET /api/urls/{code}/redirect` — 302 redirect; expired links return 410, unknown codes return 404.

## Analytics
`GET /api/analytics/{code}` — click total, referrer distribution and last 100 events.

## Start workflow
`POST /api/workflows` JSON: `{"scenario":"greenfield|brownfield|ambiguous","requirement":"..."}`

## Workflow operations
- `GET /api/workflows/{id}` — state, graph and audit.
- `GET /api/workflows/{id}/metrics` — workflow reliability metrics.
- `POST /api/workflows/{id}/approval` JSON: `{"approved":true,"comment":"..."}`.
- `POST /api/workflows/{id}/safe-stop` — stop execution.
