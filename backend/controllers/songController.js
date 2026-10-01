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
      console.log(`[Song Controller] Unlinking local temporary file: ${file.path}`);
      fs.promises.unlink(file.path).catch((err) => {
        console.error(`[Song Controller] Failed to unlink local file ${file.path}: ${err.message}`);
      });
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
 */
exports.getSongs = async (req, res, next) => {
  console.log(`[Song Controller] getSongs request received. Path: ${req.path}`);
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
    console.log(`[Song Controller] getSongs query completed. Found ${songs.length} songs. Sending response...`);
    
    res.json({
      success: true,
      data: {
        songs: songs.map(formatSong),
      },
    });
  } catch (error) {
    next(error);
  }
};

/**
 * POST /api/songs
 * Admin UI: upload form (title, category, cover image, audio file, optional fallback audio)
 */
exports.createSong = async (req, res, next) => {
  console.log(`[Song Controller] createSong request received.`);
  
  const coverFile = req.files?.cover?.[0];
  const audioFile = req.files?.audio?.[0];
  const fallbackFile = req.files?.fallback?.[0];
  const uploadedFiles = [coverFile, audioFile, fallbackFile].filter(Boolean);

  try {
    const { title, category, artist, duration, section, album, albumId } = req.body;
    let cover = req.body.cover;
    let audioUrl = req.body.audioUrl;

    console.log(`[Song Controller] Input validation starting. title="${title}", category="${category}", coverFile=${!!coverFile}, audioFile=${!!audioFile}, fallbackFile=${!!fallbackFile}`);

    if (!title?.trim() || !category || (!cover && !coverFile) || (!audioUrl && !audioFile)) {
      console.warn(`[Song Controller] Input validation failed. Missing required fields.`);
      removeUploadedFiles(uploadedFiles);
      return res.status(400).json({
        success: false,
        message: "Title, category, cover, and audio file are required",
        error: "BadRequestError",
      });
    }

    console.log(`[Song Controller] Validation passed.`);

    let resolvedDuration = duration?.trim();
    if (audioFile) {
      console.log(`[Song Controller] Measuring audio duration for file: ${audioFile.path}`);
      resolvedDuration = await getAudioDuration(audioFile.path).catch(err => {
        console.warn(`[Song Controller] getAudioDuration failed, defaulting. Error: ${err.message}`);
        return "0:00";
      });
    }
    resolvedDuration = resolvedDuration || "0:00";
    console.log(`[Song Controller] Resolved duration: "${resolvedDuration}"`);

    // Upload to Filebase S3 if local files are provided
    if (coverFile) {
      const extension = path.extname(coverFile.originalname) || ".jpg";
      const key = `covers/${require("crypto").randomUUID()}${extension}`;
      console.log(`[Song Controller] Cover upload starting to Filebase: key=${key}`);
      cover = await uploadToFilebase(coverFile.path, key, coverFile.mimetype);
      console.log(`[Song Controller] Cover upload finished: ${cover}`);
    }

    if (audioFile) {
      const extension = path.extname(audioFile.originalname) || ".mp3";
      const key = `songs/${require("crypto").randomUUID()}${extension}`;
      console.log(`[Song Controller] Audio upload starting to Filebase: key=${key}`);
      audioUrl = await uploadToFilebase(audioFile.path, key, audioFile.mimetype);
      console.log(`[Song Controller] Audio upload finished: ${audioUrl}`);
    }

    // Upload the separate fallback file if provided
    let fallbackUploadedUrl = null;
    if (fallbackFile) {
      const extension = path.extname(fallbackFile.originalname) || ".mp3";
      const key = `fallbacks/${require("crypto").randomUUID()}${extension}`;
      console.log(`[Song Controller] Fallback audio upload starting to Filebase: key=${key}`);
      fallbackUploadedUrl = await uploadToFilebase(fallbackFile.path, key, fallbackFile.mimetype);
      console.log(`[Song Controller] Fallback audio upload finished: ${fallbackUploadedUrl}`);
    }

    // Determine surroundUrl and fallbackUrl based on uploaded files.
    let surroundUrl = null;
    let fallbackUrl = null;

    if (fallbackUploadedUrl) {
      // Explicit fallback file provided — primary audio is surround, fallback is separate
      surroundUrl = audioUrl;
      fallbackUrl = fallbackUploadedUrl;
    } else if (audioFile) {
      // Single audio file — determine roles based on format
      const ext = path.extname(audioFile.originalname).toLowerCase() || ".mp3";
      if (isBrowserPlayable(ext)) {
        // Browser-playable format (MP3/AAC/WAV/FLAC) serves as both
        surroundUrl = audioUrl;
        fallbackUrl = audioUrl;
      } else {
        // Surround-only format (DTS/AC3) — no browser fallback available
        surroundUrl = audioUrl;
        fallbackUrl = null;
      }
    } else if (audioUrl) {
      surroundUrl = audioUrl;
      fallbackUrl = audioUrl;
    }

    console.log(`[Song Controller] MongoDB save starting...`);
    const song = await Song.create({
      title: title.trim(),
      category,
      cover,
      audioUrl,
      surroundUrl,
      fallbackUrl,
      artist: artist?.trim() || "Unknown Artist",
      album: album?.trim() || undefined,
      albumId: albumId || undefined,
      duration: resolvedDuration,
      section: section || "latest",
      uploadedBy: req.user?._id,
      audioSizeBytes: (audioFile ? audioFile.size : 0) + (fallbackFile ? fallbackFile.size : 0),
      coverSizeBytes: coverFile ? coverFile.size : 0,
    });
    console.log(`[Song Controller] MongoDB save completed. Song ID: ${song._id}`);

    // Clean up temporary local files
    removeUploadedFiles(uploadedFiles);

    console.log(`[Song Controller] Sending success response.`);
    res.status(201).json({
      success: true,
      data: {
        message: "Song uploaded successfully!",
        song: formatSong(song),
      },
    });
  } catch (error) {
    console.error(`[Song Controller] Exception occurred in createSong: ${error.message}`);
    removeUploadedFiles(uploadedFiles);
    next(error);
  }
};

