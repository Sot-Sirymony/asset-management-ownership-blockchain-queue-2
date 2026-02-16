# 🚀 Starting Blockchain Network

## Current Status

You currently have:
- ✅ `ownership-api` container running (port 8081)
- ✅ `otel-collector` running

But missing blockchain network containers. You need to start the full network.

## Start the Blockchain Network

Run this command:

```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source/ownership-network-master"
./net.sh up
```

This will start:
1. Certificate Authorities (CA)
2. Generate crypto materials
3. Start Fabric network (orderers, peers, CouchDB)
4. Create and join channels
5. Deploy chaincode
6. Start blockchain explorer

**Time:** 2-5 minutes for first startup

## Check Progress

While it's running, you can check status:

```bash
./net.sh status
```

You should see containers like:
- `ca_orderer` or `ca.org1.ownify.com`
- `peer0.org1.ownify.com`
- `orderer.ownify.com` (or `orderer1.ownify.com`, etc.)
- `couchdb0`
- `explorer` (if enabled)

## Step-by-Step (If `./net.sh up` Fails)

If the full startup fails, you can run steps individually:

### Step 1: Start Certificate Authorities
```bash
./net.sh up-ca
```

Wait for CAs to be ready, then:

### Step 2: Generate Crypto Materials
```bash
./net.sh gen-crypto
```

### Step 3: Start Fabric Network
```bash
./net.sh up-fabric
```

### Step 4: Create Channel and Join Peers
```bash
./net.sh channel
```

### Step 5: Deploy Chaincode
```bash
./net.sh deploy-cc
```

### Step 6: Start Explorer (Optional)
```bash
./net.sh up-explorer
```

## Verify Network is Running

After startup completes, check:

```bash
./net.sh status
```

Expected output should show:
```
NAMES                          STATUS          PORTS
ca_orderer                     Up X minutes    ...
ca.org1.ownify.com             Up X minutes    ...
peer0.org1.ownify.com          Up X minutes    ...
orderer.ownify.com             Up X minutes    ...
couchdb0                       Up X minutes    ...
explorer                       Up X minutes    ... (if enabled)
```

## View Logs

If something fails, check logs:

```bash
# Explorer logs
./net.sh logs explorer

# CA logs
./net.sh logs ca

# Or check specific container
docker logs <container-name>
```

## Troubleshooting

### If containers fail to start:
```bash
# Check Docker is running
docker ps

# Check for errors
docker ps -a
docker logs <container-name>

# Reset and try again
./net.sh reset
./net.sh up
```

### If crypto materials missing:
```bash
# Check if crypto-config exists
ls -la channel/crypto-config

# If missing, regenerate
./net.sh gen-crypto
```

### If channel creation fails:
```bash
# Check if channel already exists
docker exec -it cli peer channel list

# If needed, recreate
./net.sh channel
```

## Next Steps

Once network is running:

1. **Verify API can connect:**
   ```bash
   curl http://localhost:8081/swagger-ui.html
   ```

2. **Start Frontend:**
   ```bash
   cd ../ownership-ui-master
   npm run dev
   ```

3. **Test the system:**
   - Login via frontend
   - Create assets
   - Test verification endpoints
   - Test asset transfers

## Quick Commands Reference

| Command | Purpose |
|---------|---------|
| `./net.sh up` | Start everything |
| `./net.sh status` | Check running containers |
| `./net.sh down` | Stop network |
| `./net.sh reset` | Clean reset |
| `./net.sh logs explorer` | View logs |
