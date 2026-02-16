# 🎯 Immediate Next Step

## Right Now: Set Environment Variable and Build

### Step 1: Set Your Database Password

Open a terminal and run:

```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source/ownership-api-master"

# Set your actual database password (replace with your real password)
export SPRING_DATASOURCE_PASSWORD=Mony@1144

# Verify it's set
echo $SPRING_DATASOURCE_PASSWORD
```

**Note:** The default password in your `application.properties` is `Mony@1144`. If this is correct, you can skip this step for now, but it's better to use environment variables.

### Step 2: Build the Application

```bash
# Still in the ownership-api-master directory
mvn clean compile -DskipTests
```

**Expected output:** `BUILD SUCCESS`

### Step 3: Check if Prerequisites are Running

Before starting the application, verify:

```bash
# Check if PostgreSQL is accessible
# (Adjust host/port based on your setup)
ping -c 1 host.docker.internal

# Check if port 8081 is available
lsof -i :8081 || echo "Port 8081 is available"
```

### Step 4: Start the Application

```bash
# Start Spring Boot application
mvn spring-boot:run
```

**What to look for:**
- ✅ `Started ArtifactsSdkApplication` in logs
- ✅ `RateLimitConfig` initialized
- ✅ `CacheConfig` initialized
- ✅ No database connection errors

### Step 5: Quick Verification

Once the app is running, open a new terminal and test:

```bash
# Test if API is responding
curl http://localhost:8081/swagger-ui.html

# Or open in browser:
# http://localhost:8081/swagger-ui.html
```

## 🚨 If You Encounter Issues

### Build Fails?
```bash
# Clear Maven cache and retry
mvn clean install -U -DskipTests
```

### Database Connection Error?
- Check PostgreSQL is running
- Verify password is correct
- Check connection string in `application.properties`

### Port Already in Use?
```bash
# Find what's using port 8081
lsof -i :8081

# Kill the process or change port in application.properties
```

## ✅ Success Checklist

After starting the application, verify:

- [ ] Application starts without errors
- [ ] Swagger UI accessible at `http://localhost:8081/swagger-ui.html`
- [ ] No errors in console logs
- [ ] Can see new endpoints in Swagger:
  - `/api/v1/user/verifyAsset/{id}`
  - `/api/v1/user/verifyAssetExternal/{id}`
  - `/api/v1/user/verificationTrail/{id}`

## 📋 After Application Starts

Once running, test the new features:

1. **Login to get token:**
   ```bash
   curl -X POST http://localhost:8081/rest/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"your_username","password":"your_password"}'
   ```

2. **Test rate limiting** (make 101 requests)
3. **Test verification endpoints**
4. **Test asset transfer with ownership validation**

## 🎯 Your Immediate Action

**Run these commands now:**

```bash
cd "/Users/sotsirymony/Desktop/Block Chain Assignment/All In One Source/ownership-api-master"
export SPRING_DATASOURCE_PASSWORD=Mony@1144
mvn clean compile -DskipTests
```

If build succeeds, then:
```bash
mvn spring-boot:run
```

That's it! Start with these commands and let me know if you encounter any issues.
