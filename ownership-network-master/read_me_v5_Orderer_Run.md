Sure — here’s a clean “re-run checklist” you can copy/paste next time. I’ll assume you’re at **`ownership-network-master/`** and your Docker network is **`test`**.

---

## 0) Go to project root

```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source/ownership-network-master"
```

---

## 1) Start CA containers

```bash
docker compose -f create-certificate-with-ca/docker-compose.yaml up -d ca_orderer ca_org1
docker ps --format "table {{.Names}}\t{{.Networks}}\t{{.Ports}}"
```

(Optional) watch logs:

```bash
docker logs -f ca_orderer
```

---

## 2) Run the orderer cert script (inside a CA client container)

```bash
docker run --rm -it \
  --network test \
  -v "$PWD":/work \
  -w /work/create-certificate-with-ca/orderer \
  hyperledger/fabric-ca:1.5 \
  bash
```

Inside container:

```bash
ls -l ../fabric-ca/ordererOrg/tls-cert.pem
chmod +x ./start-orderers-v3.sh
./start-orderers-v3.sh
```

---

## 3) Fix `priv_sk` copy (only if you ever see the cp error again)

Inside the container:

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

## 4) Verify output on Mac

```bash
ls -l channel/crypto-config/ordererOrganizations/ownify.com/ca
ls -l channel/crypto-config/ordererOrganizations/ownify.com/orderers
```

---

## 5) If you want a clean re-run (reset everything)

⚠️ This deletes generated crypto material and you must re-run scripts.

```bash
rm -rf channel/crypto-config/ordererOrganizations/ownify.com
```

If you also want to reset CA persisted data:

```bash
docker compose -f create-certificate-with-ca/docker-compose.yaml down
rm -rf create-certificate-with-ca/fabric-ca/ordererOrg/*
rm -rf create-certificate-with-ca/fabric-ca/org1/*
docker compose -f create-certificate-with-ca/docker-compose.yaml up -d ca_orderer ca_org1
```

---

### Notes (so you don’t hit old issues again)

* Always run Fabric CA commands **inside the container** (not on macOS), otherwise `fabric-ca-client` will be missing.
* Paths with spaces are OK now because the script uses `SCRIPT_DIR` and quotes.

If your next step is **generate org1 peer certs** or **start orderer nodes**, tell me which and I’ll give the same kind of step-by-step command list.
