// Song / Category / Artist response formatters

const formatSong = (song) => {
  const json = song.toJSON();
  delete json.isActive;
  delete json.uploadedBy;
  delete json.externalId;

  const songId = song._id.toString();

  if (json.cover) {
    const isUrl = /^(https?:)/i.test(json.cover);
    if (!isUrl || json.cover.includes("filebase.io")) {
      json.cover = `/api/files/cover/${songId}`;
    }
  }

  if (json.audioUrl) {
    const isUrl = /^(https?:)/i.test(json.audioUrl);
    if (!isUrl || json.audioUrl.includes("filebase.io")) {
      json.audioUrl = `/api/files/audio/${songId}`;
    }
  } else {
    json.audioUrl = null;
  }

  if (json.surroundUrl) {
    const isUrl = /^(https?:)/i.test(json.surroundUrl);
    if (!isUrl || json.surroundUrl.includes("filebase.io")) {
      json.surroundUrl = `/api/files/surround/${songId}`;
    }
  } else {
    json.surroundUrl = null;
  }

  if (json.fallbackUrl) {
    const isUrl = /^(https?:)/i.test(json.fallbackUrl);
    if (!isUrl || json.fallbackUrl.includes("filebase.io")) {
      json.fallbackUrl = `/api/files/fallback/${songId}`;
    }
  } else {
    json.fallbackUrl = null;
  }

  return json;
};

const formatCategory = (category) => ({
  id: category._id.toString(),
  name: category.name,
  count: category.songCount,
  color: category.color,
});

const formatArtist = (artist) => ({
  id: artist._id ? artist._id.toString() : artist.id,
  name: artist.name,
  image: artist.image,
  songCount: artist.songCount || 0,
});

const formatAlbum = (album) => ({
  id: album._id ? album._id.toString() : album.id,
  title: album.title,
  artistId: album.artistId ? album.artistId.toString() : null,
  artistName: album.artistName,
  cover: album.cover || null,
  releaseYear: album.releaseYear || null,
  songCount: album.songCount || 0,
});

const formatUser = (user) => user.toJSON();

module.exports = {
  formatSong,
  formatCategory,
  formatArtist,
  formatAlbum,
  formatUser,
};
