const express = require("express");
const artistController = require("../controllers/artistController");

const router = express.Router();

router.get("/", artistController.getArtists);

module.exports = router;
