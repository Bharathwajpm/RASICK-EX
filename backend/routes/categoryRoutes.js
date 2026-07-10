const express = require("express");
const categoryController = require("../controllers/categoryController");
const { authenticate, requireAdmin } = require("../middleware/auth");

const router = express.Router();

router.get("/", categoryController.getCategories);
router.post("/", authenticate, requireAdmin, categoryController.createCategory);
router.put("/:id", authenticate, requireAdmin, categoryController.updateCategory);
router.delete("/:id", authenticate, requireAdmin, categoryController.deleteCategory);

module.exports = router;
