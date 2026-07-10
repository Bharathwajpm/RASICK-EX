const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const User = require("../models/User");

/**
 * POST /api/auth/login
 *
 * Request body (matches login UI state fields):
 * {
 *   "username": "admin",
 *   "password": "admin123",
 *   "role": "admin" | "user"
 * }
 *
 * Admin login page sends role: "admin"
 * User login page sends role: "user"
 */
exports.login = async (req, res, next) => {
  try {
    const { username, password, role } = req.body;

    if (!username?.trim() || !password?.trim()) {
      return res.status(400).json({
        message: "Please enter both username and password.",
      });
    }

    if (!role || !["admin", "user"].includes(role)) {
      return res.status(400).json({
        message: "Role must be admin or user",
      });
    }

    const user = await User.findOne({
      username: username.trim().toLowerCase(),
      role,
    }).select("+password");

    if (!user) {
      return res.status(401).json({
        message: "Invalid Username or Password",
      });
    }

    const isMatch = await bcrypt.compare(password, user.password);

    if (!isMatch) {
      return res.status(401).json({
        message: "Invalid Username or Password",
      });
    }

    const token = jwt.sign(
      { id: user._id, username: user.username, role: user.role },
      process.env.JWT_SECRET || "fallback-secret-key-12345",
      { expiresIn: "7d" }
    );

    res.json({
      token,
      role: user.role,
      username: user.username,
    });
  } catch (error) {
    next(error);
  }
};
