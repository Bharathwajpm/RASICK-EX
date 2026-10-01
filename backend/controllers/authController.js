const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const User = require("../models/User");
const { getJwtSecret } = require("../config/secrets");

/**
 * POST /api/auth/register
 *
 * Request body:
 * {
 *   "username": "newuser",
 *   "password": "securepass"
 * }
 *
 * Always creates a USER account. The "role" field in the request body
 * is intentionally ignored to prevent privilege escalation.
 */
exports.register = async (req, res, next) => {
  try {
    const { username, password } = req.body;

    // Validate required fields
    if (!username?.trim()) {
      return res.status(400).json({
        success: false,
        message: "Username is required.",
        error: "BadRequestError",
      });
    }

    if (!password || password.length < 6) {
      return res.status(400).json({
        success: false,
        message: "Password must be at least 6 characters.",
        error: "BadRequestError",
      });
    }

    const normalizedUsername = username.trim().toLowerCase();

    if (normalizedUsername.length < 2) {
      return res.status(400).json({
        success: false,
        message: "Username must be at least 2 characters.",
        error: "BadRequestError",
      });
    }

    if (normalizedUsername.length > 50) {
      return res.status(400).json({
        success: false,
        message: "Username cannot exceed 50 characters.",
        error: "BadRequestError",
      });
    }

    // Check if username already exists for the "user" role
    const existingUser = await User.findOne({
      username: normalizedUsername,
      role: "user",
    });

    if (existingUser) {
      return res.status(409).json({
        success: false,
        message: "Username is already taken.",
        error: "ConflictError",
      });
    }

    // Hash password (10 rounds — matches seed.js)
    const hashedPassword = await bcrypt.hash(password, 10);

    // Force role to "user" — never accept role from request body
    const user = await User.create({
      username: normalizedUsername,
      password: hashedPassword,
      role: "user",
    });

    // Generate JWT (same payload shape as login)
    const token = jwt.sign(
      { id: user._id, username: user.username, role: user.role },
      getJwtSecret(),
      { expiresIn: "7d" }
    );

    res.status(201).json({
      success: true,
      data: {
        token,
        role: user.role,
        username: user.username,
      },
    });
  } catch (error) {
    // Handle Mongoose duplicate key error (race condition fallback)
    if (error.code === 11000) {
      return res.status(409).json({
        success: false,
        message: "Username is already taken.",
        error: "ConflictError",
      });
    }
    next(error);
  }
};

/**
 * POST /api/auth/login
 *
 * Request body (matches login UI state fields):
 * {
 *   "username": "admin",
 *   "password": "secret",
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
        success: false,
        message: "Please enter both username and password.",
        error: "BadRequestError",
      });
    }

    if (!role || !["admin", "user"].includes(role)) {
      return res.status(400).json({
        success: false,
        message: "Role must be admin or user",
        error: "BadRequestError",
      });
    }

    const user = await User.findOne({
      username: username.trim().toLowerCase(),
      role,
    }).select("+password");

    if (!user) {
      return res.status(401).json({
        success: false,
        message: "Invalid Username or Password",
        error: "UnauthorizedError",
      });
    }

    const isMatch = await bcrypt.compare(password, user.password);

    if (!isMatch) {
      return res.status(401).json({
        success: false,
        message: "Invalid Username or Password",
        error: "UnauthorizedError",
      });
    }

    const token = jwt.sign(
      { id: user._id, username: user.username, role: user.role },
      getJwtSecret(),
      { expiresIn: "7d" }
    );

    res.json({
      success: true,
      data: {
        token,
        role: user.role,
        username: user.username,
      },
    });
  } catch (error) {
    next(error);
  }
};
