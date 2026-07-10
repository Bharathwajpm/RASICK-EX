import { Play } from "lucide-react";
import { usePlayer } from "@/contexts/player-context";
import type { Song } from "@/lib/mock-data";

export function SongCard({ song, queue }: { song: Song; queue: Song[] }) {
  const { play } = usePlayer();
  return (
    <button
      onClick={() => play(song, queue)}
      className="group w-36 shrink-0 text-left"
    >
      <div className="relative overflow-hidden rounded-2xl shadow-elevated">
        <img src={song.cover} alt={song.title} className="aspect-square w-full object-cover transition-transform duration-500 group-hover:scale-110" />
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-transparent to-transparent opacity-0 transition-opacity group-hover:opacity-100" />
        <div className="absolute bottom-2 right-2 grid h-10 w-10 translate-y-3 place-items-center rounded-full bg-gradient-primary text-primary-foreground opacity-0 shadow-glow transition-all group-hover:translate-y-0 group-hover:opacity-100">
          <Play className="h-4 w-4 fill-current" />
        </div>
      </div>
      <p className="mt-2 truncate text-sm font-semibold">{song.title}</p>
      <p className="truncate text-xs text-muted-foreground">{song.artist}</p>
    </button>
  );
}