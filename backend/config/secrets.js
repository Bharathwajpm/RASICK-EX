/**
 * Centralized secret access — never import secrets directly from process.env
 * in controllers/middleware. This module validates presence and refuses to
 * return insecure defaults.
 */

let _jwtSecret = null;

/**
 * Returns the JWT signing secret from environment variables.
 * Throws immediately if JWT_SECRET is not configured.
 */
const getJwtSecret = () => {
  if (_jwtSecret) return _jwtSecret;

  const secret = process.env.JWT_SECRET;

  if (!secret || secret.trim().length === 0) {
    throw new Error(
      "[SECURITY] JWT_SECRET is not set in environment variables. " +
      "Server cannot sign or verify tokens. Set JWT_SECRET in your .env file."
    );
  }

  // Reject obviously placeholder/weak values in production
  const isProduction = process.env.NODE_ENV === "production";
  const weakPatterns = [
    "change-this",
    "fallback-secret",
    "use-a-secure",
    "your-secret-here",
    "secret",
    "12345",
  ];
  if (isProduction && weakPatterns.some((p) => secret.toLowerCase().includes(p))) {
    throw new Error(
      "[SECURITY] JWT_SECRET appears to be a weak placeholder. " +
      "Generate a strong secret: openssl rand -base64 48"
    );
  }

  _jwtSecret = secret;
  return _jwtSecret;
};

/**
 * Validates all required secrets at startup.
 * Call this during server initialization before accepting requests.
 */
const validateSecrets = () => {
  const required = [
    { key: "JWT_SECRET", label: "JWT signing secret" },
    { key: "MONGODB_URI", label: "MongoDB connection string" },
  ];

  const missing = [];

  for (const { key, label } of required) {
    const value = process.env[key];
    if (!value || value.trim().length === 0) {
      missing.push(`  • ${key} — ${label}`);
    }
  }

  if (missing.length > 0) {
    const message =
      "[SECURITY] Required environment variables are not set:\n" +
      missing.join("\n") +
      "\n\nCopy .env.example to .env and fill in all required values.";
    console.error(message);
    throw new Error(message);
  }

  // Validate JWT_SECRET strength (will throw if weak in production)
  getJwtSecret();

  // Warn (but don't block) on missing optional Filebase credentials
  if (!process.env.FILEBASE_ACCESS_KEY || !process.env.FILEBASE_SECRET_KEY) {
    console.warn(
      "[WARN] FILEBASE_ACCESS_KEY / FILEBASE_SECRET_KEY not set. " +
      "Uploads and streaming from Filebase S3 will fail."
    );
  }

  console.log("[Security] All required secrets validated successfully.");
};

module.exports = {
  getJwtSecret,
  validateSecrets,
};
