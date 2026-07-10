#!/bin/bash
# RasickEx Oracle Cloud Deployment Helper Script
set -e

echo "==========================================="
echo "Starting Oracle Cloud Production Deployment"
echo "==========================================="

# Define target paths
TARGET_DIR="/opt/rasick"
BACKEND_DIR="$TARGET_DIR/backend"
LOGS_DIR="$TARGET_DIR/logs"
UPLOADS_DIR="$TARGET_DIR/uploads"
BACKUPS_DIR="$TARGET_DIR/backups"

echo "Creating target directories if they don't exist..."
sudo mkdir -p "$TARGET_DIR" "$LOGS_DIR" "$UPLOADS_DIR" "$BACKUPS_DIR"
sudo chown -R $USER:$USER "$TARGET_DIR"

echo "Copying backend files..."
mkdir -p "$BACKEND_DIR"
cp -r . "$BACKEND_DIR/"

cd "$BACKEND_DIR"

echo "Installing production dependencies..."
npm install --production

# Check if .env exists, if not copy from example
if [ ! -f ".env" ]; then
  echo "No .env found, initializing from .env.example..."
  cp .env.example .env
  echo "WARNING: Please edit /opt/rasick/backend/.env with your production credentials!"
fi

echo "Orchestrating server with PM2..."
if pm2 show rasick-backend > /dev/null 2>&1; then
  echo "Restarting existing PM2 process..."
  pm2 restart ecosystem.config.js
else
  echo "Launching new PM2 instance..."
  pm2 start ecosystem.config.js
fi

# Save PM2 process list to restore on boot
pm2 save

echo "==========================================="
echo "Deployment Complete!"
echo "Verify endpoint status: curl http://localhost:5000/health"
echo "==========================================="
