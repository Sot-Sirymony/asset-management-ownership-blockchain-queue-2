# Next Steps After Implementation

## 1. Environment Setup

### Set Environment Variables
Create a `.env` file or set environment variables for sensitive data:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/asset_holder_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your_secure_password
```

Or use the `.env.example` file as a template.

## 2. Build and Test

### Build the Application
```bash
cd ownership-api-master
mvn clean install
```

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests (requires blockchain network)
```bash
mvn test -Dtest=BlockchainIntegrationTest
```

## 3. Start the Application

### Start Database
Ensure PostgreSQL is running:
```bash
# Using Docker
docker run -d --name postgres \
  -e POSTGRES_PASSWORD=your_password \
  -e POSTGRES_DB=asset_holder_db \
  -p 5432:5432 \
  postgres:latest
```

### Start Blockchain Network
Ensure your Hyperledger Fabric network is running:
```bash
cd ownership-network-master
# Follow your network startup scripts
```

### Start the API
```bash
cd ownership-api-master
mvn spring-boot:run
```

Or with environment variables:
```bash
SPRING_DATASOURCE_PASSWORD=your_password mvn spring-boot:run
```

## 4. Verify Implementation

### Test Rate Limiting
```bash
# Make 101 requests quickly to test rate limiting
for i in {1..101}; do
  curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8081/api/v1/user/getAllAsset
done
# Should get 429 Too Many Requests after 100 requests
```

### Test New Endpoints

#### Verification Endpoints
```bash
# Internal verification
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8081/api/v1/user/verifyAsset/asset-123

# External verification
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8081/api/v1/user/verifyAssetExternal/asset-123

# Verification trail
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8081/api/v1/user/verificationTrail/asset-123
```

#### Test Asset Transfer with Ownership Validation
```bash
# Should fail if user doesn't own the asset
curl -X PUT \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"newAssignTo": 2}' \
  http://localhost:8081/api/v1/admin/transferAsset/asset-123
```

### Test Caching
```bash
# First request - should hit database
time curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8081/api/v1/admin/department?page=1&size=10

# Second request - should be faster (cached)
time curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8081/api/v1/admin/department?page=1&size=10
```

## 5. Review Changes

### Check Logs
Monitor application logs for:
- Asset ID generation (should see UUID-based IDs)
- Transfer notifications
- Rate limiting warnings
- Cache hits/misses

### API Documentation
Visit Swagger UI to see updated endpoints:
```
http://localhost:8081/swagger-ui.html
```

## 6. Additional Improvements (Optional)

### Email Notifications
To enable email notifications, update `NotificationServiceImp.java`:
1. Add Spring Mail dependency to `pom.xml`
2. Configure SMTP in `application.properties`
3. Uncomment email sending code in `NotificationServiceImp`

### Enhanced Monitoring
Consider adding:
- Actuator endpoints for health checks
- Prometheus metrics
- Distributed tracing (Jaeger/Zipkin)

### Database Migrations
Consider using Flyway or Liquibase for database schema management.

## 7. Deployment Checklist

- [ ] All environment variables configured
- [ ] Database migrations applied
- [ ] Blockchain network configured and running
- [ ] Rate limiting tested
- [ ] Security headers verified
- [ ] Caching working correctly
- [ ] Error handling tested
- [ ] API documentation updated
- [ ] Logging configured properly
- [ ] Backup strategy in place

## 8. Documentation Updates

Update your project documentation:
- API documentation (Swagger)
- Deployment guide
- Environment setup guide
- Architecture documentation

## Troubleshooting

### Rate Limiting Not Working
- Check that `RateLimitConfig` bean is created
- Verify interceptor is registered in `WebConfig`
- Check logs for rate limit messages

### Caching Not Working
- Verify `@EnableCaching` is present
- Check cache configuration in `CacheConfig`
- Ensure `@Cacheable` annotations are correct

### Transfer Fails
- Verify user owns the asset
- Check blockchain network is running
- Review logs for detailed error messages

### Database Connection Issues
- Verify environment variables are set
- Check PostgreSQL is running
- Verify connection string is correct
