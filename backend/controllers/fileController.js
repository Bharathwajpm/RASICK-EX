const path = require("path");
const { GetObjectCommand } = require("@aws-sdk/client-s3");
const { getSignedUrl } = require("@aws-sdk/s3-request-presigner");
const { s3Client } = require("../config/filebase");
const Song = require("../models/Song");

// Presigned URL expiration: 1 hour (seconds).
// Sufficient for any single song/file; each new play request generates a fresh URL.
const PRESIGNED_EXPIRY = 3600;

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
 * Generates a presigned S3 URL and redirects the client (302) to Filebase.
 *
 * The client (ExoPlayer/Media3, browser, etc.) follows the redirect and
 * fetches audio bytes directly from Filebase, bypassing Render's bandwidth.
 *
 * - Filebase egress is FREE.
 * - Filebase supports Range requests natively on presigned URLs.
 * - Original bytes (DTS, AC-3, FLAC, etc.) are served without transcoding.
 * - The presigned URL expires after PRESIGNED_EXPIRY seconds.
 * - The S3 secret key is never exposed to the client.
 *
 * @param {string} url   The stored URL (audioUrl, surroundUrl, or fallbackUrl)
 * @param {string} label The field name for logging
 */
const redirectToPresigned = async (url, label, song, req, res, next) => {
  try {
    if (!url) {
      return res.status(404).json({
        success: false,
        message: `Song has no ${label} content`,
        error: "NotFoundError",
      });
    }

    // If it's a standard web URL (not Filebase), redirect the player directly to it
    if (!url.includes("filebase.io")) {
      return res.redirect(url);
    }

    const s3Key = getS3Key(url);

    const command = new GetObjectCommand({
      Bucket: process.env.FILEBASE_BUCKET || "rasick-music",
      Key: s3Key,
    });

    const presignedUrl = await getSignedUrl(s3Client, command, {
      expiresIn: PRESIGNED_EXPIRY,
    });

    // 302 redirect — ExoPlayer, OkHttp, and browsers follow this automatically.
    // The client then fetches bytes directly from Filebase, supporting Range
    // requests, correct Content-Type, and original audio bytes.
    res.redirect(302, presignedUrl);
  } catch (error) {
    if (error.name === "NoSuchKey" || error.code === "NoSuchKey") {
      return res.status(404).json({
        success: false,
        message: `${label} file not found in S3 storage`,
        error: "NotFoundError",
      });
    }
    next(error);
  }
};

exports.streamAudio = async (req, res, next) => {
  try {
    const { songId } = req.params;
    const song = await findSongById(songId);
    if (!song) {
      return res.status(404).json({
        success: false,
        message: "Song not found",
        error: "NotFoundError",
      });
    }

    await redirectToPresigned(song.audioUrl, "audio", song, req, res, next);
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
      return res.status(404).json({
        success: false,
        message: "Song not found",
        error: "NotFoundError",
      });
    }

    await redirectToPresigned(song.surroundUrl, "surround audio", song, req, res, next);
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
      return res.status(404).json({
        success: false,
        message: "Song not found",
        error: "NotFoundError",
      });
    }

    await redirectToPresigned(song.fallbackUrl, "fallback audio", song, req, res, next);
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
      return res.status(404).json({
        success: false,
        message: "Song not found",
        error: "NotFoundError",
      });
    }

    const coverUrl = song.cover;
    if (!coverUrl) {
      return res.status(404).json({
        success: false,
        message: "Song has no cover image",
        error: "NotFoundError",
      });
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

    const presignedUrl = await getSignedUrl(s3Client, command, {
      expiresIn: PRESIGNED_EXPIRY,
    });

    res.redirect(302, presignedUrl);
  } catch (error) {
    if (error.name === "NoSuchKey" || error.code === "NoSuchKey") {
      return res.status(404).json({
        success: false,
        message: "Cover image not found in S3 storage",
        error: "NotFoundError",
      });
    }
    next(error);
  }
};

exports.downloadSong = async (req, res, next) => {
  try {
    const { songId } = req.params;
    const song = await findSongById(songId);
    if (!song) {
      return res.status(404).json({
        success: false,
        message: "Song not found",
        error: "NotFoundError",
      });
    }

    const audioUrl = song.audioUrl;
    if (!audioUrl) {
      return res.status(404).json({
        success: false,
        message: "Song has no audio content to download",
        error: "NotFoundError",
      });
    }

    // If it's a standard web URL (not Filebase), redirect directly
    if (!audioUrl.includes("filebase.io")) {
      return res.redirect(audioUrl);
    }

    const s3Key = getS3Key(audioUrl);
    const ext = path.extname(s3Key) || ".mp3";
    const safeTitle = song.title.replace(/[^a-zA-Z0-9-_]/g, "_");

    // Presigned URL with Content-Disposition override so the browser/client
    // downloads the file with a human-readable filename.
    const command = new GetObjectCommand({
      Bucket: process.env.FILEBASE_BUCKET || "rasick-music",
      Key: s3Key,
      ResponseContentDisposition: `attachment; filename="${safeTitle}${ext}"`,
    });

    const presignedUrl = await getSignedUrl(s3Client, command, {
      expiresIn: PRESIGNED_EXPIRY,
    });

    res.redirect(302, presignedUrl);
    Song.updateOne({ _id: song._id }, { $inc: { downloadCount: 1 } }).catch(() => {});
  } catch (error) {
    if (error.name === "NoSuchKey" || error.code === "NoSuchKey") {
      return res.status(404).json({
        success: false,
        message: "Audio file not found in S3 storage",
        error: "NotFoundError",
      });
    }
    next(error);
  }
};
