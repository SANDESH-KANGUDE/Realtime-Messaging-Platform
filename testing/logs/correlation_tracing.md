# Correlation ID Tracing Diagnostics

To trace a single request's lifecycle across our distributed microservices, we inject and propagate a unique `X-Correlation-ID` header. 

Here is how to verify that tracing works successfully across the **Gateway, Auth, and User services**.

---

## 1. Retrieve Correlation ID from API Response Headers
When you call any API via the API Gateway (`http://localhost:8080`), the response contains an `X-Correlation-ID` header.
* Example Response Header in Postman:
  ```http
  X-Correlation-ID: corr_12345678-abcd-ef01-2345-6789abcdef01
  ```

---

## 2. Inspect Correlation ID in Service Logs
When running the microservices, their logs are output to stdout (or specific log files). Because we configure standard JSON logging, each log line embeds the correlation ID context.

To verify correlation ID consistency, run the following commands while running the services:

### A. Trace Gateway Service Logs
Look for the Correlation ID in Gateway logs during routing:
```powershell
# If checking live stdout logs in your Gateway shell
# Look for the correlation ID:
# "gateway-service [corr_...] Incoming request /api/v1/users/me"
```

### B. Trace Auth Service Logs
Verify that the `X-Correlation-ID` was successfully propagated via WebClient/Feign or request headers to the Auth Service:
```powershell
# Look for:
# "auth-service [corr_...] Validating token for user A"
```

### C. Trace User Service Logs
Verify the User Service logged the request under the exact same correlation context:
```powershell
# Look for:
# "user-service [corr_...] Fetching user profile for UUID"
```

If the logs in all three services show the **exact same Correlation ID** value, your tracing is verified and 100% correct!
