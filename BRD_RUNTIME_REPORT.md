# BRD Runtime Verification Report

Date: 2026-02-16

## What was fixed in this run

- Patched `ownership-network-master/net.sh` orchestration:
  - Wait for CA readiness before enrollment
  - Generate channel artifacts before orderers start
  - Split startup sequence to avoid CA container conflicts
  - Add deterministic container readiness checks
  - Fix chaincode deploy paths
  - Clear Fabric CA/crypto/channel artifacts and orderer volumes on reset
- Patched `ownership-network-master/channel/5.create-app-channel.sh` path handling for container-safe paths.
- Patched Fabric CA bootstrap scripts for NodeOU certificate filename consistency:
  - `create-certificate-with-ca/orderer/start-orderers-v3.sh`
  - `create-certificate-with-ca/org1/start-orgs.sh`
- Patched channel tx naming consistency:
  - `ownership-network-master/channel/2.create-channelTx.sh`
  - `ownership-network-master/channel/6.join-peers-to-channel.sh`
- Patched peer Docker socket mount for chaincode build:
  - `ownership-network-master/docker-compose.yaml`
- Patched API runtime wiring for Fabric gateway:
  - Added datasource env defaults in `ownership-api-master/docker-compose.yml`
  - Added `FABRIC_DISCOVERY=false` in `ownership-api-master/docker-compose.yml`
  - Fixed `connection-profile/connection.yaml` file content and channel name (`channel-org`)
  - Regenerated `ownership-api-master/wallet/admin.id` from current crypto material
  - Re-enrolled CA `admin` with registrar attributes in cert (`hf.Registrar.*`) and updated wallet identity used by `register_user`

## Runtime verification results (Pass/Fail)

1. Network bootstrap (`net.sh up`)  
   - **PASS**  
   - CA, orderers, peers, CLI, explorer all come up.

2. Channel create + peers join + anchor update  
   - **PASS**  
   - `channel-org` created and both peers joined.

3. Chaincode package/install/approve/commit/init  
   - **PASS**  
   - `basic` chaincode installed and initialized successfully.

4. API authentication (`POST /rest/auth/login`)  
   - **PASS**  
   - Admin login returns `200` with JWT.

5. Asset creation (`POST /api/v1/admin/createAsset`)  
   - **PASS**  
   - Returns `200` with created on-chain asset payload.

6. Asset listing (`GET /api/v1/user/getAllAsset`)  
   - **PASS**  
   - Returns `200` and includes created assets.

7. Verification endpoints  
   - **PASS**  
   - `GET /api/v1/user/verifyAsset/{id}` -> `200`  
   - `GET /api/v1/user/verifyAssetExternal/{id}` -> `200`  
   - `GET /api/v1/user/verificationTrail/{id}` -> `200`

8. Ownership transfer validation (same-owner transfer)  
   - **PASS (business rule enforced)**  
   - Transfer to same assignee is rejected by chaincode/API path (non-success response with message indicating already assigned).

9. API rate limiting  
   - **PASS**  
   - Stress check produced `429` responses (`33`/`120` requests).

10. User enrollment (`POST /api/v1/admin/register_user`)  
    - **PASS**  
    - New user enrollment now returns `200`.

11. Transfer to newly registered user (`PUT /api/v1/admin/transferAsset/{id}`)  
    - **PASS**  
    - Admin can transfer asset to the newly enrolled user; response returns `200`.
    - New user can verify the transferred asset (`GET /api/v1/user/verifyAsset/{id}` -> `200`).

## Final BRD status

- **Overall: FULL PASS (runtime validation scope)**
- Validated end-to-end:
  - network up
  - channel lifecycle
  - chaincode lifecycle
  - user enrollment
  - asset create/transfer/verify on-chain
  - rate limiting behavior
