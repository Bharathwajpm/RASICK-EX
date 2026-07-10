/**
 * Run with backend + MongoDB up:
 *   node scripts/integration-test.js
 */
const BASE = process.env.API_URL || "http://localhost:5000";

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
  return { res, data };
}

async function run() {
  console.log("Integration test against", BASE);

  const health = await request("/health");
  assert(health.res.ok, `Health check failed: ${health.res.status}`);
  console.log("✓ Health");

  const badLogin = await request("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username: "admin", password: "wrong", role: "admin" }),
  });
  assert(badLogin.res.status === 401, "Expected 401 for bad login");
  console.log("✓ Login rejects invalid credentials");

  const adminLogin = await request("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username: "admin", password: "admin123", role: "admin" }),
  });
  assert(adminLogin.res.ok, `Admin login failed: ${adminLogin.data.message}`);
  assert(adminLogin.data.role === "admin", "Admin role mismatch");
  const token = adminLogin.data.token;
  assert(token, "Admin login response missing JWT token");
  console.log("✓ Admin login");

  const userLogin = await request("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username: "user", password: "user123", role: "user" }),
  });
  assert(userLogin.res.ok, `User login failed: ${userLogin.data.message}`);
  console.log("✓ User login");

  const trending = await request("/api/songs?section=trending");
  assert(trending.res.ok, "Failed to fetch trending songs");
  assert(Array.isArray(trending.data.songs), "Songs response missing array");
  assert(trending.data.songs.length > 0, "No trending songs seeded");
  const song = trending.data.songs[0];
  assert(song.id && song.title && song.artist && song.cover && song.duration, "Song fields missing");
  console.log(`✓ User songs (${trending.data.songs.length} trending)`);

  const adminHeaders = {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${token}`,
  };

  const created = await request("/api/songs", {
    method: "POST",
    headers: adminHeaders,
    body: JSON.stringify({
      title: "Integration Test Song",
      category: "Tamil Hits",
      cover: "https://picsum.photos/seed/integration/600/600",
      audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
      artist: "Test Artist",
      duration: "3:21",
      section: "latest",
    }),
  });
  assert(created.res.status === 201, `Create song failed: ${created.data.message}`);
  const createdId = created.data.song?.id;
  assert(createdId, "Created song missing id");
  console.log("✓ Admin upload");

  const updated = await request(`/api/songs/${createdId}`, {
    method: "PUT",
    headers: adminHeaders,
    body: JSON.stringify({ title: "Integration Test Song Updated" }),
  });
  assert(updated.res.ok, `Update song failed: ${updated.data.message}`);
  console.log("✓ Admin edit");

  const categories = await request("/api/categories");
  assert(categories.res.ok, "Failed to fetch categories");
  assert(categories.data.categories?.length > 0, "No categories seeded");
  console.log(`✓ Categories (${categories.data.categories.length})`);

  // Verify that an unauthorized spoofed request (using headers instead of token) is rejected
  const unauthorizedDelete = await request(`/api/songs/${createdId}`, {
    method: "DELETE",
    headers: {
      "x-username": "admin",
      "x-user-role": "admin",
    },
  });
  assert(unauthorizedDelete.res.status === 401, "Expected 401 when attempting header-based spoofing");
  console.log("✓ Reject header-based auth spoofing attempts");

  const deleted = await request(`/api/songs/${createdId}`, {
    method: "DELETE",
    headers: {
      "Authorization": `Bearer ${token}`,
    },
  });
  assert(deleted.res.ok, `Delete song failed: ${deleted.data.message}`);
  console.log("✓ Admin delete");

  console.log("\nAll integration checks passed.");
}

run().catch((err) => {
  console.error("\nIntegration test failed:", err.message);
  process.exit(1);
});
