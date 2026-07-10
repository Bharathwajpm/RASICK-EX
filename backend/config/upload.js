const path = require("path");
const fs = require("fs");
const multer = require("multer");
const { randomUUID } = require("crypto");

const UPLOAD_TEMP_DIR = path.resolve(process.env.UPLOAD_TEMP_DIR || path.join(__dirname, "..", "uploads", "temp"));

if (!fs.existsSync(UPLOAD_TEMP_DIR)) {
  fs.mkdirSync(UPLOAD_TEMP_DIR, { recursive: true });
}

const storage = multer.diskStorage({
  destination(_req, _file, cb) {
    cb(null, UPLOAD_TEMP_DIR);
  },
  filename(_req, file, cb) {
    const ext = path.extname(file.originalname) || (file.fieldname === "audio" ? ".mp3" : ".jpg");
    cb(null, `${randomUUID()}${ext}`);
  },
});

const SUPPORTED_AUDIO_EXTENSIONS = new Set([".mp3", ".dts", ".ac3", ".wav", ".aac", ".flac"]);
const SUPPORTED_AUDIO_MIME_TYPES = new Set([
  "audio/mpeg",
  "audio/mp3",
  "audio/x-mpeg",
  "audio/x-dts",
  "audio/vnd.dts",
  "audio/vnd.dts.hd",
  "audio/ac3",
  "audio/x-ac3",
  "audio/vnd.dolby.dd-raw",
  "audio/wav",
  "audio/x-wav",
  "audio/flac",
  "audio/x-flac",
  "audio/aac",
  "audio/x-aac",
  "audio/mp4",
  "audio/x-m4a",
  "audio/m4a",
]);

const isSupportedAudioFile = (file) => {
  const ext = path.extname(file.originalname || "").toLowerCase();
  if (SUPPORTED_AUDIO_EXTENSIONS.has(ext)) {
    return true;
  }

  if (SUPPORTED_AUDIO_MIME_TYPES.has(file.mimetype)) {
    return true;
  }

  return false;
};

const fileFilter = (_req, file, cb) => {
  if (file.fieldname === "audio") {
    return isSupportedAudioFile(file)
      ? cb(null, true)
      : cb(new Error("Only .mp3, .dts, .ac3, .wav, .aac, and .flac audio files are allowed"));
  }
  if (file.fieldname === "cover") {
    return file.mimetype.startsWith("image/")
      ? cb(null, true)
      : cb(new Error("Only image files are allowed"));
  }
  cb(new Error("Unexpected upload field"));
};

const upload = multer({
  storage,
  fileFilter,
  limits: { fileSize: parseInt(process.env.MAX_UPLOAD_SIZE, 10) || 50 * 1024 * 1024 },
});

const uploadSongFiles = upload.fields([
  { name: "cover", maxCount: 1 },
  { name: "audio", maxCount: 1 },
]);

module.exports = {
  upload,
  uploadSongFiles,
  UPLOAD_TEMP_DIR,
  isSupportedAudioFile,
};
