# Messaging System Documentation

## Overview
This messaging system provides real-time chat functionality between Clients and Developers in the DevConnect platform using WebSocket (STOMP protocol).

## Architecture

### Models
- **User**: Represents a user (CLIENT or DEVELOPER) with online/offline status
- **Message**: Individual chat messages with read receipts
- **Chat**: Conversation thread between two users linked to a project

### Key Features
- ✅ Real-time messaging via WebSocket
- ✅ Message persistence in PostgreSQL
- ✅ Read receipts (sent, delivered, read)
- ✅ Typing indicators
- ✅ Online/offline status
- ✅ Unread message counts
- ✅ Project-based chat relationships

## API Endpoints

### REST Endpoints

#### 1. Get User Chats
```http
GET /api/messages/chats/{userId}
```
Returns list of all chats for a user with unread counts.

#### 2. Get Conversation
```http
GET /api/messages/conversation?userId1={id1}&userId2={id2}
```
Returns all messages between two users.

#### 3. Send Message (HTTP)
```http
POST /api/messages/send
Content-Type: application/json

{
  "senderId": 1,
  "receiverId": 2,
  "text": "Hello!",
  "projectId": 10
}
```

#### 4. Mark Messages as Read
```http
PUT /api/messages/read?senderId={id1}&receiverId={id2}
```

#### 5. Update User Status
```http
PUT /api/messages/status/{userId}?status=ONLINE
```

#### 6. Get User Status
```http
GET /api/messages/status/{userId}
```

### WebSocket Endpoints

#### Connection
Connect to: `ws://localhost:8080/ws`

#### Subscribe to Queues (Private)
```javascript
// Receive messages
stompClient.subscribe('/user/queue/messages', callback);

// Receive typing indicators
stompClient.subscribe('/user/queue/typing', callback);

// Receive read receipts
stompClient.subscribe('/user/queue/read-receipts', callback);

// Receive delivery receipts
stompClient.subscribe('/user/queue/delivery-receipts', callback);
```

#### Subscribe to Topics (Public)
```javascript
// User status updates (online/offline)
stompClient.subscribe('/topic/user-status', callback);
```

#### Send Messages
```javascript
// Send a chat message
stompClient.send('/app/chat', {}, JSON.stringify({
  senderId: 1,
  receiverId: 2,
  text: "Hello!",
  projectId: 10
}));

// Send typing indicator
stompClient.send('/app/typing', {}, JSON.stringify({
  senderId: 1,
  receiverId: 2,
  isTyping: true
}));

// Mark message as delivered
stompClient.send('/app/message-delivered', {}, JSON.stringify({
  conversation_id: 123,
  receiverId: 2
}));

// Mark messages as read
stompClient.send('/app/messages-read', {}, JSON.stringify({
  senderId: 2,
  receiverId: 1
}));
```

## Frontend Integration Guide

### 1. Install Dependencies
```bash
npm install @stomp/stompjs sockjs-client
```

### 2. WebSocket Service Example
```javascript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
  constructor() {
    this.client = null;
    this.currentUserId = null;
  }

  connect(userId) {
    this.currentUserId = userId;
    
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      
      onConnect: () => {
        console.log('Connected to WebSocket');
        
        // Subscribe to private message queue
        this.client.subscribe(`/user/queue/messages`, (message) => {
          const messageData = JSON.parse(message.body);
          this.handleIncomingMessage(messageData);
        });
        
        // Subscribe to typing indicators
        this.client.subscribe(`/user/queue/typing`, (message) => {
          const typingData = JSON.parse(message.body);
          this.handleTypingIndicator(typingData);
        });
        
        // Subscribe to read receipts
        this.client.subscribe(`/user/queue/read-receipts`, (message) => {
          const receiptData = JSON.parse(message.body);
          this.handleReadReceipt(receiptData);
        });
        
        // Subscribe to user status updates
        this.client.subscribe('/topic/user-status', (message) => {
          const statusData = JSON.parse(message.body);
          this.handleUserStatus(statusData);
        });
        
        // Update status to online
        this.updateStatus('ONLINE');
      },
      
      onDisconnect: () => {
        console.log('Disconnected from WebSocket');
      }
    });
    
    this.client.activate();
  }

  sendMessage(receiverId, text, projectId) {
    if (this.client && this.client.connected) {
      this.client.publish({
        destination: '/app/chat',
        body: JSON.stringify({
          senderId: this.currentUserId,
          receiverId: receiverId,
          text: text,
          projectId: projectId
        })
      });
    }
  }

  sendTypingIndicator(receiverId, isTyping) {
    if (this.client && this.client.connected) {
      this.client.publish({
        destination: '/app/typing',
        body: JSON.stringify({
          senderId: this.currentUserId,
          receiverId: receiverId,
          isTyping: isTyping
        })
      });
    }
  }

  markMessagesAsRead(senderId) {
    if (this.client && this.client.connected) {
      this.client.publish({
        destination: '/app/messages-read',
        body: JSON.stringify({
          senderId: senderId,
          receiverId: this.currentUserId
        })
      });
    }
  }

  updateStatus(status) {
    fetch(`http://localhost:8080/api/messages/status/${this.currentUserId}?status=${status}`, {
      method: 'PUT'
    });
  }

  disconnect() {
    if (this.client) {
      this.updateStatus('OFFLINE');
      this.client.deactivate();
    }
  }

  handleIncomingMessage(messageData) {
    // Handle incoming message (update UI)
    console.log('New message:', messageData);
  }

  handleTypingIndicator(typingData) {
    // Handle typing indicator (show/hide typing animation)
    console.log('Typing indicator:', typingData);
  }

  handleReadReceipt(receiptData) {
    // Handle read receipt (update message status)
    console.log('Read receipt:', receiptData);
  }

  handleUserStatus(statusData) {
    // Handle user status change (update online/offline indicator)
    console.log('User status:', statusData);
  }
}

