import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { AppShell } from "@/components/app-shell";
import { Heart, Download, Clock } from "lucide-react";
import {
  getDownloadedSongs,
  getLikedSongs,
  getRecentlyPlayed,
  subscribeLibraryChange,
} from "@/lib/user-library";

export const Route = createFileRoute("/library")({
  head: () => ({ meta: [{ title: "Your Library — PERAMBALUR-AUDIOS" }] }),
  component: Library,
});

function Library() {
  const [likedCount, setLikedCount] = useState(0);
  const [downloadCount, setDownloadCount] = useState(0);
  const [recentCount, setRecentCount] = useState(0);

  useEffect(() => {
    const refresh = () => {
      setLikedCount(getLikedSongs().length);
      setDownloadCount(getDownloadedSongs().length);
      setRecentCount(getRecentlyPlayed().length);
    };
    refresh();
    return subscribeLibraryChange(refresh);
  }, []);

  const shortcuts = [
    {
      icon: Heart,
      label: "Liked Songs",
      count: `${likedCount} song${likedCount === 1 ? "" : "s"}`,
      color: "from-pink-500 to-rose-700",
      to: "/library/liked" as const,
    },
    {
      icon: Download,
      label: "Downloads",
      count: `${downloadCount} song${downloadCount === 1 ? "" : "s"}`,
      color: "from-emerald-500 to-teal-700",
      to: "/downloads" as const,
    },
    {
      icon: Clock,
      label: "Recently Played",
      count: recentCount > 0 ? `${recentCount} songs` : "None yet",
      color: "from-amber-500 to-orange-700",
      to: "/library/recent" as const,
    },
  ];

  return (
    <AppShell>
      <header className="flex items-center justify-between px-5 pt-10">
        <h1 className="text-2xl font-bold">Your Library</h1>
      </header>

      <section className="mt-6 grid grid-cols-2 gap-3 px-5">
        {shortcuts.map(({ icon: Icon, label, count, color, to }) => (
          <Link
            key={label}
            to={to}
            className="glass overflow-hidden rounded-2xl p-3 text-left transition-transform hover:scale-[1.02]"
          >
            <div className={`grid h-10 w-10 place-items-center rounded-xl bg-gradient-to-br ${color}`}>
              <Icon className="h-5 w-5 text-white" />
            </div>
            <p className="mt-3 text-sm font-semibold">{label}</p>
            <p className="text-xs text-muted-foreground">{count}</p>
          </Link>
        ))}
      </section>
    </AppShell>
  );
}
