/**
 * One-time migration script: fix-fallback-urls.js
 *
 * Cleans corrupted Song documents by removing shared demo fallback URLs.
 *
 * Usage:
 *   cd backend
 *   node scripts/fix-fallback-urls.js
 */
require("dotenv").config({ path: require("path").resolve(__dirname, "..", ".env") });
const mongoose = require("mongoose");
const connectDB = require("../config/db");
const Song = require("../models/Song");

// Known demo / placeholder URLs that should never be used for real playback
const DEMO_URL_PATTERNS = [
  /Kalimba\.mp3/i,
  /horse\.mp3/i,
  /soundhelix\.com/i,
  /learningcontainer\.com/i,
  /example\.com\/mock_surround/i,
];

function isDemoUrl(url) {
  if (!url || typeof url !== "string") return false;
  return DEMO_URL_PATTERNS.some((pattern) => pattern.test(url));
}

function isValidFilebaseUrl(url) {
  return url && typeof url === "string" && url.includes("filebase.io");
}

async function migrate() {
  await connectDB();
  console.log("[Migration] Connected to MongoDB");

  const songs = await Song.find({});
  console.log(`[Migration] Found ${songs.length} total songs`);

  let fixed = 0;
  let cleared = 0;
  let skipped = 0;

  for (const song of songs) {
    const updates = {};
    let action = "";

    const audioIsDemoUrl = isDemoUrl(song.audioUrl);
    const surroundIsDemoUrl = isDemoUrl(song.surroundUrl);
    const fallbackIsDemoUrl = isDemoUrl(song.fallbackUrl);

    if (audioIsDemoUrl) {
      // Song's audioUrl is a demo — clear all audio fields
      updates.audioUrl = null;
      updates.surroundUrl = null;
      updates.fallbackUrl = null;
      action = "CLEARED (audioUrl was demo)";
      cleared++;
    } else if (isValidFilebaseUrl(song.audioUrl)) {
      // Song has a valid Filebase audioUrl — fix surround/fallback
      if (surroundIsDemoUrl || !song.surroundUrl) {
        updates.surroundUrl = song.audioUrl;
      }
      if (fallbackIsDemoUrl || !song.fallbackUrl) {
        updates.fallbackUrl = song.audioUrl;
      }
      if (Object.keys(updates).length > 0) {
        action = "FIXED (copied audioUrl to surround/fallback)";
        fixed++;
      } else {
        action = "SKIPPED (already correct)";
        skipped++;
      }
    } else if (surroundIsDemoUrl || fallbackIsDemoUrl) {
      // No valid audioUrl but demo surround/fallback — clear them
      if (surroundIsDemoUrl) updates.surroundUrl = null;
      if (fallbackIsDemoUrl) updates.fallbackUrl = null;
      action = "CLEARED (removed demo surround/fallback)";
      cleared++;
    } else {
      action = "SKIPPED (no changes needed)";
      skipped++;
    }

    if (Object.keys(updates).length > 0) {
      await Song.updateOne({ _id: song._id }, { $set: updates });
      console.log(
        `[Migration] ${action} — _id: ${song._id}, title: "${song.title}"` +
        `\n    audioUrl: ${song.audioUrl || "(null)"}` +
        `\n    surroundUrl: ${song.surroundUrl || "(null)"} -> ${updates.surroundUrl !== undefined ? (updates.surroundUrl || "(null)") : "(unchanged)"}` +
        `\n    fallbackUrl: ${song.fallbackUrl || "(null)"} -> ${updates.fallbackUrl !== undefined ? (updates.fallbackUrl || "(null)") : "(unchanged)"}`
      );
    }
  }

  console.log("\n[Migration] Summary:");
  console.log(`  Fixed:   ${fixed} songs (copied valid audioUrl to surround/fallback)`);
  console.log(`  Cleared: ${cleared} songs (removed demo URLs)`);
  console.log(`  Skipped: ${skipped} songs (already correct)`);
  console.log(`  Total:   ${songs.length} songs`);

  await mongoose.disconnect();
  console.log("[Migration] Done. MongoDB disconnected.");
}

migrate().catch((err) => {
  console.error("[Migration] FATAL:", err);
  process.exit(1);
});
