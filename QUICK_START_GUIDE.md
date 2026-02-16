# Quick Start Guide - Next Steps

## 🚀 Immediate Actions

### Step 1: Set Up Environment Variables

Create a `.env` file or export environment variables:

```bash
# Option 1: Export in terminal (temporary)
export SPRING_DATASOURCE_PASSWORD=your_actual_password
export SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/asset_holder_db
export SPRING_DATASOURCE_USERNAME=postgres

# Option 2: Create .env file (recommended for development)
cd ownership-api-master
cat > .env << EOF
SPRING_DATASOURCE_PASSWORD=your_actual_password
SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/asset_holder_db
SPRING_DATASOURCE_USERNAME=postgres
EOF
```

### Step 2: Verify Prerequisites

Check that required services are running:

```bash
# Check PostgreSQL
psql -h host.docker.internal -U postgres -d asset_holder_db -c "SELECT 1;"

# Check if blockchain network is accessible
# (Adjust based on your network setup)
```

### Step 3: Build the Application

```bash
cd ownership-api-master

# Clean and build (skip tests if blockchain network not available)
mvn clean install -DskipTests

# Or run tests if network is available
mvn clean test
```

### Step 4: Start the Application

```bash
# With environment variables set
mvn spring-boot:run

# Or with explicit environment variable
SPRING_DATASOURCE_PASSWORD=your_password mvn spring-boot:run
```

### Step 5: Verify Application Started

Check logs for:
- ✅ "Started ArtifactsSdkApplication"
- ✅ No errors about database connection
- ✅ Rate limiting initialized
- ✅ Cache manager initialized

Access Swagger UI:
```
http://localhost:8081/swagger-ui.html
```

## 🧪 Testing Checklist

### Test 1: Rate Limiting
```bash
# Get a JWT token first (login)
TOKEN=$(curl -X POST http://localhost:8081/rest/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"adminpw"}' | jq -r '.payload.token')

# Make 101 rapid requests
for i in {1..101}; do
  curl -H "Authorization: Bearer $TOKEN" \
    http://localhost:8081/api/v1/user/getAllAsset \
    -w "\nStatus: %{http_code}\n"
done
# Should see 429 Too Many Requests after 100 requests
```

### Test 2: Asset Transfer Ownership Validation
```bash
# Create asset as User A
curl -X POST http://localhost:8081/api/v1/admin/createAsset \
  -H "Authorization: Bearer $TOKEN_A" \
  -H "Content-Type: application/json" \
  -d '{
    "assetName": "Test Asset",
    "qty": "1",
    "unit": "piece",
    "condition": "New",
    "assignTo": 1
  }'

# Try to transfer as User B (should fail)
curl -X PUT http://localhost:8081/api/v1/admin/transferAsset/asset-123 \
  -H "Authorization: Bearer $TOKEN_B" \
  -H "Content-Type: application/json" \
  -d '{"newAssignTo": 2}'
# Should return error: "You do not have permission to transfer this asset"
```

### Test 3: Verification Endpoints
```bash
# Internal verification
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/v1/user/verifyAsset/asset-123

# External verification
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/v1/user/verifyAssetExternal/asset-123

# Verification trail
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/v1/user/verificationTrail/asset-123
```

### Test 4: Caching
```bash
# First request (hits database)
time curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8081/api/v1/admin/department?page=1&size=10"

# Second request (should be faster - cached)
time curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8081/api/v1/admin/department?page=1&size=10"
```

### Test 5: Check Logs for Debug Code
```bash
# Check application logs - should see proper log levels
tail -f logs/application.log | grep -E "INFO|WARN|ERROR|DEBUG"

# Verify no System.out.println in logs
tail -f logs/application.log | grep -i "system.out"
# Should return no results
```

### Test 6: Asset ID Generation
```bash
# Create multiple assets and verify UUID-based IDs
for i in {1..5}; do
  curl -X POST http://localhost:8081/api/v1/admin/createAsset \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "assetName": "Asset '$i'",
      "qty": "1",
      "unit": "piece",
      "condition": "New",
      "assignTo": 1
    }' | jq '.payload.asset_id'
done
# Should see UUID-based IDs like "asset-abc123def456..."
```

## 📊 Monitoring & Verification

### Check Application Health
```bash
# View application logs
tail -f ownership-api-master/logs/application.log

# Or if using console output
# Look for:
# - Rate limit warnings
# - Cache statistics
# - Asset creation/transfer logs
# - Notification logs
```

### Verify Features in Swagger UI
1. Open: `http://localhost:8081/swagger-ui.html`
2. Authenticate with JWT token
3. Test endpoints:
   - `/api/v1/user/verifyAsset/{id}` - New verification endpoint
   - `/api/v1/admin/transferAsset/{id}` - Transfer with ownership validation
   - `/api/v1/admin/department` - Cached endpoint

## 🔧 Troubleshooting

### Build Fails
```bash
# Clear Maven cache and rebuild
rm -rf ~/.m2/repository/com/hrd
mvn clean install -U
```

### Database Connection Error
```bash
# Verify PostgreSQL is running
docker ps | grep postgres

# Check connection string
echo $SPRING_DATASOURCE_URL

# Test connection manually
psql -h host.docker.internal -U postgres -d asset_holder_db
```

### Rate Limiting Not Working
- Check `RateLimitConfig` bean is created (check logs)
- Verify interceptor is registered in `WebConfig`
- Check that requests are hitting `/api/**` paths

### Caching Not Working
- Verify `@EnableCaching` is present
- Check cache statistics in logs
- Ensure `@Cacheable` annotation is correct

## 📝 Next Improvements (Optional)

### 1. Enable Email Notifications
Update `NotificationServiceImp.java`:
- Add Spring Mail dependency
- Configure SMTP in `application.properties`
- Uncomment email sending code

### 2. Add Monitoring
- Spring Boot Actuator for health checks
- Prometheus metrics
- Distributed tracing

### 3. Production Deployment
- Use environment-specific profiles
- Set up CI/CD pipeline
- Configure production database
- Set up monitoring and alerting

### 4. Frontend Integration
Update frontend to use:
- New verification endpoints
- Improved error messages
- Rate limit handling (429 responses)

## ✅ Success Criteria

You'll know everything is working when:
- ✅ Application starts without errors
- ✅ Rate limiting returns 429 after 100 requests
- ✅ Asset transfer validates ownership correctly
- ✅ Verification endpoints return complete data
- ✅ Caching improves response times
- ✅ Logs show proper logging (no debug statements)
- ✅ Asset IDs are UUID-based

## 🆘 Need Help?

Check these files:
- `VERIFICATION_CHECKLIST.md` - Detailed verification steps
- `IMPLEMENTATION_SUMMARY.md` - Complete implementation summary
- `NEXT_STEPS.md` - Extended next steps guide
