import type { Song } from "@/lib/mock-data";

const API_BASE = (() => {
  if (import.meta.env.VITE_API_URL) return import.meta.env.VITE_API_URL;
  if (import.meta.env.DEV) return ""; // Vite dev proxy handles /api
  // Production: use relative URLs (same origin) when VITE_API_URL is not set.
  // Set VITE_API_URL at build time if backend is on a different origin.
  return "";
})();

export function resolveMediaUrl(url?: string): string {
  if (!url) return "";
  if (/^(https?:|data:|blob:)/i.test(url)) return url;
  return `${API_BASE}${url.startsWith("/") ? url : `/${url}`}`;
}

function normalizeSong(song: Song): Song {
  return {
    ...song,
    cover: resolveMediaUrl(song.cover),
    audioUrl: song.audioUrl ? resolveMediaUrl(song.audioUrl) : song.audioUrl,
    surroundUrl: song.surroundUrl ? resolveMediaUrl(song.surroundUrl) : song.surroundUrl,
    fallbackUrl: song.fallbackUrl ? resolveMediaUrl(song.fallbackUrl) : song.fallbackUrl,
  };
}

export interface AuthUser {
  username: string;
  role: "admin" | "user";
  token?: string;
}

const AUTH_KEY = "rasick-user";

export function getStoredUser(): AuthUser | null {
  if (typeof window === "undefined") return null;
  const raw = sessionStorage.getItem(AUTH_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

export function setStoredUser(user: AuthUser | null) {
  if (user) {
    sessionStorage.setItem(AUTH_KEY, JSON.stringify(user));
  } else {
    sessionStorage.removeItem(AUTH_KEY);
  }
}

export function getAuthHeaders(): Record<string, string> {
  const user = getStoredUser();
  if (!user || !user.token) return {};
  return {
    "Authorization": `Bearer ${user.token}`,
  };
}

async function parseError(res: Response) {
  try {
    const data = await res.json();
    return data.message ?? data.error ?? "Request failed";
  } catch {
    return res.statusText || "Request failed";
  }
}

async function apiFetch(path: string, init?: RequestInit) {
  try {
    return await fetch(`${API_BASE}${path}`, init);
  } catch {
    throw new Error("Unable to connect to server. Is the backend running?");
  }
}

export async function loginApi(
  username: string,
  password: string,
  role: "admin" | "user"
): Promise<{ user: AuthUser } | { error: string }> {
  let res: Response;
  try {
    res = await apiFetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password, role }),
    });
  } catch (err) {
    return { error: err instanceof Error ? err.message : "Unable to connect to server" };
  }

  let responseData: any;
  try {
    responseData = await res.json();
  } catch (err) {
    return { error: "Invalid response from server" };
  }

  if (!res.ok) {
    return { error: responseData.message ?? responseData.error ?? "Invalid Username or Password" };
  }

  const payload = responseData.success && responseData.data ? responseData.data : responseData;

  if (!payload.username || !payload.role || !payload.token) {
    return { error: "Invalid response from server" };
  }

  return {
    user: {
      username: payload.username.toLowerCase(),
      role: payload.role,
      token: payload.token,
    },
  };
}

export async function fetchSongs(params?: {
  section?: string;
  category?: string;
  q?: string;
}): Promise<Song[]> {
  const search = new URLSearchParams();
  if (params?.section) search.set("section", params.section);
  if (params?.category) search.set("category", params.category);
  if (params?.q) search.set("q", params.q);

  const query = search.toString();
  const res = await apiFetch(`/api/songs${query ? `?${query}` : ""}`);

  if (!res.ok) {
    throw new Error(await parseError(res));
  }

  const data = await res.json();
  const songsData = (data.success && data.data ? data.data.songs : data.songs) ?? [];
  return (songsData as Song[]).map(normalizeSong);
}

export async function createSong(payload: {
  title: string;
  category: string;
  coverFile: File;
  audioFile: File;
  fallbackFile?: File;
  artist?: string;
  section?: string;
  album?: string;
  albumId?: string;
}): Promise<{ message: string; song: Song }> {
  const form = new FormData();
  form.append("title", payload.title);
  form.append("category", payload.category);
  form.append("cover", payload.coverFile);
  form.append("audio", payload.audioFile);
  if (payload.fallbackFile) form.append("fallback", payload.fallbackFile);
  if (payload.artist) form.append("artist", payload.artist);
  if (payload.section) form.append("section", payload.section);
  if (payload.album) form.append("album", payload.album);
  if (payload.albumId) form.append("albumId", payload.albumId);

  const res = await apiFetch("/api/songs", {
    method: "POST",
    headers: getAuthHeaders(),
    body: form,
  });

  // Guard: always parse JSON safely. If the backend crashed mid-upload
  // (ECONNRESET / empty body), res.json() throws "Unexpected end of JSON input".
  let data: any = {};
  try {
    data = await res.json();
  } catch {
    // The backend returned no body or a broken stream.
    if (!res.ok) {
      throw new Error(
        `Upload failed (HTTP ${res.status}). The server may have crashed during the upload. Check backend logs.`
      );
    }
    throw new Error("Server returned an empty response. Check backend logs.");
  }

  if (!res.ok) throw new Error(data.message ?? data.error ?? "Upload failed");
  
  const payloadData = data.success && data.data ? data.data : data;
  return {
    message: payloadData.message ?? "Song uploaded successfully!",
    song: normalizeSong(payloadData.song as Song),
  };
}

