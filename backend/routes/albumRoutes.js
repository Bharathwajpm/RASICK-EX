const express = require("express");
const albumController = require("../controllers/albumController");
const { authenticate, requireAdmin } = require("../middleware/auth");

const router = express.Router();

// Public — browse albums
router.get("/", albumController.getAlbums);
router.get("/:id", albumController.getAlbum);

// Admin only — manage albums
router.post("/", authenticate, requireAdmin, albumController.createAlbum);
router.put("/:id", authenticate, requireAdmin, albumController.updateAlbum);
router.delete("/:id", authenticate, requireAdmin, albumController.deleteAlbum);

module.exports = router;
