const jwt = require("jsonwebtoken");
const User = require("../models/User");
const { getJwtSecret } = require("../config/secrets");

const authenticate = async (req, res, next) => {
  try {
    const authHeader = req.headers["authorization"];
    let token = authHeader && authHeader.startsWith("Bearer ") ? authHeader.split(" ")[1] : null;

    // Fallback: accept JWT via ?token= query parameter.
    // HTML5 <audio> / <video> elements and some media players cannot set
    // custom HTTP headers, so they pass the token in the URL instead.
    if (!token && req.query.token) {
      token = req.query.token;
    }

    if (!token) {
      return res.status(401).json({
        success: false,
        message: "Authentication required",
        error: "UnauthorizedError",
      });
    }

    const decoded = jwt.verify(token, getJwtSecret());

    const user = await User.findById(decoded.id).select("-password");

    if (!user) {
      return res.status(401).json({
        success: false,
        message: "User not found",
        error: "UnauthorizedError",
      });
    }

    req.user = user;
    next();
  } catch (error) {
    if (error.name === "JsonWebTokenError" || error.name === "TokenExpiredError") {
      return res.status(401).json({
        success: false,
        message: "Invalid or expired token",
        error: "UnauthorizedError",
      });
    }
    next(error);
  }
};

const requireAdmin = (req, res, next) => {
  if (!req.user || req.user.role !== "admin") {
    return res.status(403).json({
      success: false,
      message: "Admin access required",
      error: "ForbiddenError",
    });
  }
  next();
};

module.exports = {
  authenticate,
  requireAdmin,
};
