import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useCallback, useEffect, useState } from "react";
import { Download, Play, Trash2, ArrowLeft, Music2 } from "lucide-react";
import type { Song } from "@/lib/mock-data";
import { usePlayer } from "@/contexts/player-context";
import {
  clearAllDownloads,
  getDownloadedSongs,
  removeDownload,
  subscribeLibraryChange,
} from "@/lib/user-library";

export const Route = createFileRoute("/downloads")({
  head: () => ({ meta: [{ title: "Downloads — PERAMBALUR-AUDIOS" }] }),
  component: Downloads,
});

function Downloads() {
  const { play } = usePlayer();
  const navigate = useNavigate();
  const [downloadedSongs, setDownloadedSongs] = useState<Song[]>([]);

  const loadDownloads = useCallback(() => {
    setDownloadedSongs(getDownloadedSongs());
  }, []);

  useEffect(() => {
    loadDownloads();
    return subscribeLibraryChange(loadDownloads);
  }, [loadDownloads]);

  const handleDelete = async (song: Song) => {
    if (!window.confirm(`Remove "${song.title}" from downloads?`)) return;
    await removeDownload(song.id);
    loadDownloads();
  };

  const handleClearAll = async () => {
    if (!window.confirm("Clear all downloaded songs?")) return;
    await clearAllDownloads();
    loadDownloads();
  };

  return (
    <main className="relative min-h-screen bg-background pb-10">
      <div
        className="pointer-events-none absolute inset-x-0 top-0 h-64"
        style={{ background: "var(--gradient-glow)", opacity: 0.45 }}
      />

      <header className="relative z-10 flex items-center justify-between px-5 pt-10">
        <button
          onClick={() => navigate({ to: "/home" })}
          className="flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors"
        >
          <ArrowLeft className="h-4 w-4" /> Home
        </button>
        <div className="flex items-center gap-3">
          {downloadedSongs.length > 0 && (
            <button
              onClick={handleClearAll}
              className="text-xs text-destructive hover:underline"
            >
              Clear all
            </button>
          )}
          <span className="text-xs text-muted-foreground">{downloadedSongs.length} songs</span>
        </div>
      </header>

      <div className="relative z-10 flex flex-col items-center px-5 pt-6">
        <div className="grid h-14 w-14 place-items-center rounded-2xl bg-gradient-to-br from-emerald-500 to-teal-700 shadow-elevated">
          <Download className="h-7 w-7 text-white" />
        </div>
        <h1 className="mt-4 text-xl font-bold">Downloads</h1>
        <p className="text-xs text-muted-foreground">Songs saved for offline listening</p>
      </div>

      {downloadedSongs.length === 0 ? (
        <div className="relative z-10 flex flex-col items-center justify-center gap-3 px-5 pt-24 text-center">
          <Music2 className="h-12 w-12 text-muted-foreground/40" />
          <p className="text-sm font-semibold text-muted-foreground">No downloads yet</p>
          <p className="text-xs text-muted-foreground">Songs you download will appear here</p>
          <button
            onClick={() => navigate({ to: "/home" })}
            className="mt-4 rounded-full bg-gradient-primary px-6 py-2.5 text-sm font-semibold text-primary-foreground shadow-glow"
          >
            Browse Categories
          </button>
        </div>
      ) : (
        <section className="relative z-10 mt-6 flex flex-col gap-2 px-5">
          {downloadedSongs.map((song) => (
            <div
              key={song.id}
              className="glass flex items-center gap-3 rounded-2xl p-3 transition-colors hover:bg-secondary/60"
            >
              <div className="relative shrink-0">
                <img
                  src={song.cover}
                  alt={song.title}
                  className="h-14 w-14 rounded-xl object-cover"
                />
                <div className="absolute -bottom-1 -right-1 grid h-5 w-5 place-items-center rounded-full bg-emerald-500">
                  <Download className="h-2.5 w-2.5 text-white" />
                </div>
              </div>

              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-semibold">{song.title}</p>
                <p className="truncate text-xs text-muted-foreground">{song.artist}</p>
                <p className="mt-0.5 text-[10px] text-muted-foreground/60">{song.duration} · Offline</p>
              </div>

              <div className="flex items-center gap-1.5">
                <button
                  onClick={() => play(song, downloadedSongs)}
                  className="grid h-9 w-9 place-items-center rounded-full bg-gradient-primary text-primary-foreground shadow-glow transition-transform hover:scale-110"
                >
                  <Play className="h-3.5 w-3.5 fill-current" />
                </button>
                <button
                  onClick={() => handleDelete(song)}
                  className="grid h-8 w-8 place-items-center rounded-lg text-muted-foreground hover:bg-secondary/80 hover:text-destructive transition-colors"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                </button>
              </div>
            </div>
          ))}
        </section>
      )}
    </main>
  );
}
