import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  useRef,
  type ReactNode,
} from "react";
import type { Song } from "@/lib/mock-data";
import { addRecentlyPlayed, getCachedAudioUrl, getDownloadedSongs, subscribeLibraryChange } from "@/lib/user-library";
import { createBrowserPlaybackService, type PlaybackService } from "@/lib/playback-service";

function parseDurationLabel(duration: string): number {
  const parts = duration.split(":").map(Number);
  if (parts.length === 2) return parts[0] * 60 + parts[1];
  if (parts.length === 3) return parts[0] * 3600 + parts[1] * 60 + parts[2];
  return 0;
}

export function formatPlaybackTime(seconds: number): string {
  if (!Number.isFinite(seconds) || seconds < 0) return "0:00";
  const total = Math.floor(seconds);
  const minutes = Math.floor(total / 60);
  const remaining = total % 60;
  return `${minutes}:${remaining.toString().padStart(2, "0")}`;
}

type PlayerState = {
  current: Song | null;
  isPlaying: boolean;
  queue: Song[];
  isExpanded: boolean;
  currentTime: number;
  duration: number;
  play: (song: Song, queue?: Song[]) => void;
  togglePlay: () => void;
  next: () => void;
  prev: () => void;
  setExpanded: (v: boolean) => void;
  seek: (time: number) => void;
};

const PlayerCtx = createContext<PlayerState | null>(null);

