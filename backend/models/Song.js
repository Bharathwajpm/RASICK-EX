const mongoose = require("mongoose");

/**
 * Song model — supports music browsing, playback, search, and admin uploads.
 *
 * UI sources:
 * - mock-data.ts Song type: id, title, artist, cover, duration, category?
 * - home.tsx: trending, latest, perambalur, recommended sections
 * - search.tsx: search by title/artist/category
 * - library.tsx / downloads.tsx: playable offline song lists
 * - player-context.tsx: playback queue items
 * - admin/upload.tsx: title, category, cover image, audio file
 * - admin/dashboard.tsx: song list with title, artist, duration, category
 */
const songSchema = new mongoose.Schema(
  {
    // Stable id for seeded/mock songs (e.g. "t1", "l2"); new uploads use Mongo _id
    externalId: {
      type: String,
      unique: true,
      sparse: true,
      trim: true,
    },

    // Song card, player, search, and admin list
    title: {
      type: String,
      required: [true, "Song title is required"],
      trim: true,
      maxlength: [200, "Title cannot exceed 200 characters"],
    },

    // Song card, player, search, and admin list
    artist: {
      type: String,
      required: [true, "Artist name is required"],
      trim: true,
      maxlength: [120, "Artist name cannot exceed 120 characters"],
    },

    // Cover image shown on cards, library, downloads, and admin upload
    cover: {
      type: String,
      required: [true, "Cover image URL is required"],
      trim: true,
    },

    // Displayed as "3:42" across home, library, downloads, and admin
    duration: {
      type: String,
      required: [true, "Duration is required"],
      trim: true,
    },

    // Browse/search categories and perambalur specials (Temple, Folk, DJ, etc.)
    category: {
      type: String,
      trim: true,
    },

    // Album association (optional — for catalog grouping)
    album: {
      type: String,
      trim: true,
    },

    albumId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Album",
    },

    // Home feed grouping: Trending, Latest Releases, Local Specials, Recommended
    section: {
      type: String,
      enum: {
        values: ["trending", "latest", "perambalur", "recommended", "general"],
        message: "Section must be trending, latest, perambalur, recommended, or general",
      },
      default: "general",
    },

    // Audio source for player and admin upload
    audioUrl: {
      type: String,
      trim: true,
    },

    surroundUrl: {
      type: String,
      trim: true,
    },

    fallbackUrl: {
      type: String,
      trim: true,
    },

    audioSizeBytes: {
      type: Number,
      default: 0,
    },

    coverSizeBytes: {
      type: Number,
      default: 0,
    },

    playCount: {
      type: Number,
      default: 0,
    },

    downloadCount: {
      type: Number,
      default: 0,
    },

    // Admin who uploaded the track
    uploadedBy: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
    },

    // Used by admin delete action without removing history immediately
    isActive: {
      type: Boolean,
      default: true,
    },
  },
  {
    timestamps: true,
    toJSON: {
      virtuals: true,
      transform: (_doc, ret) => {
        ret.id = ret.externalId || String(ret._id);
        delete ret._id;
        delete ret.__v;
        delete ret.externalId;
        return ret;
      },
    },
    toObject: { virtuals: true },
  }
);

songSchema.index({ isActive: 1, section: 1, createdAt: -1 });
songSchema.index({ isActive: 1, category: 1 });
songSchema.index({ isActive: 1, artist: 1 });
songSchema.index({ isActive: 1, albumId: 1 });
songSchema.index({ title: "text", artist: "text", category: "text" });

module.exports = mongoose.model("Song", songSchema);
