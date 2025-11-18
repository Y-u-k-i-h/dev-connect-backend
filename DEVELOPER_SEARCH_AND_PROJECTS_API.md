# Developer Search & Project Visibility API

## Overview
This document describes the new endpoints for:
1. **Developer Search** - Clients can search for developers to message
2. **Available Projects** - Developers can see projects they can claim

---

## 🔍 Developer Search Endpoints

### Base URL
`/api/developers`

### 1. Get All Developers
Get a list of all registered developers.

**Endpoint:** `GET /api/developers`

**Response:**
```json
[
  {
    "developerId": 1,
    "userId": 5,
    "username": "john_dev",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "bio": "Full-stack developer with 5 years experience",
    "skills": "Java, Spring Boot, React, PostgreSQL",
    "hourlyRate": 75.00,
    "githubUrl": "https://github.com/johndoe",
    "linkedinUrl": "https://linkedin.com/in/johndoe",
    "portfolioUrl": "https://johndoe.com",
    "averageRating": 4.8,
    "totalProjectsCompleted": 15,
    "userStatus": "ONLINE"
  }
]
```

### 2. Search Developers (with filters)
Search developers by skills, rating, or hourly rate.

**Endpoint:** `GET /api/developers/search`

**Query Parameters:**
- `skills` (optional) - Search by skills (case-insensitive)
- `minRating` (optional) - Minimum average rating
- `minRate` (optional) - Minimum hourly rate
- `maxRate` (optional) - Maximum hourly rate

**Examples:**

Search by skills:
```bash
GET /api/developers/search?skills=react
GET /api/developers/search?skills=java spring boot
```

Search by minimum rating:
```bash
GET /api/developers/search?minRating=4.5
```

Search by hourly rate range:
```bash
GET /api/developers/search?minRate=50&maxRate=100
```

**Response:** Same as "Get All Developers"

### 3. Get Developer by ID
Get a specific developer's details.

**Endpoint:** `GET /api/developers/{id}`

**Example:**
```bash
GET /api/developers/1
```

**Response:** Single developer object (same structure as above)

### 4. Get Developer by User ID
Get developer details by their user ID.

**Endpoint:** `GET /api/developers/user/{userId}`

**Example:**
```bash
GET /api/developers/user/5
```

**Response:** Single developer object

### 5. Get Developers by Minimum Rating
Get all developers with at least the specified rating.

**Endpoint:** `GET /api/developers/rating/{minRating}`

**Example:**
```bash
GET /api/developers/rating/4.0
```

**Response:** Array of developer objects

---

## 📋 Available Projects Endpoint

### Get Available Projects
Get all unclaimed projects that developers can claim (projects with `devId = null` and `status = PENDING`).

**Endpoint:** `GET /api/projects/available`

**Response:**
```json
[
  {
    "projectId": 1,
    "projectName": "E-commerce Website",
    "devId": null,
    "clientId": 3,
    "description": "Build a modern e-commerce platform with payment integration",
    "status": "PENDING",
    "projectBudget": 5000.00,
    "timeline": "2025-12-31T23:59:59",
    "createdAt": "2025-11-18T10:00:00",
    "updatedAt": "2025-11-18T10:00:00"
  }
]
```

---

## 🎯 Frontend Integration Examples

### For Clients - Search Developers to Message

```javascript
// Get all developers
const getAllDevelopers = async () => {
  const response = await fetch('http://localhost:8081/api/developers', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return await response.json();
};

// Search developers by skills
const searchDevelopersBySkills = async (skillQuery) => {
  const response = await fetch(
    `http://localhost:8081/api/developers/search?skills=${encodeURIComponent(skillQuery)}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  return await response.json();
};

// Search developers with filters
const searchDevelopers = async (filters) => {
  const params = new URLSearchParams();
  if (filters.skills) params.append('skills', filters.skills);
  if (filters.minRating) params.append('minRating', filters.minRating);
  if (filters.minRate) params.append('minRate', filters.minRate);
  if (filters.maxRate) params.append('maxRate', filters.maxRate);

  const response = await fetch(
    `http://localhost:8081/api/developers/search?${params.toString()}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  return await response.json();
};

// Start a conversation with a developer
const startConversation = async (developerId) => {
  const developerDetails = await fetch(
    `http://localhost:8081/api/developers/${developerId}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  
  const developer = await developerDetails.json();
  
  // Navigate to messaging with developer's userId
  navigateToChat(developer.userId);
};
```

### For Developers - View Available Projects

```javascript
// Get all available (unclaimed) projects
const getAvailableProjects = async () => {
  const response = await fetch('http://localhost:8081/api/projects/available', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return await response.json();
};

// Claim a project
const claimProject = async (projectId, devId) => {
  const response = await fetch(
    `http://localhost:8081/api/projects/${projectId}/claim`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ devId })
    }
  );
  
  if (response.status === 409) {
    alert('This project has already been claimed by another developer');
    return null;
  }
  
  return await response.json();
};

