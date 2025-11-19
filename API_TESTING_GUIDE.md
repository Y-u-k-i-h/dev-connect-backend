# API Testing Guide - How to Access Protected Endpoints

Your API uses **JWT (JSON Web Token) authentication**. Most endpoints require a valid token.

## Step 1: Register a New User

**Endpoint:** `POST http://localhost:8081/api/users/register`

**Request Body:**
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Password123!",
  "role": "DEVELOPER"
}
```

**Example with curl:**
```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"Password123!\",\"role\":\"DEVELOPER\"}"
```

**Expected Response (201 Created):**
```json
{
  "userId": 1,
  "username": "testuser",
  "email": "test@example.com",
  "role": "DEVELOPER",
  "status": "OFFLINE"
}
```

---

## Step 2: Login to Get JWT Token

**Endpoint:** `POST http://localhost:8081/api/users/login`

**Request Body:**
```json
{
  "email": "test@example.com",
  "password": "Password123!"
}
```

**Example with curl:**
```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@example.com\",\"password\":\"Password123!\"}"
```

**Expected Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "userId": 1,
    "username": "testuser",
    "email": "test@example.com",
    "role": "DEVELOPER",
    "status": "OFFLINE"
  }
}
```

**IMPORTANT:** Copy the `accessToken` value - you'll need it for all protected endpoints!

---

## Step 3: Access Protected Endpoints

Now use the token in the `Authorization` header for all other requests.

### Example: Get All Users

**Endpoint:** `GET http://localhost:8081/api/users`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Example with curl:**
```bash
curl -X GET http://localhost:8081/api/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Example: Get User by ID

**Endpoint:** `GET http://localhost:8081/api/users/1`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

---

## Testing with Postman

### 1. Register User
- Method: `POST`
- URL: `http://localhost:8081/api/users/register`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Password123!",
  "role": "DEVELOPER"
}
```

### 2. Login
- Method: `POST`
- URL: `http://localhost:8081/api/users/login`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "email": "test@example.com",
  "password": "Password123!"
}
```
- Copy the `accessToken` from the response

### 3. Access Protected Endpoint
- Method: `GET`
- URL: `http://localhost:8081/api/users`
- Headers:
  - `Content-Type: application/json`
  - `Authorization: Bearer YOUR_ACCESS_TOKEN`
  
  (Replace `YOUR_ACCESS_TOKEN` with the token from login response)

---

## Public Endpoints (No Token Required)

These endpoints don't need authentication:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/users/register` | POST | Register new user |
| `/api/users/login` | POST | Login user |
| `/api/users/refresh` | POST | Refresh access token |
| `/api/users/exists/{email}` | GET | Check if email exists |

---

## Protected Endpoints (Require Token)

All other endpoints need the JWT token in the `Authorization` header:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/users` | GET | Get all users |
| `/api/users/{id}` | GET | Get user by ID |
| `/api/users/{id}` | PUT | Update user |
| `/api/users/{id}` | DELETE | Delete user |
| `/api/messages/**` | ALL | All messaging endpoints |

---

## Common Issues

### ❌ 401 Unauthorized
**Cause:** Missing or invalid JWT token
**Solution:** Make sure you're sending the token in the `Authorization` header:
```
Authorization: Bearer YOUR_TOKEN_HERE
```

### ❌ 403 Forbidden
**Cause:** Token is valid but you don't have permission
**Solution:** Check your user role and permissions

### ❌ Token Expired
**Cause:** Access token expired (15 minutes by default)
**Solution:** Use the refresh token to get a new access token:
```bash
POST http://localhost:8081/api/users/refresh
{
  "refreshToken": "YOUR_REFRESH_TOKEN"
}
```

---

## Quick Test Script (PowerShell)

```powershell
# 1. Register
$registerBody = @{
    username = "testuser"
    email = "test@example.com"
    password = "Password123!"
    role = "DEVELOPER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/users/register" `
  -Method POST `
  -ContentType "application/json" `
  -Body $registerBody

# 2. Login
$loginBody = @{
    email = "test@example.com"
    password = "Password123!"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8081/api/users/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body $loginBody

$token = $response.accessToken
Write-Host "Token: $token"

# 3. Get all users (with token)
$headers = @{
    Authorization = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8081/api/users" `
  -Method GET `
  -Headers $headers
```

---

## Token Information

- **Access Token Expiration:** 15 minutes (900000 ms)
- **Refresh Token Expiration:** 30 days (2592000000 ms)
- **Token Type:** JWT (JSON Web Token)
- **Algorithm:** RS256 (RSA Signature)

When the access token expires, use the refresh token to get a new one without logging in again.

---

**Need Help?** 
- Make sure the server is running: `.\gradlew.bat bootRun`
- Check server is on: http://localhost:8081
- All requests need `Content-Type: application/json` header
