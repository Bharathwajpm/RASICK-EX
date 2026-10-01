const mongoose = require("mongoose");

const albumSchema = new mongoose.Schema(
  {
    title: {
      type: String,
      required: [true, "Album title is required"],
      trim: true,
      maxlength: [200, "Title cannot exceed 200 characters"],
    },
    artistId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Artist",
      required: [true, "Artist is required"],
    },
    artistName: {
      type: String,
      required: true,
      trim: true,
    },
    cover: {
      type: String,
      trim: true,
    },
    releaseYear: {
      type: Number,
    },
    songCount: {
      type: Number,
      default: 0,
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
        return ret;
      },
    },
    toObject: { virtuals: true },
  }
);

albumSchema.index({ artistId: 1 });
albumSchema.index({ title: "text", artistName: "text" });

module.exports = mongoose.model("Album", albumSchema);