// Get my claimed projects
const getMyProjects = async (devId) => {
  const response = await fetch(
    `http://localhost:8081/api/projects/developer/${devId}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  return await response.json();
};
```

---

## 🔧 React Component Examples

### Client - Developer Search Component

```jsx
import React, { useState, useEffect } from 'react';

function DeveloperSearch({ onSelectDeveloper }) {
  const [developers, setDevelopers] = useState([]);
  const [searchSkills, setSearchSkills] = useState('');
  const [loading, setLoading] = useState(false);

  const searchDevelopers = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const url = searchSkills
        ? `http://localhost:8081/api/developers/search?skills=${encodeURIComponent(searchSkills)}`
        : 'http://localhost:8081/api/developers';
      
      const response = await fetch(url, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      
      const data = await response.json();
      setDevelopers(data);
    } catch (error) {
      console.error('Error searching developers:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    searchDevelopers();
  }, []);

  return (
    <div className="developer-search">
      <h2>Find Developers</h2>
      
      <div className="search-bar">
        <input
          type="text"
          placeholder="Search by skills (e.g., React, Java, Python)"
          value={searchSkills}
          onChange={(e) => setSearchSkills(e.target.value)}
        />
        <button onClick={searchDevelopers}>Search</button>
      </div>

      {loading ? (
        <p>Loading...</p>
      ) : (
        <div className="developer-list">
          {developers.map((dev) => (
            <div key={dev.developerId} className="developer-card">
              <h3>{dev.firstName} {dev.lastName}</h3>
              <p><strong>@{dev.username}</strong></p>
              <p>{dev.bio}</p>
              <p><strong>Skills:</strong> {dev.skills}</p>
              <p><strong>Hourly Rate:</strong> ${dev.hourlyRate}</p>
              <p><strong>Rating:</strong> ⭐ {dev.averageRating} ({dev.totalProjectsCompleted} projects)</p>
              <p><strong>Status:</strong> {dev.userStatus}</p>
              
              <button onClick={() => onSelectDeveloper(dev.userId)}>
                💬 Message Developer
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default DeveloperSearch;
```

### Developer - Available Projects Component

```jsx
import React, { useState, useEffect } from 'react';

function AvailableProjects({ currentDevId }) {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAvailableProjects();
  }, []);

  const loadAvailableProjects = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8081/api/projects/available', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      
      const data = await response.json();
      setProjects(data);
    } catch (error) {
      console.error('Error loading projects:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleClaimProject = async (projectId) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(
        `http://localhost:8081/api/projects/${projectId}/claim`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify({ devId: currentDevId })
        }
      );

      if (response.ok) {
        alert('Project claimed successfully!');
        loadAvailableProjects(); // Refresh list
      } else if (response.status === 409) {
        alert('This project has already been claimed');
        loadAvailableProjects();
      }
    } catch (error) {
      console.error('Error claiming project:', error);
      alert('Failed to claim project');
    }
  };

  if (loading) return <p>Loading available projects...</p>;

  return (
    <div className="available-projects">
      <h2>Available Projects</h2>
      
      {projects.length === 0 ? (
        <p>No available projects at the moment</p>
      ) : (
        <div className="project-list">
          {projects.map((project) => (
            <div key={project.projectId} className="project-card">
              <h3>{project.projectName}</h3>
              <p>{project.description}</p>
              <p><strong>Budget:</strong> ${project.projectBudget}</p>
              <p><strong>Deadline:</strong> {new Date(project.timeline).toLocaleDateString()}</p>
              <p><strong>Status:</strong> {project.status}</p>
              
              <button onClick={() => handleClaimProject(project.projectId)}>
                ✅ Claim Project
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default AvailableProjects;
```

---

## 📊 Database Schema Reference

### Projects Table
```sql
project_id    BIGINT (PK)
project_name  VARCHAR
dev_id        BIGINT (NULL when unclaimed)  ← KEY FIELD
client_id     BIGINT (NOT NULL)
description   TEXT
status        ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')
project_budget DECIMAL
timeline      TIMESTAMP
created_at    TIMESTAMP
updated_at    TIMESTAMP
```

**Important:** When `dev_id` is NULL and `status` is 'PENDING', the project is available for developers to claim.

---

## 🚀 Testing Commands

```bash
# Get all developers
curl http://localhost:8081/api/developers

# Search developers by skills
curl "http://localhost:8081/api/developers/search?skills=react"

# Get available projects
curl http://localhost:8081/api/projects/available

# Claim a project
curl -X POST http://localhost:8081/api/projects/1/claim \
  -H "Content-Type: application/json" \
  -d '{"devId": 5}'
```

---

## ✅ Summary

### For Clients:
1. Use `GET /api/developers` or `GET /api/developers/search?skills=...` to find developers
2. Click "Message Developer" to start a conversation using the developer's `userId`

### For Developers:
1. Use `GET /api/projects/available` to see unclaimed projects
2. Use `POST /api/projects/{projectId}/claim` to claim a project
3. Use `GET /api/projects/developer/{devId}` to see your claimed projects

All endpoints are now ready to use! 🎉
