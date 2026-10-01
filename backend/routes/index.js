const express = require("express");
const authRoutes = require("./authRoutes");
const songRoutes = require("./songRoutes");
const categoryRoutes = require("./categoryRoutes");
const adminRoutes = require("./adminRoutes");
const artistRoutes = require("./artistRoutes");
const albumRoutes = require("./albumRoutes");
const fileRoutes = require("./fileRoutes");

const router = express.Router();

router.use("/auth", authRoutes);
router.use("/songs", songRoutes);
router.use("/categories", categoryRoutes);
router.use("/admin", adminRoutes);
router.use("/artists", artistRoutes);
router.use("/albums", albumRoutes);
router.use("/files", fileRoutes);

module.exports = router;
