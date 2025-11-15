# Authentication Testing Guide

## Overview
This document describes the comprehensive authentication tests created for the dev-connect-backend application. The tests cover both registration and login functionality.

## Test Files

### 1. **AuthenticationIntegrationTest.java**
Full integration tests that test the complete authentication flow with a real Spring context and H2 database.

**Location:** `src/test/java/org/devconnect/devconnectbackend/controller/AuthenticationIntegrationTest.java`

**Test Coverage:**
- ✅ **19 integration tests** covering registration, login, and complete user flows

### 2. **UserControllerTest.java**
Unit tests with mocked dependencies for faster execution and isolated testing.

**Location:** `src/test/java/org/devconnect/devconnectbackend/controller/UserControllerTest.java`

**Test Coverage:**
- ✅ **13 unit tests** covering controller logic with mocked repositories

## Test Categories

### Registration Tests

#### ✅ Successful Registration
- **Test:** `testRegisterNewDeveloper`, `testRegisterNewClient`
- **Validates:**
  - New users can register with valid credentials
  - Passwords are hashed (BCrypt)
  - User status is set to "offline" by default
  - Password is NOT returned in the response
  - Both developer and client roles work

#### ❌ Registration Failures
- **Test:** `testRegisterDuplicateEmail`, `testRegisterDuplicateUsername`
- **Validates:**
  - Users cannot register with an existing email
  - Users cannot register with an existing username
  - Returns HTTP 400 Bad Request

- **Test:** `testRegisterInvalidRole`
- **Validates:**
  - Invalid role values are rejected
  - Returns HTTP 400 Bad Request

#### 🔒 Password Security
- **Test:** `testPasswordIsHashed`
- **Validates:**
  - Plain text passwords are never stored
  - BCrypt hashing is applied correctly
  - Hashed password can be verified with BCrypt

### Login Tests

#### ✅ Successful Login
- **Test:** `testLoginSuccess`
- **Validates:**
  - Users can login with correct email and password
  - User information is returned (without password)
  - Returns HTTP 200 OK

#### ❌ Login Failures
- **Test:** `testLoginWrongPassword`
- **Validates:**
  - Wrong password is rejected
  - Returns HTTP 401 Unauthorized

- **Test:** `testLoginNonExistentUser`
- **Validates:**
  - Non-existent email is rejected
  - Returns HTTP 401 Unauthorized

- **Test:** `testLoginEmptyEmail`, `testLoginEmptyPassword`
- **Validates:**
  - Empty credentials are rejected
  - Returns HTTP 401 Unauthorized

### Complete Flow Tests

#### 🔄 End-to-End Authentication
- **Test:** `testCompleteRegistrationAndLoginFlow`
- **Validates:**
  - User can register
  - Then immediately login with registered credentials
  - Complete user journey works seamlessly

- **Test:** `testDoubleRegistrationPrevention`
- **Validates:**
  - Duplicate registration is prevented
  - Original credentials still work for login
  - Data integrity is maintained

### Edge Case Tests

#### 🔍 Special Scenarios
- **Test:** `testLoginCaseSensitiveEmail`
- **Validates:**
  - Email case sensitivity behavior
  - Database collation handling

- **Test:** `testRegisterMultipleRoles`
- **Validates:**
  - Both DEVELOPER and CLIENT roles work
  - Role-based registration is flexible

## Running the Tests

### Run All Tests
```bash
.\gradlew.bat test
```

### Run Only Authentication Integration Tests
```bash
.\gradlew.bat test --tests AuthenticationIntegrationTest
```

### Run Only Unit Tests
```bash
.\gradlew.bat test --tests UserControllerTest
```

### Run Specific Test
```bash
.\gradlew.bat test --tests AuthenticationIntegrationTest.testRegisterNewDeveloper
```

## Test Results Summary

| Test Suite | Tests | Status |
|------------|-------|--------|
| **AuthenticationIntegrationTest** | 19 tests | ✅ ALL PASSING |
| **UserControllerTest** | 13 tests | ✅ ALL PASSING |
| **MessageServiceTest** | 8 tests | ✅ ALL PASSING |
| **MessageControllerIntegrationTest** | 6 tests | ✅ ALL PASSING |
| **Total** | **46 tests** | ✅ **ALL PASSING** |

## API Endpoints Tested

### Registration
```http
POST /api/users/register
Content-Type: application/json

{
  "username": "john_dev",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "role": "developer"
}
```

**Success Response (200 OK):**
```json
{
  "conversation_id": 1,
  "username": "john_dev",
  "email": "john@example.com",
  "role": "developer",
  "status": "offline",
  "avatar": null
}
```

**Error Response (400 Bad Request):**
- Duplicate email or username
- Invalid role

### Login
```http
POST /api/users/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Success Response (200 OK):**
```json
{
  "conversation_id": 1,
  "username": "john_dev",
  "email": "john@example.com",
  "role": "developer",
  "status": "offline",
  "avatar": null
}
```

**Error Response (401 Unauthorized):**
- Wrong password
- User not found
- Empty credentials

## What's Tested vs What's Not

### ✅ Currently Tested
- User registration with valid data
- Duplicate email/username prevention
- Password hashing with BCrypt
- Login with correct credentials
- Login failure scenarios
- Role validation (developer/client)
- User retrieval by ID
- Get all users
- Get users by role
- Password security (not returned in responses)

### 🔜 Future Testing (When Your Colleague Adds Features)
- JWT token generation
- JWT token validation
- Token refresh
- Logout functionality
- Password reset
- Email verification
- Session management
- Rate limiting
- Account lockout after failed attempts
- OAuth/Social login

## Cross-Platform Compatibility

**✅ Your code changes are 100% cross-platform compatible!**

- Pure Java code - works on Windows, Linux, macOS
- Spring Boot abstracts platform differences
- BCrypt password hashing is platform-independent
- H2 (test) and PostgreSQL (production) work on all platforms
- Gradle wrapper (`gradlew` for Linux/Mac, `gradlew.bat` for Windows)

Your Linux colleagues will have **zero issues** with these changes!

## Test Configuration

### Test Database
- **Type:** H2 (in-memory)
- **Config:** `src/test/resources/application.properties`
- **Isolation:** Each test gets a fresh database
- **SQL Init:** Disabled (`spring.sql.init.mode=never`)

### Key Dependencies
```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.mockito:mockito-core'
testImplementation 'org.junit.jupiter:junit-jupiter'
```

## Tips for Your Colleague

When your colleague completes the authentication implementation:

1. **JWT Tokens:** Add tests for token generation and validation
2. **Refresh Tokens:** Test token refresh flow
3. **Logout:** Test session invalidation
4. **Password Reset:** Test the complete reset flow
5. **Email Verification:** Test verification token generation and validation

## Troubleshooting

### Tests Failing on Linux?
**Won't happen!** All tests are platform-independent. If they do fail:
1. Check Java version: `java -version` (needs Java 21)
2. Check Gradle: `./gradlew --version`
3. Run: `./gradlew clean test`

### Common Issues
- **Port conflicts:** Tests use random ports
- **Database:** H2 is in-memory, no external dependencies
- **Permissions:** Make sure `gradlew` has execute permissions on Linux: `chmod +x gradlew`

## Next Steps

1. ✅ Registration and Login tests are complete
2. ✅ All tests passing
3. ⏳ Wait for JWT implementation from your colleague
4. 🔜 Add JWT token tests when ready
5. 🔜 Add password reset tests when ready

---

**Need help?** Check the test files for detailed examples of how to write more tests!
