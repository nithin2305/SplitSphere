# Deploying SplitSphere to Render.com - Complete Configuration Guide

This comprehensive guide provides step-by-step instructions for deploying the SplitSphere backend to Render.com with PostgreSQL database.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Deployment Options](#deployment-options)
3. [Step-by-Step Configuration](#step-by-step-configuration)
4. [Environment Variables Setup](#environment-variables-setup)
5. [Post-Deployment Configuration](#post-deployment-configuration)
6. [Troubleshooting](#troubleshooting)

## Prerequisites

Before starting the deployment, ensure you have:
- ✅ GitHub account with SplitSphere repository forked/cloned
- ✅ Render.com account (sign up at https://render.com - free tier available)
- ✅ Basic understanding of environment variables
- ✅ Your repository pushed to GitHub

## Deployment Options

Render offers two deployment methods. Choose the one that best fits your needs:

| Method | Complexity | Configuration | Best For |
|--------|-----------|---------------|----------|
| **Blueprint (render.yaml)** | Easy | Automatic | Quick deployment, recommended for beginners |
| **Manual Setup** | Moderate | Manual | Custom configurations, learning purposes |

---

## Step-by-Step Configuration

### Option 1: Using render.yaml (Blueprint - Recommended) ⭐

This is the **easiest and fastest** method. The `render.yaml` file in the backend directory contains all necessary configuration.

#### Step 1: Prepare Your Repository

1. **Ensure your code is on GitHub**
   - If you haven't already, fork the SplitSphere repository
   - Or clone and push to your own GitHub repository
   - Verify the `backend/render.yaml` file exists in your repository

#### Step 2: Access Render Dashboard

1. **Sign in to Render**
   - Navigate to https://dashboard.render.com
   - Click "Sign In" or "Get Started" if you don't have an account
   - You can sign up using GitHub (recommended for easier integration)

   📸 *Dashboard Location: Main dashboard after login*

#### Step 3: Create New Blueprint

1. **Click "New +" button** (top right of dashboard)
2. **Select "Blueprint"** from the dropdown menu

   📸 *Location: New button → Blueprint option*

3. **Connect your GitHub account** (if not already connected)
   - Click "Connect GitHub"
   - Authorize Render to access your repositories
   - Select "All repositories" or "Only select repositories"

#### Step 4: Select Repository

1. **Search and select your SplitSphere repository**
   - Type "SplitSphere" in the search box
   - Click on your repository from the list

2. **Render auto-detects `render.yaml`**
   - You should see: "Blueprint file detected: backend/render.yaml"
   - Review the detected services:
     - ✅ Web Service: `splitsphere-backend`
     - ✅ Database: `splitsphere-db`

   📸 *Location: Blueprint detection screen*

#### Step 5: Configure Blueprint Settings

1. **Review Service Group name** (optional)
   - Default: "SplitSphere"
   - You can keep the default or customize it

2. **Select Branch**
   - Choose the branch to deploy (e.g., `main` or `copilot/build-expense-tracking-app`)
   - This branch will auto-deploy on new commits

#### Step 6: Review Environment Variables

Before clicking "Apply", review the auto-configured environment variables:

1. **Database Connection** (auto-configured):
   - ✅ `SPRING_DATASOURCE_URL` - linked to database
   - ✅ `SPRING_DATASOURCE_USERNAME` - linked to database
   - ✅ `SPRING_DATASOURCE_PASSWORD` - linked to database

2. **Application Settings**:
   - ✅ `SPRING_JPA_HIBERNATE_DDL_AUTO`: `update`
   - ✅ `SPRING_JPA_DATABASE_PLATFORM`: `org.hibernate.dialect.PostgreSQLDialect`
   - ✅ `JAVA_VERSION`: `17`
   - ✅ `MAVEN_VERSION`: `3.9.5`

3. **Security Settings**:
   - ✅ `JWT_SECRET`: Auto-generated secure value
   - ⚠️ `CORS_ALLOWED_ORIGINS`: **NEEDS UPDATE** after frontend deployment

#### Step 7: Apply Blueprint

1. **Click "Apply"** button
2. **Confirm the services** to be created:
   - Web Service: splitsphere-backend
   - PostgreSQL Database: splitsphere-db

3. **Wait for deployment** (~5-10 minutes for first deployment)
   - You'll see real-time logs in the deployment screen
   - Database creation: ~2-3 minutes
   - Backend build and deployment: ~5-7 minutes

   📸 *Location: Deployment progress screen*

#### Step 8: Monitor Deployment

1. **Check Build Logs**:
   ```
   Building Spring Boot application...
   [INFO] Building splitsphere-backend 1.0.0
   [INFO] BUILD SUCCESS
   Build complete!
   ```

2. **Check Deployment Status**:
   - Wait for status to change from "Building" → "Deploying" → "Live"
   - Green checkmark indicates successful deployment

#### Step 9: Get Your Backend URL

1. **Copy Backend URL**:
   - Click on the `splitsphere-backend` service
   - Copy the URL shown at the top (e.g., `https://splitsphere-backend.onrender.com`)
   - **Save this URL** - you'll need it for frontend configuration

   📸 *Location: Service dashboard, URL at the top*

2. **Test the Backend**:
   ```bash
   # Replace with your actual URL
   curl https://your-backend-url.onrender.com/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"accountName":"Test","userId":"test","code":"1234"}'
   ```

✅ **Blueprint deployment complete!** Your backend and database are now running on Render.

### Option 2: Manual Deployment (Step-by-Step)

This method gives you more control over each component and helps you understand the deployment process better.

#### Part A: Create PostgreSQL Database

**Step 1: Navigate to New Database**

1. In Render Dashboard, click **"New +"** button
2. Select **"PostgreSQL"** from the dropdown menu

   📸 *Location: New button → PostgreSQL option*

**Step 2: Configure Database Settings**

1. **Database Name**: `splitsphere-db`
   - This is the identifier in Render dashboard
   
2. **Database**: `splitsphere`
   - This is the actual PostgreSQL database name
   
3. **User**: `splitsphere_user`
   - Default user for database connections
   
4. **Region**: Select closest to your location
   - 🌎 **Oregon (US West)** - West Coast US
   - 🌎 **Ohio (US East)** - East Coast US
   - 🌍 **Frankfurt** - Europe
   - 🌏 **Singapore** - Asia Pacific
   
   ⚠️ **Important**: Remember this region - backend must be in the same region!

5. **PostgreSQL Version**: Leave as default (latest stable)

6. **Instance Type**: Select **"Free"**
   - Free tier: 1GB storage, 90-day retention
   - ⚠️ Database will be deleted after 90 days of inactivity

**Step 3: Create Database**

1. **Click "Create Database"** button
2. **Wait for provisioning** (~2-3 minutes)
   - Status will show "Creating" → "Available"
   
   📸 *Location: Database status indicator*

**Step 4: Save Database Connection Details**

Once the database is created, you'll see the connection details:

1. **Internal Database URL**: 
   ```
   postgresql://splitsphere_user:password@dpg-xxxxx-internal/splitsphere
   ```
   
2. **External Database URL**: 
   ```
   postgresql://splitsphere_user:password@dpg-xxxxx.oregon-postgres.render.com/splitsphere
   ```

3. **Connection Details** (also available):
   - Hostname: `dpg-xxxxx.oregon-postgres.render.com`
   - Port: `5432`
   - Database: `splitsphere`
   - Username: `splitsphere_user`
   - Password: `[generated password]`

**Important Notes**:
- ✅ Use **Internal Database URL** for Render services (faster, free bandwidth)
- ✅ Save the password - you'll need it for environment variables
- ✅ Keep database info tab open for next steps

📸 *Location: Database dashboard → Info tab → Connection String*

---

#### Part B: Deploy Backend Web Service

**Step 1: Create Web Service**

1. Click **"New +"** → **"Web Service"**
2. **Connect GitHub Repository**:
   - If first time: Click "Connect GitHub" and authorize Render
   - Search for "SplitSphere" repository
   - Click "Connect" next to your repository

   📸 *Location: Repository selection screen*

**Step 2: Configure Basic Settings**

Fill in the following configuration:

1. **Name**: `splitsphere-backend`
   - This will be part of your URL: `splitsphere-backend.onrender.com`
   
2. **Region**: **⚠️ MUST be same as database**
   - Select the same region you chose for the database
   
3. **Branch**: Select your deployment branch
   - `main` or `copilot/build-expense-tracking-app`
   - This branch will auto-deploy on new commits
   
4. **Root Directory**: `backend`
   - ⚠️ **IMPORTANT**: Must be set to `backend`
   - This tells Render where to find the Spring Boot application
   
5. **Runtime**: **Java**
   - Render auto-detects from `pom.xml`

**Step 3: Configure Build Settings**

1. **Build Command**: 
   ```bash
   ./render-build.sh
   ```
   - This script runs `mvn clean install -DskipTests`
   
2. **Start Command**:
   ```bash
   java -Dserver.port=$PORT -jar target/splitsphere-backend-1.0.0.jar
   ```
   - `$PORT` is automatically provided by Render
   - Jar file name must match your `pom.xml` configuration

**Step 4: Select Instance Type**

1. **Instance Type**: **Free**
   - Free tier includes:
     - ✅ 750 hours/month (sufficient for always-on on free tier)
     - ⚠️ Sleeps after 15 min inactivity
     - ⚠️ ~30-60 second cold start on first request
   
2. For production, consider **Starter ($7/month)**:
   - ✅ No sleep/cold starts
   - ✅ Always-on service

**Step 5: Configure Environment Variables** 

⚠️ **CRITICAL STEP**: Add the following environment variables:

Click **"Add Environment Variable"** for each:

1. **SPRING_DATASOURCE_URL**
   - **Format**: Must be JDBC format, not standard PostgreSQL format
   - **How to get it**:
     1. Go to your database dashboard
     2. Copy the Internal Database URL: 
        ```
        postgresql://splitsphere_user:password@dpg-xxxxx-internal/splitsphere
        ```
     3. Convert to JDBC format by adding `jdbc:` prefix:
        ```
        jdbc:postgresql://dpg-xxxxx-internal:5432/splitsphere
        ```
   - **Example**:
     ```
     jdbc:postgresql://dpg-abc123-internal:5432/splitsphere
     ```

2. **SPRING_DATASOURCE_USERNAME**
   - **Value**: `splitsphere_user`
   - Copy from database connection info

3. **SPRING_DATASOURCE_PASSWORD**
   - **Value**: Copy the password from database connection info
   - ⚠️ This is the auto-generated password, not your Render password
   
4. **SPRING_JPA_HIBERNATE_DDL_AUTO**
   - **Value**: `update`
   - **Purpose**: Updates database schema automatically
   - ⚠️ **DO NOT USE** `create-drop` in production (will delete data on restart)

5. **SPRING_JPA_DATABASE_PLATFORM**
   - **Value**: `org.hibernate.dialect.PostgreSQLDialect`
   - **Purpose**: Tells Hibernate to use PostgreSQL syntax

6. **JWT_SECRET**
   - **Value**: Generate a secure random string (minimum 32 characters)
   - **How to generate**:
     ```bash
     # On Linux/Mac:
     openssl rand -base64 32
     
     # Or use online generator:
     # https://www.random.org/strings/
     ```
   - **Example**: `xK9mN2pQ7vR4sW8yZ1bC3dE5fG6hJ9k0`
   - ⚠️ **IMPORTANT**: Keep this secret, never commit to code

7. **CORS_ALLOWED_ORIGINS**
   - **Initial Value**: `http://localhost:4200`
   - **After frontend deployed**: Update to include your Netlify URL
   - **Example**: `https://splitsphere.netlify.app,http://localhost:4200`
   - ⚠️ Multiple origins separated by commas (no spaces)

8. **H2_CONSOLE_ENABLED** (Optional but recommended for production)
   - **Value**: `false`
   - **Purpose**: Disables H2 console in production for security

9. **LOG_LEVEL** (Optional)
   - **Value**: `INFO`
   - **Purpose**: Production logging level (not DEBUG)

**Environment Variables Summary**:

| Variable | Value | Notes |
|----------|-------|-------|
| SPRING_DATASOURCE_URL | `jdbc:postgresql://dpg-xxx-internal:5432/splitsphere` | From database |
| SPRING_DATASOURCE_USERNAME | `splitsphere_user` | From database |
| SPRING_DATASOURCE_PASSWORD | `[generated-password]` | From database |
| SPRING_JPA_HIBERNATE_DDL_AUTO | `update` | Schema management |
| SPRING_JPA_DATABASE_PLATFORM | `org.hibernate.dialect.PostgreSQLDialect` | PostgreSQL dialect |
| JWT_SECRET | `[32+ character random string]` | **Generate new!** |
| CORS_ALLOWED_ORIGINS | `http://localhost:4200` | Update after frontend deploy |
| H2_CONSOLE_ENABLED | `false` | Security |
| LOG_LEVEL | `INFO` | Production logging |

📸 *Location: Environment variables section in create web service form*

**Step 6: Create Web Service**

1. **Review all settings**:
   - ✅ Name: splitsphere-backend
   - ✅ Region: Same as database
   - ✅ Root Directory: backend
   - ✅ Build Command: ./render-build.sh
   - ✅ Start Command: java -Dserver.port=$PORT -jar target/splitsphere-backend-1.0.0.jar
   - ✅ All environment variables added

2. **Click "Create Web Service"**

3. **Wait for deployment** (~5-10 minutes for first build)
   - Watch build logs in real-time
   - Status: "Building" → "Deploying" → "Live"

**Step 7: Verify Deployment**

1. **Check Build Logs**:
   ```
   Building Spring Boot application...
   [INFO] Scanning for projects...
   [INFO] Building splitsphere-backend 1.0.0
   [INFO] BUILD SUCCESS
   Build complete!
   ```

2. **Check Deploy Logs**:
   ```
   Starting application...
   Started SplitSphereApplication in X.XXX seconds
   ```

3. **Service Status**: Should show "Live" with green indicator

**Step 8: Get and Test Backend URL**

1. **Copy your backend URL**:
   - Shown at top of service dashboard
   - Format: `https://splitsphere-backend.onrender.com`
   - **Save this URL** for frontend configuration

2. **Test the backend**:
   ```bash
   # Replace with your actual Render URL
   BACKEND_URL="https://splitsphere-backend.onrender.com"
   
   # Test registration endpoint
   curl -X POST ${BACKEND_URL}/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "accountName": "Test User",
       "userId": "testuser",
       "code": "1234"
     }'
   ```

   **Expected Response**:
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiJ9...",
     "userId": "testuser",
     "accountName": "Test User"
   }
   ```

✅ **Manual deployment complete!** Your backend is now running on Render.

---

## Environment Variables Setup

### Complete Reference Table

#### Required Variables (Must Set)

| Variable | Description | Example | How to Get |
|----------|-------------|---------|------------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL in JDBC format | `jdbc:postgresql://dpg-xxx-internal:5432/splitsphere` | Database dashboard → Info → Convert Internal URL to JDBC |
| `SPRING_DATASOURCE_USERNAME` | Database username | `splitsphere_user` | Database dashboard → Info |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `[auto-generated]` | Database dashboard → Info |
| `JWT_SECRET` | Secret key for JWT tokens | `xK9mN2pQ7vR4sW8yZ1bC3dE5fG6hJ9k0` | Generate using `openssl rand -base64 32` |

⚠️ **Important Notes**:
- **SPRING_DATASOURCE_URL**: Must be in `jdbc:postgresql://` format, not plain `postgresql://`
- **JWT_SECRET**: Minimum 32 characters, use cryptographically secure random string
- Never commit these values to your repository

#### Optional Variables (Recommended)

| Variable | Description | Default | Production Value | Impact |
|----------|-------------|---------|------------------|--------|
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Database schema management | `create-drop` | `update` | **Critical**: `create-drop` deletes data on restart |
| `SPRING_JPA_DATABASE_PLATFORM` | Database dialect | H2Dialect | `org.hibernate.dialect.PostgreSQLDialect` | Required for PostgreSQL |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins | `http://localhost:4200` | `https://your-app.netlify.app,http://localhost:4200` | Security: Controls who can access API |
| `H2_CONSOLE_ENABLED` | Enable H2 console | `true` | `false` | Security: Disable in production |
| `LOG_LEVEL` | Application log level | `DEBUG` | `INFO` or `WARN` | Performance: Less verbose in production |
| `SPRING_JPA_SHOW_SQL` | Show SQL queries in logs | `true` | `false` | Performance: Reduce log noise |

### How to Update Environment Variables

**After Initial Deployment**:

1. **Navigate to Service Settings**:
   - Go to Render dashboard
   - Click on your `splitsphere-backend` service
   - Click "Environment" tab in left sidebar

2. **Add/Edit Variable**:
   - Click "Add Environment Variable" for new variables
   - Click edit icon for existing variables
   - Enter key and value
   - Click "Save Changes"

3. **Redeploy** (if needed):
   - Most changes trigger automatic redeploy
   - Manual redeploy: Settings → "Manual Deploy" → "Deploy latest commit"

📸 *Location: Service → Environment tab*

### Environment Variable Checklist

Use this checklist when setting up your Render deployment:

#### Initial Setup
- [ ] `SPRING_DATASOURCE_URL` - Set to JDBC format from database
- [ ] `SPRING_DATASOURCE_USERNAME` - Set to database username  
- [ ] `SPRING_DATASOURCE_PASSWORD` - Set to database password
- [ ] `JWT_SECRET` - Generated secure random string (32+ chars)
- [ ] `SPRING_JPA_HIBERNATE_DDL_AUTO` - Set to `update`
- [ ] `SPRING_JPA_DATABASE_PLATFORM` - Set to PostgreSQL dialect
- [ ] `CORS_ALLOWED_ORIGINS` - Set to `http://localhost:4200` initially
- [ ] `H2_CONSOLE_ENABLED` - Set to `false`
- [ ] `LOG_LEVEL` - Set to `INFO`

#### After Frontend Deployment
- [ ] Update `CORS_ALLOWED_ORIGINS` with Netlify URL
- [ ] Test frontend can connect to backend
- [ ] Verify CORS is working (no console errors)

---

## Post-Deployment Configuration

### Task 1: Test Your Backend API

**Step 1: Verify Backend is Running**

1. **Check Service Status**:
   - Go to Render dashboard → Your service
   - Status should show "Live" with green indicator
   - Note any errors in the Events tab

2. **Test Health Endpoint** (Basic connectivity):
   ```bash
   # Replace with your URL
   curl https://your-backend-url.onrender.com/api/auth/register \
     -X POST \
     -H "Content-Type: application/json" \
     -d '{"accountName":"Test","userId":"test123","code":"1234"}'
   ```

   **Expected**: JSON response with token
   **If fails**: Check logs and environment variables

**Step 2: Test Database Connection**

1. **View Logs** in Render dashboard:
   ```
   ✅ Look for: "Started SplitSphereApplication"
   ✅ Look for: "HHH000204: Processing PersistenceUnitInfo"
   ❌ Avoid: "Connection refused" or "Authentication failed"
   ```

2. **Test User Registration** (creates database entry):
   ```bash
   curl https://your-backend-url.onrender.com/api/auth/register \
     -X POST \
     -H "Content-Type: application/json" \
     -d '{"accountName":"John Doe","userId":"john123","code":"1234"}'
   ```

3. **Verify Database** (optional):
   - Go to database dashboard → "Explore" tab
   - Connect using provided credentials
   - Run: `SELECT * FROM users;`
   - Should see your test user

### Task 2: Update Frontend Configuration

Now that your backend is deployed, configure the frontend to use the production API.

**Step 1: Update Production Environment File**

1. **Open** `frontend/src/environments/environment.prod.ts`

2. **Update the apiUrl**:
   ```typescript
   export const environment = {
     production: true,
     apiUrl: 'https://your-backend-url.onrender.com/api'  // ← Replace with your URL
   };
   ```

   **Example**:
   ```typescript
   export const environment = {
     production: true,
     apiUrl: 'https://splitsphere-backend.onrender.com/api'
   };
   ```

3. **Commit and push changes**:
   ```bash
   git add frontend/src/environments/environment.prod.ts
   git commit -m "Configure production API URL"
   git push
   ```

**Step 2: Verify Angular Build Configuration**

Ensure `angular.json` uses production environment:

```json
"configurations": {
  "production": {
    "fileReplacements": [
      {
        "replace": "src/environments/environment.ts",
        "with": "src/environments/environment.prod.ts"
      }
    ],
    "optimization": true,
    "outputHashing": "all"
  }
}
```

### Task 3: Deploy Frontend to Netlify

**Step 1: Sign Up/Login to Netlify**

1. Go to https://app.netlify.com
2. Sign up with GitHub (recommended) or email
3. Authorize Netlify to access your repositories

**Step 2: Create New Site**

1. Click **"Add new site"** → **"Import from Git"**
2. Choose **"GitHub"**
3. Authorize and select **SplitSphere repository**
4. Click repository from list

**Step 3: Configure Build Settings**

Netlify should auto-detect `netlify.toml` in root directory, but verify:

1. **Base directory**: `frontend`
2. **Build command**: `npm run build` or `ng build --configuration=production`
3. **Publish directory**: `frontend/dist/frontend` or `dist/frontend`
   - Depends on Angular configuration
   - Check `angular.json` → `outputPath`

**Step 4: Deploy**

1. Click **"Deploy site"**
2. Wait for deployment (~2-5 minutes)
3. Watch build logs for any errors

**Step 5: Get Frontend URL**

1. Copy your Netlify URL:
   - Default: `random-name-12345.netlify.app`
   - Can customize: Site settings → Domain management → Custom domain

2. **Save this URL** - you'll need it for CORS configuration

**Step 6: (Optional) Customize Domain**

1. Go to **Site settings** → **Domain management**
2. Click **"Options"** → **"Edit site name"**
3. Change to: `splitsphere.netlify.app` (if available)
4. Or add custom domain

### Task 4: Update Backend CORS Configuration

**Critical Step**: Allow your frontend to communicate with backend.

**Step 1: Update CORS Environment Variable**

1. **Go to Render dashboard**
2. Click your `splitsphere-backend` service
3. Click **"Environment"** tab
4. Find `CORS_ALLOWED_ORIGINS` variable
5. Click **edit icon**

**Step 2: Update Value**

Replace with your Netlify URL:

```
https://your-app.netlify.app,http://localhost:4200
```

**Example**:
```
https://splitsphere.netlify.app,http://localhost:4200
```

**Important Notes**:
- ✅ Include both production and localhost for development
- ✅ Use comma to separate (NO spaces)
- ✅ Include `https://` protocol
- ✅ NO trailing slash

**Step 3: Save and Redeploy**

1. Click **"Save Changes"**
2. Render will automatically redeploy
3. Wait ~2-3 minutes for redeploy
4. Service will restart with new CORS settings

**Step 4: Verify CORS is Working**

1. Open your frontend: `https://your-app.netlify.app`
2. Open browser DevTools (F12) → Console tab
3. Try to login/register
4. **Should NOT see**: "CORS policy" errors
5. **Should see**: Successful API calls

📸 *Location: Backend service → Environment → CORS_ALLOWED_ORIGINS*

---

## Verification & Testing

### Complete Deployment Test

Run through this checklist to ensure everything is working:

#### Backend Tests
- [ ] Backend service shows "Live" status in Render
- [ ] Can access backend URL (not 404)
- [ ] Logs show "Started SplitSphereApplication"
- [ ] Database connection successful (check logs)
- [ ] Test registration endpoint works (returns JWT token)

#### Frontend Tests
- [ ] Frontend deployed successfully on Netlify
- [ ] Can access frontend URL
- [ ] No CORS errors in browser console
- [ ] Can register new user
- [ ] Can login with credentials
- [ ] Can create a group
- [ ] Can add expense
- [ ] Can view balances

#### Integration Tests
```bash
# 1. Register user via backend
curl https://your-backend.onrender.com/api/auth/register \
  -X POST -H "Content-Type: application/json" \
  -d '{"accountName":"Test","userId":"test","code":"1234"}'

# 2. Login via backend
curl https://your-backend.onrender.com/api/auth/login \
  -X POST -H "Content-Type: application/json" \
  -d '{"userId":"test","code":"1234"}'

# 3. Test from frontend UI
# - Open https://your-app.netlify.app
# - Register/Login
# - Create group, add expense
```

### Common Issues Checklist

If something doesn't work, check:

- [ ] Environment variables are all set correctly
- [ ] CORS_ALLOWED_ORIGINS includes your Netlify URL
- [ ] SPRING_DATASOURCE_URL is in JDBC format (starts with `jdbc:postgresql://`)
- [ ] Database and backend are in same region
- [ ] Frontend environment.prod.ts has correct backend URL
- [ ] Both services show "Live" status
- [ ] No error in service logs

---

## Troubleshooting

### Issue 1: Backend Won't Start

**Symptoms**: Service shows "Failed" or keeps restarting

**How to Diagnose**:

1. **Check Logs**:
   - Go to Render dashboard → Your service
   - Click "Logs" tab
   - Look for error messages

**Common Error 1: Build Fails**

```
Error: ./render-build.sh: Permission denied
```

**Solution**:
```bash
# Make build script executable
chmod +x backend/render-build.sh
git add backend/render-build.sh
git commit -m "Make render-build.sh executable"
git push
```

**Common Error 2: Java Version Mismatch**

```
Error: Java version 11 found, but Spring Boot requires 17
```

**Solution**:
- Add environment variable: `JAVA_VERSION` = `17`
- Verify `system.properties` has: `java.runtime.version=17`

**Common Error 3: JAR File Not Found**

```
Error: Unable to access jarfile target/splitsphere-backend-1.0.0.jar
```

**Solution**:
- Check `pom.xml` → `<finalName>` matches JAR name in start command
- Verify build command: `./render-build.sh`
- Check build logs for Maven errors

**Common Error 4: Port Already in Use**

```
Error: Web server failed to start. Port 8080 was already in use
```

**Solution**:
- Verify start command uses `$PORT` environment variable:
  ```bash
  java -Dserver.port=$PORT -jar target/splitsphere-backend-1.0.0.jar
  ```

### Issue 2: Database Connection Fails

**Symptoms**: Backend starts but can't connect to database

**Error Messages**:
```
Error: Connection to localhost:5432 refused
Error: FATAL: password authentication failed
Error: database "splitsphere" does not exist
```

**Solution Checklist**:

1. **Verify Database URL Format**:
   - ❌ Wrong: `postgresql://dpg-xxx-internal/splitsphere`
   - ✅ Correct: `jdbc:postgresql://dpg-xxx-internal:5432/splitsphere`
   - Must start with `jdbc:` and include port `:5432`

2. **Check Credentials**:
   ```bash
   # Go to database dashboard → Info tab
   # Copy exact values:
   - Username: splitsphere_user (NOT your Render username)
   - Password: [auto-generated password] (NOT your Render password)
   ```

3. **Verify Region Matching**:
   - Database region: Check in database dashboard
   - Backend region: Check in web service settings
   - **Must be the same** (e.g., both in Oregon)

4. **Use Internal URL**:
   - ✅ Use: Internal Database URL (ends with `-internal`)
   - ❌ Avoid: External Database URL (costs bandwidth)

5. **Check Database Status**:
   - Go to database dashboard
   - Status should be "Available" (green)
   - If "Suspended", upgrade to paid tier or create new database

**Test Database Connection**:
```bash
# From your local machine (using external URL)
psql postgresql://splitsphere_user:PASSWORD@dpg-xxx.oregon-postgres.render.com/splitsphere

# Should connect and show prompt:
# splitsphere=>
```

### Issue 3: CORS Errors

**Symptoms**: Frontend can't connect to backend

**Error in Browser Console**:
```
Access to XMLHttpRequest at 'https://backend.onrender.com/api/auth/login' 
from origin 'https://your-app.netlify.app' has been blocked by CORS policy
```

**Solution Steps**:

1. **Check CORS_ALLOWED_ORIGINS Variable**:
   - Go to Render → Backend service → Environment
   - Find `CORS_ALLOWED_ORIGINS`
   - Should include your Netlify URL

2. **Correct Format**:
   ```
   ✅ Correct: https://splitsphere.netlify.app,http://localhost:4200
   ❌ Wrong: https://splitsphere.netlify.app/, http://localhost:4200
   ❌ Wrong: splitsphere.netlify.app,localhost:4200
   ```

   **Rules**:
   - Include `https://` or `http://` protocol
   - NO trailing slash
   - NO spaces after comma
   - Comma-separated for multiple origins

3. **Save and Redeploy**:
   - After updating, wait for automatic redeploy
   - Or manually redeploy: Settings → Manual Deploy

4. **Clear Browser Cache**:
   ```
   - Hard refresh: Ctrl+Shift+R (Windows/Linux) or Cmd+Shift+R (Mac)
   - Or open DevTools → Network tab → "Disable cache"
   ```

5. **Verify in Network Tab**:
   - Open DevTools → Network tab
   - Try login again
   - Click the failed request
   - Check Response Headers for:
     ```
     Access-Control-Allow-Origin: https://your-app.netlify.app
     ```

### Issue 4: Frontend Build Fails on Netlify

**Symptoms**: Netlify deployment fails during build

**Common Errors**:

1. **"Command not found: ng"**
   ```
   Solution: Ensure package.json has @angular/cli in devDependencies
   ```

2. **"Module not found"**
   ```
   Solution: Run `npm install` locally, commit package-lock.json
   ```

3. **"Build directory not found"**
   ```
   Solution: Check angular.json → outputPath matches Netlify publish directory
   ```

### Issue 5: Slow First Request (Cold Start)

**Symptoms**: First request after inactivity takes 30-60 seconds

**Explanation**:
- Render's free tier sleeps after 15 minutes of inactivity
- Service needs to "wake up" on first request
- Subsequent requests are fast

**Solutions**:

1. **Upgrade to Paid Tier** ($7/month):
   - Always-on service
   - No cold starts
   - Better performance

2. **Use Ping Service** (free workaround):
   - Use UptimeRobot or Cron-job.org
   - Ping your backend every 10-14 minutes
   - Keeps service awake
   - ⚠️ Uses build minutes

3. **Accept the Limitation**:
   - Warn users about first-load delay
   - Add loading indicator
   - Good for testing/demo purposes

### Issue 6: Database Connection Timeout

**Symptoms**: Backend starts but times out connecting to database

**Solutions**:

1. **Check Region Matching**:
   - Ensure backend and database are in the same region
   - Different regions = higher latency and potential timeouts

2. **Verify Database is Running**:
   - Go to database dashboard
   - Check status is "Available"
   - Look for any alerts or warnings

3. **Check Connection Pool Settings**:
   - For high traffic, may need to configure Hikari connection pool
   - Add to application.properties if needed

4. **Firewall Rules**:
   - Shouldn't be an issue on Render (same network)
   - If using external database, check security groups

---

## Performance Tips

1. **Use Production Settings:**
   - Set `SPRING_JPA_SHOW_SQL=false`
   - Set `LOG_LEVEL=INFO` or `WARN`
   - Set `SPRING_JPA_HIBERNATE_DDL_AUTO=update` (not `create-drop`)

2. **Database Optimization:**
   - Create indexes on frequently queried columns
   - Monitor database size (free tier: 1GB)

3. **Monitoring:**
   - Check Render dashboard for resource usage
   - Monitor response times
   - Set up uptime monitoring (e.g., UptimeRobot)

## Updating Your Deployment

Render automatically deploys when you push to your connected branch:

1. Make changes locally
2. Commit and push to GitHub
3. Render automatically detects changes and redeploys
4. Monitor deployment in Render dashboard

## Cost & Limits

**Free Tier Limits:**
- 750 hours/month for web services
- Backend sleeps after 15 min inactivity
- 1GB PostgreSQL database
- 400 build minutes/month

**Paid Options:**
- Starter ($7/month): Always-on backend, no sleep
- Standard ($25/month): More resources, better performance
- PostgreSQL Starter ($7/month): 10GB database

## Migration from Free to Paid

1. Go to service settings
2. Change plan tier
3. Update will take effect immediately
4. No downtime required

## Support

- Render Docs: https://render.com/docs
- Render Community: https://community.render.com
- SplitSphere Issues: GitHub repository issues

## Security Checklist

Before going live:
- [ ] Set strong JWT_SECRET (32+ characters)
- [ ] Set CORS_ALLOWED_ORIGINS to only your frontend domain
- [ ] Set H2_CONSOLE_ENABLED=false
- [ ] Set LOG_LEVEL=INFO (not DEBUG)
- [ ] Review database backup strategy
- [ ] Enable HTTPS (automatic on Render)
- [ ] Monitor logs for suspicious activity

---

## Quick Reference Guide

### Essential URLs

| Resource | URL Pattern | Example |
|----------|-------------|---------|
| Render Dashboard | https://dashboard.render.com | N/A |
| Backend URL | https://SERVICE-NAME.onrender.com | https://splitsphere-backend.onrender.com |
| Frontend URL | https://SITE-NAME.netlify.app | https://splitsphere.netlify.app |
| Database Connection | Internal: dpg-XXX-internal:5432 | Use in SPRING_DATASOURCE_URL |

### Critical Environment Variables

Quick copy-paste reference:

```bash
# Required
SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-xxx-internal:5432/splitsphere
SPRING_DATASOURCE_USERNAME=splitsphere_user
SPRING_DATASOURCE_PASSWORD=[from-database-dashboard]
JWT_SECRET=[generate-with: openssl rand -base64 32]

# Production Settings
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
CORS_ALLOWED_ORIGINS=https://your-app.netlify.app,http://localhost:4200
H2_CONSOLE_ENABLED=false
LOG_LEVEL=INFO
```

### Common Commands

**Test Backend**:
```bash
# Health check
curl https://your-backend.onrender.com/api/auth/register \
  -X POST -H "Content-Type: application/json" \
  -d '{"accountName":"Test","userId":"test","code":"1234"}'
```

**Generate JWT Secret**:
```bash
openssl rand -base64 32
```

**Make Script Executable**:
```bash
chmod +x backend/render-build.sh
git add backend/render-build.sh
git commit -m "Make build script executable"
git push
```

### Deployment Checklist

#### Initial Setup
- [ ] 1. Create PostgreSQL database on Render
- [ ] 2. Note database connection details
- [ ] 3. Create Web Service on Render
- [ ] 4. Set all environment variables
- [ ] 5. Wait for deployment to complete
- [ ] 6. Test backend API endpoints
- [ ] 7. Update frontend environment.prod.ts
- [ ] 8. Deploy frontend to Netlify
- [ ] 9. Update CORS_ALLOWED_ORIGINS with Netlify URL
- [ ] 10. Test end-to-end functionality

#### Before Going Live
- [ ] All environment variables set correctly
- [ ] CORS configured for production domain only
- [ ] JWT_SECRET is strong and secure
- [ ] Database backups configured
- [ ] Logs checked for errors
- [ ] Frontend and backend tested together
- [ ] Security checklist completed

### Troubleshooting Quick Reference

| Issue | Quick Fix |
|-------|-----------|
| Build fails | Check `render-build.sh` is executable: `chmod +x` |
| Database connection fails | Verify JDBC format: `jdbc:postgresql://...` |
| CORS errors | Update `CORS_ALLOWED_ORIGINS`, no trailing slash |
| Slow first request | Free tier cold start - upgrade to paid tier |
| JWT errors | Check `JWT_SECRET` is set (32+ chars) |
| Frontend can't connect | Verify `environment.prod.ts` has correct backend URL |

### Support Resources

- 📚 **Render Docs**: https://render.com/docs
- 💬 **Render Community**: https://community.render.com
- 📝 **Netlify Docs**: https://docs.netlify.com
- 🐛 **SplitSphere Issues**: https://github.com/nithin2305/SplitSphere/issues
- 🔧 **Spring Boot Docs**: https://spring.io/projects/spring-boot

---

## Summary

You've successfully configured and deployed SplitSphere to Render.com! 🎉

**What you've accomplished**:
- ✅ Deployed Spring Boot backend to Render
- ✅ Configured PostgreSQL database
- ✅ Set up environment variables
- ✅ Deployed Angular frontend to Netlify
- ✅ Configured CORS for frontend-backend communication
- ✅ Tested the complete application

**Next steps**:
1. Share your live URL with users
2. Monitor logs and performance
3. Consider upgrading to paid tier if needed
4. Set up database backups
5. Implement monitoring and alerts

For questions or issues, refer to the troubleshooting section or create an issue on GitHub.
