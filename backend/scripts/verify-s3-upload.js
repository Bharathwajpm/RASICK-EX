const fs = require("fs");
const path = require("path");

const BASE = "http://localhost:5000";

const assert = (condition, message) => {
  if (!condition) throw new Error(message);
};

async function run() {
  console.log("=== S3 Upload & Temp Cleanup Verification ===");

  // 1. Check temp directory initially
  const tempDir = path.resolve(__dirname, "..", "uploads", "temp");
  console.log("Temp directory path:", tempDir);
  const initialFiles = fs.existsSync(tempDir) ? fs.readdirSync(tempDir) : [];
  console.log(`Initial files in temp dir: ${initialFiles.length}`, initialFiles);

  // 2. Prepare FormData
  const formData = new FormData();
  formData.append("title", "S3 Verification Song");
  formData.append("category", "Tamil Hits");
  formData.append("artist", "Verification Bot");
  formData.append("duration", "2:30");
  formData.append("section", "latest");

  // We append blobs with filenames to simulate multipart file upload
  const coverBlob = new Blob(["fake cover image content"], { type: "image/jpeg" });
  const audioBlob = new Blob(["fake audio content mp3"], { type: "audio/mpeg" });

  formData.append("cover", coverBlob, "test-cover.jpg");
  formData.append("audio", audioBlob, "test-audio.mp3");

  // 3. Post to create song
  console.log("Uploading files to backend...");
  const uploadRes = await fetch(`${BASE}/api/songs`, {
    method: "POST",
    headers: {
      "x-username": "admin",
      "x-user-role": "admin",
    },
    body: formData,
  });

  const uploadResult = await uploadRes.json();
  assert(uploadRes.status === 201, `Upload failed: ${JSON.stringify(uploadResult)}`);
  console.log("✓ Song created successfully via multipart upload.");
  
  const song = uploadResult.song;
  console.log("Uploaded song details:", song);

  // 4. Assert Filebase URLs
  assert(song.cover.startsWith("https://") && song.cover.includes(".filebase.io"), `Cover URL is not a Filebase URL: ${song.cover}`);
  assert(song.audioUrl.startsWith("https://") && song.audioUrl.includes(".filebase.io"), `Audio URL is not a Filebase URL: ${song.audioUrl}`);
  console.log("✓ Verified cover & audio are hosted on Filebase S3.");

  // 5. Check temp directory for cleanup
  const postUploadFiles = fs.readdirSync(tempDir);
  console.log(`Post-upload files in temp dir: ${postUploadFiles.length}`, postUploadFiles);
  
  // Since files should be cleaned up immediately, count should be the same as initial or at least not have the newly uploaded files
  // If there are files, we check if they are the newly created ones. Let's make sure no new file remains.
  // Actually, we can check that they are deleted. Let's assert that no new files are left behind.
  const newFiles = postUploadFiles.filter(f => !initialFiles.includes(f));
  assert(newFiles.length === 0, `Temporary files were not cleaned up: ${newFiles.join(", ")}`);
  console.log("✓ Checked temp directory: temporary files were successfully deleted!");

  // 6. Clean up: delete the song from DB and S3
  console.log("Deleting the uploaded song...");
  const deleteRes = await fetch(`${BASE}/api/songs/${song.id}`, {
    method: "DELETE",
    headers: {
      "x-username": "admin",
      "x-user-role": "admin",
    },
  });

  const deleteResult = await deleteRes.json();
  assert(deleteRes.status === 200, `Delete failed: ${JSON.stringify(deleteResult)}`);
  console.log("✓ Song deleted successfully.");

  console.log("\n=== All Verification Checks Passed Successfully ===");
}

run().catch((err) => {
  console.error("\nVerification failed:", err);
  process.exit(1);
});
