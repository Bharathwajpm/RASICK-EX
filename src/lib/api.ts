import type { Song } from "@/lib/mock-data";

const API_BASE =
  import.meta.env.VITE_API_URL ?? (import.meta.env.DEV ? "" : "http://localhost:5000");

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
    return data.message ?? "Request failed";
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

  let data: { message?: string; username?: string; role?: "admin" | "user"; token?: string };
  try {
    data = await res.json();
  } catch (err) {
    return { error: "Invalid response from server" };
  }

  if (!res.ok) {
    return { error: data.message ?? "Invalid Username or Password" };
  }

  if (!data.username || !data.role || !data.token) {
    return { error: "Invalid response from server" };
  }

  return {
    user: {
      username: data.username.toLowerCase(),
      role: data.role,
      token: data.token,
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
  return ((data.songs ?? []) as Song[]).map(normalizeSong);
}

export async function createSong(payload: {
  title: string;
  category: string;
  coverFile: File;
  audioFile: File;
  artist?: string;
  section?: string;
}): Promise<{ message: string; song: Song }> {
  const form = new FormData();
  form.append("title", payload.title);
  form.append("category", payload.category);
  form.append("cover", payload.coverFile);
  form.append("audio", payload.audioFile);
  if (payload.artist) form.append("artist", payload.artist);
  if (payload.section) form.append("section", payload.section);

  const res = await apiFetch("/api/songs", {
    method: "POST",
    headers: getAuthHeaders(),
    body: form,
  });

  const data = await res.json();
  if (!res.ok) throw new Error(data.message ?? "Upload failed");
  return {
    ...data,
    song: normalizeSong(data.song as Song),
  };
}

export async function updateSong(
  id: string,
  payload: Partial<Pick<Song, "title" | "artist" | "cover" | "duration" | "category">>
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
  if (!res.ok) throw new Error(data.message ?? "Update failed");
  return data;
}

export async function deleteSong(id: string): Promise<{ message: string }> {
  const res = await apiFetch(`/api/songs/${encodeURIComponent(id)}`, {
    method: "DELETE",
    headers: getAuthHeaders(),
  });

  const data = await res.json();
  if (!res.ok) throw new Error(data.message ?? "Delete failed");
  return data;
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
  return (data.categories ?? []) as Category[];
}

export async function createCategory(name: string, color?: string): Promise<Category> {
  const res = await apiFetch("/api/categories", {
    method: "POST",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
    body: JSON.stringify({ name, color }),
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.message ?? "Failed to create category");
  return data.category as Category;
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
  if (!res.ok) throw new Error(data.message ?? "Failed to update category");
  return data.category as Category;
}

export async function deleteCategory(id: string): Promise<void> {
  const res = await apiFetch(`/api/categories/${encodeURIComponent(id)}`, {
    method: "DELETE",
    headers: getAuthHeaders(),
  });
  if (!res.ok) {
    const data = await res.json();
    throw new Error(data.message ?? "Failed to delete category");
  }
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
  return {
    ...data,
    songs: ((data.songs ?? []) as Song[]).map(normalizeSong),
  };
}
