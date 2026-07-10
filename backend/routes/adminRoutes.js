const express = require("express");
const adminController = require("../controllers/adminController");
const { authenticate, requireAdmin } = require("../middleware/auth");

const router = express.Router();

router.get("/dashboard", authenticate, requireAdmin, adminController.getDashboard);

module.exports = router;
