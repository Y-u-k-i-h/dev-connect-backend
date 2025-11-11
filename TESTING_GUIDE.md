# Messaging System Testing Guide

This guide explains how to test the messaging functionality in the dev-connect-backend application.

## 📋 Test Files Created

### 1. **Unit Tests** - `MessageServiceTest.java`
Tests the business logic of the messaging service in isolation using mocks.

**What it tests:**
- ✅ Sending messages successfully
- ✅ Handling sender/receiver not found errors
- ✅ Retrieving conversation history
- ✅ Marking messages as read
- ✅ Marking messages as delivered
- ✅ WebSocket notifications

**Run this test:**
```bash
# Windows
.\gradlew.bat test --tests MessageServiceTest

# Linux/Mac
./gradlew test --tests MessageServiceTest
```

### 2. **Integration Tests** - `MessageControllerIntegrationTest.java`
Tests the REST API endpoints with a real Spring context and database.

**What it tests:**
- ✅ POST /api/messages/send - Send message via REST
- ✅ GET /api/messages/conversation - Get message history
- ✅ PUT /api/messages/read - Mark messages as read
- ✅ PUT /api/messages/status/{userId} - Update user status
- ✅ GET /api/messages/status/{userId} - Get user status
- ✅ Error handling for invalid users

**Run this test:**
```bash
# Windows
.\gradlew.bat test --tests MessageControllerIntegrationTest

# Linux/Mac
./gradlew test --tests MessageControllerIntegrationTest
```

### 3. **WebSocket Tests** - `WebSocketMessagingIntegrationTest.java`
Tests real-time WebSocket connections and messaging.

**What it tests:**
- ✅ WebSocket connection establishment
- ✅ Receiving messages via WebSocket
- ✅ Multiple concurrent connections
- ✅ Error handling

**Run this test:**
```bash
# Windows
.\gradlew.bat test --tests WebSocketMessagingIntegrationTest

# Linux/Mac
./gradlew test --tests WebSocketMessagingIntegrationTest
```

## 🚀 Running All Tests

### Run All Tests
```bash
# Windows
.\gradlew.bat test

# Linux/Mac
./gradlew test
```

### Run All Messaging Tests
```bash
# Windows
.\gradlew.bat test --tests "*Message*"

# Linux/Mac
./gradlew test --tests "*Message*"
```

### Run Tests with Detailed Output
```bash
# Windows
.\gradlew.bat test --info

# Linux/Mac
./gradlew test --info
```

### Generate Test Report
```bash
# Windows
.\gradlew.bat test

# Linux/Mac
./gradlew test
```

Then open: `build/reports/tests/test/index.html` in your browser

## 🧪 Manual Testing the REST API

### 1. Start the Application
```bash
# Windows
.\gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

### 2. Test Endpoints with cURL

#### Create Test Users (First time)
```bash
# Create sender (client)
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testclient",
    "email": "client@test.com",
    "password": "password123",
    "role": "CLIENT"
  }'

# Create receiver (developer)
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testdev",
    "email": "dev@test.com",
    "password": "password123",
    "role": "DEVELOPER"
  }'
```

#### Send a Message
```bash
curl -X POST http://localhost:8081/api/messages/send \
  -H "Content-Type: application/json" \
  -d '{
    "senderId": 1,
    "receiverId": 2,
    "text": "Hello, how can I help you?",
    "projectId": 101
  }'
```

#### Get Conversation
```bash
curl "http://localhost:8081/api/messages/conversation?userId1=1&userId2=2"
```

#### Mark Messages as Read
```bash
curl -X PUT "http://localhost:8081/api/messages/read?senderId=1&receiverId=2"
```

#### Update User Status
```bash
curl -X PUT "http://localhost:8081/api/messages/status/1?status=ONLINE"
```

#### Get User Status
```bash
curl "http://localhost:8081/api/messages/status/1"
```

## 🌐 Testing WebSocket (Advanced)

### Using JavaScript Client (Browser Console)

1. Open your browser's developer console
2. Run this code:

```javascript
// Connect to WebSocket
const socket = new SockJS('http://localhost:8081/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to receive messages
    stompClient.subscribe('/user/queue/messages', function(message) {
        console.log('Received:', JSON.parse(message.body));
    });
    
    // Send a message
    stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
        senderId: 1,
        receiverId: 2,
        text: 'WebSocket test message',
        projectId: 101
    }));
});
```

**Note**: Include SockJS and STOMP libraries:
```html
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2/lib/stomp.min.js"></script>
```

## 📊 Test Coverage

To see code coverage:

```bash
# Windows
.\gradlew.bat test jacocoTestReport

