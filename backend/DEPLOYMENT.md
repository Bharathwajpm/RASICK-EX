# RASICK-EX Backend — Cloud Deployment Guide

## Architecture

```
Admin Mobile APK  →  Cloud Backend  ←  TV APK
                        ↓    ↓
                  MongoDB Atlas  Filebase S3
```

## Prerequisites

| Service | Purpose |
|---------|---------|
| **Node.js ≥ 18** | Runtime |
| **MongoDB Atlas** | Database (cloud-hosted) |
| **Filebase** | S3-compatible audio/cover storage |

## Required Environment Variables

Set these in your cloud platform's environment configuration. **Never commit real values.**

| Variable | Required | Description |
|----------|----------|-------------|
| `PORT` | Optional | Server port (cloud provides this) |
| `NODE_ENV` | Yes | Set to `production` |
| `JWT_SECRET` | Yes | Strong random secret: `openssl rand -base64 48` |
| `MONGODB_URI` | Yes | MongoDB Atlas connection string |
| `FILEBASE_ACCESS_KEY` | Yes | Filebase S3 access key |
| `FILEBASE_SECRET_KEY` | Yes | Filebase S3 secret key |
| `FILEBASE_BUCKET` | Optional | S3 bucket name (default: `rasick-music`) |
| `FILEBASE_ENDPOINT` | Optional | S3 endpoint (default: `https://s3.filebase.io`) |
| `CLIENT_URL` | Yes | Comma-separated allowed CORS origins |
| `SEED_ADMIN_USERNAME` | First run | Admin username (seed only) |
| `SEED_ADMIN_PASSWORD` | First run | Admin password (seed only) |
| `SEED_USER_USERNAME` | First run | User username (seed only) |
| `SEED_USER_PASSWORD` | First run | User password (seed only) |

## Build & Start

```bash
# Install dependencies
cd backend
npm install --omit=dev

# Start production server
NODE_ENV=production node server.js
```

## Docker

```bash
cd backend

# Build
docker build -t rasick-ex-backend .

# Run (pass env vars or use --env-file)
docker run -p 5000:5000 \
  -e NODE_ENV=production \
  -e JWT_SECRET="your-secret" \
  -e MONGODB_URI="your-uri" \
  -e FILEBASE_ACCESS_KEY="your-key" \
  -e FILEBASE_SECRET_KEY="your-key" \
  -e CLIENT_URL="https://your-admin-web.com" \
  -e SEED_ADMIN_USERNAME="RASICKEX" \
  -e SEED_ADMIN_PASSWORD="your-password" \
  -e SEED_USER_USERNAME="RASICKEX2026" \
  -e SEED_USER_PASSWORD="your-password" \
  rasick-ex-backend
```

## Health Check

```
GET /health → 200 {"status":"OK"}
```

Cloud platforms should monitor this endpoint.

## CORS Configuration

Set `CLIENT_URL` to a comma-separated list of allowed origins:

```
CLIENT_URL=https://your-admin-web.com,https://another-allowed-origin.com
```

- Mobile/TV apps (no browser origin) are allowed by default.
- In development, all localhost origins are allowed automatically.
- In production, only `CLIENT_URL` origins are allowed.

## Client API URL Configuration

### Web Admin (`VITE_API_URL`)

Set at **build time**:

```bash
VITE_API_URL=https://your-backend.example.com npm run build
```

If web frontend and backend share the same origin (reverse proxy), `VITE_API_URL` can be omitted (relative URLs).

### Android APKs (`BACKEND_URL`)

Configured in `android/shared/build.gradle.kts`:

- **Debug**: `http://10.0.2.2:5000/` (Android emulator localhost)
- **Release**: Update the `release` block with your production URL:

```kotlin
buildConfigField("String", "BACKEND_URL", "\"https://your-backend.example.com/\"")
```

Rebuild the APKs after changing the URL.

## Verify Deployment

```bash
# Health
curl https://your-backend.example.com/health

# Auth
curl -X POST https://your-backend.example.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"RASICKEX","password":"your-admin-password","role":"admin"}'

# Songs
curl https://your-backend.example.com/api/songs

# Artists
curl https://your-backend.example.com/api/artists
```

## Cloud Platforms

The backend is a standard Node.js/Express app. Compatible with:

- **Render** — set Build Command: `npm install`, Start Command: `node server.js`
- **Railway** — auto-detects Node.js, set env vars
- **Fly.io** — use the included Dockerfile
- **Google Cloud Run** — use the included Dockerfile
- **AWS ECS/Fargate** — use the included Dockerfile
- **DigitalOcean App Platform** — set Build/Run commands

## MongoDB Atlas Notes

- Ensure your cloud server's IP is whitelisted in Atlas Network Access
- Or use `0.0.0.0/0` (allow all) if the platform uses dynamic IPs
- Connection uses `serverSelectionTimeoutMS: 5000`
- The server starts accepting HTTP requests immediately while MongoDB connects asynchronously

## Filebase Notes

- Uses S3-compatible API with `forcePathStyle: true`
- Uploaded files are served via authenticated streaming proxy (`/api/files/*`)
- Original audio formats (MP3, FLAC, DTS, AC-3) are preserved without transcoding
- Content-Type is preserved from the original upload
