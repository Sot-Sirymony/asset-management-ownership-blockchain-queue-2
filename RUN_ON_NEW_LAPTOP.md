# Run On New Laptop (Mac + Windows WSL2)

This guide is the Version 2 onboarding runbook for this repository.

Supported platforms:
- macOS (Intel/Apple Silicon)
- Windows 10/11 using WSL2 + Docker Desktop

## 1) 10-Minute Quick Start

From repository root:

```bash
chmod +x start-all-projects.sh stop-all-projects.sh status-all.sh logs-all.sh reset-all.sh
./start-all-projects.sh
```

Open:
- UI: `http://localhost:3000`
- API Swagger: `http://localhost:8081/swagger-ui/index.html`

Stop everything:

```bash
./stop-all-projects.sh
```

Check status/logs quickly:

```bash
./status-all.sh
./logs-all.sh all
```

## 2) Preflight Checklist (Required)

Run and verify:

```bash
docker --version
docker compose version
git --version
node --version
npm --version
java -version
mvn -version
```

Expected baseline:
- Node.js `>=18`
- Java `17`
- Maven installed and runnable
- Docker Desktop running

Also ensure these ports are not occupied:
- `3000` (UI)
- `8081` (API)
- `55432` (PostgreSQL compose default; `5432` if you run local postgres directly)
- Fabric/network ports used by compose files (`7050`, `7051`, `7054`, `8050`, `8051`, `9050`, `9054`, `5984`, `5985`, `5986`, `8080`)

## 3) Environment Setup

## API
Use template:

```bash
cd ownership-api-master
cp .env.example .env
```

Minimum values to review:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `FABRIC_CHANNEL`
- `FABRIC_CHAINCODE`

Important:
- `application.properties` already reads DB settings from environment variables.
- Root `start-all-projects.sh` uses `SPRING_DATASOURCE_PASSWORD=postgres` default if not set.
- If local PostgreSQL is not running, startup scripts will auto-start `postgres` from `ownership-api-master/docker-compose.yml`.

## UI
UI currently uses `ownership-ui-master/.env` in this repository. Confirm:
- `NEXT_PUBLIC_API_URL=http://localhost:8081`
- `NEXTAUTH_URL=http://localhost:3000`

## 4) macOS Steps

From repository root:

```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source"
chmod +x start-all-projects.sh stop-all-projects.sh status-all.sh logs-all.sh reset-all.sh
./start-all-projects.sh
```

If you want to run API + UI only:

```bash
./start-api-frontend.sh
```

If you run API in Docker Compose (optional), default host port is `18081`:

```bash
docker compose -f ownership-api-master/docker-compose.yml up -d api postgres otel-collector
# Swagger:
# http://localhost:18081/swagger-ui/index.html
```

## 5) Windows Steps (WSL2 + Docker Desktop)

Official V2 Windows path is WSL2 only.

1. Install:
- Docker Desktop (with WSL2 integration enabled)
- WSL2 Ubuntu
- Git, Node.js, Java 17, Maven in WSL

2. Clone/open repo inside WSL filesystem.

3. Run inside WSL terminal:

```bash
cd "/path/to/All In One Source"
chmod +x start-all-projects.sh stop-all-projects.sh status-all.sh logs-all.sh reset-all.sh
./start-all-projects.sh
```

Notes:
- Do not run `.sh` scripts from CMD/PowerShell directly.
- Keep Docker Desktop running in Windows while executing commands in WSL.

## 6) Lifecycle Commands (Current Mapping)

From repository root:

- Start all:
  ```bash
  ./start-all-projects.sh
  ```
- Stop all:
  ```bash
  ./stop-all-projects.sh
  ```
- Status all:
  ```bash
  ./status-all.sh
  ```
- Logs all (snapshot):
  ```bash
  ./logs-all.sh all
  ```
- Logs live:
  ```bash
  ./logs-all.sh api
  ./logs-all.sh ui
  ./logs-all.sh network
  ```
- Reset all:
  ```bash
  ./reset-all.sh
  # Optional full cleanup:
  ./reset-all.sh --full
  ```

Network lifecycle:

```bash
cd ownership-network-master
./net.sh status
./net.sh logs
./net.sh logs explorer
./net.sh logs ca
./net.sh reset
```

API/UI logs:
- API: `ownership-api-master/api.log`
- UI: `ownership-ui-master/ui.log`

Example:

```bash
tail -f ownership-api-master/api.log
tail -f ownership-ui-master/ui.log
```

## 7) Stable Port Table

| Component | Default Port | Purpose |
|---|---:|---|
| UI (Next.js) | 3000 | Frontend |
| API (Spring Boot) | 8081 | REST API + Swagger |
| PostgreSQL | 55432 | Application database (compose default host port) |
| Explorer | 8080 | Blockchain Explorer UI |
| CA Org1 | 7054 | Fabric CA |
| CA Orderer | 9054 | Fabric CA |
| Orderer | 7050 | Fabric ordering |
| Peer0 Org1 | 7051 | Fabric peer |
| Peer1 Org1 | 8051 | Fabric peer |
| CouchDB0 | 5984 | Ledger state DB |
| CouchDB1 | 5985 | Ledger state DB |
| CouchDB2 | 5986 | Ledger state DB |

If conflict exists:
- Stop local process using that port.
- Or change service config and update docs/env consistently.

## 8) Quick Verification Flow (E2E)

1. Open UI and login.
2. Create asset from admin/user flow.
3. Transfer asset to another owner.
4. Verify trail using API:

```bash
curl -H "Authorization: Bearer <TOKEN>" http://localhost:8081/api/v1/user/verifyAsset/<ASSET_ID>
curl -H "Authorization: Bearer <TOKEN>" http://localhost:8081/api/v1/user/verifyAssetExternal/<ASSET_ID>
curl -H "Authorization: Bearer <TOKEN>" http://localhost:8081/api/v1/user/verificationTrail/<ASSET_ID>
```

Expected:
- Verification endpoints return asset identity and history/trail data.

## 9) Troubleshooting

### Issue: Script fails with permission denied
Run:

```bash
chmod +x start-all-projects.sh stop-all-projects.sh
chmod +x ownership-network-master/net.sh
```

### Issue: Docker network/service startup failure
Run:

```bash
cd ownership-network-master
./net.sh status
./net.sh logs
```

Then retry:

```bash
./net.sh down
./net.sh up
```

### Issue: API does not start
Check:
- DB credentials and DB availability on `55432` (or `5432` if custom URL)
- API log: `ownership-api-master/api.log`
- Swagger health: `http://localhost:8081/swagger-ui/index.html`

Start DB manually if needed:

```bash
docker compose -f ownership-api-master/docker-compose.yml up -d postgres
```

### Issue: UI does not start
Check:
- `ownership-ui-master/.env` API URL value
- UI log: `ownership-ui-master/ui.log`
- Port `3000` availability

### Known limitation (important)
`ownership-api-master/docker-compose.yml` now uses portable relative/env-based mount paths (`NETWORK_CRYPTO_PATH`, `NETWORK_CONNECTION_PROFILE`). If your repo layout differs, set these variables in `ownership-api-master/.env` before running Docker Compose.
