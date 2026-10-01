const express = require("express");
const fileController = require("../controllers/fileController");
const { authenticate } = require("../middleware/auth");

const router = express.Router();

// All media routes require authentication.
// Media3/ExoPlayer and browser players send the JWT via Authorization header.
router.get("/audio/:songId", authenticate, fileController.streamAudio);
router.get("/surround/:songId", authenticate, fileController.streamSurround);
router.get("/fallback/:songId", authenticate, fileController.streamFallback);
router.get("/cover/:songId", authenticate, fileController.streamCover);
router.get("/download/:songId", authenticate, fileController.downloadSong);

module.exports = router;
