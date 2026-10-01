const Album = require("../models/Album");
const Artist = require("../models/Artist");
const Song = require("../models/Song");
const { formatAlbum } = require("../config/formatters");

exports.getAlbums = async (req, res, next) => {
  try {
    const filter = {};
    if (req.query.artistId) filter.artistId = req.query.artistId;

    const albums = await Album.find(filter).sort({ title: 1 });

    // Attach live song counts
    const counts = await Song.aggregate([
      { $match: { isActive: true, albumId: { $exists: true } } },
      { $group: { _id: "$albumId", count: { $sum: 1 } } },
    ]);
    const countById = Object.fromEntries(
      counts.map((r) => [r._id.toString(), r.count])
    );

    res.json({
      success: true,
      data: {
        albums: albums.map((a) =>
          formatAlbum({ ...a.toObject(), songCount: countById[a._id.toString()] || 0 })
        ),
      },
    });
  } catch (error) {
    next(error);
  }
};

exports.getAlbum = async (req, res, next) => {
  try {
    const album = await Album.findById(req.params.id);
    if (!album) {
      return res.status(404).json({
        success: false,
        message: "Album not found",
        error: "NotFoundError",
      });
    }

    const songs = await Song.find({ albumId: album._id, isActive: true }).sort({ createdAt: -1 });
    const { formatSong } = require("../config/formatters");

    res.json({
      success: true,
      data: {
        album: formatAlbum(album),
        songs: songs.map(formatSong),
      },
    });
  } catch (error) {
    next(error);
  }
};

exports.createAlbum = async (req, res, next) => {
  try {
    const { title, artistId, cover, releaseYear } = req.body;

    if (!title || !title.trim()) {
      return res.status(400).json({
        success: false,
        message: "Album title is required",
        error: "BadRequestError",
      });
    }

    if (!artistId) {
      return res.status(400).json({
        success: false,
        message: "Artist is required",
        error: "BadRequestError",
      });
    }

    const artist = await Artist.findById(artistId);
    if (!artist) {
      return res.status(404).json({
        success: false,
        message: "Artist not found",
        error: "NotFoundError",
      });
    }

    const album = await Album.create({
      title: title.trim(),
      artistId: artist._id,
      artistName: artist.name,
      cover: cover || null,
      releaseYear: releaseYear || null,
    });

    res.status(201).json({
      success: true,
      data: { album: formatAlbum(album) },
    });
  } catch (error) {
    next(error);
  }
};

exports.updateAlbum = async (req, res, next) => {
  try {
    const album = await Album.findById(req.params.id);
    if (!album) {
      return res.status(404).json({
        success: false,
        message: "Album not found",
        error: "NotFoundError",
      });
    }

    if (req.body.title !== undefined) album.title = req.body.title.trim();
    if (req.body.cover !== undefined) album.cover = req.body.cover;
    if (req.body.releaseYear !== undefined) album.releaseYear = req.body.releaseYear;

    // Allow reassigning to a different artist
    if (req.body.artistId && req.body.artistId !== album.artistId.toString()) {
      const newArtist = await Artist.findById(req.body.artistId);
      if (!newArtist) {
        return res.status(404).json({
          success: false,
          message: "New artist not found",
          error: "NotFoundError",
        });
      }
      album.artistId = newArtist._id;
      album.artistName = newArtist.name;
    }

    const oldTitle = album.title;
    await album.save();

    // Cascade album name to songs if changed
    if (oldTitle !== album.title) {
      await Song.updateMany({ albumId: album._id }, { album: album.title });
    }

    res.json({
      success: true,
      data: { album: formatAlbum(album) },
    });
  } catch (error) {
    next(error);
  }
};

exports.deleteAlbum = async (req, res, next) => {
  try {
    const album = await Album.findById(req.params.id);
    if (!album) {
      return res.status(404).json({
        success: false,
        message: "Album not found",
        error: "NotFoundError",
      });
    }

    // Unlink songs from this album (don't delete songs)
    await Song.updateMany(
      { albumId: album._id },
      { $unset: { album: "", albumId: "" } }
    );

    await Album.findByIdAndDelete(req.params.id);

    res.json({
      success: true,
      data: { message: "Album deleted successfully" },
    });
  } catch (error) {
    next(error);
  }
};
