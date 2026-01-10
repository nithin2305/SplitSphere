# SplitSphere

A comprehensive expense splitting and tracking application built with Angular frontend and Spring Boot backend.

## Features

- **User Management**: Register and login with userId and 4-digit code
- **Group Management**: Create groups and invite members via join codes
- **Expense Tracking**: Add shared expenses and split them among group members
- **Real-time Balances**: View who owes whom with automatic calculation
- **Audit Logging**: Track all actions for accountability
- **Proper Accounting**: Accurate balance calculations and expense splitting

## Technology Stack

### Backend
- Spring Boot 3.2.1
- Java 17
- Spring Data JPA
- Spring Security with JWT
- H2 Database (development) / PostgreSQL (production)
- Maven

### Frontend
- Angular 17
- TypeScript
- RxJS
- HttpClient

## Getting Started

### Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- Maven 3.6 or higher
- npm or yarn

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

   The backend will start on `http://localhost:8080`

4. Access H2 Console (development only):
   - URL: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:splitsphere`
   - Username: `sa`
   - Password: (leave blank)

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

   The frontend will start on `http://localhost:4200`

### Running Tests

#### Backend Tests
```bash
cd backend
mvn test
```

#### Frontend Tests
```bash
cd frontend
npm test
```

## Usage

1. **Register**: Create an account with your name, a unique userId, and a 4-digit code
2. **Login**: Use your userId and 4-digit code to log in
3. **Create Group**: Create a new group for shared expenses
4. **Invite Members**: Share the join code with others to add them to your group
5. **Add Expenses**: Record expenses and select who should split the cost
6. **View Balances**: See who owes whom in real-time

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and receive JWT token

### Groups
- `GET /api/groups` - Get user's groups
- `POST /api/groups` - Create a new group
- `POST /api/groups/join/{joinCode}` - Join a group
- `GET /api/groups/{groupId}` - Get group details

### Expenses
- `POST /api/expenses` - Create a new expense
- `GET /api/expenses/group/{groupId}` - Get expenses for a group

### Balances
- `GET /api/balances/group/{groupId}` - Get balances for a group

## Security

- JWT-based authentication
- BCrypt password encoding for 4-digit codes
- CORS configuration for frontend-backend communication
- Input validation on all endpoints

## Database Schema

### Users
- id, accountName, userId (unique), code (encrypted), createdAt

### Groups
- id, name, joinCode (unique), creator, createdAt

### Expenses
- id, description, amount, payer, group, participants, createdAt

### Audit Logs
- id, action, entityType, entityId, user, details, timestamp

## Deployment

### Cloud Build with Google Cloud Platform

For deploying to Google Cloud Platform using Cloud Build and Artifact Registry, see the [Cloud Build Setup Guide](CLOUD_BUILD_SETUP.md).

### Other Deployment Options

For alternative deployment options (Render, Railway, Netlify), see:
- [Deployment Guide](DEPLOYMENT_GUIDE.md) - Comprehensive deployment options
- [Quick Deploy](QUICK_DEPLOY.md) - Quick deployment to Render.com

## License

This project is open source and available under the MIT License.
