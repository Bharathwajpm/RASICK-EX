const Artist = require("../models/Artist");
const Song = require("../models/Song");
const Album = require("../models/Album");
const { formatArtist } = require("../config/formatters");

exports.getArtists = async (req, res, next) => {
  try {
    const artists = await Artist.find().sort({ name: 1 });

    // Attach song counts
    const counts = await Song.aggregate([
      { $match: { isActive: true, artist: { $exists: true, $ne: "" } } },
      { $group: { _id: "$artist", count: { $sum: 1 } } },
    ]);
    const countByName = Object.fromEntries(counts.map((r) => [r._id, r.count]));

    res.json({
      success: true,
      data: {
        artists: artists.map((a) =>
          formatArtist({ ...a.toObject(), songCount: countByName[a.name] || 0 })
        ),
      },
    });
  } catch (error) {
    next(error);
  }
};

exports.getArtist = async (req, res, next) => {
  try {
    const artist = await Artist.findById(req.params.id);
    if (!artist) {
      return res.status(404).json({
        success: false,
        message: "Artist not found",
        error: "NotFoundError",
      });
    }

    const songCount = await Song.countDocuments({ artist: artist.name, isActive: true });
    const albums = await Album.find({ artistId: artist._id }).sort({ title: 1 });

    res.json({
      success: true,
      data: {
        artist: formatArtist({ ...artist.toObject(), songCount }),
        albums: albums.map((a) => ({
          id: a._id.toString(),
          title: a.title,
          artistName: a.artistName,
          cover: a.cover || null,
          releaseYear: a.releaseYear || null,
          songCount: a.songCount || 0,
        })),
      },
    });
  } catch (error) {
    next(error);
  }
};

exports.createArtist = async (req, res, next) => {
  try {
    const { name, image } = req.body;

    if (!name || !name.trim()) {
      return res.status(400).json({
        success: false,
        message: "Artist name is required",
        error: "BadRequestError",
      });
    }

    const existing = await Artist.findOne({ name: name.trim() });
    if (existing) {
      return res.status(409).json({
        success: false,
        message: "An artist with this name already exists",
        error: "ConflictError",
      });
    }

    const artist = await Artist.create({
      name: name.trim(),
      image: image || `https://picsum.photos/seed/${encodeURIComponent(name.trim())}/600/600`,
    });

    res.status(201).json({
      success: true,
      data: { artist: formatArtist(artist) },
    });
  } catch (error) {
    if (error.code === 11000) {
      return res.status(409).json({
        success: false,
        message: "An artist with this name already exists",
        error: "ConflictError",
      });
    }
    next(error);
  }
};

exports.updateArtist = async (req, res, next) => {
  try {
    const artist = await Artist.findById(req.params.id);
    if (!artist) {
      return res.status(404).json({
        success: false,
        message: "Artist not found",
        error: "NotFoundError",
      });
    }

    const oldName = artist.name;

    if (req.body.name !== undefined) {
      artist.name = req.body.name.trim();
    }
    if (req.body.image !== undefined) {
      artist.image = req.body.image;
    }

    await artist.save();

    // Cascade artist name to songs and albums if it changed
    if (oldName !== artist.name) {
      await Song.updateMany({ artist: oldName }, { artist: artist.name });
      await Album.updateMany({ artistId: artist._id }, { artistName: artist.name });
    }

    res.json({
      success: true,
      data: { artist: formatArtist(artist) },
    });
  } catch (error) {
    next(error);
  }
};

exports.deleteArtist = async (req, res, next) => {
  try {
    const artist = await Artist.findById(req.params.id);
    if (!artist) {
      return res.status(404).json({
        success: false,
        message: "Artist not found",
        error: "NotFoundError",
      });
    }

    // Check for dependent songs
    const songCount = await Song.countDocuments({ artist: artist.name, isActive: true });
    if (songCount > 0) {
      return res.status(409).json({
        success: false,
        message: `Cannot delete artist with ${songCount} active song(s). Remove or reassign songs first.`,
        error: "ConflictError",
      });
    }

    // Remove associated albums with no songs
    await Album.deleteMany({ artistId: artist._id });
    await Artist.findByIdAndDelete(req.params.id);

    res.json({
      success: true,
      data: { message: "Artist deleted successfully" },
    });
  } catch (error) {
    next(error);
  }
};
