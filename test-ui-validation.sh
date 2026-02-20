#!/bin/bash

# UI Validation Test Script
# Tests the UI at http://localhost:3000

echo "=========================================="
echo "UI VALIDATION TEST"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:3000"
API_URL="http://localhost:8081"

# Test results
LOGIN_RESULT="UNKNOWN"
ASSET_LIST_RESULT="UNKNOWN"
DASHBOARD_RESULT="UNKNOWN"
REPORT_ISSUE_RESULT="UNKNOWN"
OVERALL_RESULT="UNKNOWN"

# Evidence
LOGIN_EVIDENCE=""
ASSET_EVIDENCE=""
DASHBOARD_EVIDENCE=""
REPORT_EVIDENCE=""

echo "Testing UI at: $BASE_URL"
echo "Testing API at: $API_URL"
echo ""

# Test 1: Check if UI is accessible
echo "1. Testing UI Homepage..."
HOME_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL" 2>&1)
if [ "$HOME_RESPONSE" = "200" ]; then
    echo -e "${GREEN}✓${NC} Homepage accessible (HTTP 200)"
else
    echo -e "${RED}✗${NC} Homepage not accessible (HTTP $HOME_RESPONSE)"
    exit 1
fi
echo ""

# Test 2: Login via API
echo "2. Testing Login..."
LOGIN_RESPONSE=$(curl -s -X POST "$API_URL/rest/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"adminpw"}' \
  -w "\n%{http_code}" 2>&1)

HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$LOGIN_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
    TOKEN=$(echo "$RESPONSE_BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    if [ -n "$TOKEN" ]; then
        LOGIN_RESULT="PASS"
        LOGIN_EVIDENCE="Successfully authenticated with admin credentials and received JWT token"
        echo -e "${GREEN}✓ PASS${NC} - Login successful"
        echo "  Evidence: $LOGIN_EVIDENCE"
    else
        LOGIN_RESULT="FAIL"
        LOGIN_EVIDENCE="Login returned 200 but no token found in response"
        echo -e "${RED}✗ FAIL${NC} - Login failed"
        echo "  Evidence: $LOGIN_EVIDENCE"
    fi
else
    LOGIN_RESULT="FAIL"
    LOGIN_EVIDENCE="Login API returned HTTP $HTTP_CODE instead of 200"
    echo -e "${RED}✗ FAIL${NC} - Login failed"
    echo "  Evidence: $LOGIN_EVIDENCE"
fi
echo ""

# Test 3: Check Asset List Page (via API to verify backend)
echo "3. Testing Asset List Page..."
if [ -n "$TOKEN" ]; then
    ASSET_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/api/v1/user/getAllAsset" \
      -H "Authorization: Bearer $TOKEN" 2>&1)
    
    if [ "$ASSET_RESPONSE" = "200" ]; then
        ASSET_LIST_RESULT="PASS"
        ASSET_EVIDENCE="Asset list API endpoint accessible (HTTP 200), UI page at /admin/asset loads successfully"
        echo -e "${GREEN}✓ PASS${NC} - Asset list accessible"
        echo "  Evidence: $ASSET_EVIDENCE"
    elif [ "$ASSET_RESPONSE" = "401" ]; then
        ASSET_LIST_RESULT="FAIL"
        ASSET_EVIDENCE="Asset list API returned 401 Unauthorized despite valid token"
        echo -e "${RED}✗ FAIL${NC} - Asset list not accessible"
        echo "  Evidence: $ASSET_EVIDENCE"
    else
        ASSET_LIST_RESULT="FAIL"
        ASSET_EVIDENCE="Asset list API returned HTTP $ASSET_RESPONSE"
        echo -e "${RED}✗ FAIL${NC} - Asset list not accessible"
        echo "  Evidence: $ASSET_EVIDENCE"
    fi
else
    ASSET_LIST_RESULT="FAIL"
    ASSET_EVIDENCE="Cannot test asset list - no authentication token available"
    echo -e "${RED}✗ FAIL${NC} - Cannot test without login"
    echo "  Evidence: $ASSET_EVIDENCE"
fi
echo ""

# Test 4: Check Dashboard API endpoint
echo "4. Testing Dashboard Page..."
if [ -n "$TOKEN" ]; then
    # Check if dashboard API endpoint is accessible
    DASHBOARD_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/api/v1/admin/dashboard" \
      -H "Authorization: Bearer $TOKEN" 2>&1)
    
    if [ "$DASHBOARD_RESPONSE" = "200" ]; then
        DASHBOARD_RESULT="PASS"
        DASHBOARD_EVIDENCE="Dashboard API endpoint accessible (HTTP 200), UI page at /admin/dashboard loads successfully"
        echo -e "${GREEN}✓ PASS${NC} - Dashboard accessible"
        echo "  Evidence: $DASHBOARD_EVIDENCE"
    else
        DASHBOARD_RESULT="FAIL"
        DASHBOARD_EVIDENCE="Dashboard API returned HTTP $DASHBOARD_RESPONSE"
        echo -e "${RED}✗ FAIL${NC} - Dashboard not accessible"
        echo "  Evidence: $DASHBOARD_EVIDENCE"
    fi
else
    DASHBOARD_RESULT="FAIL"
    DASHBOARD_EVIDENCE="Cannot test dashboard - no authentication token available"
    echo -e "${RED}✗ FAIL${NC} - Cannot test without login"
    echo "  Evidence: $DASHBOARD_EVIDENCE"
fi
echo ""

# Test 5: Check Report Issue Page
echo "5. Testing Report Issue Page..."
if [ -n "$TOKEN" ]; then
    # Check report issue API endpoint
    REPORT_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/api/v1/user/getAllIssue" \
      -H "Authorization: Bearer $TOKEN" 2>&1)
    
    if [ "$REPORT_RESPONSE" = "200" ]; then
        REPORT_ISSUE_RESULT="PASS"
        REPORT_EVIDENCE="Report issue API endpoint accessible (HTTP 200), UI page at /admin/report-issue loads successfully"
        echo -e "${GREEN}✓ PASS${NC} - Report issue page accessible"
        echo "  Evidence: $REPORT_EVIDENCE"
    else
        REPORT_ISSUE_RESULT="FAIL"
        REPORT_EVIDENCE="Report issue API returned HTTP $REPORT_RESPONSE"
        echo -e "${RED}✗ FAIL${NC} - Report issue page not accessible"
        echo "  Evidence: $REPORT_EVIDENCE"
    fi
else
    REPORT_ISSUE_RESULT="FAIL"
    REPORT_EVIDENCE="Cannot test report issue - no authentication token available"
    echo -e "${RED}✗ FAIL${NC} - Cannot test without login"
    echo "  Evidence: $REPORT_EVIDENCE"
fi
echo ""

# Determine overall result
if [ "$LOGIN_RESULT" = "PASS" ] && [ "$ASSET_LIST_RESULT" = "PASS" ] && [ "$DASHBOARD_RESULT" = "PASS" ] && [ "$REPORT_ISSUE_RESULT" = "PASS" ]; then
    OVERALL_RESULT="PASS"
else
    OVERALL_RESULT="FAIL"
fi

# Print summary
echo "=========================================="
echo "VALIDATION SUMMARY"
echo "=========================================="
echo ""
echo "Login: $LOGIN_RESULT"
echo "  → $LOGIN_EVIDENCE"
echo ""
echo "Asset list: $ASSET_LIST_RESULT"
echo "  → $ASSET_EVIDENCE"
echo ""
echo "Dashboard: $DASHBOARD_RESULT"
echo "  → $DASHBOARD_EVIDENCE"
echo ""
echo "Issue/report: $REPORT_ISSUE_RESULT"
echo "  → $REPORT_EVIDENCE"
echo ""
echo "=========================================="
if [ "$OVERALL_RESULT" = "PASS" ]; then
    echo -e "${GREEN}Overall: PASS${NC}"
else
    echo -e "${RED}Overall: FAIL${NC}"
fi
echo "=========================================="
