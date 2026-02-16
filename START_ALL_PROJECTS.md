# 🚀 Start All Projects Guide

## Quick Start

Run the startup script:

```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source"
./start-all-projects.sh
```

This will start:
1. **Blockchain Network** (Hyperledger Fabric)
2. **API Backend** (Spring Boot on port 8081)
3. **Frontend** (Next.js on port 3000)

## Manual Start (Step by Step)

### Step 1: Start Blockchain Network

```bash
cd ownership-network-master

# Make script executable (first time only)
chmod +x net.sh

# Start the network
./net.sh up
```

**Wait for:** Network containers to be running (check with `./net.sh status`)

### Step 2: Start API Backend

```bash
cd ../ownership-api-master

# Set environment variable
export SPRING_DATASOURCE_PASSWORD=Mony@1144

# Build (if needed)
mvn clean compile -DskipTests

# Start API
mvn spring-boot:run
```

**Wait for:** `Started ArtifactsSdkApplication` in logs

### Step 3: Start Frontend

Open a **new terminal**:

```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source/ownership-ui-master"

# Install dependencies (first time only)
npm install

# Start frontend
npm run dev
```

**Wait for:** Frontend to start on `http://localhost:3000`

## Access Points

Once all services are running:

- **Frontend UI:** http://localhost:3000
- **API Swagger:** http://localhost:8081/swagger-ui.html
- **API Base:** http://localhost:8081/api/v1
- **Blockchain Explorer:** (if enabled) Check network logs

## Stop All Projects

```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source"
./stop-all-projects.sh
```

Or manually:
- **Frontend:** `Ctrl+C` in frontend terminal
- **API:** `Ctrl+C` in API terminal  
- **Network:** `cd ownership-network-master && ./net.sh down`

## Troubleshooting

### Port Already in Use

```bash
# Check what's using the ports
lsof -i :8081  # API
lsof -i :3000  # Frontend

# Kill processes if needed
kill -9 <PID>
```

### Blockchain Network Issues

```bash
cd ownership-network-master

# Check status
./net.sh status

# View logs
./net.sh logs

# Reset network (clean start)
./net.sh reset
./net.sh up
```

### API Won't Start

```bash
cd ownership-api-master

# Check logs
tail -f api.log

# Verify database connection
export SPRING_DATASOURCE_PASSWORD=your_password
mvn spring-boot:run
```

### Frontend Won't Start

```bash
cd ownership-ui-master

# Clear cache and reinstall
rm -rf node_modules .next
npm install
npm run dev
```

## Prerequisites Check

Before starting, ensure:

- ✅ Docker is running
- ✅ PostgreSQL is accessible (if using external DB)
- ✅ Ports 3000, 8081 are available
- ✅ Node.js >= 18.0.0 installed
- ✅ Java 17+ installed
- ✅ Maven installed

## Expected Startup Time

- **Blockchain Network:** 2-5 minutes (first time)
- **API Backend:** 30-60 seconds
- **Frontend:** 10-20 seconds

Total: ~3-7 minutes for first startup

## Verification

After all services start:

1. **Check Blockchain:**
   ```bash
   cd ownership-network-master
   ./net.sh status
   ```

2. **Check API:**
   ```bash
   curl http://localhost:8081/swagger-ui.html
   ```

3. **Check Frontend:**
   ```bash
   curl http://localhost:3000
   ```

## Next Steps

Once everything is running:

1. Open frontend: http://localhost:3000
2. Login with your credentials
3. Test new features:
   - Verification endpoints
   - Asset transfer with ownership validation
   - Rate limiting
   - Caching
