import { createFileRoute, Link } from "@tanstack/react-router";
import { useCallback, useEffect, useState } from "react";
import { Search as SearchIcon, Mic } from "lucide-react";
import { AppShell } from "@/components/app-shell";
import { SongCard } from "@/components/song-card";
import type { Song } from "@/lib/mock-data";
import { fetchCategories, fetchSongs, type Category } from "@/lib/api";
import { useVoiceSearch } from "@/hooks/use-voice-search";

export const Route = createFileRoute("/search")({
  head: () => ({ meta: [{ title: "Search — PERAMBALUR-AUDIOS" }] }),
  component: SearchPage,
});

function SearchPage() {
  const [query, setQuery] = useState("");
  const [songs, setSongs] = useState<Song[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [allCategories, setAllCategories] = useState<Category[]>([]);

  const handleVoiceResult = useCallback((text: string) => {
    setQuery(text);
  }, []);

  const { isSupported, status, message, startListening, clearMessage } = useVoiceSearch(handleVoiceResult);

  useEffect(() => {
    fetchCategories().then(setAllCategories).catch(() => setAllCategories([]));
  }, []);

  useEffect(() => {
    const trimmed = query.trim();
    if (!trimmed) {
      setSongs([]);
      setCategories([]);
      return;
    }

    const timer = setTimeout(() => {
      fetchSongs({ q: trimmed })
        .then(setSongs)
        .catch(() => setSongs([]));

      const lower = trimmed.toLowerCase();
      setCategories(
        allCategories.filter((cat) => cat.name.toLowerCase().includes(lower))
      );
    }, 300);

    return () => clearTimeout(timer);
  }, [query, allCategories]);

  const handleMicClick = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    clearMessage();
    if (!isSupported) {
      window.alert("Voice search is not supported in this browser. Please use Chrome or Edge.");
      return;
    }
    startListening();
  };

  const isListening = status === "listening" || status === "processing";

  return (
    <AppShell>
      <header className="px-5 pt-10">
        <h1 className="text-2xl font-bold">Search</h1>
        <div className="glass mt-4 flex items-center gap-3 rounded-2xl px-4 py-3">
          <SearchIcon className="h-4 w-4 text-muted-foreground" />
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search songs or categories…"
            className="w-full bg-transparent text-sm placeholder:text-muted-foreground focus:outline-none"
          />
          <button
            type="button"
            onClick={handleMicClick}
            className={`text-primary transition-opacity ${isListening ? "animate-pulse opacity-80" : ""}`}
            aria-label="Voice search"
          >
            <Mic className="h-4 w-4" />
          </button>
        </div>

        {message && (
          <p className="mt-2 text-xs text-muted-foreground">{message}</p>
        )}
      </header>

      {query.trim() && (
        <>
          {categories.length > 0 && (
            <section className="mt-8 px-5">
              <h2 className="mb-3 text-sm font-semibold">Categories</h2>
              <div className="grid grid-cols-2 gap-3">
                {categories.map((cat) => (
                  <Link
                    key={cat.id}
                    to="/category/$name"
                    params={{ name: cat.name }}
                    className={`relative h-24 overflow-hidden rounded-2xl bg-gradient-to-br ${cat.color} p-3 shadow-elevated`}
                  >
                    <p className="text-sm font-bold text-white">{cat.name}</p>
                    <p className="text-[11px] text-white/80">{cat.count} songs</p>
                    <div className="absolute -bottom-4 -right-4 h-20 w-20 rounded-full bg-white/15 blur-2xl" />
                  </Link>
                ))}
              </div>
            </section>
          )}

          <section className="mt-8">
            <h2 className="mb-3 px-5 text-sm font-semibold">Songs</h2>
            {songs.length === 0 ? (
              <p className="px-5 text-sm text-muted-foreground">No songs found</p>
            ) : (
              <div className="flex gap-3 overflow-x-auto px-5 scrollbar-hide">
                {songs.map((s) => <SongCard key={s.id} song={s} queue={songs} />)}
              </div>
            )}
          </section>
        </>
      )}
    </AppShell>
  );
}
