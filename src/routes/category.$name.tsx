import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { ArrowLeft, Download, Play } from "lucide-react";
import { AppShell } from "@/components/app-shell";
import type { Song } from "@/lib/mock-data";
import { fetchCategories, fetchSongs, type Category } from "@/lib/api";
import { usePlayer } from "@/contexts/player-context";
import { downloadSong, isDownloaded } from "@/lib/user-library";

export const Route = createFileRoute("/category/$name")({
  head: () => ({ meta: [{ title: "Category — PERAMBALUR-AUDIOS" }] }),
  component: CategoryPage,
});

function CategoryPage() {
  const { name } = Route.useParams();
  const { play } = usePlayer();
  const [songs, setSongs] = useState<Song[]>([]);
  const [category, setCategory] = useState<Category | null>(null);
  const [downloadingId, setDownloadingId] = useState<string | null>(null);
  const [downloadedIds, setDownloadedIds] = useState<Set<string>>(new Set());

  useEffect(() => {
    fetchCategories()
      .then((cats) => setCategory(cats.find((c) => c.name === name) ?? null))
      .catch(() => setCategory(null));

    fetchSongs({ category: name })
      .then((data) => {
        setSongs(data);
        setDownloadedIds(new Set(data.filter((s) => isDownloaded(s.id)).map((s) => s.id)));
      })
      .catch(() => setSongs([]));
  }, [name]);

  const handleDownload = async (song: Song) => {
    if (downloadedIds.has(song.id)) return;
    setDownloadingId(song.id);
    try {
      await downloadSong(song);
      setDownloadedIds((prev) => new Set(prev).add(song.id));
    } catch (err) {
      window.alert(err instanceof Error ? err.message : "Download failed");
    } finally {
      setDownloadingId(null);
    }
  };

  return (
    <AppShell>
      <header className="relative px-5 pt-10">
        <Link
          to="/home"
          className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors"
        >
          <ArrowLeft className="h-4 w-4" /> Home
        </Link>
        <div className="mt-4">
          <h1 className="text-2xl font-bold">{name}</h1>
          <p className="text-xs text-muted-foreground">
            {category ? `${category.count} songs` : `${songs.length} songs`}
          </p>
        </div>
      </header>

      <section className="mt-6 flex flex-col gap-2 px-5 pb-4">
        {songs.length === 0 ? (
          <p className="py-12 text-center text-sm text-muted-foreground">No songs in this category yet</p>
        ) : (
          songs.map((song) => (
            <div
              key={song.id}
              className="glass flex items-center gap-3 rounded-2xl p-3 transition-colors hover:bg-secondary/60"
            >
              <img src={song.cover} alt={song.title} className="h-14 w-14 rounded-xl object-cover" />
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-semibold">{song.title}</p>
                <p className="truncate text-xs text-muted-foreground">{song.artist}</p>
                <p className="mt-0.5 text-[10px] text-muted-foreground/60">{song.duration}</p>
              </div>
              <div className="flex items-center gap-1.5">
                <button
                  onClick={() => play(song, songs)}
                  className="grid h-9 w-9 place-items-center rounded-full bg-gradient-primary text-primary-foreground shadow-glow"
                >
                  <Play className="h-3.5 w-3.5 fill-current" />
                </button>
                <button
                  onClick={() => handleDownload(song)}
                  disabled={downloadingId === song.id || downloadedIds.has(song.id)}
                  className="grid h-8 w-8 place-items-center rounded-lg text-muted-foreground hover:bg-secondary/80 hover:text-emerald-400 transition-colors disabled:opacity-40"
                  title={downloadedIds.has(song.id) ? "Downloaded" : "Download"}
                >
                  <Download className="h-3.5 w-3.5" />
                </button>
              </div>
            </div>
          ))
        )}
      </section>
    </AppShell>
  );
}
