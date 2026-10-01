require("dotenv").config();

// ─── Validate required secrets before anything else ─────────────────────────
const { validateSecrets } = require("./config/secrets");
try {
  validateSecrets();
} catch (err) {
  console.error(err.message);
  process.exit(1);
}
// ─────────────────────────────────────────────────────────────────────────────

// ─── Process-level crash guards ─────────────────────────────────────────────
// Without these, an unhandled promise rejection during a Filebase upload will
// crash the Node process entirely, closing the TCP socket mid-request and
// causing "ECONNRESET" on the Vite proxy and "Unexpected end of JSON input"
// on the frontend — with no HTTP error response ever sent.
process.on("uncaughtException", (err) => {
  console.error("[FATAL] Uncaught Exception — server will attempt to continue:");
  console.error(`  name:    ${err.name}`);
  console.error(`  message: ${err.message}`);
  console.error(err.stack);
});

process.on("unhandledRejection", (reason) => {
  console.error("[FATAL] Unhandled Promise Rejection — server will attempt to continue:");
  console.error(reason);
});
// ─────────────────────────────────────────────────────────────────────────────

const express = require("express");
const cors = require("cors");
const rateLimit = require("express-rate-limit");
const connectDB = require("./config/db");
const seedDatabase = require("./config/seed");
const apiRoutes = require("./routes");
const errorHandler = require("./middleware/errorHandler");

const app = express();
const PORT = process.env.PORT || 5000;
const isProduction = process.env.NODE_ENV === "production";

// ─── Security headers (lightweight, no helmet dependency) ───────────────────
app.disable("x-powered-by");
app.use((req, res, next) => {
  res.setHeader("X-Content-Type-Options", "nosniff");
  res.setHeader("X-Frame-Options", "DENY");
  if (isProduction) {
    res.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
  }
  next();
});

// ─── CORS ───────────────────────────────────────────────────────────────────
const defaultOrigins = ["http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173"];
const allowedOrigins = process.env.CLIENT_URL
  ? process.env.CLIENT_URL.split(",").map((o) => o.trim())
  : defaultOrigins;

app.use(
  cors({
    origin(origin, callback) {
      // Allow requests with no origin (mobile apps, curl, server-to-server)
      if (!origin) {
        callback(null, true);
        return;
      }
      // Always check the explicit whitelist
      if (allowedOrigins.includes(origin)) {
        callback(null, true);
        return;
      }
      // In development only, also allow any localhost/127.0.0.1 origin
      if (!isProduction && /^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?$/.test(origin)) {
        callback(null, true);
        return;
      }
      callback(null, false);
    },
    credentials: true,
  })
);

// ─── Rate limiting on auth-sensitive routes ─────────────────────────────────
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 20,                   // max 20 login attempts per window per IP
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    success: false,
    message: "Too many login attempts. Please try again later.",
    error: "RateLimitError",
  },
});

app.use(express.json({ limit: "50mb" }));
app.use(express.urlencoded({ extended: true, limit: "50mb" }));

// Local uploads are migrated to Filebase S3; static serving of local uploads is disabled.

app.get("/health", (req, res) => {
  res.json({ status: "OK" });
});

// Apply rate limiter to auth routes only
app.use("/api/auth", authLimiter);

app.use("/api", apiRoutes);

app.use((req, res) => {
  res.status(404).json({
    success: false,
    message: "Route not found",
    error: "NotFoundError"
  });
});

app.use(errorHandler);

// Start listening immediately to prevent proxy timeouts during cold starts
const server = app.listen(PORT, "0.0.0.0", () => {
  console.log(`RASICK-EX backend running on http://0.0.0.0:${PORT}`);
});

// Connect to MongoDB and seed database asynchronously (using Mongoose command buffering)
const initDatabase = async () => {
  try {
    await connectDB();
    await seedDatabase();
  } catch (error) {
    console.error("[FATAL] Database initialization failed:", error.message);
  }
};

initDatabase();

// ─── Graceful shutdown ──────────────────────────────────────────────────────
const shutdown = (signal) => {
  console.log(`\n[${signal}] Graceful shutdown initiated...`);
  server.close(() => {
    console.log("[Shutdown] HTTP server closed.");
    const mongoose = require("mongoose");
    mongoose.connection.close(false).then(() => {
      console.log("[Shutdown] MongoDB connection closed.");
      process.exit(0);
    }).catch(() => {
      process.exit(0);
    });
  });

  // Force exit after 10 seconds if graceful shutdown stalls
  setTimeout(() => {
    console.error("[Shutdown] Forced exit after timeout.");
    process.exit(1);
  }, 10000).unref();
};

process.on("SIGTERM", () => shutdown("SIGTERM"));
process.on("SIGINT", () => shutdown("SIGINT"));

