# Quick Start Guide - Project Claim Feature

## 🚀 Quick Test (5 minutes)

### 1. Start the Server
```bash
cd /home/issa/SE/dev-connect-backend
source .env
./gradlew bootRun
```

### 2. Login and Get Token
```bash
curl -X POST http://localhost:8081/api/users/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"issa.abdullah@strathmore.edu","password":"your-password"}' | jq .
```

Copy the `accessToken` from the response.

### 3. Set Environment Variables
```bash
export JWT_TOKEN="paste-your-token-here"
export DEV_ID=26  # Your user ID from login response
```

### 4. Run Tests
```bash
./test-claim-endpoint.sh
```

## 📋 API Quick Reference

### Claim a Project
```bash
POST /api/projects/{projectId}/claim
Body: {"devId": 26}
Headers: Authorization: Bearer <token>
```

**Returns**:
- `200 OK` - Successfully claimed
- `404 NOT FOUND` - Project doesn't exist
- `409 CONFLICT` - Already claimed

### Get Available Projects
```bash
GET /api/projects/status/PENDING
Headers: Authorization: Bearer <token>
```

### Get My Projects (as Developer)
```bash
GET /api/projects/developer/{devId}
Headers: Authorization: Bearer <token>
```

### Get My Projects (as Client)
```bash
GET /api/projects/client/{clientId}
Headers: Authorization: Bearer <token>
```

## 🔍 Troubleshooting

### Server not starting?
- Check if `.env` file exists and has all required variables
- Verify database connection (check school wifi vs hotspot)
- Look at terminal logs for specific errors

### Getting 403 Forbidden?
- Verify JWT token is valid and not expired
- Check token is included in Authorization header: `Bearer <token>`
- Try logging in again to get fresh token

### Getting 409 Conflict unexpectedly?
- Check if project was already claimed: `GET /api/projects/{id}`
- Verify project status in database
- Check server logs for details

## 📱 Frontend Integration

Replace your current claim logic with:

```javascript
async function claimProject(projectId, devId) {
  try {
    const response = await fetch(
      `http://localhost:8081/api/projects/${projectId}/claim`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${getToken()}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ devId })
      }
    );
    
    if (response.status === 409) {
      throw new Error('Project already claimed by another developer');
    }
    
    if (!response.ok) {
      throw new Error('Failed to claim project');
    }
    
    return await response.json();
  } catch (error) {
    console.error('Claim error:', error);
    throw error;
  }
}
```

## ✅ Verification Checklist

- [ ] Server starts without errors
- [ ] Can login and get JWT token
- [ ] Can create new project (status=PENDING, devId=null)
- [ ] Can see project in PENDING list
- [ ] Can claim project successfully
- [ ] Second claim attempt returns 409
- [ ] Claimed project not in PENDING list
- [ ] Claimed project appears in developer's list
- [ ] Project status is IN_PROGRESS after claim

## 🎯 Next Steps

1. Test the atomic claim endpoint thoroughly
2. Update frontend to use new endpoint
3. Test concurrent claims with multiple users
4. Monitor logs for any issues
5. Consider adding rate limiting for production

## 📞 Need Help?

Check these files for more details:
- `CLAIM_FEATURE_IMPLEMENTATION.md` - Full implementation details
- `test-claim-endpoint.sh` - Automated test script
- Server logs - Check terminal running `bootRun`
