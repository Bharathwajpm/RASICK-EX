const multer = require("multer");

const errorHandler = (err, req, res, next) => {
  const status = err.status || 500;
  const errorName = err.name || "Error";
  const errorMessage = err.message || "Internal server error";

  // Log errors server-side — suppress stack traces in production
  const isProduction = process.env.NODE_ENV === "production";
  if (isProduction) {
    console.error(`[ERROR] ${req.method} ${req.path} → ${status} ${errorName}: ${errorMessage}`);
  } else {
    console.error(`[ERROR LOG]
    Request Path:   ${req.path}
    HTTP Status:    ${status}
    Error Name:     ${errorName}
    Error Message:  ${errorMessage}
    Stack Trace:
${err.stack || "N/A"}
  `);
  }

  if (err instanceof multer.MulterError) {
    const message =
      err.code === "LIMIT_FILE_SIZE"
        ? "Upload too large. Try a smaller file."
        : err.message;
    return res.status(400).json({
      success: false,
      message,
      error: "MulterError",
    });
  }

  if (err.message === "Only audio files are allowed" || err.message === "Only image files are allowed") {
    return res.status(400).json({
      success: false,
      message: err.message,
      error: "ValidationError",
    });
  }

  if (err.name === "ValidationError") {
    return res.status(400).json({
      success: false,
      message: err.message,
      error: "ValidationError",
    });
  }

  if (err.code === 11000) {
    return res.status(409).json({
      success: false,
      message: "Duplicate entry",
      error: "ConflictError",
    });
  }

  if (err.name === "CastError") {
    return res.status(400).json({
      success: false,
      message: "Invalid identifier",
      error: "CastError",
    });
  }

  if (err.type === "entity.too.large") {
    return res.status(413).json({
      success: false,
      message: "Upload too large. Try a smaller file.",
      error: "PayloadTooLargeError",
    });
  }

  res.status(status).json({
    success: false,
    message: errorMessage,
    error: errorName,
  });
};

module.exports = errorHandler;
