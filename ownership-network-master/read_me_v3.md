Yes — the **real root cause** is:

### ✅ Root Cause

Your Fabric **identity materials (MSP) were never generated**, so the folders you mount into the containers **do not contain the required certificate files** — especially:

* `msp/signcerts/`  (the node’s signing certificate)
* (and often also `msp/cacerts/`, `msp/admincerts/`, etc.)

That’s why Fabric crashes with:

* Orderer: `.../msp/signcerts: no such file or directory`
* Peer: `.../msp/signcerts: no such file or directory`

### Why were MSP files never generated?

Because the scripts that should create them (your `create-certificate-with-ca/.../*.sh`) **did not successfully run** due to a second direct cause:

✅ **`fabric-ca-client` is missing** in the environment where you executed the scripts.

You confirmed it yourself:

```bash
which fabric-ca-client
fabric-ca-client version
# -> command not found
```

So every “enroll” and “register” step failed, therefore:

* No enrollment happened
* No certificates were produced
* `channel/crypto-config/.../msp/signcerts` was never created
* Containers mount empty MSP folders → Fabric panics

---

## Summary in 1 sentence

**Your orderers/peers fail because they mount MSP directories that are empty, and they are empty because `fabric-ca-client` was not available so certificate enrollment never happened.**
