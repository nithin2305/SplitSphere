# Cloud Build Setup Guide

This guide explains how to set up Cloud Build with Artifact Registry for the SplitSphere backend.

## Prerequisites

1. Google Cloud Project with billing enabled
2. Required APIs enabled:
   - Cloud Build API
   - Artifact Registry API
   - Cloud Resource Manager API

## Setup Steps

### 1. Enable Required APIs

```bash
gcloud services enable cloudbuild.googleapis.com
gcloud services enable artifactregistry.googleapis.com
```

### 2. Create Artifact Registry Repository

Before running Cloud Build, you need to create an Artifact Registry repository:

```bash
# Set your project ID
export PROJECT_ID="your-project-id"
export REGION="us-central1"
export REPOSITORY="splitsphere"

# Create the repository
gcloud artifacts repositories create ${REPOSITORY} \
    --repository-format=docker \
    --location=${REGION} \
    --description="Docker repository for SplitSphere backend"
```

### 3. Grant Cloud Build Permissions

Grant the Cloud Build service account permission to push to Artifact Registry:

```bash
# Get the Cloud Build service account
export PROJECT_NUMBER=$(gcloud projects describe ${PROJECT_ID} --format="value(projectNumber)")
export CLOUD_BUILD_SA="${PROJECT_NUMBER}@cloudbuild.gserviceaccount.com"

# Grant Artifact Registry Writer role
gcloud artifacts repositories add-iam-policy-binding ${REPOSITORY} \
    --location=${REGION} \
    --member="serviceAccount:${CLOUD_BUILD_SA}" \
    --role="roles/artifactregistry.writer"
```

### 4. Create Cloud Build Trigger

You can create a trigger manually in the Cloud Console or using gcloud:

```bash
gcloud builds triggers create github \
    --name="splitsphere-build" \
    --repo-name="SplitSphere" \
    --repo-owner="nithin2305" \
    --branch-pattern="^main$" \
    --build-config="cloudbuild.yaml" \
    --substitutions="_REGION=${REGION},_REPOSITORY=${REPOSITORY}"
```

Or via Cloud Console:
1. Go to Cloud Build > Triggers
2. Click "Create Trigger"
3. Connect your GitHub repository
4. Set the configuration:
   - Name: `splitsphere-build`
   - Event: Push to a branch
   - Source: `^main$` (or your desired branch)
   - Configuration: Cloud Build configuration file
   - Location: `/cloudbuild.yaml`
5. Add substitution variables:
   - `_REGION`: `us-central1` (or your preferred region)
   - `_REPOSITORY`: `splitsphere`

### 5. Test the Build

You can manually trigger a build:

```bash
gcloud builds submit --config=cloudbuild.yaml .
```

Or push to the configured branch to trigger automatically.

## Configuration Details

### Substitution Variables

The `cloudbuild.yaml` uses the following substitution variables:

- `_REGION`: The Artifact Registry region (default: `us-central1`)
- `_REPOSITORY`: The Artifact Registry repository name (default: `splitsphere`)
- `PROJECT_ID`: Automatically provided by Cloud Build
- `SHORT_SHA`: Automatically provided by Cloud Build (git commit SHA)

### Image Tags

The build creates two tags for each image:
1. `splitsphere:${SHORT_SHA}` - Tagged with the git commit SHA
2. `splitsphere:latest` - Tagged as the latest version

### Build Process

The Cloud Build process:
1. Builds the Docker image using `backend/Dockerfile`
2. Tags the image with the commit SHA and "latest"
3. Pushes both tagged images to Artifact Registry

## Troubleshooting

### Error: "Repository does not exist"

This means the Artifact Registry repository hasn't been created. Follow Step 2 above.

### Error: "Permission denied"

The Cloud Build service account doesn't have permission to push. Follow Step 3 above.

### Error: "API not enabled"

Enable the required APIs as shown in Step 1.

## Viewing Build Results

- **Cloud Console**: Go to Cloud Build > History
- **Command Line**: `gcloud builds list`
- **View Logs**: `gcloud builds log <BUILD_ID>`

## Artifact Registry Images

View your images:

```bash
gcloud artifacts docker images list \
    ${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/splitsphere
```

## Deploy the Image

After the image is built and pushed, you can deploy it to:
- Google Cloud Run
- Google Kubernetes Engine (GKE)
- Compute Engine
- Any container orchestration platform

Example Cloud Run deployment:

```bash
gcloud run deploy splitsphere-backend \
    --image=${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/splitsphere:latest \
    --platform=managed \
    --region=${REGION} \
    --allow-unauthenticated
```

## Cost Optimization

- Artifact Registry: Storage and data transfer charges apply
- Cloud Build: 120 build-minutes/day free, then $0.003/build-minute
- Consider using build caching to speed up builds

## Migration from GCR (Legacy Container Registry)

If you were previously using `gcr.io`, Artifact Registry is the recommended replacement:
- More features (vulnerability scanning, artifact management)
- Better access control
- Unified artifact management

To migrate existing GCR images:

```bash
gcr.io/${PROJECT_ID}/image:tag → ${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/image:tag
```

## Security Best Practices

1. Use specific image tags (commit SHAs) for production deployments
2. Enable vulnerability scanning in Artifact Registry
3. Limit service account permissions to least privilege
4. Regularly rotate credentials and review access
5. Use Binary Authorization for deployment policies

## References

- [Cloud Build Documentation](https://cloud.google.com/build/docs)
- [Artifact Registry Documentation](https://cloud.google.com/artifact-registry/docs)
- [Cloud Build Triggers](https://cloud.google.com/build/docs/automating-builds/create-manage-triggers)
