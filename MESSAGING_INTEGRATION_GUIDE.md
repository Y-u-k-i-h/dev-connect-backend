# DevConnect Messaging - Frontend Integration Guide

## Overview

This guide provides complete instructions for integrating the DevConnect real-time messaging system into your frontend application. The system supports WebSocket-based real-time messaging with REST API fallbacks.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Backend API Endpoints](#backend-api-endpoints)
3. [WebSocket Setup](#websocket-setup)
4. [REST API Integration](#rest-api-integration)
5. [React Implementation](#react-implementation)
6. [Vue.js Implementation](#vuejs-implementation)
7. [Testing Guide](#testing-guide)
8. [Troubleshooting](#troubleshooting)

---

## Architecture Overview

### Communication Flow

```
Frontend ←→ WebSocket ←→ Backend (Port 8081)
Frontend ←→ REST API ←→ Backend (Port 8081)
```

### Key Components

- **WebSocket Endpoints**: Real-time bidirectional communication
- **REST API**: Message history, user lists, fallback
- **STOMP Protocol**: Message routing over WebSocket
- **JWT Authentication**: Secure access to messaging

---

## Backend API Endpoints

### REST API Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/messages/chats/{userId}` | Get all chats for a user | ✅ |
| GET | `/api/messages/conversation?userId1={id1}&userId2={id2}` | Get conversation between two users | ✅ |
| GET | `/api/messages/conversation/{conversationId}?userId={id}` | Get messages by conversation ID | ✅ |
| POST | `/api/messages/send` | Send a message (REST fallback) | ✅ |
| PUT | `/api/messages/read?conversationId={id}&readerId={id}` | Mark messages as read | ✅ |
| PUT | `/api/messages/status/{userId}?status={online\|offline}` | Update user status | ✅ |
| GET | `/api/messages/status/{userId}` | Get user status | ✅ |

### WebSocket Endpoints

| Destination | Type | Description |
|-------------|------|-------------|
| `/ws` | Connect | WebSocket connection endpoint |
| `/app/chat.sendMessage` | Send | Send a message |
| `/app/typing` | Send | Send typing indicator |
| `/app/message-delivered` | Send | Confirm message delivery |
| `/app/messages-read` | Send | Mark messages as read |
| `/user/{userId}/queue/messages` | Subscribe | Receive messages |
| `/user/{userId}/queue/typing` | Subscribe | Receive typing indicators |
| `/user/{userId}/queue/read-receipts` | Subscribe | Receive read receipts |

---

## WebSocket Setup

### Install Dependencies

```bash
npm install @stomp/stompjs sockjs-client axios
```

### WebSocket Service (JavaScript/TypeScript)

```javascript
// services/websocketService.js
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
  constructor() {
    this.client = null;
    this.subscriptions = new Map();
    this.userId = null;
    this.isConnected = false;
  }

  /**
   * Connect to WebSocket server
   * @param {number} userId - Current user ID
   * @returns {Promise<void>}
   */
  connect(userId) {
    this.userId = userId;

    return new Promise((resolve, reject) => {
      this.client = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8081/ws'),
        
        debug: (str) => {
          console.log('[STOMP]', str);
        },
        
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,

        onConnect: () => {
          console.log('✅ WebSocket Connected');
          this.isConnected = true;
          resolve();
        },

        onStompError: (frame) => {
          console.error('❌ STOMP Error:', frame.headers['message']);
          this.isConnected = false;
          reject(new Error(frame.headers['message']));
        },

        onWebSocketError: (error) => {
          console.error('❌ WebSocket Error:', error);
          this.isConnected = false;
          reject(error);
        },

        onWebSocketClose: () => {
          console.log('🔌 WebSocket Disconnected');
          this.isConnected = false;
        }
      });

      this.client.activate();
    });
  }

  /**
   * Subscribe to incoming messages
   * @param {Function} callback - Message handler
   */
  subscribeToMessages(callback) {
    if (!this.client || !this.userId) {
      throw new Error('WebSocket not connected');
    }

    const subscription = this.client.subscribe(
      `/user/${this.userId}/queue/messages`,
      (message) => {
        try {
          const data = JSON.parse(message.body);
          callback(data);
        } catch (error) {
          console.error('Error parsing message:', error);
        }
      }
    );

    this.subscriptions.set('messages', subscription);
  }

  /**
   * Subscribe to typing indicators
   * @param {Function} callback - Typing indicator handler
   */
  subscribeToTyping(callback) {
    if (!this.client || !this.userId) {
      throw new Error('WebSocket not connected');
    }

    const subscription = this.client.subscribe(
      `/user/${this.userId}/queue/typing`,
      (message) => {
        try {
          const data = JSON.parse(message.body);
          callback(data);
        } catch (error) {
          console.error('Error parsing typing indicator:', error);
        }
      }
    );

    this.subscriptions.set('typing', subscription);
  }

  /**
   * Subscribe to read receipts
   * @param {Function} callback - Read receipt handler
   */
  subscribeToReadReceipts(callback) {
    if (!this.client || !this.userId) {
      throw new Error('WebSocket not connected');
    }

    const subscription = this.client.subscribe(
      `/user/${this.userId}/queue/read-receipts`,
      (message) => {
        try {
          const data = JSON.parse(message.body);
          callback(data);
        } catch (error) {
          console.error('Error parsing read receipt:', error);
        }
      }
    );

    this.subscriptions.set('read-receipts', subscription);
  }

  /**
   * Send a message via WebSocket
   * @param {number} senderId - Sender user ID
   * @param {number} receiverId - Receiver user ID
   * @param {string} text - Message text
   */
  sendMessage(senderId, receiverId, text) {
    if (!this.client || !this.isConnected) {
      throw new Error('WebSocket not connected');
    }

    const message = {
      senderId: senderId,
      receiverId: receiverId,
      text: text,
      timestamp: new Date().toISOString()
    };

    this.client.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify(message)
    });
  }

  /**
   * Send typing indicator
   * @param {number} senderId - Sender user ID
   * @param {number} receiverId - Receiver user ID
   * @param {boolean} isTyping - Is typing status
   */
  sendTypingIndicator(senderId, receiverId, isTyping) {
    if (!this.client || !this.isConnected) {
      return;
    }

    const indicator = {
      senderId: senderId,
      receiverId: receiverId,
      isTyping: isTyping
    };

    this.client.publish({
      destination: '/app/typing',
      body: JSON.stringify(indicator)
    });
  }

  /**
   * Mark messages as read
   * @param {number} conversationId - Conversation ID
   * @param {number} readerId - Reader user ID
   */
  markMessagesAsRead(conversationId, readerId) {
    if (!this.client || !this.isConnected) {
      return;
    }

    const readRequest = {
      conversationId: conversationId,
      readerId: readerId
    };

    this.client.publish({
      destination: '/app/messages-read',
      body: JSON.stringify(readRequest)
    });
  }

  /**
   * Disconnect from WebSocket
   */
  disconnect() {
    // Unsubscribe all
    this.subscriptions.forEach((subscription) => {
      subscription.unsubscribe();
    });
    this.subscriptions.clear();

    // Deactivate client
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }

    this.isConnected = false;
  }

  /**
   * Check connection status
   * @returns {boolean}
   */
  isConnected() {
    return this.isConnected;
  }
}

export default new WebSocketService();
```

---

## REST API Integration

### Messaging API Service

```javascript
// services/messagingApi.js
import axios from 'axios';

const API_BASE = 'http://localhost:8081/api/messages';

class MessagingApi {
  constructor() {
    this.axiosInstance = axios.create({
      baseURL: API_BASE
    });

    // Add JWT token to all requests
    this.axiosInstance.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );
  }

  /**
   * Get all chats for a user
   * @param {number} userId - User ID
   * @returns {Promise<Array>} List of chats
   */
  async getUserChats(userId) {
    try {
      const response = await this.axiosInstance.get(`/chats/${userId}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching chats:', error);
      throw error;
    }
  }

  /**
   * Get conversation between two users
   * @param {number} userId1 - First user ID
   * @param {number} userId2 - Second user ID
   * @returns {Promise<Array>} List of messages
   */
  async getConversation(userId1, userId2) {
    try {
      const response = await this.axiosInstance.get(
        `/conversation?userId1=${userId1}&userId2=${userId2}`
      );
      return response.data;
    } catch (error) {
      console.error('Error fetching conversation:', error);
      throw error;
    }
  }

  /**
   * Get messages by conversation ID
   * @param {number} conversationId - Conversation ID
   * @param {number} userId - User ID
   * @returns {Promise<Array>} List of messages
   */
  async getConversationMessages(conversationId, userId) {
    try {
      const response = await this.axiosInstance.get(
        `/conversation/${conversationId}?userId=${userId}`
      );
      return response.data;
    } catch (error) {
      console.error('Error fetching messages:', error);
      throw error;
    }
  }

  /**
   * Send a message (REST fallback)
   * @param {Object} message - Message object
   * @returns {Promise<Object>} Sent message
   */
  async sendMessage(message) {
    try {
      const response = await this.axiosInstance.post('/send', message);
      return response.data;
    } catch (error) {
      console.error('Error sending message:', error);
      throw error;
    }
  }

  /**
   * Mark messages as read
   * @param {number} conversationId - Conversation ID
   * @param {number} readerId - Reader user ID
   */
  async markMessagesAsRead(conversationId, readerId) {
    try {
      await this.axiosInstance.put(
        `/read?conversationId=${conversationId}&readerId=${readerId}`
      );
    } catch (error) {
      console.error('Error marking messages as read:', error);
      throw error;
    }
  }

  /**
   * Update user status
   * @param {number} userId - User ID
   * @param {string} status - 'online' or 'offline'
   */
  async updateUserStatus(userId, status) {
    try {
      await this.axiosInstance.put(`/status/${userId}?status=${status}`);
    } catch (error) {
      console.error('Error updating status:', error);
      throw error;
    }
  }

  /**
   * Get user status
   * @param {number} userId - User ID
   * @returns {Promise<string>} User status
   */
  async getUserStatus(userId) {
    try {
      const response = await this.axiosInstance.get(`/status/${userId}`);
      return response.data.status;
    } catch (error) {
      console.error('Error fetching status:', error);
      throw error;
    }
  }
}

export default new MessagingApi();
```

---

## React Implementation

### Chat Interface Component

```jsx
// components/ChatInterface.jsx
import React, { useState, useEffect, useRef } from 'react';
import websocketService from '../services/websocketService';
import messagingApi from '../services/messagingApi';
import './ChatInterface.css';

const ChatInterface = ({ currentUserId, recipientUserId, recipientName }) => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [isConnected, setIsConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  
  const messagesEndRef = useRef(null);
  const typingTimeoutRef = useRef(null);

  // Initialize WebSocket connection
  useEffect(() => {
    let mounted = true;

    const initWebSocket = async () => {
      try {
        await websocketService.connect(currentUserId);
        
        if (!mounted) return;
        
        setIsConnected(true);

        // Subscribe to incoming messages
        websocketService.subscribeToMessages((message) => {
          if (
            (message.senderId === recipientUserId && message.receiverId === currentUserId) ||
            (message.senderId === currentUserId && message.receiverId === recipientUserId)
          ) {
            setMessages((prev) => {
              // Avoid duplicates
              if (prev.some(m => m.id === message.id)) {
                return prev;
              }
              return [...prev, message];
            });
            scrollToBottom();
          }
        });

        // Subscribe to typing indicators
        websocketService.subscribeToTyping((indicator) => {
          if (indicator.senderId === recipientUserId) {
            setIsTyping(indicator.isTyping);
            
            // Auto-hide typing indicator after 3 seconds
            setTimeout(() => setIsTyping(false), 3000);
          }
        });

        // Set user online
        await messagingApi.updateUserStatus(currentUserId, 'online');
        
      } catch (error) {
        console.error('WebSocket connection failed:', error);
        setIsConnected(false);
      }
    };

    initWebSocket();

    return () => {
      mounted = false;
      websocketService.disconnect();
      messagingApi.updateUserStatus(currentUserId, 'offline');
    };
  }, [currentUserId, recipientUserId]);

  // Load conversation history
  useEffect(() => {
    const loadMessages = async () => {
      try {
        setIsLoading(true);
        const history = await messagingApi.getConversation(
          currentUserId,
          recipientUserId
        );
        setMessages(history);
        scrollToBottom();
      } catch (error) {
        console.error('Failed to load messages:', error);
      } finally {
        setIsLoading(false);
      }
    };

    loadMessages();
  }, [currentUserId, recipientUserId]);

  const scrollToBottom = () => {
    setTimeout(() => {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  };

  const handleSendMessage = async () => {
    if (!newMessage.trim()) return;

    const messageText = newMessage.trim();
    setNewMessage('');

    try {
      if (isConnected) {
        // Send via WebSocket
        websocketService.sendMessage(currentUserId, recipientUserId, messageText);
        
        // Stop typing indicator
        websocketService.sendTypingIndicator(currentUserId, recipientUserId, false);
      } else {
        // Fallback to REST API
        const message = {
          senderId: currentUserId,
          receiverId: recipientUserId,
          text: messageText
        };
        
        const sentMessage = await messagingApi.sendMessage(message);
        setMessages((prev) => [...prev, sentMessage]);
        scrollToBottom();
      }
    } catch (error) {
      console.error('Failed to send message:', error);
      alert('Failed to send message. Please try again.');
    }
  };

  const handleTyping = (text) => {
    setNewMessage(text);

    if (isConnected && text.trim()) {
      websocketService.sendTypingIndicator(currentUserId, recipientUserId, true);

      // Clear previous timeout
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
      }

      // Stop typing indicator after 1 second of inactivity
      typingTimeoutRef.current = setTimeout(() => {
        websocketService.sendTypingIndicator(currentUserId, recipientUserId, false);
      }, 1000);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  if (isLoading) {
    return (
      <div className="chat-loading">
        <div className="spinner"></div>
        <p>Loading conversation...</p>
      </div>
    );
  }

  return (
    <div className="chat-container">
      {/* Chat Header */}
      <div className="chat-header">
        <div className="chat-header-info">
          <h2>{recipientName}</h2>
          <span className={`connection-status ${isConnected ? 'connected' : 'disconnected'}`}>
            {isConnected ? '🟢 Connected' : '🔴 Disconnected'}
          </span>
        </div>
      </div>

      {/* Messages List */}
      <div className="messages-list">
        {messages.length === 0 ? (
          <div className="no-messages">
            <p>No messages yet. Start the conversation!</p>
          </div>
        ) : (
          messages.map((message, index) => (
            <div
              key={message.id || index}
              className={`message ${
                message.senderId === currentUserId ? 'sent' : 'received'
              }`}
            >
              <div className="message-bubble">
                <div className="message-text">{message.text}</div>
                <div className="message-meta">
                  <span className="timestamp">
                    {message.timestamp
                      ? new Date(message.timestamp).toLocaleTimeString([], {
                          hour: '2-digit',
                          minute: '2-digit'
                        })
                      : 'Sending...'}
                  </span>
                  {message.senderId === currentUserId && (
                    <span className={`status ${message.status || 'sent'}`}>
                      {message.status === 'read' && '✓✓'}
                      {message.status === 'delivered' && '✓✓'}
                      {message.status === 'sent' && '✓'}
                    </span>
                  )}
                </div>
              </div>
            </div>
          ))
        )}
        
        {isTyping && (
          <div className="typing-indicator">
            <div className="typing-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
            <span className="typing-text">{recipientName} is typing...</span>
          </div>
        )}
        
        <div ref={messagesEndRef} />
      </div>

      {/* Message Input */}
      <div className="message-input-container">
        <div className="message-input">
          <textarea
            value={newMessage}
            onChange={(e) => handleTyping(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="Type a message..."
            rows="1"
          />
          <button
            onClick={handleSendMessage}
            disabled={!newMessage.trim()}
            className="send-button"
          >
            <span>Send</span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default ChatInterface;
```

### Chat List Component

```jsx
// components/ChatList.jsx
import React, { useState, useEffect } from 'react';
import messagingApi from '../services/messagingApi';
import './ChatList.css';

const ChatList = ({ currentUserId, onSelectChat }) => {
  const [chats, setChats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadChats();
    
    // Refresh chats every 30 seconds
    const interval = setInterval(loadChats, 30000);
    
    return () => clearInterval(interval);
  }, [currentUserId]);

  const loadChats = async () => {
    try {
      const data = await messagingApi.getUserChats(currentUserId);
      setChats(data);
      setError(null);
    } catch (err) {
      console.error('Failed to load chats:', err);
      setError('Failed to load conversations');
    } finally {
      setLoading(false);
    }
  };

  const formatLastMessageTime = (timestamp) => {
    if (!timestamp) return '';
    
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);
    
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    
    return date.toLocaleDateString();
  };

  if (loading) {
    return (
      <div className="chat-list-loading">
        <div className="spinner"></div>
        <p>Loading conversations...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="chat-list-error">
        <p>{error}</p>
        <button onClick={loadChats}>Retry</button>
      </div>
    );
  }

  return (
    <div className="chat-list">
      <div className="chat-list-header">
        <h2>Messages</h2>
        <button onClick={loadChats} className="refresh-button">
          ↻
        </button>
      </div>

      {chats.length === 0 ? (
        <div className="no-chats">
          <p>No conversations yet</p>
          <small>Start chatting with developers or clients!</small>
        </div>
      ) : (
        <ul className="chat-list-items">
          {chats.map((chat) => (
            <li
              key={chat.id}
              onClick={() => onSelectChat(chat)}
              className="chat-item"
            >
              <div className="chat-avatar">
                {chat.userAvatar ? (
                  <img src={chat.userAvatar} alt={chat.userName} />
                ) : (
                  <div className="avatar-placeholder">
                    {chat.userName.charAt(0).toUpperCase()}
                  </div>
                )}
                <span className={`user-status ${chat.userStatus?.toLowerCase()}`}>
                  {chat.userStatus === 'online' ? '🟢' : '⚪'}
                </span>
              </div>

              <div className="chat-info">
                <div className="chat-header-row">
                  <h3 className="chat-name">{chat.userName}</h3>
                  <span className="chat-time">
                    {formatLastMessageTime(chat.lastMessageTime)}
                  </span>
                </div>

                <div className="chat-preview-row">
                  <p className="last-message">
                    {chat.lastMessage || 'No messages yet'}
                  </p>
                  {chat.unreadCount > 0 && (
                    <span className="unread-badge">{chat.unreadCount}</span>
                  )}
                </div>

                <div className="chat-meta">
                  <span className="user-role">{chat.userRole}</span>
                </div>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default ChatList;
```

### Main Messaging Page

```jsx
// pages/MessagingPage.jsx
import React, { useState } from 'react';
import ChatList from '../components/ChatList';
import ChatInterface from '../components/ChatInterface';
import './MessagingPage.css';

const MessagingPage = () => {
  const [selectedChat, setSelectedChat] = useState(null);
  const [currentUserId] = useState(() => {
    // Get from authentication context or localStorage
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    return user.userId;
  });

  const handleSelectChat = (chat) => {
    setSelectedChat(chat);
  };

  const handleBackToList = () => {
    setSelectedChat(null);
  };

  if (!currentUserId) {
    return (
      <div className="messaging-error">
        <p>Please log in to access messaging</p>
      </div>
    );
  }

  return (
    <div className="messaging-page">
      <div className={`chat-list-panel ${selectedChat ? 'hidden-mobile' : ''}`}>
        <ChatList
          currentUserId={currentUserId}
          onSelectChat={handleSelectChat}
        />
      </div>

      <div className={`chat-panel ${!selectedChat ? 'hidden-mobile' : ''}`}>
        {selectedChat ? (
          <>
            <button className="back-button mobile-only" onClick={handleBackToList}>
              ← Back
            </button>
            <ChatInterface
              currentUserId={currentUserId}
              recipientUserId={selectedChat.userId}
              recipientName={selectedChat.userName}
            />
          </>
        ) : (
          <div className="no-chat-selected">
            <p>Select a conversation to start messaging</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default MessagingPage;
```

---

## Vue.js Implementation

### Composable for WebSocket

```javascript
// composables/useWebSocket.js
import { ref, onMounted, onUnmounted } from 'vue';
import websocketService from '../services/websocketService';

export function useWebSocket(userId) {
  const isConnected = ref(false);
  const messages = ref([]);

  onMounted(async () => {
    try {
      await websocketService.connect(userId.value);
      isConnected.value = true;

      websocketService.subscribeToMessages((message) => {
        messages.value.push(message);
      });
    } catch (error) {
      console.error('WebSocket connection failed:', error);
      isConnected.value = false;
    }
  });

  onUnmounted(() => {
    websocketService.disconnect();
  });

  const sendMessage = (senderId, receiverId, text) => {
    if (isConnected.value) {
      websocketService.sendMessage(senderId, receiverId, text);
    }
  };

  return {
    isConnected,
    messages,
    sendMessage
  };
}
```

---

## Testing Guide

### Test WebSocket Connection

```javascript
// Test in browser console
import websocketService from './services/websocketService';

// Connect
await websocketService.connect(1);

// Subscribe to messages
websocketService.subscribeToMessages((msg) => {
  console.log('Received:', msg);
});

// Send test message
websocketService.sendMessage(1, 2, 'Hello!');

// Disconnect
websocketService.disconnect();
```

### Test REST API

```bash
# Get JWT token first (login)
TOKEN="your_jwt_token_here"

# Get user chats
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/messages/chats/1

# Get conversation
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8081/api/messages/conversation?userId1=1&userId2=2"

# Send message
curl -X POST http://localhost:8081/api/messages/send \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"senderId":1,"receiverId":2,"text":"Hello from API!"}'
```

---

## Troubleshooting

### Common Issues

#### 1. WebSocket Connection Fails

**Symptoms:**
- Console shows "WebSocket connection error"
- Connection status shows "Disconnected"

**Solutions:**
- Check backend is running on port 8081
- Verify CORS settings allow your frontend origin
- Check JWT token is valid
- Try connecting with SockJS fallback

#### 2. Messages Not Received

**Symptoms:**
- Sent messages don't appear
- No real-time updates

**Solutions:**
- Verify subscription path matches userId
- Check backend logs for errors
- Test with REST API as fallback
- Ensure WebSocket is connected before sending

#### 3. CORS Errors

**Symptoms:**
- "CORS policy blocked" in console
- Requests fail with 403 status

**Solutions:**
- Add your frontend origin to backend CORS config
- Check `CorsConfig.java` allows your domain
- Verify credentials are included in requests

#### 4. Authentication Issues

**Symptoms:**
- 401 Unauthorized errors
- Cannot access protected endpoints

**Solutions:**
- Ensure JWT token is stored correctly
- Check token expiration
- Verify Authorization header format: `Bearer <token>`
- Re-login to get fresh token

### Debug Mode

Enable detailed logging:

```javascript
// In websocketService.js
this.client = new Client({
  debug: (str) => {
    console.log('[STOMP DEBUG]', new Date().toISOString(), str);
  },
  // ... other config
});
```

### Network Inspection

Use browser DevTools:
1. Open Network tab
2. Filter by "WS" (WebSocket)
3. Click on WebSocket connection
4. View Messages tab for real-time data

---

## Best Practices

### 1. Connection Management

```javascript
// Reconnect on visibility change
document.addEventListener('visibilitychange', () => {
  if (document.visibilityState === 'visible' && !websocketService.isConnected()) {
    websocketService.connect(userId);
  }
});
```

### 2. Error Handling

```javascript
// Always handle errors gracefully
try {
  await websocketService.connect(userId);
} catch (error) {
  // Fallback to REST API
  console.error('WebSocket failed, using REST API');
}
```

### 3. Message Persistence

```javascript
// Save to REST API as backup
const sendMessage = async (message) => {
  try {
    if (isConnected) {
      websocketService.sendMessage(message);
    } else {
      // Fallback to REST
      await messagingApi.sendMessage(message);
    }
  } catch (error) {
    // Save to local queue for retry
    saveToLocalQueue(message);
  }
};
```

### 4. Performance Optimization

```javascript
// Limit message history
const MAX_MESSAGES = 100;

const addMessage = (message) => {
  setMessages(prev => {
    const updated = [...prev, message];
    return updated.slice(-MAX_MESSAGES);
  });
};
```

---

## Additional Resources

### Backend Endpoints Summary

```
Base URL: http://localhost:8081

WebSocket: ws://localhost:8081/ws
STOMP Prefix: /app
User Queue: /user/{userId}/queue
```

### Required Headers

```javascript
{
  'Authorization': 'Bearer <JWT_TOKEN>',
  'Content-Type': 'application/json'
}
```

### Environment Variables

```env
REACT_APP_API_URL=http://localhost:8081
REACT_APP_WS_URL=ws://localhost:8081/ws
```

---

## Support

For issues or questions:
1. Check backend logs: `.\gradlew.bat bootRun`
2. Test endpoints with Postman or curl
3. Verify authentication is working
4. Check browser console for errors

**Backend Server Status:** http://localhost:8081/actuator/health (if actuator enabled)

---

**Last Updated:** November 18, 2025
**Version:** 1.0.0
