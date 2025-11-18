# 🔍 User Search for Messaging - Complete Guide

## Problem Solved
Users can now search for other users (clients or developers) to start conversations in the messaging system.

---

## 🎯 New Endpoint: User Search

### **GET /api/users/search**

Search for users by name, username, or email with optional role filtering.

---

## 📋 Request Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `query` | string | No | Search term (searches in firstName, lastName, username, email) |
| `role` | string | No | Filter by role: `CLIENT` or `DEVELOPER` |

---

## 🚀 Usage Examples

### 1. **Search All Users by Name/Email**
```javascript
// Search for "john" across all users
GET /api/users/search?query=john

// Returns users where firstName, lastName, username, or email contains "john"
```

### 2. **Search Only Developers**
```javascript
// Clients searching for developers to message
GET /api/users/search?query=python&role=DEVELOPER

// Returns only developers with "python" in their name, username, or email
```

### 3. **Search Only Clients**
```javascript
// Developers searching for clients to message
GET /api/users/search?query=startup&role=CLIENT

// Returns only clients with "startup" in their name, username, or email
```

### 4. **Get All Users of a Role (No Search)**
```javascript
// Get all developers (no search query)
GET /api/users/search?role=DEVELOPER

// Get all clients
GET /api/users/search?role=CLIENT
```

### 5. **Get All Users**
```javascript
// Get everyone (no query, no role filter)
GET /api/users/search
```

---

## 📦 Response Format

```json
[
  {
    "userId": 50,
    "email": "abdullahisiyad19@gmail.com",
    "username": "abdullahisiyad19",
    "firstName": "Abdullah",
    "lastName": "Isiyad",
    "telephone": "+1234567890",
    "userRole": "CLIENT",
    "userStatus": "ACTIVE",
    "isActive": true,
    "isVerified": true,
    "createdAt": "2025-11-18T19:30:15.123",
    "lastSeen": "2025-11-18T22:44:05.456"
  },
  {
    "userId": 51,
    "email": "abdisamadmohamed139@gmail.com",
    "username": "abdisamadmohamed139",
    "firstName": "Abdisamad",
    "lastName": "Mohamed",
    "userRole": "DEVELOPER",
    "userStatus": "ACTIVE",
    "isActive": true,
    "isVerified": true,
    "createdAt": "2025-11-17T14:20:00.789",
    "lastSeen": "2025-11-18T22:37:40.123"
  }
]
```

---

## 💻 Frontend Implementation

### React Component: User Search for Messaging

```javascript
import React, { useState, useEffect } from 'react';
import axios from 'axios';

const UserSearchForMessaging = ({ currentUserId, currentUserRole }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);

  // Determine which role to search for based on current user
  const targetRole = currentUserRole === 'CLIENT' ? 'DEVELOPER' : 'CLIENT';

  const searchUsers = async (query = '') => {
    setLoading(true);
    try {
      const params = { role: targetRole };
      if (query.trim()) {
        params.query = query;
      }

      const response = await axios.get('http://localhost:8081/api/users/search', {
        params,
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        }
      });

      // Filter out current user
      setUsers(response.data.filter(user => user.userId !== currentUserId));
    } catch (error) {
      console.error('Error searching users:', error);
    } finally {
      setLoading(false);
    }
  };

  // Load all users of target role on mount
  useEffect(() => {
    searchUsers();
  }, []);

  // Debounced search on query change
  useEffect(() => {
    const timer = setTimeout(() => {
      searchUsers(searchQuery);
    }, 500); // Wait 500ms after user stops typing

    return () => clearTimeout(timer);
  }, [searchQuery]);

  const startConversation = async (userId) => {
    // Navigate to chat with this user
    // Or call your conversation creation endpoint
    window.location.href = `/messages?userId=${userId}`;
  };

  return (
    <div className="user-search-messaging">
      <h2>
        Find {targetRole === 'CLIENT' ? 'Clients' : 'Developers'} to Message
      </h2>

      {/* Search Bar */}
      <input
        type="text"
        placeholder={`Search by name or email...`}
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
        className="search-input"
      />

      {/* Loading State */}
      {loading && <p>Searching...</p>}

      {/* Results */}
      <div className="user-list">
        {users.length === 0 && !loading && (
          <p>No {targetRole.toLowerCase()}s found</p>
        )}

        {users.map(user => (
          <div key={user.userId} className="user-card">
            <div className="user-info">
              <h3>{user.firstName} {user.lastName}</h3>
              <p className="username">@{user.username}</p>
              <p className="email">{user.email}</p>
              <span className={`role-badge ${user.userRole.toLowerCase()}`}>
                {user.userRole}
              </span>
            </div>
            <button onClick={() => startConversation(user.userId)}>
              Message
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default UserSearchForMessaging;
```

