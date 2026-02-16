# 📊 Blockchain Network Status Check

## Current Status: ⚠️ Docker Permission Issue

The network startup **failed** due to Docker permission errors. The log shows:

```
permission denied while trying to connect to the Docker daemon socket
unable to get image 'hyperledger/fabric-ca:1.5': permission denied
```

## 🔧 Fix Required: Docker Access

### Step 1: Check Docker Desktop Status

**In your terminal, run:**

```bash
docker ps
```

**Expected results:**
- ✅ **If Docker is running:** You'll see container list (even if empty)
- ❌ **If Docker is NOT running:** You'll see "permission denied" or "Cannot connect to Docker"

### Step 2: Start Docker Desktop

1. **Open Docker Desktop application** on your Mac
2. **Wait for it to fully start** (whale icon in menu bar should be steady)
3. **Verify it's running:**
   ```bash
   docker ps
   ```
   Should return container list (or empty list, not an error)

### Step 3: Check Current Containers

**Run this command to see what's currently running:**

```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source/ownership-network-master"
./net.sh status
```

**Or directly:**

```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

## 📋 Expected Containers (When Network is Running)

When the blockchain network is **fully started**, you should see:

| Container Name | Purpose | Status Should Be |
|---------------|---------|------------------|
| `ca_orderer` or `ca.org1.ownify.com` | Certificate Authority | Up X minutes |
| `peer0.org1.ownify.com` | Peer node | Up X minutes |
| `orderer.ownify.com` | Orderer node | Up X minutes |
| `couchdb0` | CouchDB database | Up X minutes |
| `explorer` | Blockchain Explorer | Up X minutes (if enabled) |

## 🚀 Start the Network (After Docker is Ready)

Once Docker Desktop is running, execute:

```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source/ownership-network-master"
./net.sh up
```

**This will:**
1. ✅ Start Certificate Authorities
2. ✅ Generate crypto materials  
3. ✅ Start Fabric network
4. ✅ Create channels
5. ✅ Deploy chaincode
6. ✅ Start explorer

**Time:** 2-5 minutes

## 📊 Monitor Progress

**While it's starting, check status:**

```bash
# Check status
./net.sh status

# Or check Docker directly
docker ps

# View logs if needed
./net.sh logs explorer
./net.sh logs ca
```

## ✅ Verification Checklist

After startup completes, verify:

- [ ] Docker Desktop is running
- [ ] `docker ps` shows containers (no permission errors)
- [ ] `./net.sh status` shows blockchain containers
- [ ] Peer container is running (`peer0.org1.ownify.com`)
- [ ] Orderer container is running (`orderer.ownify.com`)
- [ ] CouchDB is running (`couchdb0`)
- [ ] No error messages in logs

## 🔍 Quick Status Commands

```bash
# Check all containers
docker ps

# Check only blockchain containers
docker ps | grep -E "peer|orderer|ca|couchdb|explorer"

# Check network status via script
cd ownership-network-master
./net.sh status

# View startup log (if exists)
tail -f network-startup.log
```

## 🆘 Troubleshooting

### Docker Permission Denied

**Solution:**
1. Open Docker Desktop
2. Go to Settings → Resources → File Sharing
3. Ensure project directory is accessible
4. Restart Docker Desktop
5. Try again

### Containers Not Starting

```bash
# Check what failed
docker ps -a

# View logs of failed container
docker logs <container-name>

# Reset and retry
cd ownership-network-master
./net.sh reset
./net.sh up
```

### Port Conflicts

```bash
# Check what's using ports
lsof -i :7050  # Orderer
lsof -i :7051  # Peer
lsof -i :5984  # CouchDB

# Kill conflicting processes if needed
kill -9 <PID>
```

## 📝 Next Steps

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

## Current Status Summary

Based on your earlier `./net.sh status` output:
- ✅ `ownership-api` - Running (port 8081)
- ✅ `otel-collector` - Running
- ❌ Blockchain network containers - **Not started** (Docker permission issue)

**Action:** Start Docker Desktop, then run `./net.sh up` again.
