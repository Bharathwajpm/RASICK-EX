const mongoose = require("mongoose");

/**
 * User model — supports login, profile, and role-based access in the UI.
 *
 * UI sources:
 * - login/admin.tsx, login/user.tsx: username + password + role
 * - auth-context.tsx: username, role ("admin" | "user")
 * - profile.tsx: username display, role label ("Administrator" | "Music Lover")
 * - bottom-nav.tsx / admin routes: isAdmin gate
 */
const userSchema = new mongoose.Schema(
  {
    // Shown on profile and used during sign-in
    username: {
      type: String,
      required: [true, "Username is required"],
      trim: true,
      lowercase: true,
      minlength: [2, "Username must be at least 2 characters"],
      maxlength: [50, "Username cannot exceed 50 characters"],
    },

    // Hashed credential — never returned in API responses
    password: {
      type: String,
      required: [true, "Password is required"],
      minlength: [6, "Password must be at least 6 characters"],
      select: false,
    },

    // Selected on login-selection screen and validated on login
    role: {
      type: String,
      enum: {
        values: ["admin", "user"],
        message: "Role must be admin or user",
      },
      required: [true, "Role is required"],
    },
  },
  {
    timestamps: true,
    toJSON: {
      virtuals: true,
      transform: (_doc, ret) => {
        ret.id = ret._id.toString();
        delete ret._id;
        delete ret.__v;
        delete ret.password;
        return ret;
      },
    },
    toObject: { virtuals: true },
  }
);

// Login checks username + role together (admin login vs user login)
userSchema.index({ username: 1, role: 1 }, { unique: true });

module.exports = mongoose.model("User", userSchema);
