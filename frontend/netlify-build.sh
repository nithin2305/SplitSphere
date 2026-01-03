#!/bin/bash

# Netlify build script for Angular frontend

echo "Installing dependencies..."
cd frontend
npm install

echo "Building Angular application for production..."
npm run build -- --configuration=production

echo "Build complete!"
