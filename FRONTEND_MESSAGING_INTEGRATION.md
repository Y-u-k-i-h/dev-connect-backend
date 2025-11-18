# Frontend Messaging Integration - Quick Start

## 🎯 Problem
- **Developers** can't see clients to message because they need to search for them
- **Clients** can't search for developers to message
- Messaging page shows "No clients for your projects yet"

## ✅ Solution - Backend Endpoints Ready

Your backend already has these endpoints working:

### For Developers - Find Clients to Message

**Endpoint 1: Get Clients from Your Projects**
```javascript
// Get all your claimed projects (as a developer)
GET /api/projects/developer/{devId}

// Then extract the clientId from each project and get client details
GET /api/users/{clientId}
```

**Endpoint 2: Get All Users (filter by role)**
```javascript
// Get all clients
GET /api/users/role/CLIENT

// Get all developers  
GET /api/users/role/DEVELOPER
```

**Example Implementation:**
```javascript
// For Developer - Find clients from my projects
async function getMyClients(devId) {
  const token = localStorage.getItem('token');
  
  // Step 1: Get my projects
  const projectsResponse = await fetch(
    `http://localhost:8081/api/projects/developer/${devId}`,
    {
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );
  const projects = await projectsResponse.json();
  
  // Step 2: Get unique client IDs
  const clientIds = [...new Set(projects.map(p => p.clientId))];
  
  // Step 3: Get client details
  const clients = await Promise.all(
    clientIds.map(clientId =>
      fetch(`http://localhost:8081/api/users/${clientId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      }).then(r => r.json())
    )
  );
  
  return clients;
}

// For Developer - Get ALL clients (broader search)
async function getAllClients() {
  const token = localStorage.getItem('token');
  const response = await fetch(
    'http://localhost:8081/api/users/role/CLIENT',
    {
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );
  return await response.json();
}
```

### For Clients - Find Developers to Message

**Endpoint: Search All Developers**
```javascript
// Get all developers
GET /api/developers

// Search developers by skills
GET /api/developers/search?skills=java

// Search by rating
GET /api/developers/search?minRating=4.5

// Search by hourly rate
GET /api/developers/search?minRate=50&maxRate=100
```

**Example Implementation:**
```javascript
// For Client - Find developers to message
async function searchDevelopers(skillQuery = '') {
  const token = localStorage.getItem('token');
  
  const url = skillQuery 
    ? `http://localhost:8081/api/developers/search?skills=${encodeURIComponent(skillQuery)}`
    : 'http://localhost:8081/api/developers';
  
  const response = await fetch(url, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  return await response.json();
}
```

---

## 🔧 Frontend Changes Needed

### 1. Add "Find Developers" Button (for Clients)

In your Messages page, add:

```jsx
// MessagesPage.jsx (for CLIENT role)
import { useState, useEffect } from 'react';

function MessagesForClient() {
  const [developers, setDevelopers] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [showDeveloperList, setShowDeveloperList] = useState(false);

  const searchDevelopers = async () => {
    const token = localStorage.getItem('token');
    const url = searchTerm
      ? `http://localhost:8081/api/developers/search?skills=${encodeURIComponent(searchTerm)}`
      : 'http://localhost:8081/api/developers';
    
    const response = await fetch(url, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const data = await response.json();
    setDevelopers(data);
    setShowDeveloperList(true);
  };

  const startConversationWithDeveloper = async (developer) => {
    // Create conversation by sending first message or just navigate to chat
    const currentUserId = getCurrentUserId(); // Your function to get logged-in user ID
    
    // Option 1: Navigate to chat interface with developer's userId
    navigateToChat(developer.userId);
    
    // Option 2: Create conversation immediately
    const response = await fetch(
      `http://localhost:8081/api/messages/conversation?userId1=${currentUserId}&userId2=${developer.userId}`,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }
    );
    const messages = await response.json();
    // Now open chat interface
  };

  return (
    <div>
      {/* Search Bar */}
      <div className="search-section">
        <input
          type="text"
          placeholder="Search developers by skills (e.g., Java, React, Python)"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        <button onClick={searchDevelopers}>Find Developers</button>
      </div>

      {/* Developer List */}
      {showDeveloperList && (
        <div className="developer-list">
          <h3>Available Developers</h3>
          {developers.map((dev) => (
            <div key={dev.developerId} className="developer-card">
              <h4>{dev.firstName} {dev.lastName}</h4>
              <p><strong>@{dev.username}</strong></p>
              <p>{dev.bio}</p>
              <p><strong>Skills:</strong> {dev.skills}</p>
              <p><strong>Rate:</strong> ${dev.hourlyRate}/hr</p>
              <p><strong>Rating:</strong> ⭐ {dev.averageRating}</p>
              <button onClick={() => startConversationWithDeveloper(dev)}>
                💬 Message Developer
              </button>
            </div>
          ))}
        </div>
      )}

      {/* Existing conversation list */}
      <ConversationList />
    </div>
  );
}
```

### 2. Add "Find Clients" Button (for Developers)

```jsx
// MessagesPage.jsx (for DEVELOPER role)
function MessagesForDeveloper() {
  const [clients, setClients] = useState([]);
  const [showClientList, setShowClientList] = useState(false);
  const devId = getCurrentUserId(); // Your function

  const findMyClients = async () => {
    const token = localStorage.getItem('token');
    
    // Get clients from my projects
    const projectsResponse = await fetch(
      `http://localhost:8081/api/projects/developer/${devId}`,
      { headers: { 'Authorization': `Bearer ${token}` }}
    );
    const projects = await projectsResponse.json();
    
    // Get unique client IDs
    const clientIds = [...new Set(projects.map(p => p.clientId))];
    
    // Get client details
    const clientPromises = clientIds.map(clientId =>
      fetch(`http://localhost:8081/api/users/${clientId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      }).then(r => r.json())
    );
    
    const clientData = await Promise.all(clientPromises);
    setClients(clientData);
    setShowClientList(true);
  };

  const findAllClients = async () => {
    const token = localStorage.getItem('token');
    const response = await fetch(
      'http://localhost:8081/api/users/role/CLIENT',
      { headers: { 'Authorization': `Bearer ${token}` }}
    );
    const data = await response.json();
    setClients(data);
    setShowClientList(true);
  };

  const startConversationWithClient = (client) => {
    const currentUserId = getCurrentUserId();
    navigateToChat(client.userId);
  };

  return (
    <div>
      {/* Action Buttons */}
      <div className="action-buttons">
        <button onClick={findMyClients}>
          👥 Clients from My Projects
        </button>
        <button onClick={findAllClients}>
          🔍 Search All Clients
        </button>
      </div>

      {/* Client List */}
      {showClientList && (
        <div className="client-list">
          <h3>Clients</h3>
          {clients.map((client) => (
            <div key={client.userId} className="client-card">
              <h4>{client.firstName} {client.lastName}</h4>
              <p><strong>Email:</strong> {client.email}</p>
              <button onClick={() => startConversationWithClient(client)}>
                💬 Message Client
              </button>
            </div>
          ))}
        </div>
      )}

      {/* Existing conversation list */}
      <ConversationList />
    </div>
  );
}
```

---

## 🚀 Quick Implementation Steps

### Step 1: Add to Your Messages Page Component

Replace the "No clients for your projects yet" section with buttons:

```jsx
{conversations.length === 0 && (
  <div className="empty-state">
    {userRole === 'CLIENT' ? (
      <>
        <p>No conversations yet</p>
        <button onClick={() => setShowDeveloperSearch(true)}>
          🔍 Find Developers
        </button>
      </>
    ) : (
      <>
        <p>No conversations yet</p>
        <button onClick={findMyClients}>
          👥 My Project Clients
        </button>
        <button onClick={findAllClients}>
          🔍 Search All Clients
        </button>
      </>
    )}
  </div>
)}
```

### Step 2: Test the Endpoints

```bash
# Test as Client - Find developers
curl http://localhost:8081/api/developers

# Test as Developer - Find clients
curl http://localhost:8081/api/users/role/CLIENT

# Test getting user details
curl http://localhost:8081/api/users/1
```

---

## 📊 Complete User Flow

### For Clients:
1. **Click "Find Developers"** button on Messages page
2. **Search/browse developers** by skills, rating, or rate
3. **Click "Message Developer"** on a developer card
4. **Chat interface opens** with that developer
5. **Conversation appears** in the conversation list

### For Developers:
1. **Click "My Project Clients"** to see clients from claimed projects
2. **OR Click "Search All Clients"** to browse all clients
3. **Click "Message Client"** on a client card
4. **Chat interface opens** with that client
5. **Conversation appears** in the conversation list

---

## ✅ Backend Status

All these endpoints are **READY and WORKING**:

- ✅ `GET /api/developers` - Get all developers
- ✅ `GET /api/developers/search?skills={query}` - Search developers
- ✅ `GET /api/users/role/CLIENT` - Get all clients
- ✅ `GET /api/users/role/DEVELOPER` - Get all developers (alternative)
- ✅ `GET /api/users/{id}` - Get user details
- ✅ `GET /api/projects/developer/{devId}` - Get developer's projects
- ✅ `GET /api/messages/conversation?userId1={id1}&userId2={id2}` - Get/create conversation

**Your backend is 100% ready!** Just need to add these UI elements to your frontend. 🎉

---

## 🎨 Simple HTML/CSS Example

If you want a quick test, add this to your Messages page:

```html
<!-- For Developer View -->
<div class="find-clients-section">
  <h3>Start a Conversation</h3>
  <button onclick="findClients()">Find My Project Clients</button>
  <button onclick="findAllClients()">Search All Clients</button>
  
  <div id="client-list" style="display:none;">
    <!-- Client cards will be inserted here -->
  </div>
</div>

<script>
async function findClients() {
  const devId = getCurrentUserId(); // Your function
  const token = localStorage.getItem('token');
  
  const response = await fetch(
    `http://localhost:8081/api/projects/developer/${devId}`,
    { headers: { 'Authorization': `Bearer ${token}` }}
  );
  const projects = await response.json();
  
  const clientIds = [...new Set(projects.map(p => p.clientId))];
  
  const clients = await Promise.all(
    clientIds.map(id =>
      fetch(`http://localhost:8081/api/users/${id}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      }).then(r => r.json())
    )
  );
  
  displayClients(clients);
}

async function findAllClients() {
  const token = localStorage.getItem('token');
  const response = await fetch(
    'http://localhost:8081/api/users/role/CLIENT',
    { headers: { 'Authorization': `Bearer ${token}` }}
  );
  const clients = await response.json();
  displayClients(clients);
}

function displayClients(clients) {
  const listDiv = document.getElementById('client-list');
  listDiv.innerHTML = clients.map(client => `
    <div class="client-card">
      <h4>${client.firstName} ${client.lastName}</h4>
      <p>Email: ${client.email}</p>
      <button onclick="startChat(${client.userId})">Message</button>
    </div>
  `).join('');
  listDiv.style.display = 'block';
}

function startChat(userId) {
  // Navigate to chat with this user
  window.location.href = `/messages?chatWith=${userId}`;
}
</script>
```

---

## 🎯 Summary

**The Problem:** No way to find users to start conversations with

**The Solution:** Add search/find buttons that call these endpoints:
- Clients → Find developers via `/api/developers`
- Developers → Find clients via `/api/users/role/CLIENT` or from projects

**Status:** Backend is 100% ready, just needs frontend UI buttons!
