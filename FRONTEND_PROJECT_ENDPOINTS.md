# 🎯 Frontend Project Endpoints Guide

## Problem Summary
- **CLIENT**: Sees "Available" status on their projects ✅ (Correct - means unclaimed)
- **DEVELOPER**: Sees "No projects yet" ❌ (Wrong endpoint being used)

---

## ✅ Correct Endpoints to Use

### For **CLIENTS** (My Projects Page)
```javascript
// Get CLIENT's own projects (that they created)
GET /api/projects/client/{clientId}

// Response includes ALL projects created by this client
// Status "Available" means dev_id=null (not claimed yet)
// Status "In Progress" means dev_id is set (claimed by developer)
```

**Frontend should:**
- Call `/api/projects/client/{clientId}` with the logged-in client's ID
- Display status based on `devId` field:
  - `devId === null` → Show "Available" badge
  - `devId !== null` → Show "In Progress" or "Assigned to Developer X"

---

### For **DEVELOPERS** (Browse Projects Page)
```javascript
// Get AVAILABLE projects that developers can claim
GET /api/projects/available

// Response ONLY includes:
// - Projects where dev_id = null
// - Projects where status = PENDING
```

**Frontend should:**
- Call `/api/projects/available` (NOT `/api/projects/developer/{devId}`)
- This shows all unclaimed projects across ALL clients
- Developer can browse and claim these projects

---

### For **DEVELOPERS** (My Projects Page)
```javascript
// Get developer's CLAIMED projects
GET /api/projects/developer/{devId}

// Response ONLY includes:
// - Projects where dev_id = {this developer's ID}
```

**Frontend should:**
- Call `/api/projects/developer/{devId}` to show "My Projects"
- This is different from "Browse Projects" which uses `/available`

---

## 🔧 Quick Fix for Frontend

### Developer Browse Projects Page
```javascript
// ❌ WRONG - This shows developer's claimed projects
fetch(`/api/projects/developer/${developerId}`)

// ✅ CORRECT - This shows all available projects to claim
fetch('/api/projects/available')
```

### Example Response from `/api/projects/available`
```json
[
  {
    "projectId": 21,
    "projectName": "deks project",
    "devId": null,
    "clientId": 50,
    "description": "good",
    "status": "PENDING",
    "projectBudget": 100.00,
    "timeline": "2025-11-28T00:00:00",
    "createdAt": "2025-11-18T22:36:41.904289",
    "updatedAt": "2025-11-18T22:36:41.904289"
  }
]
```

---

## 📊 Complete Project Flow

### 1. Client Creates Project
```
POST /api/projects/create
Body: { projectName, description, budget, timeline, clientId, devId: null }
→ Project created with status=PENDING, devId=null
```

### 2. Developer Browses Available Projects
```
GET /api/projects/available
→ Returns all projects where devId=null AND status=PENDING
```

### 3. Developer Claims Project
```
POST /api/projects/{projectId}/claim
Body: { devId: 123 }
→ Project updated: devId=123, status=IN_PROGRESS
```

### 4. Developer Views Their Projects
```
GET /api/projects/developer/{devId}
→ Returns all projects where devId={this developer}
```

### 5. Client Views Their Projects
```
GET /api/projects/client/{clientId}
→ Returns ALL projects created by this client
→ Frontend checks devId to show "Available" or "Assigned"
```

---

## 🎨 Frontend Status Display Logic

### For Client's Project List
```javascript
const getProjectStatus = (project) => {
  if (project.devId === null) {
    return { text: "Available", color: "blue", description: "Waiting for developer" };
  } else if (project.status === "IN_PROGRESS") {
    return { text: "In Progress", color: "yellow", description: "Developer working" };
  } else if (project.status === "COMPLETED") {
    return { text: "Completed", color: "green", description: "Project finished" };
  }
};
```

### For Developer's Browse Page
```javascript
// Just fetch and display all available projects
const availableProjects = await fetch('/api/projects/available').then(r => r.json());
// All projects here are claimable by any developer
```

---

## 🚀 Summary

| User Role | Page | Endpoint to Use | What It Returns |
|-----------|------|----------------|-----------------|
| **CLIENT** | My Projects | `/api/projects/client/{clientId}` | All projects created by this client |
| **DEVELOPER** | Browse Projects | `/api/projects/available` | All unclaimed projects (devId=null) |
| **DEVELOPER** | My Projects | `/api/projects/developer/{devId}` | All projects claimed by this developer |

---

## ✅ Backend Status: FULLY WORKING

All endpoints are live and tested:
- ✅ Database migration complete (dev_id allows NULL)
- ✅ Projects can be created without developers
- ✅ `/api/projects/available` returns unclaimed projects
- ✅ `/api/projects/client/{id}` returns client's projects
- ✅ `/api/projects/developer/{id}` returns developer's projects
- ✅ Server running on http://localhost:8081

**The issue is purely frontend routing - developers need to call `/api/projects/available` not `/api/projects/developer/{id}` when browsing new projects to claim.**
