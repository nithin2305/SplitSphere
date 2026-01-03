# Quick Deploy to Render.com

## 🚀 Deploy in 5 Minutes

### Prerequisites
- GitHub account
- Render.com account (free)

### Step 1: Deploy Backend + Database (2 minutes)

1. **Go to Render Dashboard**: https://dashboard.render.com
2. Click **"New +"** → **"Blueprint"**
3. Connect your GitHub repository
4. Render detects `backend/render.yaml` automatically
5. Click **"Apply"**
6. Wait 5-10 minutes for deployment
7. **Copy your backend URL** (e.g., `https://splitsphere-backend.onrender.com`)

### Step 2: Update Frontend Configuration (1 minute)

Edit `frontend/src/environments/environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://YOUR-BACKEND-URL.onrender.com/api'  // ← Paste your URL here
};
```

Commit and push:
```bash
git add frontend/src/environments/environment.prod.ts
git commit -m "Update production API URL"
git push
```

### Step 3: Deploy Frontend to Netlify (2 minutes)

1. **Go to Netlify**: https://app.netlify.com
2. Click **"Add new site"** → **"Import from Git"**
3. Connect GitHub and select SplitSphere repository
4. Netlify detects `netlify.toml` automatically
5. Click **"Deploy"**
6. Wait 2-3 minutes
7. **Copy your frontend URL** (e.g., `https://splitsphere.netlify.app`)

### Step 4: Update CORS (1 minute)

1. Go back to **Render Dashboard**
2. Click your backend service → **"Environment"** tab
3. Update `CORS_ALLOWED_ORIGINS`:
   ```
   https://YOUR-FRONTEND-URL.netlify.app,http://localhost:4200
   ```
4. Service will auto-redeploy

### ✅ Done! 

Visit your frontend URL and start using SplitSphere!

---

## Environment Variables (Render Backend)

These are set automatically by `render.yaml`, but you can customize:

| Variable | Default | Description |
|----------|---------|-------------|
| `JWT_SECRET` | Auto-generated | **IMPORTANT**: Generate a secure random string |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | Schema management |
| `CORS_ALLOWED_ORIGINS` | localhost | Update with your Netlify URL |

---

## Troubleshooting

### Backend deployment fails
- Check logs in Render dashboard
- Ensure Java 17 is specified
- Verify `render-build.sh` exists

### Frontend can't connect to backend
- Verify `environment.prod.ts` has correct backend URL
- Check CORS is updated with Netlify URL
- Ensure backend is running (check Render dashboard)

### Database connection fails
- Render automatically creates and connects PostgreSQL
- Check environment variables in Render dashboard
- Database is created in same region as backend

---

## Next Steps

- Test your application
- Share your live URL! 🎉
- Monitor usage in Render/Netlify dashboards
- See [RENDER_DEPLOYMENT.md](backend/RENDER_DEPLOYMENT.md) for advanced configuration

---

## Cost: $0/month

Both Render and Netlify offer free tiers:
- ✅ Frontend: Unlimited (Netlify)
- ✅ Backend: Free with sleep after 15 min (Render)
- ✅ Database: 1GB PostgreSQL (Render)

**Note**: Backend will sleep after 15 minutes of inactivity. First request after sleep takes ~30 seconds.

Upgrade to Render Starter ($7/month) for always-on backend.
