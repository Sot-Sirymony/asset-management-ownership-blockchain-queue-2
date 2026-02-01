Got it 👍
Here’s a **clean, repeatable checklist** that includes **both scripts**:

* `start-orderers-v3.sh` (orderer CA + orderer MSP/TLS)
* `start-orgs.sh` (Org1 CA + peer MSP/TLS)

You can save this as your **“how to re-run everything” guide**.

---

# ✅ FULL STEP-BY-STEP RE-RUN GUIDE

Assumption:

* Project root = `ownership-network-master`
* Docker network = `test`
* CA containers defined in `create-certificate-with-ca/docker-compose.yaml`

---

## 🔹 STEP 0 — Go to project root (Mac)

```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source/ownership-network-master"
```

---

## 🔹 STEP 1 — Start CA containers

```bash
docker compose -f create-certificate-with-ca/docker-compose.yaml up -d ca_orderer ca_org1
```

Verify:

```bash
docker ps --format "table {{.Names}}\t{{.Networks}}\t{{.Ports}}"
```

You should see:

* `ca_orderer` → `9054`
* `ca.org1.ownify.com` → `7054`

---

## 🔹 STEP 2 — Run ORDERER certificate script

### Enter Fabric CA client container

```bash
docker run --rm -it \
  --network test \
  -v "$PWD":/work \
  -w /work/create-certificate-with-ca/orderer \
  hyperledger/fabric-ca:1.5 \
  bash
```

### Inside container

```bash
ls -l ../fabric-ca/ordererOrg/tls-cert.pem
chmod +x ./start-orderers-v3.sh
./start-orderers-v3.sh
```

### (Only if needed) Fix `priv_sk`

```bash
CRYPTO_DIR="/work/channel/crypto-config/ordererOrganizations/ownify.com"

rm -rf "${CRYPTO_DIR}/ca"
mkdir -p "${CRYPTO_DIR}/ca"

cp "${CRYPTO_DIR}/orderers/orderer.ownify.com/msp/cacerts/"* \
  "${CRYPTO_DIR}/ca/ca.ownify.com-cert.pem"

keyfile="$(ls -1 "${CRYPTO_DIR}/msp/keystore"/* | head -n 1)"
cp "${keyfile}" "${CRYPTO_DIR}/ca/priv_sk"

ls -l "${CRYPTO_DIR}/ca"
```

Exit container:

```bash
exit
```

---

## 🔹 STEP 3 — Run ORG1 (PEER) certificate script (`start-orgs.sh`)

### Enter Fabric CA client container

```bash
docker run --rm -it \
  --network test \
  -v "$PWD":/work \
  -w /work/create-certificate-with-ca/org1 \
  hyperledger/fabric-ca:1.5 \
  bash
```

### Inside container

```bash
ls -l ../fabric-ca/org1/tls-cert.pem
chmod +x ./start-orgs.sh
./start-orgs.sh
```

👉 If you see
`Identity 'peer0' is already registered`
that means it already ran successfully before (safe to ignore unless you want a clean reset).

Exit container:

```bash
exit
```

---

## 🔹 STEP 4 — Verify generated crypto (Mac)

```bash
ls channel/crypto-config/ordererOrganizations/ownify.com
ls channel/crypto-config/peerOrganizations
```

You should see:

* `orderers/orderer.ownify.com`
* `peerOrganizations/org1.ownify.com`

---

## 🔹 STEP 5 — Clean re-run (ONLY if you want to reset everything)

⚠️ This deletes all generated certs.

```bash
rm -rf channel/crypto-config
docker compose -f create-certificate-with-ca/docker-compose.yaml down
rm -rf create-certificate-with-ca/fabric-ca/ordererOrg/*
rm -rf create-certificate-with-ca/fabric-ca/org1/*
docker compose -f create-certificate-with-ca/docker-compose.yaml up -d ca_orderer ca_org1
```

Then repeat Steps **2 → 4**.

---

# 🧠 Key rules (remember these)

* ✅ Always run `start-orderers*.sh` and `start-orgs.sh` **inside** the Fabric CA container
* ❌ Never run `fabric-ca-client` directly on macOS
* ✅ Always use `--network test`
* ✅ Always mount project root to `/work`

---

If you want, next I can:

* verify your **Org1 MSP + NodeOUs**
* help you **start orderer & peer containers**
* help you **create channel + join peers**

Just tell me the next step 
