import type { Song } from "@/lib/mock-data";

const DOWNLOADS_KEY = "rasick-downloads";
const LIKES_KEY = "rasick-likes";
const RECENT_KEY = "rasick-recent";
const DB_NAME = "rasick-ex-offline";
const DB_VERSION = 1;
const AUDIO_STORE = "audio";

const MAX_RECENT = 30;
const LIBRARY_EVENT = "rasick-library-changed";

function notifyLibraryChange() {
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event(LIBRARY_EVENT));
  }
}

export function subscribeLibraryChange(listener: () => void) {
  if (typeof window === "undefined") return () => {};
  window.addEventListener(LIBRARY_EVENT, listener);
  return () => window.removeEventListener(LIBRARY_EVENT, listener);
}

function readJson<T>(key: string, fallback: T): T {
  if (typeof window === "undefined") return fallback;
  try {
    const raw = localStorage.getItem(key);
    return raw ? (JSON.parse(raw) as T) : fallback;
  } catch {
    return fallback;
  }
}

function writeJson<T>(key: string, value: T) {
  localStorage.setItem(key, JSON.stringify(value));
  notifyLibraryChange();
}

function openDb(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);
    request.onupgradeneeded = () => {
      const db = request.result;
      if (!db.objectStoreNames.contains(AUDIO_STORE)) {
        db.createObjectStore(AUDIO_STORE);
      }
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error ?? new Error("Failed to open offline storage"));
  });
}

async function cacheAudioBlob(songId: string, blob: Blob) {
  const db = await openDb();
  await new Promise<void>((resolve, reject) => {
    const tx = db.transaction(AUDIO_STORE, "readwrite");
    tx.objectStore(AUDIO_STORE).put(blob, songId);
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error ?? new Error("Failed to cache audio"));
  });
  db.close();
}

async function removeAudioBlob(songId: string) {
  const db = await openDb();
  await new Promise<void>((resolve, reject) => {
    const tx = db.transaction(AUDIO_STORE, "readwrite");
    tx.objectStore(AUDIO_STORE).delete(songId);
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error ?? new Error("Failed to remove cached audio"));
  });
  db.close();
}

async function clearAudioBlobs() {
  const db = await openDb();
  await new Promise<void>((resolve, reject) => {
    const tx = db.transaction(AUDIO_STORE, "readwrite");
    tx.objectStore(AUDIO_STORE).clear();
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error ?? new Error("Failed to clear cached audio"));
  });
  db.close();
}

export async function getCachedAudioUrl(songId: string): Promise<string | null> {
  try {
    const db = await openDb();
    const blob = await new Promise<Blob | undefined>((resolve, reject) => {
      const tx = db.transaction(AUDIO_STORE, "readonly");
      const req = tx.objectStore(AUDIO_STORE).get(songId);
      req.onsuccess = () => resolve(req.result as Blob | undefined);
      req.onerror = () => reject(req.error ?? new Error("Failed to read cached audio"));
    });
    db.close();
    return blob ? URL.createObjectURL(blob) : null;
  } catch {
    return null;
  }
}

export function getDownloadedSongs(): Song[] {
  return readJson<Song[]>(DOWNLOADS_KEY, []);
}

export function isDownloaded(songId: string): boolean {
  return getDownloadedSongs().some((s) => s.id === songId);
}

export async function downloadSong(song: Song): Promise<void> {
  if (!song.audioUrl) {
    throw new Error("This song has no audio file");
  }

  const response = await fetch(song.audioUrl);
  if (!response.ok) {
    throw new Error("Failed to download audio file");
  }

  const blob = await response.blob();
  await cacheAudioBlob(song.id, blob);

  const downloads = getDownloadedSongs().filter((s) => s.id !== song.id);
  downloads.unshift(song);
  writeJson(DOWNLOADS_KEY, downloads);
}

export async function removeDownload(songId: string): Promise<void> {
  localStorage.setItem(
    DOWNLOADS_KEY,
    JSON.stringify(getDownloadedSongs().filter((s) => s.id !== songId))
  );
  await removeAudioBlob(songId);
  notifyLibraryChange();
}

export async function clearAllDownloads(): Promise<void> {
  localStorage.setItem(DOWNLOADS_KEY, JSON.stringify([]));
  await clearAudioBlobs();
  notifyLibraryChange();
}

export function getLikedSongs(): Song[] {
  return readJson<Song[]>(LIKES_KEY, []);
}

export function isLiked(songId: string): boolean {
  return getLikedSongs().some((s) => s.id === songId);
}

export function toggleLike(song: Song): boolean {
  const liked = getLikedSongs();
  const exists = liked.some((s) => s.id === song.id);
  if (exists) {
    writeJson(
      LIKES_KEY,
      liked.filter((s) => s.id !== song.id)
    );
    return false;
  }
  writeJson(LIKES_KEY, [song, ...liked]);
  return true;
}

export function getRecentlyPlayed(): Song[] {
  return readJson<Song[]>(RECENT_KEY, []);
}

export function addRecentlyPlayed(song: Song) {
  const recent = getRecentlyPlayed().filter((s) => s.id !== song.id);
  recent.unshift(song);
  writeJson(RECENT_KEY, recent.slice(0, MAX_RECENT));
}
