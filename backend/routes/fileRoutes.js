const express = require("express");
const fileController = require("../controllers/fileController");

const router = express.Router();

router.get("/audio/:songId", fileController.streamAudio);
router.get("/surround/:songId", fileController.streamSurround);
router.get("/fallback/:songId", fileController.streamFallback);
router.get("/cover/:songId", fileController.streamCover);
router.get("/download/:songId", fileController.downloadSong);

module.exports = router;
