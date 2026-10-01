const Category = require("../models/Category");
const Song = require("../models/Song");
const { formatCategory } = require("../config/formatters");

const refreshSongCounts = async () => {
  const categories = await Category.find();
  await Promise.all(
    categories.map(async (category) => {
      const count = await Song.countDocuments({ category: category.name });
      category.songCount = count;
      await category.save();
    })
  );
};

exports.getCategories = async (req, res, next) => {
  try {
    const categories = await Category.find().sort({ name: 1 });
    const counts = await Song.aggregate([
      { $match: { isActive: true, category: { $exists: true, $ne: "" } } },
      { $group: { _id: "$category", count: { $sum: 1 } } },
    ]);
    const countByName = Object.fromEntries(counts.map((row) => [row._id, row.count]));

    res.json({
      success: true,
      data: {
        categories: categories.map((category) =>
          formatCategory({
            ...category.toObject(),
            songCount: countByName[category.name] || 0,
          })
        ),
      },
    });
  } catch (error) {
    next(error);
  }
};

exports.createCategory = async (req, res, next) => {
  try {
    const { name, color } = req.body;

    if (!name || !name.trim()) {
      return res.status(400).json({
        success: false,
        message: "Category name is required",
        error: "BadRequestError",
      });
    }

    const category = await Category.create({
      name: name.trim(),
      color: color || "from-blue-500 to-indigo-600",
    });

    res.status(201).json({
      success: true,
      data: {
        category: formatCategory(category),
      },
    });
  } catch (error) {
    next(error);
  }
};

exports.updateCategory = async (req, res, next) => {
  try {
    const { id } = req.params;
    const category = await Category.findById(id);

    if (!category) {
      return res.status(404).json({
        success: false,
        message: "Category not found",
        error: "NotFoundError",
      });
    }

    if (req.body.name) {
      const oldName = category.name;
      category.name = req.body.name.trim();

      if (oldName !== category.name) {
        await Song.updateMany({ category: oldName }, { category: category.name });
      }
    }

    if (req.body.color) {
      category.color = req.body.color;
    }

    await category.save();
    await refreshSongCounts();

    res.json({
      success: true,
      data: {
        category: formatCategory(category),
      },
    });
  } catch (error) {
    next(error);
  }
};

exports.deleteCategory = async (req, res, next) => {
  try {
    const { id } = req.params;
    const category = await Category.findByIdAndDelete(id);

    if (!category) {
      return res.status(404).json({
        success: false,
        message: "Category not found",
        error: "NotFoundError",
      });
    }

    await Song.updateMany({ category: category.name }, { $unset: { category: "" } });

    res.json({
      success: true,
      data: {
        message: "Category deleted successfully",
      },
    });
  } catch (error) {
    next(error);
  }
};
