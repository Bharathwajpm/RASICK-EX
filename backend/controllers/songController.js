const fs = require("fs");
const path = require("path");
const Song = require("../models/Song");
const { formatSong } = require("../config/formatters");
const { getAudioDuration } = require("../utils/duration");
const { uploadToFilebase, deleteFromFilebase } = require("../config/filebase");

const findSongById = async (id) =>
  (await Song.findOne({ externalId: id, isActive: true })) ||
  (await Song.findOne({ _id: id, isActive: true }).catch(() => null));

const removeUploadedFiles = (files = []) => {
  for (const file of files) {
    if (file?.path) {
      fs.promises.unlink(file.path).catch(() => {});
    }
  }
};

// Surround-only formats that browsers cannot decode
const SURROUND_ONLY_EXTENSIONS = new Set([".dts", ".ac3"]);

/**
 * Determines whether a given audio extension is browser-playable.
 * Browser-playable formats can serve as both surround and fallback.
 */
const isBrowserPlayable = (ext) => !SURROUND_ONLY_EXTENSIONS.has(ext.toLowerCase());

/**
 * GET /api/songs
 * User UI: home, search, library, downloads
 *
 * Query params:
 *   section=trending|latest|perambalur|recommended
 *   category=Tamil Hits
 *   q=search text
 */
exports.getSongs = async (req, res, next) => {
  try {
    const { section, category, q } = req.query;
    const filter = { isActive: true };

    if (section) filter.section = section;
    if (category) filter.category = category;

    if (q) {
      const escaped = String(q).trim().replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
      const regex = new RegExp(escaped, "i");
      filter.$or = [{ title: regex }, { artist: regex }, { category: regex }];
    }

    const songs = await Song.find(filter).sort({ createdAt: -1 });
    res.json({ songs: songs.map(formatSong) });
  } catch (error) {
    next(error);
  }
};

/**
 * POST /api/songs
 * Admin UI: upload form (title, category, cover image, audio file)
 * Accepts multipart/form-data (preferred) or JSON with cover/audioUrl URLs.
 */
exports.createSong = async (req, res, next) => {
  const coverFile = req.files?.cover?.[0];
  const audioFile = req.files?.audio?.[0];
  const uploadedFiles = [coverFile, audioFile].filter(Boolean);

  try {
    const { title, category, artist, duration, section } = req.body;

    let cover = req.body.cover;
    let audioUrl = req.body.audioUrl;

    if (!title?.trim() || !category || (!cover && !coverFile) || (!audioUrl && !audioFile)) {
      removeUploadedFiles(uploadedFiles);
      return res.status(400).json({
        message: "Title, category, cover, and audio file are required",
      });
    }

    let resolvedDuration = duration?.trim();
    if (audioFile) {
      resolvedDuration = await getAudioDuration(audioFile.path);
    }
    resolvedDuration = resolvedDuration || "0:00";

    // Upload to Filebase S3 if local files are provided
    if (coverFile) {
      const extension = path.extname(coverFile.originalname) || ".jpg";
      const key = `covers/${require("crypto").randomUUID()}${extension}`;
      cover = await uploadToFilebase(coverFile.path, key, coverFile.mimetype);
    }

    if (audioFile) {
      const extension = path.extname(audioFile.originalname) || ".mp3";
      const key = `songs/${require("crypto").randomUUID()}${extension}`;
      audioUrl = await uploadToFilebase(audioFile.path, key, audioFile.mimetype);
    }

    // Determine surroundUrl and fallbackUrl based on the uploaded file's format.
    // Surround-only formats (.dts, .ac3): set surroundUrl only, no fallback.
    // Browser-playable formats (.mp3, .aac, .wav, .flac): set all three fields.
    let surroundUrl = null;
    let fallbackUrl = null;

    if (audioFile) {
      const ext = path.extname(audioFile.originalname).toLowerCase() || ".mp3";
      if (isBrowserPlayable(ext)) {
        // Browser can play this format — use it as both surround and fallback
        surroundUrl = audioUrl;
        fallbackUrl = audioUrl;
      } else {
        // Surround-only format — no browser fallback available
        surroundUrl = audioUrl;
        fallbackUrl = null;
      }
    } else if (audioUrl) {
      // JSON-provided URL — assume it's browser-playable
      surroundUrl = audioUrl;
      fallbackUrl = audioUrl;
    }

    const song = await Song.create({
      title: title.trim(),
      category,
      cover,
      audioUrl,
      surroundUrl,
      fallbackUrl,
      artist: artist?.trim() || "Unknown Artist",
      duration: resolvedDuration,
      section: section || "latest",
      uploadedBy: req.user?._id,
      audioSizeBytes: audioFile ? audioFile.size : 0,
      coverSizeBytes: coverFile ? coverFile.size : 0,
    });

    // Clean up temporary local files
    removeUploadedFiles(uploadedFiles);

    res.status(201).json({
      message: "Song uploaded successfully!",
      song: formatSong(song),
    });
  } catch (error) {
    removeUploadedFiles(uploadedFiles);
    next(error);
  }
};

/**
 * PUT /api/songs/:id
 * Admin UI: edit button on dashboard song rows
 */
exports.updateSong = async (req, res, next) => {
  try {
    const { id } = req.params;
    const song = await findSongById(id);

    if (!song) {
      return res.status(404).json({ message: "Song not found" });
    }

    const fields = ["title", "artist", "cover", "duration", "category", "section", "audioUrl", "surroundUrl", "fallbackUrl"];
    fields.forEach((field) => {
      if (req.body[field] !== undefined) {
        song[field] = req.body[field];
      }
    });

    await song.save();

    res.json({
      message: "Song updated successfully",
      song: formatSong(song),
    });
  } catch (error) {
    next(error);
  }
};

/**
 * DELETE /api/songs/:id
 * Admin UI: delete button on dashboard song rows
 */
exports.deleteSong = async (req, res, next) => {
  try {
    const { id } = req.params;
    const song = await findSongById(id);

    if (!song) {
      return res.status(404).json({ message: "Song not found" });
    }

    // Clean up cloud storage files
    if (song.audioUrl) {
      await deleteFromFilebase(song.audioUrl);
    }
    if (song.cover) {
      await deleteFromFilebase(song.cover);
    }

    song.isActive = false;
    await song.save();

    res.json({ message: "Song deleted successfully" });
  } catch (error) {
    next(error);
  }
};
