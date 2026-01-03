# SplitSphere API Documentation

Base URL: `http://localhost:8080/api`

## Authentication Endpoints

### Register User
**POST** `/auth/register`

Register a new user account.

**Request Body:**
```json
{
  "accountName": "John Doe",
  "userId": "john123",
  "code": "1234"
}
```

**Response:** (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "john123",
  "accountName": "John Doe"
}
```

**Validation:**
- `accountName`: Required, not blank
- `userId`: Required, unique, not blank
- `code`: Required, exactly 4 digits

**Errors:**
- 400: Validation error (invalid input)
- 409: User ID already exists

---

### Login
**POST** `/auth/login`

Authenticate an existing user.

**Request Body:**
```json
{
  "userId": "john123",
  "code": "1234"
}
```

**Response:** (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "john123",
  "accountName": "John Doe"
}
```

**Errors:**
- 400: Invalid credentials
- 401: Authentication failed

---

## Group Endpoints

**Authentication Required:** All group endpoints require a valid JWT token in the Authorization header.

**Header:**
```
Authorization: Bearer <your-jwt-token>
```

### Create Group
**POST** `/groups`

Create a new expense group.

**Request Body:**
```json
{
  "name": "Trip to Paris"
}
```

**Response:** (200 OK)
```json
{
  "id": 1,
  "name": "Trip to Paris",
  "joinCode": "ABC12XYZ",
  "creatorName": "John Doe",
  "memberNames": ["John Doe"],
  "members": [
    {
      "userId": "john123",
      "accountName": "John Doe"
    }
  ],
  "createdAt": "2026-01-03T10:30:00"
}
```

**Validation:**
- `name`: Required, not blank

---

### Join Group
**POST** `/groups/join/{joinCode}`

Join an existing group using a join code.

**Path Parameters:**
- `joinCode`: 8-character alphanumeric code

**Response:** (200 OK)
```json
{
  "id": 1,
  "name": "Trip to Paris",
  "joinCode": "ABC12XYZ",
  "creatorName": "John Doe",
  "memberNames": ["John Doe", "Jane Smith"],
  "members": [
    {
      "userId": "john123",
      "accountName": "John Doe"
    },
    {
      "userId": "jane456",
      "accountName": "Jane Smith"
    }
  ],
  "createdAt": "2026-01-03T10:30:00"
}
```

**Errors:**
- 404: Invalid join code
- 400: Already a member of this group

---

### Get User's Groups
**GET** `/groups`

Retrieve all groups the authenticated user is a member of.

**Response:** (200 OK)
```json
[
  {
    "id": 1,
    "name": "Trip to Paris",
    "joinCode": "ABC12XYZ",
    "creatorName": "John Doe",
    "memberNames": ["John Doe", "Jane Smith"],
    "members": [...],
    "createdAt": "2026-01-03T10:30:00"
  },
  {
    "id": 2,
    "name": "Apartment Rent",
    "joinCode": "XYZ98ABC",
    "creatorName": "Jane Smith",
    "memberNames": ["Jane Smith", "John Doe", "Bob Wilson"],
    "members": [...],
    "createdAt": "2026-01-02T15:20:00"
  }
]
```

---

### Get Group Details
**GET** `/groups/{groupId}`

Get details of a specific group.

**Path Parameters:**
- `groupId`: Group ID

**Response:** (200 OK)
```json
{
  "id": 1,
  "name": "Trip to Paris",
  "joinCode": "ABC12XYZ",
  "creatorName": "John Doe",
  "memberNames": ["John Doe", "Jane Smith"],
  "members": [...],
  "createdAt": "2026-01-03T10:30:00"
}
```

**Errors:**
- 404: Group not found

---

## Expense Endpoints

### Create Expense
**POST** `/expenses`

Create a new shared expense.

**Request Body:**
```json
{
  "description": "Dinner at restaurant",
  "amount": 120.50,
  "groupId": 1,
  "participantUserIds": ["john123", "jane456", "bob789"]
}
```

**Response:** (200 OK)
```json
{
  "id": 1,
  "description": "Dinner at restaurant",
  "amount": 120.50,
  "payerName": "John Doe",
  "payerUserId": "john123",
  "participantNames": ["John Doe", "Jane Smith", "Bob Wilson"],
  "perPersonAmount": 40.17,
  "createdAt": "2026-01-03T19:30:00"
}
```

**Validation:**
- `description`: Required, not blank
- `amount`: Required, greater than 0.01
- `groupId`: Required, must exist
- `participantUserIds`: Required, at least one participant, all must be group members

**Errors:**
- 400: Validation error
- 403: Not a member of the group
- 404: Group not found or participant not in group

---

### Get Group Expenses
**GET** `/expenses/group/{groupId}`

Retrieve all expenses for a specific group.

**Path Parameters:**
- `groupId`: Group ID

**Response:** (200 OK)
```json
[
  {
    "id": 2,
    "description": "Groceries",
    "amount": 85.30,
    "payerName": "Jane Smith",
    "payerUserId": "jane456",
    "participantNames": ["John Doe", "Jane Smith"],
    "perPersonAmount": 42.65,
    "createdAt": "2026-01-04T10:15:00"
  },
  {
    "id": 1,
    "description": "Dinner at restaurant",
    "amount": 120.50,
    "payerName": "John Doe",
    "payerUserId": "john123",
    "participantNames": ["John Doe", "Jane Smith", "Bob Wilson"],
    "perPersonAmount": 40.17,
    "createdAt": "2026-01-03T19:30:00"
  }
]
```

**Note:** Expenses are returned in reverse chronological order (newest first).

---

## Balance Endpoints

### Get Group Balances
**GET** `/balances/group/{groupId}`

Calculate and retrieve balances for all members relative to the authenticated user.

**Path Parameters:**
- `groupId`: Group ID

**Response:** (200 OK)
```json
[
  {
    "userId": "jane456",
    "userName": "Jane Smith",
    "balance": 15.50,
    "status": "owes"
  },
  {
    "userId": "bob789",
    "userName": "Bob Wilson",
    "balance": 25.00,
    "status": "owed"
  }
]
```

**Interpretation:**
- `status: "owes"`: The user owes you this amount
- `status: "owed"`: You owe the user this amount
- Empty array: All settled up

**Errors:**
- 403: Not a member of the group
- 404: Group not found

---

## Error Responses

All endpoints may return the following error responses:

### 400 Bad Request
```json
{
  "timestamp": "2026-01-03T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/groups"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2026-01-03T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "path": "/api/groups"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2026-01-03T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/api/expenses"
}
```

### 404 Not Found
```json
{
  "timestamp": "2026-01-03T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found",
  "path": "/api/groups/999"
}
```

---

## Authentication Flow

1. **Register** or **Login** to get JWT token
2. Store token in client (localStorage, sessionStorage, etc.)
3. Include token in `Authorization` header for all subsequent requests
4. Token is valid for 24 hours (configurable)

**Example with curl:**
```bash
# 1. Register
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"accountName":"John Doe","userId":"john123","code":"1234"}' \
  | jq -r '.token')

# 2. Create group
curl -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"My Group"}'
```

---

## Rate Limiting

Currently not implemented. For production, consider implementing rate limiting to prevent abuse.

---

## CORS

Currently configured to allow all origins in development. For production, configure specific allowed origins in `SecurityConfig.java`.

---

## Pagination

Currently not implemented. All list endpoints return all results. For large datasets, consider implementing pagination.

---

## Websockets

Not implemented. Balances and expenses are fetched on-demand via HTTP requests.
