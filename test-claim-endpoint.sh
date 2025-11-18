#!/bin/bash
# Test script for Project Claim functionality
# Run this after the server is started

BASE_URL="http://localhost:8081/api"

echo "========================================="
echo "PROJECT CLAIM FUNCTIONALITY TEST"
echo "========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# First, you need to login and get a JWT token
echo "${YELLOW}Step 1: Login to get JWT token${NC}"
echo "Please run this first to get your JWT token:"
echo "curl -X POST ${BASE_URL}/users/login -H 'Content-Type: application/json' -d '{\"email\":\"your-email\",\"password\":\"your-password\"}'"
echo ""
echo "Then set your token:"
echo "export JWT_TOKEN=\"your-jwt-token-here\""
echo "export DEV_ID=26  # Your developer/user ID"
echo ""

# Check if JWT_TOKEN is set
if [ -z "$JWT_TOKEN" ]; then
    echo "${RED}JWT_TOKEN not set. Please login first and export JWT_TOKEN${NC}"
    exit 1
fi

if [ -z "$DEV_ID" ]; then
    echo "${RED}DEV_ID not set. Please export DEV_ID with your developer ID${NC}"
    exit 1
fi

echo "${YELLOW}Step 2: Get all PENDING projects${NC}"
echo "GET /projects/status/PENDING"
PENDING_PROJECTS=$(curl -s -X GET "${BASE_URL}/projects/status/PENDING" \
  -H "Authorization: Bearer ${JWT_TOKEN}")
echo "$PENDING_PROJECTS" | jq .
echo ""

# Extract first project ID
PROJECT_ID=$(echo "$PENDING_PROJECTS" | jq -r '.[0].projectId')
if [ "$PROJECT_ID" = "null" ] || [ -z "$PROJECT_ID" ]; then
    echo "${YELLOW}No PENDING projects found. Creating one...${NC}"
    
    # Create a test project
    NEW_PROJECT=$(curl -s -X POST "${BASE_URL}/projects/create" \
      -H "Authorization: Bearer ${JWT_TOKEN}" \
      -H "Content-Type: application/json" \
      -d "{
        \"projectName\": \"Test Project for Claiming\",
        \"clientId\": ${DEV_ID},
        \"description\": \"This is a test project to verify claim functionality\",
        \"projectBudget\": 500.00,
        \"timeline\": \"2025-12-31T23:59:59\"
      }")
    
    PROJECT_ID=$(echo "$NEW_PROJECT" | jq -r '.projectId')
    echo "Created project: $PROJECT_ID"
    echo "$NEW_PROJECT" | jq .
    echo ""
fi

echo "${YELLOW}Step 3: Claim project ${PROJECT_ID}${NC}"
echo "POST /projects/${PROJECT_ID}/claim"
CLAIM_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "${BASE_URL}/projects/${PROJECT_ID}/claim" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"devId\": ${DEV_ID}}")

HTTP_STATUS=$(echo "$CLAIM_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
CLAIM_BODY=$(echo "$CLAIM_RESPONSE" | sed '/HTTP_STATUS/d')

if [ "$HTTP_STATUS" = "200" ]; then
    echo "${GREEN}✓ Claim successful!${NC}"
    echo "$CLAIM_BODY" | jq .
else
    echo "${RED}✗ Claim failed with status $HTTP_STATUS${NC}"
    echo "$CLAIM_BODY" | jq .
fi
echo ""

echo "${YELLOW}Step 4: Try to claim the same project again (should get 409 Conflict)${NC}"
echo "POST /projects/${PROJECT_ID}/claim"
CLAIM_AGAIN=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "${BASE_URL}/projects/${PROJECT_ID}/claim" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"devId\": ${DEV_ID}}")

HTTP_STATUS2=$(echo "$CLAIM_AGAIN" | grep "HTTP_STATUS" | cut -d: -f2)
CLAIM_BODY2=$(echo "$CLAIM_AGAIN" | sed '/HTTP_STATUS/d')

if [ "$HTTP_STATUS2" = "409" ]; then
    echo "${GREEN}✓ Correctly returned 409 Conflict!${NC}"
    echo "$CLAIM_BODY2" | jq .
else
    echo "${RED}✗ Expected 409 but got $HTTP_STATUS2${NC}"
    echo "$CLAIM_BODY2" | jq .
fi
echo ""

echo "${YELLOW}Step 5: Verify project is no longer in PENDING list${NC}"
echo "GET /projects/status/PENDING"
PENDING_AFTER=$(curl -s -X GET "${BASE_URL}/projects/status/PENDING" \
  -H "Authorization: Bearer ${JWT_TOKEN}")
FOUND=$(echo "$PENDING_AFTER" | jq ".[] | select(.projectId == $PROJECT_ID)")
if [ -z "$FOUND" ]; then
    echo "${GREEN}✓ Project ${PROJECT_ID} is not in PENDING list${NC}"
else
    echo "${RED}✗ Project ${PROJECT_ID} is still in PENDING list${NC}"
fi
echo ""

echo "${YELLOW}Step 6: Verify project is in developer's projects${NC}"
echo "GET /projects/developer/${DEV_ID}"
DEV_PROJECTS=$(curl -s -X GET "${BASE_URL}/projects/developer/${DEV_ID}" \
  -H "Authorization: Bearer ${JWT_TOKEN}")
FOUND_IN_DEV=$(echo "$DEV_PROJECTS" | jq ".[] | select(.projectId == $PROJECT_ID)")
if [ -n "$FOUND_IN_DEV" ]; then
    echo "${GREEN}✓ Project ${PROJECT_ID} found in developer's projects${NC}"
    echo "$FOUND_IN_DEV" | jq .
else
    echo "${RED}✗ Project ${PROJECT_ID} not found in developer's projects${NC}"
fi
echo ""

echo "========================================="
echo "TEST COMPLETE"
echo "========================================="