export default new WebSocketService();
```

### 3. React Context Example (Matching Your Frontend)
```javascript
import { createContext, useContext, useEffect, useState } from 'react';
import WebSocketService from '../services/WebSocketService';

const ChatContext = createContext();

export const ChatProvider = ({ children, currentUserId }) => {
  const [messages, setMessages] = useState([]);
  const [isConnected, setIsConnected] = useState(false);
  const [isTyping, setIsTyping] = useState(false);
  const [otherUserStatus, setOtherUserStatus] = useState('offline');

  useEffect(() => {
    // Connect to WebSocket
    WebSocketService.connect(currentUserId);
    setIsConnected(true);

    // Override handlers
    WebSocketService.handleIncomingMessage = (messageData) => {
      setMessages(prev => [...prev, messageData]);
    };

    WebSocketService.handleTypingIndicator = (typingData) => {
      if (typingData.senderId !== currentUserId) {
        setIsTyping(typingData.isTyping);
      }
    };

    WebSocketService.handleUserStatus = (statusData) => {
      setOtherUserStatus(statusData.status);
    };

    return () => {
      WebSocketService.disconnect();
      setIsConnected(false);
    };
  }, [currentUserId]);

  const sendMessage = (text) => {
    WebSocketService.sendMessage(otherUserId, text, projectId);
  };

  return (
    <ChatContext.Provider value={{
      messages,
      sendMessage,
      currentUserId,
      otherUserStatus,
      isTyping,
      isConnected
    }}>
      {children}
    </ChatContext.Provider>
  );
};

export const useChat = () => useContext(ChatContext);
```

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    conversation_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OFFLINE',
    avatar VARCHAR(500),
    last_seen TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Messages Table
```sql
CREATE TABLE messages (
    conversation_id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES users(conversation_id),
    receiver_id BIGINT NOT NULL REFERENCES users(conversation_id),
    content TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SENT',
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    project_id BIGINT
);
```

### Chats Table
```sql
CREATE TABLE chats (
    conversation_id BIGSERIAL PRIMARY KEY,
    user1_id BIGINT NOT NULL REFERENCES users(conversation_id),
    user2_id BIGINT NOT NULL REFERENCES users(conversation_id),
    project_id BIGINT NOT NULL,
    last_message TEXT,
    last_message_time TIMESTAMP,
    unread_count_user1 INTEGER DEFAULT 0,
    unread_count_user2 INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Configuration

### application.properties
```properties
spring.application.name=dev-connect-backend

# Database
spring.datasource.url=${DATABASE_URL}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# WebSocket
spring.websocket.allowed-origins=http://localhost:3000
```

## Testing

### Using Postman or cURL

#### 1. Send a message
```bash
curl -X POST http://localhost:8080/api/messages/send \
  -H "Content-Type: application/json" \
  -d '{
    "senderId": 1,
    "receiverId": 2,
    "text": "Hello from cURL!",
    "projectId": 10
  }'
```

#### 2. Get user chats
```bash
curl http://localhost:8080/api/messages/chats/1
```

#### 3. Get conversation
```bash
curl "http://localhost:8080/api/messages/conversation?userId1=1&userId2=2"
```

## Security Considerations

⚠️ **Important**: This implementation doesn't include authentication yet. When your teammate finishes the auth system:

1. Add authentication to WebSocket connections
2. Secure REST endpoints with JWT tokens
3. Validate user permissions before sending messages
4. Implement rate limiting for message sending

## Next Steps

1. ✅ Backend messaging implementation complete
2. ⏳ Integrate with authentication system (when ready)
3. ⏳ Add file/image sharing support
4. ⏳ Implement message search functionality
5. ⏳ Add message notifications

## Troubleshooting

### WebSocket Connection Issues
- Check CORS configuration
- Ensure WebSocket port is open
- Verify frontend is using correct WebSocket URL

### Messages Not Delivering
- Check if users exist in database
- Verify WebSocket connection is established
- Check browser console for errors

### Database Connection Issues
- Verify DATABASE_URL environment variable is set
- Check PostgreSQL is running
- Ensure database user has proper permissions
