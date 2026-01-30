#!/bin/bash

# Build frontend and copy to backend static directory
echo "Building frontend..."
cd frontend
npm run build

echo "Frontend build completed successfully!"
echo "Output directory: ../backend/rocketmq-dashboard-api/src/main/resources/static"
