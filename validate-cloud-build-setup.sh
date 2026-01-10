#!/bin/bash

# Cloud Build Setup Validation Script
# This script helps validate that your GCP environment is properly configured for Cloud Build

set -e

echo "🔍 Cloud Build Setup Validator"
echo "=============================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print success
success() {
    echo -e "${GREEN}✓${NC} $1"
}

# Function to print error
error() {
    echo -e "${RED}✗${NC} $1"
}

# Function to print warning
warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Check if gcloud is installed
echo "1. Checking gcloud CLI..."
if command -v gcloud &> /dev/null; then
    success "gcloud CLI is installed"
    GCLOUD_VERSION=$(gcloud version --format="value(core)" 2>/dev/null)
    echo "   Version: $GCLOUD_VERSION"
else
    error "gcloud CLI is not installed"
    echo "   Install from: https://cloud.google.com/sdk/docs/install"
    exit 1
fi
echo ""

# Check if user is authenticated
echo "2. Checking authentication..."
if gcloud auth list --filter=status:ACTIVE --format="value(account)" &> /dev/null; then
    ACTIVE_ACCOUNT=$(gcloud auth list --filter=status:ACTIVE --format="value(account)")
    if [ -n "$ACTIVE_ACCOUNT" ]; then
        success "Authenticated as: $ACTIVE_ACCOUNT"
    else
        error "Not authenticated with gcloud"
        echo "   Run: gcloud auth login"
        exit 1
    fi
else
    error "Unable to check authentication"
    exit 1
fi
echo ""

# Check project configuration
echo "3. Checking project configuration..."
PROJECT_ID=$(gcloud config get-value project 2>/dev/null)
if [ -n "$PROJECT_ID" ]; then
    success "Project ID: $PROJECT_ID"
else
    error "No project configured"
    echo "   Run: gcloud config set project YOUR_PROJECT_ID"
    exit 1
fi
echo ""

# Check required APIs
echo "4. Checking required APIs..."
REQUIRED_APIS=("cloudbuild.googleapis.com" "artifactregistry.googleapis.com")

for API in "${REQUIRED_APIS[@]}"; do
    if gcloud services list --enabled --filter="name:$API" --format="value(name)" 2>/dev/null | grep -q "$API"; then
        success "$API is enabled"
    else
        error "$API is not enabled"
        echo "   Run: gcloud services enable $API"
        exit 1
    fi
done
echo ""

# Check for Artifact Registry repositories
echo "5. Checking Artifact Registry repositories..."
REGION="${REGION:-us-central1}"
REPOSITORY="${REPOSITORY:-splitsphere}"

if gcloud artifacts repositories describe "$REPOSITORY" --location="$REGION" &> /dev/null; then
    success "Repository '$REPOSITORY' exists in $REGION"
    
    # Get repository details
    REPO_FORMAT=$(gcloud artifacts repositories describe "$REPOSITORY" --location="$REGION" --format="value(format)")
    echo "   Format: $REPO_FORMAT"
    
    if [ "$REPO_FORMAT" != "DOCKER" ]; then
        warning "Repository format is $REPO_FORMAT, expected DOCKER"
    fi
else
    error "Repository '$REPOSITORY' not found in $REGION"
    echo "   Create it with:"
    echo "   gcloud artifacts repositories create $REPOSITORY \\"
    echo "       --repository-format=docker \\"
    echo "       --location=$REGION \\"
    echo "       --description='Docker repository for SplitSphere backend'"
    exit 1
fi
echo ""

# Check Cloud Build service account permissions
echo "6. Checking Cloud Build permissions..."
PROJECT_NUMBER=$(gcloud projects describe "$PROJECT_ID" --format="value(projectNumber)")
CLOUD_BUILD_SA="${PROJECT_NUMBER}@cloudbuild.gserviceaccount.com"

success "Cloud Build service account: $CLOUD_BUILD_SA"

# Check if service account has Artifact Registry Writer role
if gcloud artifacts repositories get-iam-policy "$REPOSITORY" --location="$REGION" --format="value(bindings.members)" 2>/dev/null | grep -q "$CLOUD_BUILD_SA"; then
    success "Cloud Build service account has access to Artifact Registry"
else
    warning "Cloud Build service account may not have Artifact Registry Writer role"
    echo "   Grant access with:"
    echo "   gcloud artifacts repositories add-iam-policy-binding $REPOSITORY \\"
    echo "       --location=$REGION \\"
    echo "       --member='serviceAccount:$CLOUD_BUILD_SA' \\"
    echo "       --role='roles/artifactregistry.writer'"
fi
echo ""

# Check for Cloud Build triggers
echo "7. Checking Cloud Build triggers..."
TRIGGER_COUNT=$(gcloud builds triggers list --format="value(name)" 2>/dev/null | wc -l)

if [ "$TRIGGER_COUNT" -gt 0 ]; then
    success "Found $TRIGGER_COUNT Cloud Build trigger(s)"
    echo "   Triggers:"
    gcloud builds triggers list --format="table(name,triggerTemplate.branchName,filename)" 2>/dev/null | sed 's/^/   /'
else
    warning "No Cloud Build triggers found"
    echo "   You may need to create a trigger manually or via gcloud"
fi
echo ""

# Check if cloudbuild.yaml exists
echo "8. Checking cloudbuild.yaml..."
if [ -f "cloudbuild.yaml" ]; then
    success "cloudbuild.yaml found"
    
    # Validate YAML syntax
    if command -v python3 &> /dev/null; then
        if python3 -c "import yaml; yaml.safe_load(open('cloudbuild.yaml'))" 2>/dev/null; then
            success "cloudbuild.yaml is valid YAML"
        else
            error "cloudbuild.yaml has syntax errors"
            exit 1
        fi
    fi
else
    error "cloudbuild.yaml not found in current directory"
    echo "   Make sure you're running this script from the repository root"
    exit 1
fi
echo ""

# Summary
echo "=============================="
echo "✅ Validation Complete!"
echo ""
echo "Your environment is ready for Cloud Build."
echo ""
echo "Next steps:"
echo "1. Update Cloud Build trigger substitution variables:"
echo "   _REGION: $REGION"
echo "   _REPOSITORY: $REPOSITORY"
echo ""
echo "2. Test the build:"
echo "   gcloud builds submit --config=cloudbuild.yaml ."
echo ""
echo "3. Or push to your repository to trigger automatic build"
echo ""
