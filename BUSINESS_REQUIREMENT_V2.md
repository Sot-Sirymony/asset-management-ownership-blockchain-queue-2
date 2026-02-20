# Business Requirement Version 2

## Document Control
- Project: Ownership Management Platform (Network + API + UI)
- Version: 2.0
- Status: Draft for implementation
- Audience: Developers, testers, instructors, and evaluators

## 1. Goal
Any new developer or tester can run the full project on another laptop (MacBook or Windows with WSL2) with minimal manual setup and predictable outcomes.

## 2. Business Objectives
- Reduce onboarding time and setup errors across devices.
- Make startup reproducible for demos, testing, and class evaluation.
- Standardize run flow for `ownership-network-master`, `ownership-api-master`, and `ownership-ui-master`.

## 3. Scope

### In Scope
- Local runtime setup on macOS and Windows (WSL2 path).
- Startup and lifecycle command standardization.
- Environment variable templates and setup instructions.
- End-to-end functional verification flow:
  - login
  - create asset
  - transfer asset
  - verify asset trail

### Out of Scope
- Production deployment and cloud operations.
- Auto scaling, high availability, and enterprise observability.
- Security hardening beyond current development baseline.

## 4. Stakeholders
- Student/developer team
- QA/testers
- Instructor/evaluator
- Demo operators

## 5. Functional Requirements (BR2)

### BR2-01: One-command startup
The platform must provide one documented root command to start required services in sequence.

Current command mapping:
- `./start-all-projects.sh` from repository root.

Expected behavior:
- Start blockchain network first.
- Start API second.
- Start UI third.
- Print startup summary and key URLs.

### BR2-02: Cross-platform support
The documented process must support:
- macOS (Intel and Apple Silicon)
- Windows 10/11 via WSL2 + Docker Desktop

Policy note:
- Native PowerShell flow is not official in V2 scope.
- Windows users must execute project scripts inside WSL2 shell.

### BR2-03: Standard prerequisites check
Documentation must define a preflight checklist that validates:
- Docker and Docker Compose
- Git
- Node.js and npm
- Java 17
- Maven
- Required local ports availability

If missing, documentation must provide actionable fix steps.

### BR2-04: Environment template
Project must provide environment template(s) and minimal setup steps:
- API template: `ownership-api-master/.env.example`
- UI runtime variables documented from `ownership-ui-master/.env`

User workflow:
- Copy template where applicable.
- Update only required fields.
- Start services without code edits.

### BR2-05: Lifecycle commands
V2 documentation must define and map lifecycle operations:
- start
- stop
- status
- logs
- reset

Command mapping in current repository:
- start: `./start-all-projects.sh`
- stop: `./stop-all-projects.sh`
- status: `./status-all.sh`
- logs: `./logs-all.sh` (plus `net.sh logs` subcommands)
- reset: `./reset-all.sh` (calls `net.sh reset` and cleans app caches/logs)

### BR2-06: Quick verification flow
After startup, documentation must provide a minimal functional validation flow:
1. Login via UI/API.
2. Create an asset.
3. Transfer asset ownership.
4. Verify asset history/trail.

Validation endpoints (API):
- `/api/v1/user/verifyAsset/{id}`
- `/api/v1/user/verifyAssetExternal/{id}`
- `/api/v1/user/verificationTrail/{id}`

### BR2-07: Documentation quality
A single onboarding run guide must exist:
- File: `RUN_ON_NEW_LAPTOP.md`
- Must include:
  - 10-minute quick start
  - macOS section
  - Windows (WSL2) section
  - troubleshooting section

### BR2-08: Stable default ports
The documentation must define standard local ports and conflict guidance.

Core expected ports:
- UI: `3000`
- API: `8081`
- PostgreSQL: `5432`
- Explorer: `8080`
- Fabric/CouchDB/CA ports as defined by network compose files

## 6. Non-Functional Requirements (NFR2)

### NFR2-01 Usability
A new user can complete first run by following docs only, without direct developer support.

### NFR2-02 Performance
- First full startup target: <= 15 minutes
- Subsequent startup target: <= 5 minutes

### NFR2-03 Reliability
Documented commands should produce consistent results across:
- macOS
- Windows with WSL2

### NFR2-04 Maintainability
Lifecycle command references and setup guidance must remain versioned in repository markdown docs.

## 7. Acceptance Criteria (AC2)

### AC2-01 Fresh MacBook setup
Given a fresh Mac environment with prerequisites installed, setup succeeds by following documentation only.

### AC2-02 Fresh Windows setup (WSL2)
Given Windows 10/11 with WSL2 + Docker Desktop, setup succeeds by following documentation only.

### AC2-03 End-to-end flow works
Login -> create asset -> transfer asset -> verify trail succeeds on both supported platforms.

### AC2-04 Actionable startup failures
When startup fails, docs provide troubleshooting that identifies likely cause and corrective action.

### AC2-05 Verification evidence captured
Setup duration, pass/fail result, and blocker notes are recorded in a checklist artifact.

## 8. Current Implementation Notes and Constraints
- Root lifecycle scripts exist for start/stop/status/logs/reset:
  - `start-all-projects.sh`
  - `stop-all-projects.sh`
  - `status-all.sh`
  - `logs-all.sh`
  - `reset-all.sh`
- Network lifecycle is managed by `ownership-network-master/net.sh`.
- API Docker compose uses portable path variables:
  - `NETWORK_CRYPTO_PATH`
  - `NETWORK_CONNECTION_PROFILE`
- Shell scripts assume Bash-compatible environment; Windows support in V2 is explicitly WSL2-based.

## 9. Traceability Matrix

| Requirement | Delivery Artifact |
|---|---|
| BR2-01, BR2-05 | `RUN_ON_NEW_LAPTOP.md` lifecycle section |
| BR2-02 | `RUN_ON_NEW_LAPTOP.md` OS-specific sections |
| BR2-03, BR2-04 | `RUN_ON_NEW_LAPTOP.md` prerequisites and env setup |
| BR2-06 | `RUN_ON_NEW_LAPTOP.md` verification flow |
| BR2-07 | Presence and completeness of `RUN_ON_NEW_LAPTOP.md` |
| BR2-08 | `RUN_ON_NEW_LAPTOP.md` ports and conflicts |
| AC2-01..AC2-05 | `VERIFICATION_CHECKLIST_V2.md` |
