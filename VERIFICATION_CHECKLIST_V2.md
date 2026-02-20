# Verification Checklist V2

Use this checklist to validate Business Requirement Version 2 across supported devices.

## Test Session Metadata

- Test date: 2026-02-17
- Tester name: Sotsiry Mony + Codex assistant
- Repository commit/branch:
- Device type: `Mac`
- OS version: `darwin 25.0.0`
- Notes: Targeted smoke verification completed for requested UI flows. Browser MCP automation was unavailable, so verification evidence uses live API checks (`200`) and route checks.

## A) Preflight Verification

Mark pass/fail for each prerequisite.

| Check | Command | Pass/Fail | Evidence/Output |
|---|---|---|---|
| Docker installed | `docker --version` |  |  |
| Docker Compose installed | `docker compose version` |  |  |
| Git installed | `git --version` |  |  |
| Node.js >= 18 | `node --version` |  |  |
| npm installed | `npm --version` |  |  |
| Java 17 installed | `java -version` |  |  |
| Maven installed | `mvn -version` |  |  |
| Required ports free/managed | check `3000, 8081, 55432` (or `5432` custom) and fabric ports |  |  |

## B) Startup Timing

Track duration to validate NFR2-02.

| Metric | Target | Actual | Pass/Fail |
|---|---:|---:|---|
| First full startup | <= 15 min |  |  |
| Subsequent startup | <= 5 min |  |  |

## C) Acceptance Criteria Matrix

### AC2-01: Fresh MacBook setup succeeds by docs only

| Step | Expected Result | Pass/Fail | Evidence |
|---|---|---|---|
| Follow `RUN_ON_NEW_LAPTOP.md` Mac section only | No undocumented/manual workaround required |  |  |
| Run `./start-all-projects.sh` | Network, API, UI start |  |  |
| Access UI `http://localhost:3000` | UI loads |  |  |
| Access Swagger `http://localhost:8081/swagger-ui/index.html` | API docs load |  |  |

AC2-01 final result: `PASS / FAIL`

### AC2-02: Fresh Windows setup succeeds by docs only (WSL2 path)

| Step | Expected Result | Pass/Fail | Evidence |
|---|---|---|---|
| Follow `RUN_ON_NEW_LAPTOP.md` Windows WSL2 section only | No undocumented/manual workaround required |  |  |
| Run scripts inside WSL2 shell | Scripts execute successfully |  |  |
| Run `./start-all-projects.sh` | Network, API, UI start |  |  |
| Access UI and Swagger | Both accessible |  |  |

AC2-02 final result: `PASS / FAIL`

### AC2-03: End-to-end flow works on both platforms

| Flow Step | Expected Result | Pass/Fail | Evidence |
|---|---|---|---|
| Login | Valid token/session returned |  |  |
| Create asset | Asset creation successful |  |  |
| Transfer asset | Ownership updated |  |  |
| Verify asset | `/verifyAsset/{id}` returns success |  |  |
| Verify external | `/verifyAssetExternal/{id}` returns success |  |  |
| Verify trail | `/verificationTrail/{id}` returns full trail/history |  |  |

AC2-03 final result: `PASS / FAIL`

### AC2-04: Startup failures provide actionable guidance

| Scenario | Expected Guidance | Pass/Fail | Evidence |
|---|---|---|---|
| Port conflict | User sees conflict and resolution steps |  |  |
| Docker/network issue | User can run status/log commands and recover |  |  |
| API startup issue | User can identify DB/env issue from guide/logs |  |  |
| UI startup issue | User can identify env/port issue from guide/logs |  |  |

AC2-04 final result: `PASS / FAIL`

### AC2-05: Setup result tracking is recorded

| Item | Expected | Pass/Fail | Evidence |
|---|---|---|---|
| Setup duration recorded | Start/end and total minutes documented |  |  |
| Overall pass/fail recorded | Final verdict present |  |  |
| Blockers documented | Root cause + workaround/fix captured |  |  |

AC2-05 final result: `PASS / FAIL`

## C.1) Targeted UI Smoke (Requested)

| Flow Step | Expected Result | Pass/Fail | Evidence |
|---|---|---|---|
| Login | Valid token/session returned | PASS | `POST /rest/auth/login` => `200` with `payload.token` |
| Asset list page backing call | Asset list API returns success | PASS | `GET /api/v1/user/getAllAsset` => `200`, payload `[]` |
| Dashboard page backing call | Dashboard API returns success | PASS | `GET /api/v1/admin/dashboard` => `200` |
| Issue/report page backing call | Issue API returns success | PASS | `GET /api/v1/user/getAllIssue` => `200`, payload `[]` |
| UI service reachability | UI root and protected routes respond | PASS | `/` => `200`, `/admin/asset` `/admin/dashboard` `/admin/report-issue` => `307` (auth redirect) |

Targeted UI smoke final result: `PASS`

## D) Lifecycle Command Verification

Validate command coverage in current repo.

| Lifecycle Operation | Command | Pass/Fail | Notes |
|---|---|---|---|
| start | `./start-all-projects.sh` |  |  |
| stop | `./stop-all-projects.sh` |  |  |
| status | `./status-all.sh` |  |  |
| logs | `./logs-all.sh all` (or `api`, `ui`, `network`) |  |  |
| reset | `./reset-all.sh` (or `./reset-all.sh --full`) |  |  |

## E) Final Outcome

- Device tested:
- Overall result: `PASS / FAIL`
- Failed criteria (if any):
- Blockers:
- Recommended next actions:
