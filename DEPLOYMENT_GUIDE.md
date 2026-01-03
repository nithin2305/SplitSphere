# SplitSphere Deployment Guide

## Free Deployment Options

This guide covers how to deploy SplitSphere's frontend, backend, and database using free hosting services.

> **📋 Quick Links:**
> - **[Render.com Deployment Guide](backend/RENDER_DEPLOYMENT.md)** - Detailed Render deployment instructions
> - **[Netlify Configuration](netlify.toml)** - Frontend deployment configuration

## Recommended Free Setup

### Option 1: Render.com + Netlify (Recommended) ✅

**Best for**: Testing and low-traffic applications with completely free setup

| Component | Service | Free Tier |
|-----------|---------|-----------|
| Frontend | Netlify | Unlimited bandwidth, continuous deployment |
| Backend | Render.com | Free (sleeps after 15 min inactivity) |
| Database | Render.com | Free PostgreSQL (90 days retention) |

**Pros:**
- ✅ Completely free
- ✅ No credit card required
- ✅ Backend and database on same platform (low latency)
- ✅ Automatic deployment from GitHub
- ✅ Deployment files included (`render.yaml`, `netlify.toml`)

**Cons:**
- ⚠️ Backend sleeps after 15 minutes of inactivity (cold start ~30 seconds)
- ⚠️ Database deleted after 90 days of inactivity
- ⚠️ Limited resources

**👉 [Full Render.com Deployment Guide](backend/RENDER_DEPLOYMENT.md)**

---

### Option 2: Railway.app + Netlify

**Best for**: Production-like setup with good performance

| Component | Service | Free Tier |
|-----------|---------|-----------|
| Frontend | Netlify | 100GB bandwidth/month, unlimited builds |
| Backend | Railway.app | $5 credit/month (~500 hours) |
| Database | Railway.app | PostgreSQL included |

**Pros:**
- Backend stays active (no sleeping)
- PostgreSQL on same platform as backend (low latency)
- Netlify has excellent Angular support
- Simple deployment from GitHub

**Cons:**
- $5/month credit may run out with heavy usage
- Railway requires credit card (no charges until credit exhausted)

---

## Deployment Steps

### 1. Deploy Database (Railway.app)

1. Sign up at [Railway.app](https://railway.app)
2. Create new project → "Provision PostgreSQL"
3. Note the database credentials:
   - `PGHOST`
   - `PGPORT`
   - `PGDATABASE`
   - `PGUSER`
   - `PGPASSWORD`

### 2. Deploy Backend (Railway.app)

1. In Railway, click "New" → "GitHub Repo"
2. Connect your SplitSphere repository
3. Select the `backend` folder as root
4. Add environment variables:
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}
   SPRING_DATASOURCE_USERNAME=${PGUSER}
   SPRING_DATASOURCE_PASSWORD=${PGPASSWORD}
   SPRING_JPA_HIBERNATE_DDL_AUTO=update
   JWT_SECRET=<generate-a-secure-random-string>
   ```
5. Deploy and note your backend URL (e.g., `https://your-app.railway.app`)

### 3. Configure Frontend for Production

Update `frontend/src/environments/environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://your-backend-url.railway.app/api'
};
```

Update Angular build to use production environment:
```json
// angular.json
"configurations": {
  "production": {
    "fileReplacements": [
      {
        "replace": "src/environments/environment.ts",
        "with": "src/environments/environment.prod.ts"
      }
    ]
  }
}
```

### 4. Deploy Frontend (Netlify)

