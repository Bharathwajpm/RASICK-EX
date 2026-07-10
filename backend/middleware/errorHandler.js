const multer = require("multer");

const errorHandler = (err, req, res, next) => {
  console.error(err);

  if (err instanceof multer.MulterError) {
    const message =
      err.code === "LIMIT_FILE_SIZE"
        ? "Upload too large. Try a smaller file."
        : err.message;
    return res.status(400).json({ message });
  }

  if (err.message === "Only audio files are allowed" || err.message === "Only image files are allowed") {
    return res.status(400).json({ message: err.message });
  }

  if (err.name === "ValidationError") {
    return res.status(400).json({ message: err.message });
  }

  if (err.code === 11000) {
    return res.status(409).json({ message: "Duplicate entry" });
  }

  if (err.name === "CastError") {
    return res.status(400).json({ message: "Invalid identifier" });
  }

  if (err.type === "entity.too.large") {
    return res.status(413).json({ message: "Upload too large. Try a smaller file." });
  }

  res.status(err.status || 500).json({
    message: err.message || "Internal server error",
  });
};

module.exports = errorHandler;