# Linux/Mac
./gradlew test jacocoTestReport
```

Open: `build/reports/jacoco/test/html/index.html`

## 🐛 Debugging Failed Tests

### 1. Check Test Logs
```bash
# Windows
type build\test-results\test\*.xml

# Linux/Mac
cat build/test-results/test/*.xml
```

### 2. Enable Debug Logging
Add to `src/test/resources/application.properties`:
```properties
logging.level.org.devconnect.devconnectbackend=DEBUG
logging.level.org.springframework.messaging=DEBUG
logging.level.org.springframework.web.socket=DEBUG
```

### 3. Run Single Test with Debug
```bash
# Windows
.\gradlew.bat test --tests MessageServiceTest.testSendMessage --debug

# Linux/Mac
./gradlew test --tests MessageServiceTest.testSendMessage --debug
```

## ✅ Expected Test Results

### Successful Test Run
```
BUILD SUCCESSFUL in 15s
9 actionable tasks: 9 executed

> Task :test
MessageServiceTest > Should send message successfully PASSED
MessageServiceTest > Should throw exception when sender not found PASSED
MessageServiceTest > Should get messages between users PASSED
MessageServiceTest > Should mark messages as read PASSED
MessageControllerIntegrationTest > Should send message via REST endpoint PASSED
MessageControllerIntegrationTest > Should get conversation between users PASSED
WebSocketMessagingIntegrationTest > Should connect to WebSocket successfully PASSED
```

## 🔍 Testing Best Practices

### 1. Always Run Tests Before Committing
```bash
git add .
./gradlew test  # or .\gradlew.bat test on Windows
git commit -m "Your commit message"
```

### 2. Test on Your Target Platform
If your team uses Linux servers, test there too:
```bash
# On Linux
./gradlew clean build test
```

### 3. Use Continuous Integration
Consider setting up GitHub Actions to automatically run tests on push.

## 📝 Adding New Tests

### Example: Add a New Service Test
```java
@Test
@DisplayName("Should handle your new feature")
void testNewFeature() {
    // Arrange
    // ... setup test data
    
    // Act
    // ... call your method
    
    // Assert
    assertEquals(expected, actual);
    verify(mockRepository, times(1)).save(any());
}
```

### Example: Add a New Controller Test
```java
@Test
@DisplayName("Should test your new endpoint")
void testNewEndpoint() throws Exception {
    mockMvc.perform(get("/api/messages/your-endpoint"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.field").value("expectedValue"));
}
```

## 🆘 Common Issues & Solutions

### Issue: Tests fail with "Port already in use"
**Solution**: Stop any running instances of the application
```bash
# Windows
taskkill /F /IM java.exe

# Linux/Mac
pkill -9 java
```

### Issue: "Connection refused" in WebSocket tests
**Solution**: Ensure the test is using `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`

### Issue: "H2 database locked"
**Solution**: Tests are interfering. Use `@Transactional` on test classes to auto-rollback.

### Issue: Different results on Windows vs Linux
**Solution**: Check CROSS_PLATFORM_COMPATIBILITY.md - likely a line ending issue.

## 📚 Additional Resources

- [Spring Boot Testing Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring WebSocket Testing](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket-stomp-enable)

## 🎯 Quick Test Checklist

Before pushing your changes:

- [ ] Run `./gradlew test` - All tests pass
- [ ] Run `./gradlew build` - Build succeeds
- [ ] Check test report in `build/reports/tests/test/index.html`
- [ ] No new warnings or errors in logs
- [ ] Manual smoke test of key endpoints
- [ ] Git diff reviewed for unintended changes

---

**Happy Testing! 🚀**

If you encounter any issues, check the logs in `build/test-results/` or ask your team for help.