---

## 🎨 CSS for User Search

```css
.user-search-messaging {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.search-input {
  width: 100%;
  padding: 12px 20px;
  font-size: 16px;
  border: 2px solid #ddd;
  border-radius: 8px;
  margin-bottom: 20px;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.user-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.user-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.user-info h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
  color: #333;
}

.username {
  color: #666;
  font-size: 14px;
  margin: 3px 0;
}

.email {
  color: #999;
  font-size: 13px;
  margin: 3px 0;
}

.role-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  margin-top: 8px;
}

.role-badge.client {
  background: #e3f2fd;
  color: #1976d2;
}

.role-badge.developer {
  background: #f3e5f5;
  color: #7b1fa2;
}

.user-card button {
  padding: 10px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.user-card button:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}
```

---

## 🧪 Testing with cURL

### Test 1: Search for "mohamed"
```bash
curl -X GET "http://localhost:8081/api/users/search?query=mohamed" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Test 2: Get all developers
```bash
curl -X GET "http://localhost:8081/api/users/search?role=DEVELOPER" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Test 3: Search developers with "python"
```bash
curl -X GET "http://localhost:8081/api/users/search?query=python&role=DEVELOPER" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Test 4: Get all clients
```bash
curl -X GET "http://localhost:8081/api/users/search?role=CLIENT" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🔄 Integration Flow

### For Clients (searching for Developers):
```
1. User goes to Messages page
2. Clicks "Find Developers" button
3. Frontend calls: GET /api/users/search?role=DEVELOPER
4. Display list with search bar
5. User types "python" in search
6. Frontend calls: GET /api/users/search?query=python&role=DEVELOPER
7. User clicks "Message" on a developer
8. Navigate to chat or create conversation
```

### For Developers (searching for Clients):
```
1. User goes to Messages page
2. Clicks "Find Clients" button  
3. Frontend calls: GET /api/users/search?role=CLIENT
4. Display list with search bar
5. User types company name in search
6. Frontend calls: GET /api/users/search?query=startup&role=CLIENT
7. User clicks "Message" on a client
8. Navigate to chat or create conversation
```

---

## ✅ Benefits

1. **Fast Search**: Case-insensitive search across multiple fields
2. **Role Filtering**: Clients find developers, developers find clients
3. **Real-time**: Debounced search updates as you type
4. **Flexible**: Works with or without query (get all users of a role)
5. **Secure**: Requires JWT authentication

---

## 🎯 Next Steps for Frontend

1. Add this component to your Messages page
2. Add "Find Developers" / "Find Clients" button
3. Wire up the search to the new endpoint
4. Handle the "Message" button click to start conversation
5. Test the search functionality

---

## ✅ Backend Status: FULLY READY

- ✅ Search endpoint implemented
- ✅ Role filtering working
- ✅ Multi-field search (name, username, email)
- ✅ Case-insensitive search
- ✅ JWT authentication required
- ✅ Server running on http://localhost:8081

**Your messaging search is now fully functional! 🎉**