export function PlayerProvider({ children }: { children: ReactNode }) {
  const playbackServiceRef = useRef<PlaybackService | null>(null);
  const [current, setCurrent] = useState<Song | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [queue, setQueue] = useState<Song[]>([]);
  const [isExpanded, setExpanded] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);

  const queueRef = useRef<Song[]>([]);
  const currentRef = useRef<Song | null>(null);
  const activeBlobUrlRef = useRef<string | null>(null);
  const cachedUrlsRef = useRef<Record<string, string>>({});
  const playNextInQueueRef = useRef<() => void>(() => {});

  useEffect(() => {
    queueRef.current = queue;
  }, [queue]);

  useEffect(() => {
    currentRef.current = current;
  }, [current]);

  const syncCachedUrls = useCallback(async () => {
    try {
      const downloaded = getDownloadedSongs();
      const newMap: Record<string, string> = {};

      // Revoke URLs no longer in downloads
      for (const id of Object.keys(cachedUrlsRef.current)) {
        if (!downloaded.some((s) => s.id === id)) {
          URL.revokeObjectURL(cachedUrlsRef.current[id]);
        }
      }

      // Generate object URLs for downloaded songs
      for (const song of downloaded) {
        const existing = cachedUrlsRef.current[song.id];
        if (existing) {
          newMap[song.id] = existing;
        } else {
          const url = await getCachedAudioUrl(song.id);
          if (url) {
            newMap[song.id] = url;
          }
        }
      }

      cachedUrlsRef.current = newMap;
    } catch (err) {
      console.error("Failed to sync cached offline URLs:", err);
    }
  }, []);

  useEffect(() => {
    if (typeof window !== "undefined") {
      void syncCachedUrls();
      return subscribeLibraryChange(() => {
        void syncCachedUrls();
      });
    }
  }, [syncCachedUrls]);

  useEffect(() => {
    return () => {
      for (const url of Object.values(cachedUrlsRef.current)) {
        URL.revokeObjectURL(url);
      }
    };
  }, []);

  const logContext = (func: string, line: number, extra?: any) => {
    if (!import.meta.env.DEV) return;
    const ts = new Date().toISOString();
    console.log(
      `[LOG] [${ts}] File: player-context.tsx, Func: ${func}, Line: ${line}, ` +
      `Song ID: ${current?.id ?? 'none'}, isPlaying: ${isPlaying}, ` +
      `extra: ${JSON.stringify(extra ?? {})}`
    );
  };

  const play = useCallback((song: Song, newQueue?: Song[]) => {
    const service = playbackServiceRef.current;
    if (!service) return;

    if (typeof window !== "undefined") {
      (window as any).__isPlaying = isPlaying;
      (window as any).__currentSongId = current?.id || "";
    }
    const audio = typeof window !== "undefined" ? (window as any).__audio : null;
    (window as any).__traceStep = ((window as any).__traceStep || 0) + 1;
    const step = (window as any).__traceStep;
    console.log(`[TRACE] Step: ${step}, TS: ${performance.now().toFixed(2)}ms, Func: PlayerContext.play(), src: "${audio?.src || ''}", paused: ${audio?.paused ?? true}, isPlaying: ${isPlaying}, currentSongId: "${current?.id || ''}"`);

    const cachedSource = cachedUrlsRef.current[song.id];
    const resolvedSource = service.resolveSource(song);
    const source = cachedSource || resolvedSource;

    if (source) {
      const currentSrc = service.getCurrentSource();
      if (source !== currentSrc) {
        service.setSource(source);
        setCurrentTime(0);
        setDuration(parseDurationLabel(song.duration));
      }

      setCurrent(song);
      setIsPlaying(true);
      if (newQueue) {
        setQueue(newQueue);
      }
      addRecentlyPlayed(song);

      void service.play().catch((err) => {
        console.error("Synchronous click play failed:", err);
        setIsPlaying(false);
      });
    }
  }, []);

  const playNextInQueue = useCallback(() => {
    const active = currentRef.current;
    const activeQueue = queueRef.current;
    if (!active || activeQueue.length === 0) return;
    const index = activeQueue.findIndex((song) => song.id === active.id);
    const nextSong = activeQueue[(index + 1) % activeQueue.length];
    if (nextSong) {
      play(nextSong);
    }
  }, [play]);

  useEffect(() => {
    playNextInQueueRef.current = playNextInQueue;
  }, [playNextInQueue]);

  useEffect(() => {
    const service = createBrowserPlaybackService();
    playbackServiceRef.current = service;

    const disposeHandlers = service.setEventHandlers({
      onTimeUpdate: setCurrentTime,
      onDurationChange: (durationValue) => {
        if (durationValue > 0) {
          setDuration(durationValue);
        }
      },
      onPlayStateChange: (playing) => {
        setIsPlaying((prev) => (prev === playing ? prev : playing));
      },
      onEnded: () => {
        playNextInQueueRef.current();
      },
      onError: (err) => {
        setIsPlaying(false);
        if (err.name === "AbortError" || err.message.toLowerCase().includes("interrupted")) {
          console.warn("Playback interrupted (expected):", err.message);
          return;
        }
        let errorMsg = err.message;
        if (err.message === "MEDIA_ERR_SRC_NOT_SUPPORTED") {
          errorMsg = "This browser cannot decode DTS/AC3 audio. Please download the song or use an Android Media3 client.";
          const activeSong = currentRef.current;
          if (activeSong) {
            const titleLower = activeSong.title?.toLowerCase() || "";
            if (titleLower.includes("dts")) {
              errorMsg = "This browser cannot decode DTS audio. Please download the song or use an Android Media3 client.";
            } else if (titleLower.includes("ac3") || titleLower.includes("dolby") || titleLower.includes("eac3")) {
              errorMsg = "This browser cannot decode AC3/EAC3 audio. Please download the song or use a compatible browser/player.";
            }
          }
        }
        const activeSong = currentRef.current;
        if (activeSong?.fallbackUrl?.trim()) {
          console.warn("Surround source unsupported, fallback was used. Suppressing alert.");
          return;
        }
        window.alert(errorMsg);
      },
    });

    return () => {
      disposeHandlers();
      service.dispose();
      playbackServiceRef.current = null;
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    const service = playbackServiceRef.current;
    if (!service || !current) return;

    const cachedSource = cachedUrlsRef.current[current.id];
    const resolvedSource = service.resolveSource(current);
    const source = cachedSource || resolvedSource;

    if (!source) {
      service.setSource(null);
      setCurrentTime(0);
      setDuration(parseDurationLabel(current.duration));
      return;
    }

    const currentSrc = service.getCurrentSource();
    if (source !== currentSrc) {
      service.setSource(source);
      setCurrentTime(0);
      setDuration(parseDurationLabel(current.duration));
    }
  }, [current]);

  const togglePlay = useCallback(() => {
    const service = playbackServiceRef.current;
    const song = currentRef.current;
    if (!service || !song) return;

    const audio = typeof window !== "undefined" ? (window as any).__audio : null;
    (window as any).__traceStep = ((window as any).__traceStep || 0) + 1;
    const step = (window as any).__traceStep;
    console.log(`[TRACE] Step: ${step}, TS: ${performance.now().toFixed(2)}ms, Func: PlayerContext.togglePlay(), src: "${audio?.src || ''}", paused: ${audio?.paused ?? true}, isPlaying: ${isPlaying}, currentSongId: "${current?.id || ''}"`);

    if (service.isPaused()) {
      setIsPlaying(true);
      void service.play().catch((err) => {
        console.error("Synchronous click toggle play failed:", err);
        setIsPlaying(false);
      });
    } else {
      setIsPlaying(false);
      service.pause();
    }
  }, []);

  const next = useCallback(() => {
    const active = currentRef.current;
    const activeQueue = queueRef.current;
    if (!active || activeQueue.length === 0) return;
    const index = activeQueue.findIndex((song) => song.id === active.id);
    const nextSong = activeQueue[(index + 1) % activeQueue.length];

    const audio = typeof window !== "undefined" ? (window as any).__audio : null;
    (window as any).__traceStep = ((window as any).__traceStep || 0) + 1;
    const step = (window as any).__traceStep;
    console.log(`[TRACE] Step: ${step}, TS: ${performance.now().toFixed(2)}ms, Func: PlayerContext.next(), src: "${audio?.src || ''}", paused: ${audio?.paused ?? true}, isPlaying: ${isPlaying}, currentSongId: "${current?.id || ''}"`);

    if (nextSong) {
      play(nextSong);
    }
  }, [play]);

  const prev = useCallback(() => {
    const active = currentRef.current;
    const activeQueue = queueRef.current;
    if (!active || activeQueue.length === 0) return;
    const index = activeQueue.findIndex((song) => song.id === active.id);
    const prevSong = activeQueue[(index - 1 + activeQueue.length) % activeQueue.length];

    const audio = typeof window !== "undefined" ? (window as any).__audio : null;
    (window as any).__traceStep = ((window as any).__traceStep || 0) + 1;
    const step = (window as any).__traceStep;
    console.log(`[TRACE] Step: ${step}, TS: ${performance.now().toFixed(2)}ms, Func: PlayerContext.prev(), src: "${audio?.src || ''}", paused: ${audio?.paused ?? true}, isPlaying: ${isPlaying}, currentSongId: "${current?.id || ''}"`);

    if (prevSong) {
      play(prevSong);
    }
  }, [play]);

  const seek = useCallback((time: number) => {
    const service = playbackServiceRef.current;
    if (!service || !Number.isFinite(time)) return;
    const clamped = Math.max(0, Math.min(time, duration || service.getDuration() || time));
    service.seek(clamped);
    setCurrentTime(clamped);
  }, [duration]);

  if (typeof window !== "undefined") {
    (window as any).__isPlaying = isPlaying;
    (window as any).__currentSongId = current?.id || "";
  }

  return (
    <PlayerCtx.Provider
      value={{
        current,
        isPlaying,
        queue,
        isExpanded,
        currentTime,
        duration,
        play,
        togglePlay,
        next,
        prev,
        setExpanded,
        seek,
      }}
    >
      {children}
    </PlayerCtx.Provider>
  );
}



export const usePlayer = () => {
  const ctx = useContext(PlayerCtx);
  if (!ctx) throw new Error("usePlayer must be used inside PlayerProvider");
  return ctx;
};
