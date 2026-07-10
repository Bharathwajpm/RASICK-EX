const path = require("path");
const { GetObjectCommand } = require("@aws-sdk/client-s3");
const { s3Client } = require("../config/filebase");
const Song = require("../models/Song");

const getS3Key = (urlOrKey) => {
  if (!urlOrKey) return null;
  try {
    const parsed = new URL(urlOrKey);
    // pathname starts with a slash, e.g. /songs/uuid.mp3
    let key = parsed.pathname.substring(1);
    const bucketName = process.env.FILEBASE_BUCKET || "rasick-music";
    if (key.startsWith(`${bucketName}/`)) {
      key = key.substring(bucketName.length + 1);
    }
    return key;
  } catch (e) {
    // Treat as raw key if URL parsing fails
    return urlOrKey.replace(/^\/?uploads\//, "");
  }
};

const findSongById = async (id) =>
  (await Song.findOne({ externalId: id, isActive: true })) ||
  (await Song.findOne({ _id: id, isActive: true }).catch(() => null));

/**
 * Generic helper: streams audio from S3 or redirects to an external URL.
 * @param {string} url   The stored URL (audioUrl, surroundUrl, or fallbackUrl)
 * @param {string} label The field name for logging
 */
const streamFromUrl = async (url, label, song, req, res, next) => {
  try {
    if (!url) {
      return res.status(404).json({ message: `Song has no ${label} content` });
    }

    // If it's a standard web URL (not Filebase), redirect the player directly to it
    if (!url.includes("filebase.io")) {
      return res.redirect(url);
    }

    const s3Key = getS3Key(url);
    const range = req.headers.range;

    const params = {
      Bucket: process.env.FILEBASE_BUCKET || "rasick-music",
      Key: s3Key,
    };

    if (range) {
      params.Range = range;
    }

    const command = new GetObjectCommand(params);
    const response = await s3Client.send(command);

    res.status(range ? 206 : 200);
    res.set({
      "Content-Type": response.ContentType || "audio/mpeg",
      "Accept-Ranges": "bytes",
    });

    if (response.ContentLength !== undefined) {
      res.set("Content-Length", response.ContentLength);
    }
    if (response.ContentRange) {
      res.set("Content-Range", response.ContentRange);
    }

    response.Body.pipe(res);
  } catch (error) {
    if (error.name === "NoSuchKey") {
      return res.status(404).json({ message: `${label} file not found in S3 storage` });
    }
    next(error);
  }
};

exports.streamAudio = async (req, res, next) => {
  try {
    const { songId } = req.params;
    const song = await findSongById(songId);
    if (!song) {
      return res.status(404).json({ message: "Song not found" });
    }

    await streamFromUrl(song.audioUrl, "audio", song, req, res, next);
    Song.updateOne({ _id: song._id }, { $inc: { playCount: 1 } }).catch(() => {});
  } catch (error) {
    next(error);
  }
};

exports.streamSurround = async (req, res, next) => {
  try {
    const { songId } = req.params;
    const song = await findSongById(songId);
    if (!song) {
      return res.status(404).json({ message: "Song not found" });
    }

    await streamFromUrl(song.surroundUrl, "surround audio", song, req, res, next);
    Song.updateOne({ _id: song._id }, { $inc: { playCount: 1 } }).catch(() => {});
  } catch (error) {
    next(error);
  }
};

exports.streamFallback = async (req, res, next) => {
  try {
    const { songId } = req.params;
    const song = await findSongById(songId);
    if (!song) {
      return res.status(404).json({ message: "Song not found" });
    }

    await streamFromUrl(song.fallbackUrl, "fallback audio", song, req, res, next);
    Song.updateOne({ _id: song._id }, { $inc: { playCount: 1 } }).catch(() => {});
  } catch (error) {
    next(error);
  }
};

exports.streamCover = async (req, res, next) => {
  try {
    const { songId } = req.params;
    const song = await findSongById(songId);
    if (!song) {
      return res.status(404).json({ message: "Song not found" });
    }

    const coverUrl = song.cover;
    if (!coverUrl) {
      return res.status(404).json({ message: "Song has no cover image" });
    }

    // If it's a standard web URL (like picsum), redirect directly
    if (!coverUrl.includes("filebase.io")) {
      return res.redirect(coverUrl);
    }

    const s3Key = getS3Key(coverUrl);

    const command = new GetObjectCommand({
      Bucket: process.env.FILEBASE_BUCKET || "rasick-music",
      Key: s3Key,
    });
    const response = await s3Client.send(command);

    res.status(200);
    res.set({
      "Content-Type": response.ContentType || "image/jpeg",
    });

    if (response.ContentLength !== undefined) {
      res.set("Content-Length", response.ContentLength);
    }

    response.Body.pipe(res);
  } catch (error) {
    if (error.name === "NoSuchKey") {
      return res.status(404).json({ message: "Cover image not found in S3 storage" });
    }
    next(error);
  }
};

exports.downloadSong = async (req, res, next) => {
  try {
    const { songId } = req.params;
    const song = await findSongById(songId);
    if (!song) {
      return res.status(404).json({ message: "Song not found" });
    }

    const audioUrl = song.audioUrl;
    if (!audioUrl) {
      return res.status(404).json({ message: "Song has no audio content to download" });
    }

    // If it's a standard web URL (not Filebase), redirect directly
    if (!audioUrl.includes("filebase.io")) {
      return res.redirect(audioUrl);
    }

    const s3Key = getS3Key(audioUrl);
    const ext = path.extname(s3Key) || ".mp3";
    const safeTitle = song.title.replace(/[^a-zA-Z0-9-_]/g, "_");

    const command = new GetObjectCommand({
      Bucket: process.env.FILEBASE_BUCKET || "rasick-music",
      Key: s3Key,
    });
    const response = await s3Client.send(command);

    res.status(200);
    res.set({
      "Content-Type": response.ContentType || "audio/mpeg",
      "Content-Disposition": `attachment; filename="${safeTitle}${ext}"`,
    });

    if (response.ContentLength !== undefined) {
      res.set("Content-Length", response.ContentLength);
    }

    response.Body.pipe(res);
    Song.updateOne({ _id: song._id }, { $inc: { downloadCount: 1 } }).catch(() => {});
  } catch (error) {
    if (error.name === "NoSuchKey") {
      return res.status(404).json({ message: "Audio file not found in S3 storage" });
    }
    next(error);
  }
};
