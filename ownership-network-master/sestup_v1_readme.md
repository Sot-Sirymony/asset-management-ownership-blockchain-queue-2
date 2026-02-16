That’s normal ✅ — on your Mac host you **don’t have `peer` binary**. You must run those commands **inside the `hyperledger/fabric-tools` container** (same way your `setup_v1.sh` does).

Below are copy-paste commands that will work.

---

## 1) Run peer commands inside fabric-tools (recommended)

From `ownership-network-master` (project root):

```bash
docker run --rm -it \
  --network test \
  -v "$PWD":/work \
  -w /work \
  hyperledger/fabric-tools:2.5 \
  bash
```

Now you are inside container (`root@...:/work#`).

### Set env (adjust paths if needed)

```bash
export CORE_PEER_LOCALMSPID=Org1MSP
export CORE_PEER_MSPCONFIGPATH=/work/channel/crypto-config/peerOrganizations/org1.ownify.com/users/Admin@org1.ownify.com/msp
export CORE_PEER_ADDRESS=peer0.org1.ownify.com:7051
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_TLS_ROOTCERT_FILE=/work/channel/crypto-config/peerOrganizations/org1.ownify.com/peers/peer0.org1.ownify.com/tls/ca.crt
export FABRIC_CFG_PATH=/work/channel/config
```

### Then run the checks

```bash
peer channel getinfo -c channel-org
peer lifecycle chaincode querycommitted -C channel-org
peer lifecycle chaincode queryinstalled
```

Paste the output here.

---

## 2) One-liner (no interactive shell)

If you prefer one command:

```bash
docker run --rm -t \
  --network test \
  -v "$PWD":/work \
  -w /work \
  hyperledger/fabric-tools:2.5 \
  bash -lc '
    export CORE_PEER_LOCALMSPID=Org1MSP
    export CORE_PEER_MSPCONFIGPATH=/work/channel/crypto-config/peerOrganizations/org1.ownify.com/users/Admin@org1.ownify.com/msp
    export CORE_PEER_ADDRESS=peer0.org1.ownify.com:7051
    export CORE_PEER_TLS_ENABLED=true
    export CORE_PEER_TLS_ROOTCERT_FILE=/work/channel/crypto-config/peerOrganizations/org1.ownify.com/peers/peer0.org1.ownify.com/tls/ca.crt
    export FABRIC_CFG_PATH=/work/channel/config

    echo "=== getinfo ==="
    peer channel getinfo -c channel-org

    echo "=== querycommitted ==="
    peer lifecycle chaincode querycommitted -C channel-org

    echo "=== queryinstalled ==="
    peer lifecycle chaincode queryinstalled
  '
```

---

## Quick note about your earlier `/work/crypto-config/...` vs `/work/channel/crypto-config/...`

In your previous container output you used:

`/work/crypto-config/...`

But in your `setup_v1.sh`, you mount the whole repo and run scripts under `/work/channel`, so most likely your crypto is under:

`/work/channel/crypto-config/...`

So if my env path above works, great. If it says file not found, run inside container:

```bash
ls -l /work/channel/crypto-config | head
ls -l /work/crypto-config | head
```

and use the one that exists.

---

Once you paste the 3 outputs, I’ll tell you exactly why `basic` has “no endorsers” (usually: chaincode not committed, peer not joined, or approve/commit didn’t actually succeed).
