# RASICK-EX Backend — Production Dockerfile
FROM node:22-alpine AS production

WORKDIR /app

# Copy package files and install production dependencies
COPY package.json package-lock.json* ./
RUN npm ci --omit=dev 2>/dev/null || npm install --omit=dev

# Copy application source (see .dockerignore for exclusions)
COPY . .

# Cloud platforms provide PORT; default to 5000
ENV NODE_ENV=production
EXPOSE 5000

# Health check for container orchestrators
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:${PORT:-5000}/health || exit 1

CMD ["node", "server.js"]
