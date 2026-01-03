# Deploying SplitSphere to Render.com

This guide shows you how to deploy the SplitSphere backend to Render.com with PostgreSQL database.

## Prerequisites

- GitHub account with SplitSphere repository
- Render.com account (free tier available)

## Quick Deploy to Render

### Option 1: Using render.yaml (Blueprint - Recommended)

1. **Fork/Push your repository to GitHub**

2. **Go to Render Dashboard**
   - Visit https://dashboard.render.com
   - Click "New +" → "Blueprint"

3. **Connect Repository**
   - Select your SplitSphere repository
   - Render will detect the `backend/render.yaml` file
   - Click "Apply"

4. **Automatic Setup**
   - Render will automatically create:
     - Web Service (Spring Boot backend)
     - PostgreSQL Database
     - Environment variables
   - Wait for deployment to complete (~5-10 minutes)

5. **Get Your Backend URL**
   - After deployment, note your backend URL (e.g., `https://splitsphere-backend.onrender.com`)
   - This URL will be used by your frontend

### Option 2: Manual Deployment

#### Step 1: Create PostgreSQL Database

1. In Render Dashboard, click "New +" → "PostgreSQL"
2. Configure:
   - **Name**: `splitsphere-db`
   - **Database**: `splitsphere`
   - **User**: `splitsphere_user`
   - **Region**: Choose closest to you
   - **Plan**: Free
3. Click "Create Database"
4. Wait for database to be ready
5. Note the connection details (Internal Database URL)

#### Step 2: Deploy Backend

1. Click "New +" → "Web Service"
2. Connect your GitHub repository
3. Configure:
   - **Name**: `splitsphere-backend`
   - **Region**: Same as database
   - **Branch**: `copilot/build-expense-tracking-app` (or your main branch)
   - **Root Directory**: `backend`
   - **Runtime**: Java
   - **Build Command**: `./render-build.sh`
   - **Start Command**: `java -Dserver.port=$PORT -jar target/splitsphere-backend-1.0.0.jar`
   - **Plan**: Free

4. Add Environment Variables:
   - `SPRING_DATASOURCE_URL`: (Copy from database Internal URL - format: `jdbc:postgresql://...`)
   - `SPRING_DATASOURCE_USERNAME`: `splitsphere_user`
   - `SPRING_DATASOURCE_PASSWORD`: (Copy from database)
   - `SPRING_JPA_HIBERNATE_DDL_AUTO`: `update`
   - `SPRING_JPA_DATABASE_PLATFORM`: `org.hibernate.dialect.PostgreSQLDialect`
   - `JWT_SECRET`: (Generate a secure random string - at least 32 characters)
   - `CORS_ALLOWED_ORIGINS`: `https://your-frontend-url.netlify.app,http://localhost:4200`
   - `H2_CONSOLE_ENABLED`: `false`
   - `LOG_LEVEL`: `INFO`

5. Click "Create Web Service"

6. Wait for deployment (~5-10 minutes for first deployment)

## Environment Variables Explained

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://dpg-xxx.oregon-postgres.render.com/splitsphere` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `splitsphere_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | (from Render database) |
| `JWT_SECRET` | Secret key for JWT tokens | `your-very-secure-random-string-here-min-32-chars` |

### Optional Variables

| Variable | Description | Default | Production Value |
|----------|-------------|---------|------------------|
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Database schema management | `create-drop` | `update` |
| `SPRING_JPA_DATABASE_PLATFORM` | Database dialect | H2Dialect | `org.hibernate.dialect.PostgreSQLDialect` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins | `http://localhost:4200` | `https://your-app.netlify.app` |
| `H2_CONSOLE_ENABLED` | Enable H2 console | `true` | `false` |
| `LOG_LEVEL` | Application log level | `DEBUG` | `INFO` |
| `SPRING_JPA_SHOW_SQL` | Show SQL queries in logs | `true` | `false` |

## After Deployment

### 1. Test Your Backend

```bash
# Replace with your actual Render URL
BACKEND_URL="https://splitsphere-backend.onrender.com"

# Test health/registration endpoint
curl -X POST ${BACKEND_URL}/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "accountName": "Test User",
    "userId": "testuser",
    "code": "1234"
  }'
```

### 2. Update Frontend Configuration

Update `frontend/src/environments/environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://your-backend-url.onrender.com/api'
};
```

### 3. Deploy Frontend to Netlify

See main [DEPLOYMENT_GUIDE.md](../DEPLOYMENT_GUIDE.md) for frontend deployment instructions.

### 4. Update CORS

After deploying frontend, update the `CORS_ALLOWED_ORIGINS` environment variable in Render with your Netlify URL.

## Troubleshooting

### Backend won't start

**Check Logs:**
1. Go to Render dashboard → Your service
2. Click "Logs" tab
3. Look for error messages

**Common Issues:**

1. **Build fails:**
   - Ensure `render-build.sh` is executable
   - Check Java version is 17
   - Verify pom.xml is correct

2. **Database connection fails:**
   - Verify `SPRING_DATASOURCE_URL` format: `jdbc:postgresql://host:port/database`
   - Check username and password are correct
   - Ensure database and backend are in same region

3. **Application starts but crashes:**
   - Check for missing environment variables
   - Verify JWT_SECRET is set
   - Check logs for stack traces

### Slow first request (Cold Start)

Render's free tier sleeps after 15 minutes of inactivity:
- First request after inactivity takes ~30-60 seconds
- Consider upgrading to paid tier ($7/month) for always-on service
- Or use Railway.app with $5 monthly credit

### Database connection timeout

- Ensure backend and database are in the same region
- Check firewall rules (shouldn't be an issue on Render)
- Verify database is running

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
