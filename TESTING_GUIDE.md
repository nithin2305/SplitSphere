# SplitSphere Testing Guide

## Testing the Application

### Backend Testing

#### Unit Tests
Run all unit tests:
```bash
cd backend
mvn test
```

Expected output: All 8 tests should pass

#### Manual API Testing

1. Start the backend:
```bash
cd backend
mvn spring-boot:run
```

2. Test Registration:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "accountName": "Alice Smith",
    "userId": "alice123",
    "code": "1234"
  }'
```

Expected: JWT token and user info

3. Test Login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "alice123",
    "code": "1234"
  }'
```

4. Create a Group (with JWT token):
```bash
TOKEN="<your-jwt-token>"
curl -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Trip to Paris"
  }'
```

5. Create an Expense:
```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "description": "Dinner",
    "amount": 120.50,
    "groupId": 1,
    "participantUserIds": ["alice123", "bob456"]
  }'
```

### Frontend Testing

1. Start the frontend:
```bash
cd frontend
npm start
```

2. Open browser to `http://localhost:4200`

3. Test User Flow:
   - Register a new user
   - Login
   - Create a group
   - Invite friends (share join code)
   - Add expenses
   - View balances

### Integration Testing

Test complete workflow:

1. Register two users (Alice and Bob)
2. Alice creates a group
3. Bob joins using the join code
4. Alice adds an expense for $100 with both participants
5. Check balances - Bob should owe Alice $50
6. Alice adds another expense for $60 with both participants
7. Check balances - Bob should owe Alice $80 total

### Database Verification

Access H2 Console (development only):
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:splitsphere`
- Username: `sa`
- Password: (leave blank)

Check tables:
- users
- groups
- expenses
- audit_logs

### Security Testing

1. Test JWT Authentication:
   - Try accessing protected endpoints without token → Should get 401
   - Try with invalid token → Should get 401
   - Try with valid token → Should succeed

2. Test Input Validation:
   - Try registering with 3-digit code → Should fail
   - Try registering with non-numeric code → Should fail
   - Try creating expense with negative amount → Should fail

3. Test Authorization:
   - Try joining non-existent group → Should fail
   - Try creating expense in group you're not part of → Should fail

## Troubleshooting

### Backend won't start
- Check if port 8080 is in use: `lsof -i:8080`
- Check Java version: `java -version` (should be 17+)
- Check Maven version: `mvn -version`

### Frontend won't build
- Clear node_modules: `rm -rf node_modules package-lock.json && npm install`
- Check Node version: `node -v` (should be 18+)

### CORS Issues
- Ensure backend CORS is configured for frontend URL
- Check browser console for CORS errors
- Verify `SecurityConfig` allows the frontend origin

### 403 Forbidden on /api/auth/**
- This is a known Spring Security 3.x configuration quirk
- Ensure CSRF is disabled
- Verify `requestMatchers` in SecurityConfig
- Check that JwtAuthenticationFilter doesn't block unauthenticated requests to auth endpoints

## Performance Testing

Test with multiple concurrent users:
```bash
# Using Apache Bench
ab -n 100 -c 10 -T 'application/json' \
  -p register.json \
  http://localhost:8080/api/auth/register
```

## Test Data Scenarios

### Scenario 1: Simple Split
- 2 users, 1 expense
- User A pays $100, split with User B
- Expected: User B owes User A $50

### Scenario 2: Multiple Expenses
- 3 users (A, B, C)
- A pays $90 for all three → B owes $30, C owes $30
- B pays $60 for A and B → A owes B $30
- Net result: B owes A $0, C owes A $30, C owes B $0

### Scenario 3: Complex Group
- 4 users, 10 expenses
- Various payment scenarios
- Verify balance calculations

## Known Issues

1. **Backend 403 on auth endpoints**: In some environments, Spring Security may require additional configuration. Solution: Ensure `@Configuration` is properly scanned.

2. **CORS with credentials**: When `AllowCredentials` is true, `AllowedOrigins` cannot be `*`. Use specific origins.

3. **H2 Console Access**: Requires `frameOptions().disable()` which is less secure. Only use in development.

## Next Steps for Production

1. Switch to PostgreSQL database
2. Configure proper JWT secret (not in code)
3. Add HTTPS/TLS
4. Implement rate limiting
5. Add logging and monitoring
6. Set up CI/CD pipeline
7. Add email notifications
8. Implement data backup strategy