/**
 * PUT /api/songs/:id
 * Admin UI: edit button on dashboard song rows
 */
exports.updateSong = async (req, res, next) => {
  console.log(`[Song Controller] updateSong request received for id: ${req.params.id}`);
  try {
    const { id } = req.params;
    const song = await findSongById(id);

    if (!song) {
      console.warn(`[Song Controller] Song not found: ${id}`);
      return res.status(404).json({
        success: false,
        message: "Song not found",
        error: "NotFoundError",
      });
    }

    const fields = ["title", "artist", "cover", "duration", "category", "section", "audioUrl", "surroundUrl", "fallbackUrl", "album", "albumId"];
    fields.forEach((field) => {
      if (req.body[field] !== undefined) {
        song[field] = req.body[field];
      }
    });

    await song.save();
    console.log(`[Song Controller] Song updated in DB successfully: ${id}`);

    res.json({
      success: true,
      data: {
        message: "Song updated successfully",
        song: formatSong(song),
      },
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
  console.log(`[Song Controller] deleteSong request received for id: ${req.params.id}`);
  try {
    const { id } = req.params;
    const song = await findSongById(id);

    if (!song) {
      console.warn(`[Song Controller] Song not found: ${id}`);
      return res.status(404).json({
        success: false,
        message: "Song not found",
        error: "NotFoundError",
      });
    }

    // Clean up cloud storage files (deduplicate to avoid double-deleting same file)
    const urlsToDelete = new Set();
    if (song.audioUrl) urlsToDelete.add(song.audioUrl);
    if (song.surroundUrl) urlsToDelete.add(song.surroundUrl);
    if (song.fallbackUrl) urlsToDelete.add(song.fallbackUrl);
    if (song.cover) urlsToDelete.add(song.cover);

    for (const url of urlsToDelete) {
      console.log(`[Song Controller] Deleting file from Filebase: ${url}`);
      await deleteFromFilebase(url);
    }

    song.isActive = false;
    await song.save();
    console.log(`[Song Controller] Song soft deleted in DB: ${id}`);

    res.json({
      success: true,
      data: {
        message: "Song deleted successfully",
      },
    });
  } catch (error) {
    next(error);
  }
};
