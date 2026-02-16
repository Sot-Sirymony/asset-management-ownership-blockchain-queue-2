# ✅ Correct Commands to Run Projects

## The Correct Filename

The script is named: **`start-all-projects.sh`** (with hyphens)

**NOT:** `start-allprojcts.sh` ❌

## Option 1: Start API + Frontend Only (No Docker Required)

If Docker isn't running or you want to skip blockchain:

```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source"
./start-api-frontend.sh
```

This will start:
- ✅ API Backend (port 8081)
- ✅ Frontend (port 3000)
- ⏭️ Skips blockchain (requires Docker)

## Option 2: Start All Projects (Requires Docker)

**First, ensure Docker Desktop is running!**

Then:

```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source"
./start-all-projects.sh
```

## Option 3: Manual Start (Recommended)

### Terminal 1 - API:
```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source/ownership-api-master"
export SPRING_DATASOURCE_PASSWORD=Mony@1144
mvn spring-boot:run
```

### Terminal 2 - Frontend:
```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source/ownership-ui-master"
npm run dev
```

### Terminal 3 - Blockchain (if Docker is running):
```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source/ownership-network-master"
chmod +x net.sh
./net.sh up
```

## Quick Reference

| Command | What It Does |
|---------|-------------|
| `./start-all-projects.sh` | Starts blockchain + API + frontend (needs Docker) |
| `./start-api-frontend.sh` | Starts API + frontend only (no Docker needed) |
| `./stop-all-projects.sh` | Stops all running services |

## Troubleshooting

### "No such file or directory"
- Check you're in the right directory: `pwd`
- List files: `ls -la *.sh`
- Use exact filename: `start-all-projects.sh` (with hyphens!)

### Docker Permission Denied
- Start Docker Desktop application
- Wait for it to fully start
- Try again

### Port Already in Use
```bash
# Check what's using ports
lsof -i :8081  # API
lsof -i :3000  # Frontend

# Kill if needed
kill -9 <PID>
```
