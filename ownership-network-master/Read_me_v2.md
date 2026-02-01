Great — this is a simple path mistake ✅

You ran the tools container from inside **`create-certificate-with-ca/`** folder on your Mac:

```
... % (pwd) create-certificate-with-ca
-v "$PWD:/work"
```

So inside the container, `/work` **already IS** `create-certificate-with-ca`, meaning there is **no** subfolder named `create-certificate-with-ca/...`.

That’s why it says:

`No such file or directory`

✅ Fix: use paths **without** the extra `create-certificate-with-ca/` prefix.

---

# ✅ Do this inside the container now

## 1) Patch localhost correctly (inside container)

```bash
sed -i 's/localhost:9054/ca_orderer:9054/g' orderer/*.sh
sed -i 's/localhost:7054/ca.org1.ownify.com:7054/g' org1/*.sh
grep -R --line-number "localhost:9054\|localhost:7054" . | head -n 20
```

If grep prints nothing → good.

---

## 2) Run org1 script (show errors)

```bash
set -e
cd org1
bash -x ./start-orgs.sh
```

## 3) Run orderer script

```bash
cd ../orderer
bash -x ./start-orderers.sh
exit
```

---

# ✅ After exit, verify on your Mac

Because you mounted only `create-certificate-with-ca` into the container, your scripts might be writing to `../channel/crypto-config...` (relative path). That should still work if the scripts use `../..` etc, but we must verify from Mac at the repo root:

### Go back to repo root on Mac:

```bash
cd ..
```

Then check:

```bash
find channel/crypto-config -maxdepth 6 -type d -name signcerts -print
find channel/crypto-config -type f -path "*signcerts*" -print | head
```

---

# ⚠️ If signcerts still not created

Then the scripts are writing to a different location because you mounted only a subfolder.

✅ Best solution: run the tools container from **repo root** instead of inside `create-certificate-with-ca`.

### Correct way (run from repo root):

```bash
cd ..   # make sure you're in ownership-network-master
docker run --rm -it \
  --network test \
  -v "$PWD:/work" \
  -w /work \
  hyperledger/fabric-tools:2.5 \
  bash
```

Then the patch commands will be:

```bash
sed -i 's/localhost:9054/ca_orderer:9054/g' create-certificate-with-ca/orderer/*.sh
sed -i 's/localhost:7054/ca.org1.ownify.com:7054/g' create-certificate-with-ca/org1/*.sh
```

And run:

```bash
bash -x create-certificate-with-ca/org1/start-orgs.sh
bash -x create-certificate-with-ca/orderer/start-orderers.sh
```

---

## Next message: paste the last 20 lines

If `bash -x ./start-orgs.sh` fails, paste the **last ~20 lines** of output and I’ll tell you the exact fix.
