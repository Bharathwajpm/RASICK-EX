const express = require("express");
const songController = require("../controllers/songController");
const { authenticate, requireAdmin } = require("../middleware/auth");
const { uploadSongFiles } = require("../config/upload");

const router = express.Router();

// User: fetch songs for home, search, library, downloads
router.get("/", songController.getSongs);

// Admin: upload, edit, delete
router.post("/", authenticate, requireAdmin, uploadSongFiles, songController.createSong);
router.put("/:id", authenticate, requireAdmin, songController.updateSong);
router.delete("/:id", authenticate, requireAdmin, songController.deleteSong);

module.exports = router;
