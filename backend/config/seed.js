const bcrypt = require("bcryptjs");
const User = require("../models/User");
const Song = require("../models/Song");
const Category = require("../models/Category");
const Artist = require("../models/Artist");

const categories = [
  { name: "Old Songs", color: "from-emerald-500 to-teal-700" },
  { name: "Tamil Hits", color: "from-blue-500 to-indigo-600" },
  { name: "Temple Songs", color: "from-orange-500 to-red-600" },
  { name: "DTS Audio", color: "from-cyan-400 to-blue-700" },
  { name: "DJ Mixes", color: "from-purple-500 to-violet-700" },
  { name: "Festival Songs", color: "from-amber-500 to-orange-600" },
];

const artists = [
  { name: "Anirudh", image: "https://picsum.photos/seed/anirudh-a/600/600" },
  { name: "Sid Sriram", image: "https://picsum.photos/seed/sidsriram-a/600/600" },
  { name: "Shreya Ghoshal", image: "https://picsum.photos/seed/shreya-a/600/600" },
  { name: "Yuvan", image: "https://picsum.photos/seed/yuvan-a/600/600" },
  { name: "GV Prakash", image: "https://picsum.photos/seed/gvp-a/600/600" },
  { name: "Dhee", image: "https://picsum.photos/seed/dhee-a/600/600" },
];

const cover = (seed) => `https://picsum.photos/seed/${seed}/600/600`;

// Seeded songs are catalog placeholders — no audio URLs.
// Real audio comes only from user uploads via the admin panel.
const songs = [
  { externalId: "t1", title: "Vaa Machaney", artist: "Anirudh", cover: cover("vaa"), duration: "3:42", section: "trending" },
  { externalId: "t2", title: "Kaadhal Theevey", artist: "Sid Sriram", cover: cover("kaadhal"), duration: "4:10", section: "trending" },
  { externalId: "t3", title: "Singam Roar", artist: "Yuvan Shankar Raja", cover: cover("singam"), duration: "3:25", section: "trending" },
  { externalId: "t4", title: "Mazhai Kuruvi", artist: "Shreya Ghoshal", cover: cover("mazhai"), duration: "4:32", section: "trending" },
  { externalId: "t5", title: "Naattu Koothu", artist: "Dhee", cover: cover("naattu"), duration: "3:08", section: "trending" },
  { externalId: "t6", title: "Tharam Maaru", artist: "GV Prakash", cover: cover("tharam"), duration: "3:50", section: "trending" },
  { externalId: "l1", title: "Perambalur Anthem", artist: "Local Stars", cover: cover("anthem"), duration: "4:01", section: "latest" },
  { externalId: "l2", title: "Midnight Drive", artist: "DJ Karthik", cover: cover("midnight"), duration: "5:22", section: "latest" },
  { externalId: "l3", title: "Theru Vaasal", artist: "Pradeep Kumar", cover: cover("theru"), duration: "3:38", section: "latest" },
  { externalId: "l4", title: "Kanavu Kann", artist: "Chinmayi", cover: cover("kanavu"), duration: "4:14", section: "latest" },
  { externalId: "l5", title: "Veethi Veethi", artist: "Santhosh Narayanan", cover: cover("veethi"), duration: "3:55", section: "latest" },
  { externalId: "p1", title: "Aadhi Amman Festival", artist: "Live Recording", cover: cover("amman"), duration: "6:12", section: "perambalur", category: "Temple" },
  { externalId: "p2", title: "Karagattam Beats", artist: "Folk Troupe", cover: cover("karagattam"), duration: "5:48", section: "perambalur", category: "Folk" },
  { externalId: "p3", title: "DJ Sundar Mix Vol. 4", artist: "DJ Sundar", cover: cover("djsundar"), duration: "8:30", section: "perambalur", category: "DJ" },
  { externalId: "p4", title: "Pongal Celebrations", artist: "Local Events", cover: cover("pongal"), duration: "4:44", section: "perambalur", category: "Event" },
  { externalId: "p5", title: "Thavil Thunder", artist: "Master Murugan", cover: cover("thavil"), duration: "5:10", section: "perambalur", category: "Folk" },
  { externalId: "r1", title: "Late Night Loops", artist: "Anirudh", cover: cover("late"), duration: "3:30", section: "recommended" },
  { externalId: "r2", title: "Ennai Theriyuma", artist: "Sid Sriram", cover: cover("ennai"), duration: "4:22", section: "recommended" },
  { externalId: "r3", title: "Kondaadu", artist: "Dhee", cover: cover("kondaadu"), duration: "3:14", section: "recommended" },
  { externalId: "r4", title: "Vidiyum Varai", artist: "Pradeep", cover: cover("vidiyum"), duration: "4:00", section: "recommended" },
];

const seedDatabase = async () => {
  const userCount = await User.countDocuments();
  if (userCount === 0) {
    const adminUsername = (process.env.SEED_ADMIN_USERNAME || "RASICKEX").trim().toLowerCase();
    const adminPassword = process.env.SEED_ADMIN_PASSWORD;
    const userUsername = (process.env.SEED_USER_USERNAME || "RASICKEX2026").trim().toLowerCase();
    const userPassword = process.env.SEED_USER_PASSWORD;

    if (!adminPassword || !userPassword) {
      console.warn(
        "[Seed] SEED_ADMIN_PASSWORD and/or SEED_USER_PASSWORD not set in .env. " +
        "Skipping user seeding. Set these values and restart to create default users."
      );
    } else {
      const passwordHash = await bcrypt.hash(adminPassword, 10);
      const userPasswordHash = await bcrypt.hash(userPassword, 10);

      await User.insertMany([
        { username: adminUsername, password: passwordHash, role: "admin" },
        { username: userUsername, password: userPasswordHash, role: "user" },
      ]);
      console.log(`[Seed] Default users created: ${adminUsername} (admin), ${userUsername} (user)`);
    }
  }

  const categoryCount = await Category.countDocuments();
  if (categoryCount === 0) {
    await Category.insertMany(categories);
    console.log("Seeded categories");
  }

  const artistCount = await Artist.countDocuments();
  if (artistCount === 0) {
    await Artist.insertMany(artists);
    console.log("Seeded artists");
  }

  const activeSongsCount = await Song.countDocuments({ section: "trending", isActive: true });
  if (activeSongsCount === 0) {
    console.log("No active trending songs found. Reactivating or re-seeding default catalog...");
    const defaultExternalIds = songs.map(s => s.externalId).filter(Boolean);
    await Song.deleteMany({ externalId: { $in: defaultExternalIds } });

    const enrichedSongs = songs.map(s => ({
      ...s,
      audioSizeBytes: Math.floor(Math.random() * (8000000 - 3000000)) + 3000000, // 3MB to 8MB
      coverSizeBytes: Math.floor(Math.random() * (300000 - 50000)) + 50000,     // 50KB to 300KB
      playCount: Math.floor(Math.random() * 200) + 20,
      downloadCount: Math.floor(Math.random() * 80) + 5,
      isActive: true
    }));
    await Song.insertMany(enrichedSongs);
    console.log("Seeded songs (catalog placeholders — upload real audio via admin panel)");
  }
  // No migration logic — uploaded songs are never overwritten with demo URLs.
};

module.exports = seedDatabase;
