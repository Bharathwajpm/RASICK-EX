/**
 * Phase 8 — Full Admin ↔ Backend ↔ TV Integration Test
 *
 * Run with backend + MongoDB up:
 *   node scripts/integration-test.js
 *
 * Credentials are read from environment variables:
 *   TEST_ADMIN_USERNAME / TEST_ADMIN_PASSWORD
 *   TEST_USER_USERNAME  / TEST_USER_PASSWORD
 * Or falls back to SEED_ADMIN_* / SEED_USER_* if set.
 */
require("dotenv").config();

const BASE = process.env.API_URL || "http://localhost:5000";

const ADMIN_USERNAME = process.env.TEST_ADMIN_USERNAME || process.env.SEED_ADMIN_USERNAME || "RASICKEX";
const ADMIN_PASSWORD = process.env.TEST_ADMIN_PASSWORD || process.env.SEED_ADMIN_PASSWORD;
const USER_USERNAME = process.env.TEST_USER_USERNAME || process.env.SEED_USER_USERNAME || "RASICKEX2026";
const USER_PASSWORD = process.env.TEST_USER_PASSWORD || process.env.SEED_USER_PASSWORD;

if (!ADMIN_PASSWORD || !USER_PASSWORD) {
  console.error("Error: Set TEST_ADMIN_PASSWORD and TEST_USER_PASSWORD (or SEED_*) env vars to run tests.");
  process.exit(1);
}

let passed = 0;
let failed = 0;
let skipped = 0;

const assert = (condition, message) => {
  if (!condition) throw new Error(message);
};

async function request(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, options);
  const text = await res.text();
  let data;
  try {
    data = text ? JSON.parse(text) : {};
  } catch {
    data = { raw: text };
  }

  // Support both wrapped and unwrapped responses for test compatibility
  if (data && typeof data === "object" && data.success === true && data.data) {
    const rootMessage = data.message;
    Object.assign(data, data.data);
    if (rootMessage && !data.message) {
      data.message = rootMessage;
    }
  }

  return { res, data };
}

async function test(name, fn) {
  try {
    await fn();
    passed++;
    console.log(`✓ ${name}`);
  } catch (err) {
    failed++;
    console.error(`✗ ${name}: ${err.message}`);
  }
}

function skip(name, reason) {
  skipped++;
  console.log(`⊘ ${name}: ${reason}`);
}

