#!/bin/bash
# RasickEx Production Data Backup Helper Script
set -e

# Load environment configuration values
ENV_FILE="/opt/rasick/backend/.env"
if [ -f "$ENV_FILE" ]; then
    export $(cat "$ENV_FILE" | grep -v '^#' | xargs)
fi

BACKUP_DIR="${BACKUP_DIRECTORY:-/opt/rasick/backups}"
UPLOAD_DIR="${UPLOAD_ROOT:-/opt/rasick/uploads}"
MONGO_URI="${MONGODB_URI:-mongodb://127.0.0.1:27017/rasick-ex}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo "==========================================="
echo "Starting Production Backup: $TIMESTAMP"
echo "==========================================="

mkdir -p "$BACKUP_DIR"

# 1. Backup MongoDB database
echo "Dumping database collections..."
mongodump --uri="$MONGO_URI" --out="$BACKUP_DIR/mongo_$TIMESTAMP"

# 2. Archive file structures
echo "Compressing uploaded files..."
tar -czf "$BACKUP_DIR/uploads_$TIMESTAMP.tar.gz" -C "$UPLOAD_DIR" .

# 3. Clean archives older than 7 days
echo "Removing old backup archives..."
find "$BACKUP_DIR" -type f -name "*.tar.gz" -mtime +7 -delete
find "$BACKUP_DIR" -type d -name "mongo_*" -mtime +7 -exec rm -rf {} +

echo "==========================================="
echo "Backup execution finished successfully!"
echo "Database dump: $BACKUP_DIR/mongo_$TIMESTAMP"
echo "Uploaded files: $BACKUP_DIR/uploads_$TIMESTAMP.tar.gz"
echo "==========================================="