export async function updateSong(
  id: string,
  payload: Partial<Pick<Song, "title" | "artist" | "cover" | "duration" | "category" | "album" | "albumId">>
): Promise<{ message: string; song: Song }> {
  const res = await apiFetch(`/api/songs/${encodeURIComponent(id)}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      ...getAuthHeaders(),
    },
    body: JSON.stringify(payload),
  });

  const data = await res.json();
  if (!res.ok) throw new Error(data.message ?? data.error ?? "Update failed");
  const payloadData = data.success && data.data ? data.data : data;
  return {
    message: payloadData.message ?? "Song updated successfully",
    song: normalizeSong(payloadData.song as Song),
  };
}

export async function deleteSong(id: string): Promise<{ message: string }> {
  const res = await apiFetch(`/api/songs/${encodeURIComponent(id)}`, {
    method: "DELETE",
    headers: getAuthHeaders(),
  });

  const data = await res.json();
  if (!res.ok) throw new Error(data.message ?? data.error ?? "Delete failed");
  const payloadData = data.success && data.data ? data.data : data;
  return {
    message: payloadData.message ?? "Song deleted successfully",
  };
}

export function fileToDataUrl(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = () => reject(new Error("Failed to read file"));
    reader.readAsDataURL(file);
  });
}

export interface Category {
  id: string;
  name: string;
  count: number;
  color: string;
}

export async function fetchCategories(): Promise<Category[]> {
  const res = await apiFetch("/api/categories");
  if (!res.ok) throw new Error(await parseError(res));
  const data = await res.json();
  const categoriesData = (data.success && data.data ? data.data.categories : data.categories) ?? [];
  return categoriesData as Category[];
}

export async function createCategory(name: string, color?: string): Promise<Category> {
  const res = await apiFetch("/api/categories", {
    method: "POST",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
    body: JSON.stringify({ name, color }),
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.message ?? data.error ?? "Failed to create category");
  const payloadData = data.success && data.data ? data.data : data;
  return payloadData.category as Category;
}

export async function updateCategory(
  id: string,
  payload: { name?: string; color?: string }
): Promise<Category> {
  const res = await apiFetch(`/api/categories/${encodeURIComponent(id)}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
    body: JSON.stringify(payload),
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.message ?? data.error ?? "Failed to update category");
  const payloadData = data.success && data.data ? data.data : data;
  return payloadData.category as Category;
}

export async function deleteCategory(id: string): Promise<void> {
  const res = await apiFetch(`/api/categories/${encodeURIComponent(id)}`, {
    method: "DELETE",
    headers: getAuthHeaders(),
  });
  if (!res.ok) {
    const data = await res.json();
    throw new Error(data.message ?? data.error ?? "Failed to delete category");
  }
}

// ─── Artists ─────────────────────────────────────────────────────────
export interface Artist {
  id: string;
  name: string;
  image: string | null;
  songCount: number;
}

export async function fetchArtists(): Promise<Artist[]> {
  const res = await apiFetch("/api/artists");
  if (!res.ok) throw new Error(await parseError(res));
  const data = await res.json();
  const artists = (data.success && data.data ? data.data.artists : data.artists) ?? [];
  return artists as Artist[];
}

// ─── Albums ──────────────────────────────────────────────────────────
export interface Album {
  id: string;
  title: string;
  artistId: string | null;
  artistName: string | null;
  cover: string | null;
  releaseYear: number | null;
  songCount: number;
}

export async function fetchAlbums(artistId?: string): Promise<Album[]> {
  const search = artistId ? `?artistId=${encodeURIComponent(artistId)}` : "";
  const res = await apiFetch(`/api/albums${search}`);
  if (!res.ok) throw new Error(await parseError(res));
  const data = await res.json();
  const albums = (data.success && data.data ? data.data.albums : data.albums) ?? [];
  return albums as Album[];
}

export interface AdminDashboardData {
  stats: {
    totalSongs: number;
    totalCategories: number;
    totalUsers: number;
    downloads: number;
    storage: {
      used: number;
      capacity: number;
      percentage: number;
      remaining: number;
      warning: boolean;
      rawUsed: number;
    };
    analytics: {
      uploadsToday: number;
      downloadsToday: number;
      totalDownloads: number;
      mostPlayedSong: string;
      largestSong: string;
      largestCoverImage: string;
      avgSongSize: string;
    };
    songStats: {
      totalAudioSize: string;
      avgAudioSize: string;
      largestAudioFile: string;
      smallestAudioFile: string;
      newestUpload: string;
      oldestUpload: string;
      totalCovers: number;
      avgCoverSize: string;
      largestCover: string;
    };
    userStats: {
      registeredUsers: number;
      adminUsers: number;
      normalUsers: number;
      newUsersToday: number;
      mostActiveUser: string;
    };
    downloadStats: {
      downloadedSongs: number;
      downloadsToday: number;
      mostDownloadedSong: string;
      totalOfflineStorageUsed: string;
      offlineCachedSongs: number;
    };
    playbackStats: {
      mostPlayedSong: string;
      leastPlayedSong: string;
      recentlyPlayedCount: number;
      favoriteCount: number;
      playlistCount: number;
    };
    systemHealth: {
      backend: string;
      mongodb: string;
      filebase: string;
      uploadService: string;
      streamingService: string;
      downloadService: string;
      storageService: string;
    };
  };
  songs: Song[];
}

export async function fetchAdminDashboard(): Promise<AdminDashboardData> {
  const res = await apiFetch("/api/admin/dashboard", {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error(await parseError(res));
  const data = await res.json();
  const payload = data.success && data.data ? data.data : data;
  return {
    stats: payload.stats,
    songs: ((payload.songs ?? []) as Song[]).map(normalizeSong),
  };
}
