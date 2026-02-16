# 🚀 Starting Blockchain Network

## Current Status

The blockchain network startup command has been initiated, but it requires **Docker Desktop to be running**.

## ⚠️ Docker Permission Issue

If you see "permission denied" errors, you need to:

1. **Start Docker Desktop**
   - Open Docker Desktop application on your Mac
   - Wait for it to fully start (whale icon in menu bar should be steady)
   - Make sure it's not in "Docker Desktop is starting..." state

2. **Grant Terminal Permissions** (if needed)
   - System Settings → Privacy & Security → Full Disk Access
   - Add Terminal (or your terminal app) to allowed apps

## Manual Start Command

Once Docker is running, execute this command in your terminal:

```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source/ownership-network-master"
chmod +x net.sh
./net.sh up
```

## What This Does

The `./net.sh up` command will:
1. ✅ Start Certificate Authorities (CA)
2. ✅ Generate crypto materials
3. ✅ Start Fabric network (orderers, peers, CouchDB)
4. ✅ Create and join channels
5. ✅ Deploy chaincode
6. ✅ Start blockchain explorer (optional)

**Expected time:** 2-5 minutes for first startup

## Check Status

While it's running, check status with:

```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source/ownership-network-master"
./net.sh status
```

## View Logs

If you need to see what's happening:

```bash
# View explorer logs
./net.sh logs explorer

# View CA logs  
./net.sh logs ca

# View all container logs
docker ps
docker logs <container-name>
```

## Verify Network is Running

You'll know it's ready when:

```bash
./net.sh status
```

Shows containers like:
- `peer0.org1.ownify.com` - Running
- `orderer.ownify.com` - Running
- `couchdb0` - Running
- `ca.org1.ownify.com` - Running

## Next Steps After Network Starts

Once the blockchain network is running:

1. **Start API Backend:**
   ```bash
   cd "../ownership-api-master"
   export SPRING_DATASOURCE_PASSWORD=Mony@1144
   mvn spring-boot:run
   ```

2. **Start Frontend:**
   ```bash
   cd "../ownership-ui-master"
   npm run dev
   ```

## Troubleshooting

### Docker Not Running
```bash
# Check if Docker is running
docker ps

# If error, start Docker Desktop app
```

### Permission Denied
- Ensure Docker Desktop is fully started
- Check Docker Desktop → Settings → Resources → File Sharing
- Make sure the project directory is accessible

### Containers Won't Start
```bash
# Check Docker logs
docker ps -a
docker logs <container-name>

# Reset network (clean start)
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
```

## Quick Reference

| Command | Purpose |
|---------|---------|
| `./net.sh up` | Start entire network |
| `./net.sh status` | Check running containers |
| `./net.sh down` | Stop network |
| `./net.sh reset` | Clean reset (removes everything) |
| `./net.sh logs explorer` | View explorer logs |

## Expected Output

When successful, you should see:
```
✅ Certificate Authorities started
✅ Crypto materials generated
✅ Fabric network started
✅ Channel created and joined
✅ Chaincode deployed
✅ Explorer started (if enabled)
```

Then run `./net.sh status` to verify all containers are running.
