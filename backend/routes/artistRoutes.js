const express = require("express");
const artistController = require("../controllers/artistController");
const { authenticate, requireAdmin } = require("../middleware/auth");

const router = express.Router();

// Public — browse artists
router.get("/", artistController.getArtists);
router.get("/:id", artistController.getArtist);

// Admin only — manage artists
router.post("/", authenticate, requireAdmin, artistController.createArtist);
router.put("/:id", authenticate, requireAdmin, artistController.updateArtist);
router.delete("/:id", authenticate, requireAdmin, artistController.deleteArtist);

module.exports = router;
