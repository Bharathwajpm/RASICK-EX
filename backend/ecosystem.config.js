module.exports = {
  apps: [
    {
      name: "rasick-backend",
      script: "server.js",
      cwd: "/opt/rasick/backend",
      watch: false,
      instances: 1,
      exec_mode: "fork",
      log_date_format: "YYYY-MM-DD HH:mm:ss Z",
      error_file: "/opt/rasick/logs/error.log",
      out_file: "/opt/rasick/logs/output.log",
      merge_logs: true,
      env: {
        NODE_ENV: "production",
        PORT: 5000,
        // MONGODB_URI is intentionally NOT set here.
        // It is loaded from backend/.env by dotenv (server.js line 1).
        // Hardcoding it here would override .env and shadow the Atlas URI.
        JWT_SECRET: "your-production-jwt-secret-here",
        UPLOAD_ROOT: "/opt/rasick/uploads",
        UPLOAD_AUDIO_PATH: "/opt/rasick/uploads/audio",
        UPLOAD_COVER_PATH: "/opt/rasick/uploads/covers",
        MAX_UPLOAD_SIZE: 52428800,
        LOG_DIRECTORY: "/opt/rasick/logs",
        BACKUP_DIRECTORY: "/opt/rasick/backups"
      }
    }
  ]
};