async function run() {
  console.log(`Integration test against ${BASE}\n`);

  // =============================================
  // 1. BACKEND STARTUP
  // =============================================
  console.log("--- Backend Startup ---");

  await test("Health check", async () => {
    const { res } = await request("/health");
    assert(res.ok, `Health check failed: ${res.status}`);
  });

  // =============================================
  // 2. ADMIN AUTHENTICATION
  // =============================================
  console.log("\n--- Admin Authentication ---");

  let adminToken;

  await test("Invalid admin login rejected", async () => {
    const { res } = await request("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username: ADMIN_USERNAME, password: "wrong-password-attempt", role: "admin" }),
    });
    assert(res.status === 401, `Expected 401, got ${res.status}`);
  });

  await test("Admin login succeeds", async () => {
    const { res, data } = await request("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username: ADMIN_USERNAME, password: ADMIN_PASSWORD, role: "admin" }),
    });
    assert(res.ok, `Admin login failed: ${data.message}`);
    assert(data.role === "admin", "Admin role mismatch");
    assert(data.token, "Token missing");
    adminToken = data.token;
  });

  // =============================================
  // 3. USER AUTHENTICATION
  // =============================================
  console.log("\n--- User Authentication ---");

  let userToken;

  await test("Invalid user login rejected", async () => {
    const { res } = await request("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username: USER_USERNAME, password: "wrong-password-attempt", role: "user" }),
    });
    assert(res.status === 401, `Expected 401, got ${res.status}`);
  });

  await test("User login succeeds", async () => {
    const { res, data } = await request("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username: USER_USERNAME, password: USER_PASSWORD, role: "user" }),
    });
    assert(res.ok, `User login failed: ${data.message}`);
    assert(data.token, "User token missing");
    userToken = data.token;
  });

  await test("User cannot access admin create-song", async () => {
    const { res } = await request("/api/songs", {
      method: "POST",
      headers: { "Content-Type": "application/json", "Authorization": `Bearer ${userToken}` },
      body: JSON.stringify({ title: "Unauthorized", artist: "Test", cover: "https://example.com/c.jpg", audioUrl: "https://example.com/a.mp3", duration: "1:00" }),
    });
    assert(res.status === 403, `Expected 403, got ${res.status}`);
  });

  await test("Missing token returns 401", async () => {
    const { res } = await request("/api/songs", { method: "POST", headers: { "Content-Type": "application/json" }, body: "{}" });
    assert(res.status === 401, `Expected 401, got ${res.status}`);
  });

  await test("Invalid token returns 401", async () => {
    const { res } = await request("/api/songs", {
      method: "POST",
      headers: { "Content-Type": "application/json", "Authorization": "Bearer invalidtoken123" },
      body: "{}",
    });
    assert(res.status === 401, `Expected 401, got ${res.status}`);
  });

  await test("Header-based auth spoofing rejected", async () => {
    const { res } = await request("/api/songs", {
      method: "POST",
      headers: { "x-username": "admin", "x-user-role": "admin", "Content-Type": "application/json" },
      body: "{}",
    });
    assert(res.status === 401, `Expected 401, got ${res.status}`);
  });

  // =============================================
  // 4. ARTIST + ALBUM FLOW
  // =============================================
  console.log("\n--- Artist + Album Flow ---");

  const adminHeaders = { "Content-Type": "application/json", "Authorization": `Bearer ${adminToken}` };
  let testArtistId;
  let testAlbumId;

  await test("Admin creates artist", async () => {
    const { res, data } = await request("/api/artists", {
      method: "POST",
      headers: adminHeaders,
      body: JSON.stringify({ name: "Phase8 Test Artist", bio: "Integration test", genre: "Test" }),
    });
    assert(res.status === 201, `Create artist failed: ${res.status} ${data.message}`);
    testArtistId = data.artist?.id || data.artist?._id;
    assert(testArtistId, "Created artist missing ID");
  });

  await test("Admin creates album linked to artist", async () => {
    const { res, data } = await request("/api/albums", {
      method: "POST",
      headers: adminHeaders,
      body: JSON.stringify({ title: "Phase8 Test Album", artistId: testArtistId, year: 2026, genre: "Test" }),
    });
    assert(res.status === 201, `Create album failed: ${res.status} ${data.message}`);
    testAlbumId = data.album?.id || data.album?._id;
    assert(testAlbumId, "Created album missing ID");
  });

  await test("TV user can list artists", async () => {
    const { res, data } = await request("/api/artists");
    assert(res.ok, `Fetch artists failed: ${res.status}`);
    assert(Array.isArray(data.artists), "Artists response missing array");
    const found = data.artists.find(a => a.id === testArtistId || a._id === testArtistId);
    assert(found, "Test artist not found in listing");
  });

  await test("TV user can list albums", async () => {
    const { res, data } = await request("/api/albums");
    assert(res.ok, `Fetch albums failed: ${res.status}`);
    assert(Array.isArray(data.albums), "Albums response missing array");
    const found = data.albums.find(a => a.id === testAlbumId || a._id === testAlbumId);
    assert(found, "Test album not found in listing");
  });

  await test("Albums can be filtered by artistId", async () => {
    const { res, data } = await request(`/api/albums?artistId=${testArtistId}`);
    assert(res.ok, `Filter albums failed: ${res.status}`);
    assert(Array.isArray(data.albums), "Filtered albums missing array");
    if (data.albums.length > 0) {
      const all = data.albums.every(a => (a.artistId || a.artist) === testArtistId);
      assert(all, "Filtered albums contain wrong artist");
    }
  });

  await test("User cannot create artist (admin-only)", async () => {
    const { res } = await request("/api/artists", {
      method: "POST",
      headers: { "Content-Type": "application/json", "Authorization": `Bearer ${userToken}` },
      body: JSON.stringify({ name: "Unauthorized Artist" }),
    });
    assert(res.status === 403, `Expected 403, got ${res.status}`);
  });

  // =============================================
  // 5. SONG CRUD + METADATA
  // =============================================
  console.log("\n--- Song CRUD + Metadata ---");

  let testSongId;

  await test("Admin creates song with artist+album", async () => {
    const { res, data } = await request("/api/songs", {
      method: "POST",
      headers: adminHeaders,
      body: JSON.stringify({
        title: "Phase8 Integration Song",
        artist: "Phase8 Test Artist",
        album: "Phase8 Test Album",
        albumId: testAlbumId,
        category: "Tamil Hits",
        cover: "https://picsum.photos/seed/phase8/600/600",
        audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
        duration: "5:44",
        section: "latest",
      }),
    });
    assert(res.status === 201, `Create song failed: ${res.status} ${data.message}`);
    testSongId = data.song?.id || data.song?.externalId;
    assert(testSongId, "Created song missing ID");
  });

  await test("Song metadata has correct artist/album", async () => {
    const { res, data } = await request(`/api/songs`);
    assert(res.ok, `Fetch songs failed: ${res.status}`);
    const song = data.songs?.find(s => s.id === testSongId);
    assert(song, "Test song not in listing");
    assert(song.artist === "Phase8 Test Artist", `Artist mismatch: ${song.artist}`);
    assert(song.cover, "Cover missing");
    assert(song.audioUrl || song.audio, "Audio URL missing");
  });

  await test("Admin updates song title", async () => {
    const { res, data } = await request(`/api/songs/${testSongId}`, {
      method: "PUT",
      headers: adminHeaders,
      body: JSON.stringify({ title: "Phase8 Integration Song Updated" }),
    });
    assert(res.ok, `Update failed: ${res.status} ${data.message}`);
  });

  // =============================================
  // 6. CATEGORIES
  // =============================================
  console.log("\n--- Categories ---");

  await test("Categories load", async () => {
    const { res, data } = await request("/api/categories");
    assert(res.ok, `Categories failed: ${res.status}`);
    assert(data.categories?.length > 0, "No categories");
  });

  // =============================================
  // 7. TV BROWSING (public/user endpoints)
  // =============================================
  console.log("\n--- TV Browsing ---");

  await test("Songs listing loads", async () => {
    const { res, data } = await request("/api/songs");
    assert(res.ok, `Songs failed: ${res.status}`);
    assert(Array.isArray(data.songs), "Songs not array");
    assert(data.songs.length > 0, "No songs");
  });

  await test("Songs have required fields", async () => {
    const { data } = await request("/api/songs");
    const song = data.songs[0];
    assert(song.id, "Missing id");
    assert(song.title, "Missing title");
    assert(song.artist, "Missing artist");
    assert(song.cover, "Missing cover");
    assert(song.duration, "Missing duration");
  });

  await test("Song filtering by category", async () => {
    const { res, data } = await request("/api/songs?category=Tamil%20Hits");
    assert(res.ok, `Category filter failed: ${res.status}`);
    assert(Array.isArray(data.songs), "Filtered songs not array");
  });

  // =============================================
  // 8. STREAMING (authenticated)
  // =============================================
  console.log("\n--- Streaming ---");

  await test("Streaming requires auth", async () => {
    const { data } = await request("/api/songs");
    const song = data.songs[0];
    if (song?.audioUrl?.startsWith("/api/files/")) {
      const { res } = await request(song.audioUrl);
      assert(res.status === 401, `Expected 401, got ${res.status}`);
    }
  });

  await test("Streaming with ?token= works", async () => {
    const { data } = await request("/api/songs");
    const song = data.songs[0];
    if (song?.audioUrl?.startsWith("/api/files/")) {
      const audioRes = await fetch(`${BASE}${song.audioUrl}?token=${userToken}`, { method: "HEAD" });
      assert(audioRes.ok || audioRes.status === 206 || audioRes.status === 302, `Stream failed: ${audioRes.status}`);
    }
  });

  await test("Range request returns 206", async () => {
    const { data } = await request("/api/songs");
    const song = data.songs[0];
    if (song?.audioUrl?.startsWith("/api/files/")) {
      const audioRes = await fetch(`${BASE}${song.audioUrl}?token=${userToken}`, {
        headers: { "Range": "bytes=0-1023" }
      });
      assert(audioRes.status === 206, `Expected 206, got ${audioRes.status}`);
      const acceptRanges = audioRes.headers.get("accept-ranges");
      assert(acceptRanges === "bytes", `Accept-Ranges: ${acceptRanges}`);
    }
  });

  await test("Cover art loads", async () => {
    const { data } = await request("/api/songs");
    const song = data.songs[0];
    if (song?.cover?.startsWith("/api/files/cover/")) {
      const coverRes = await fetch(`${BASE}${song.cover}?token=${userToken}`, { method: "HEAD" });
      assert(coverRes.ok || coverRes.status === 302, `Cover failed: ${coverRes.status}`);
    }
  });

  // =============================================
  // 9. ERROR / SECURITY TESTS
  // =============================================
  console.log("\n--- Error + Security Tests ---");

  await test("Nonexistent song returns 404", async () => {
    const { res } = await request("/api/songs/000000000000000000000000", {
      method: "GET",
      headers: { "Authorization": `Bearer ${adminToken}` },
    });
    assert(res.status === 404, `Expected 404, got ${res.status}`);
  });

  await test("Nonexistent artist returns 404", async () => {
    const { res } = await request("/api/artists/000000000000000000000000");
    assert(res.status === 404, `Expected 404, got ${res.status}`);
  });

  await test("Nonexistent album returns 404", async () => {
    const { res } = await request("/api/albums/000000000000000000000000");
    assert(res.status === 404, `Expected 404, got ${res.status}`);
  });

  await test("Streaming nonexistent song returns 404", async () => {
    const { res } = await request("/api/files/audio/000000000000000000000000?token=" + userToken);
    assert(res.status === 404, `Expected 404, got ${res.status}`);
  });

  await test("User cannot delete songs", async () => {
    const { res } = await request(`/api/songs/${testSongId}`, {
      method: "DELETE",
      headers: { "Authorization": `Bearer ${userToken}` },
    });
    assert(res.status === 403, `Expected 403, got ${res.status}`);
  });

  await test("User cannot delete artists", async () => {
    const { res } = await request(`/api/artists/${testArtistId}`, {
      method: "DELETE",
      headers: { "Authorization": `Bearer ${userToken}` },
    });
    assert(res.status === 403, `Expected 403, got ${res.status}`);
  });

  await test("User cannot delete albums", async () => {
    const { res } = await request(`/api/albums/${testAlbumId}`, {
      method: "DELETE",
      headers: { "Authorization": `Bearer ${userToken}` },
    });
    assert(res.status === 403, `Expected 403, got ${res.status}`);
  });

  // =============================================
  // 10. CLEANUP TEST DATA
  // =============================================
  console.log("\n--- Cleanup ---");

  await test("Admin deletes test song", async () => {
    const { res } = await request(`/api/songs/${testSongId}`, {
      method: "DELETE",
      headers: { "Authorization": `Bearer ${adminToken}` },
    });
    assert(res.ok, `Delete song failed: ${res.status}`);
  });

  await test("Admin deletes test album", async () => {
    const { res } = await request(`/api/albums/${testAlbumId}`, {
      method: "DELETE",
      headers: { "Authorization": `Bearer ${adminToken}` },
    });
    assert(res.ok, `Delete album failed: ${res.status}`);
  });

  await test("Admin deletes test artist", async () => {
    const { res } = await request(`/api/artists/${testArtistId}`, {
      method: "DELETE",
      headers: { "Authorization": `Bearer ${adminToken}` },
    });
    assert(res.ok, `Delete artist failed: ${res.status}`);
  });

  // =============================================
  // SUMMARY
  // =============================================
  console.log(`\n${"=".repeat(50)}`);
  console.log(`Results: ${passed} passed, ${failed} failed, ${skipped} skipped`);
  console.log(`${"=".repeat(50)}`);

  if (failed > 0) {
    console.error("\nIntegration test FAILED.");
    process.exit(1);
  } else {
    console.log("\nAll integration checks passed.");
  }
}

run().catch((err) => {
  console.error("\nIntegration test crashed:", err.message);
  process.exit(1);
});