1. Sign up at [Netlify](https://netlify.com)
2. Click "Add new site" → "Import from Git"
3. Connect GitHub and select SplitSphere repository
4. Configure build settings:
   - **Base directory**: `frontend`
   - **Build command**: `npm run build`
   - **Publish directory**: `frontend/dist/frontend`
5. Deploy
6. Note your frontend URL (e.g., `https://your-app.netlify.app`)

### 5. Update Backend CORS

Update `backend/src/main/java/com/splitsphere/config/SecurityConfig.java`:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:4200",
        "https://your-app.netlify.app"  // Add your Netlify URL
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

Commit and push changes to trigger redeployment.

---

## Alternative Deployment Options

### Frontend Alternatives

**Vercel:**
1. Sign up at [Vercel](https://vercel.com)
2. Import GitHub repository
3. Framework preset: Angular
4. Root directory: `frontend`
5. Deploy

**GitHub Pages:**
- Free but requires extra configuration
- No custom API support
- Good for static demo only

### Backend + Database Alternatives

**Render.com (Free Tier):**
1. Create Web Service from GitHub repo
2. Root directory: `backend`
3. Build command: `mvn clean install`
4. Start command: `java -jar target/splitsphere-backend-1.0.0.jar`
5. Add PostgreSQL database in same project
6. Set environment variables

**Fly.io:**
- Free tier: 3 VMs, 3GB storage
- Requires Dockerfile
- Good performance

**Heroku:**
- Limited free tier (eco dynos)
- Easy deployment
- PostgreSQL available

---

## Production Checklist

Before deploying to production:

### Backend
- [ ] Change JWT secret to strong random string (not in code)
- [ ] Update `spring.jpa.hibernate.ddl-auto` to `update` (not `create-drop`)
- [ ] Set up proper logging
- [ ] Configure CORS for production domain
- [ ] Enable HTTPS only
- [ ] Set up database backups
- [ ] Review security settings

### Frontend
- [ ] Build with production flag: `ng build --configuration=production`
- [ ] Update API URL to production backend
- [ ] Enable HTTPS
- [ ] Add error tracking (optional)
- [ ] Optimize bundle size
- [ ] Test all features in production

### Database
- [ ] Set up automatic backups
- [ ] Use strong password
- [ ] Limit network access
- [ ] Monitor storage usage

---

## Cost Estimates

**Completely Free Setup (Render.com):**
- Frontend: Netlify (Free)
- Backend: Render.com (Free, with limitations)
- Database: Render.com (Free, 90-day retention)
- **Total**: $0/month

**Recommended Setup (Railway + Netlify):**
- Frontend: Netlify (Free)
- Backend + DB: Railway.app ($5 credit/month, ~500 hours)
- **Total**: $0/month for light usage, $5/month if credit exhausted

**Paid Alternatives (if scaling):**
- Railway.app: $5/month + usage
- Render.com: $7/month (persistent backend)
- DigitalOcean: $12/month (full droplet)
- AWS/GCP/Azure: Variable pricing

---

## Troubleshooting

### Backend won't start on Railway
- Check logs in Railway dashboard
- Verify environment variables are set
- Ensure `pom.xml` is in backend root
- Check Java version is 17

### Frontend can't connect to backend
- Verify CORS is configured correctly
- Check API URL in environment.prod.ts
- Ensure backend is deployed and running
- Check browser console for CORS errors

### Database connection fails
- Verify PostgreSQL connection string
- Check database credentials
- Ensure database is running
- Test connection from backend logs

### Cold starts on Render.com
- First request after inactivity takes ~30 seconds
- Consider upgrading to paid tier ($7/month) for always-on
- Or switch to Railway.app

---

## Monitoring & Maintenance

**Railway.app:**
- Dashboard shows usage and credits
- Logs available in real-time
- Auto-deploys on git push

**Netlify:**
- Build logs show deployment status
- Analytics available
- Auto-deploys on git push

**Database:**
- Monitor storage usage
- Set up alerts for connection issues
- Regular backups recommended

---

## Next Steps

1. Choose your deployment platform
2. Follow the deployment steps above
3. Test all features in production
4. Share your live URL!

For questions or issues, refer to:
- [Railway.app Docs](https://docs.railway.app)
- [Netlify Docs](https://docs.netlify.com)
- [Render.com Docs](https://render.com/docs)
