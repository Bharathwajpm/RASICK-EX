import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { ArrowLeft, Play } from "lucide-react";
import { AppShell } from "@/components/app-shell";
import type { Song } from "@/lib/mock-data";
import { usePlayer } from "@/contexts/player-context";
import { getLikedSongs, subscribeLibraryChange } from "@/lib/user-library";

export const Route = createFileRoute("/library/liked")({
  head: () => ({ meta: [{ title: "Liked Songs — PERAMBALUR-AUDIOS" }] }),
  component: LikedSongs,
});

function LikedSongs() {
  const { play } = usePlayer();
  const [songs, setSongs] = useState<Song[]>([]);

  useEffect(() => {
    const load = () => setSongs(getLikedSongs());
    load();
    return subscribeLibraryChange(load);
  }, []);

  return (
    <AppShell>
      <header className="px-5 pt-10">
        <Link
          to="/library"
          className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" /> Library
        </Link>
        <h1 className="mt-4 text-2xl font-bold">Liked Songs</h1>
        <p className="text-xs text-muted-foreground">{songs.length} songs</p>
      </header>

      <section className="mt-6 flex flex-col gap-2 px-5">
        {songs.length === 0 ? (
          <p className="py-12 text-center text-sm text-muted-foreground">No liked songs yet</p>
        ) : (
          songs.map((song) => (
            <button
              key={song.id}
              onClick={() => play(song, songs)}
              className="glass flex w-full items-center gap-3 rounded-2xl p-3 text-left hover:bg-secondary/60"
            >
              <img src={song.cover} alt="" className="h-14 w-14 rounded-xl object-cover" />
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-semibold">{song.title}</p>
                <p className="truncate text-xs text-muted-foreground">{song.artist}</p>
              </div>
              <Play className="h-4 w-4 text-primary" />
            </button>
          ))
        )}
      </section>
    </AppShell>
  );
}
