import { Play, Pause, SkipForward } from "lucide-react";
import { usePlayer } from "@/contexts/player-context";

export function MiniPlayer() {
  const { current, isPlaying, togglePlay, next, setExpanded } = usePlayer();
  if (!current) return null;

  return (
    <div className="fixed bottom-[68px] left-1/2 z-30 w-[calc(100%-1rem)] max-w-[448px] -translate-x-1/2 animate-fade-up">
      <div
        onClick={() => setExpanded(true)}
        onKeyDown={(e) => {
          if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            setExpanded(true);
          }
        }}
        role="button"
        tabIndex={0}
        className="glass group flex w-full cursor-pointer items-center gap-3 rounded-2xl p-2 pr-3 text-left shadow-elevated transition-all hover:shadow-glow"
      >
        <img src={current.cover} alt="" className="h-12 w-12 rounded-xl object-cover" />
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-semibold text-foreground">{current.title}</p>
          <p className="truncate text-xs text-muted-foreground">{current.artist}</p>
        </div>
        <div className="flex items-center gap-1">
          <div className="mr-2 flex items-end gap-[3px]">
            {[0, 1, 2, 3].map((i) => (
              <span
                key={i}
                className="block h-3 w-[3px] rounded-full bg-primary animate-equalizer"
                style={{ animationDelay: `${i * 120}ms`, animationPlayState: isPlaying ? "running" : "paused" }}
              />
            ))}
          </div>
          <button
            onClick={(e) => { e.stopPropagation(); togglePlay(); }}
            className="grid h-10 w-10 place-items-center rounded-full bg-gradient-primary text-primary-foreground shadow-glow"
          >
            {isPlaying ? <Pause className="h-4 w-4 fill-current" /> : <Play className="h-4 w-4 fill-current" />}
          </button>
          <button
            onClick={(e) => { e.stopPropagation(); next(); }}
            className="grid h-10 w-10 place-items-center rounded-full text-foreground/80 hover:text-foreground"
          >
            <SkipForward className="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>
  );
}