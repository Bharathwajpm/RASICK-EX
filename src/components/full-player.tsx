import { ChevronDown, Heart, MoreHorizontal, Shuffle, SkipBack, Play, Pause, SkipForward, Repeat, ListMusic, Share2, Download } from "lucide-react";
import { usePlayer, formatPlaybackTime } from "@/contexts/player-context";
import { useEffect, useRef, useState } from "react";
import {
  downloadSong,
  isDownloaded,
  isLiked,
  subscribeLibraryChange,
  toggleLike,
} from "@/lib/user-library";

export function FullPlayer() {
  const { current, isExpanded, setExpanded, isPlaying, togglePlay, next, prev, currentTime, duration, seek } = usePlayer();
  const progressRef = useRef<HTMLDivElement>(null);
  const [liked, setLiked] = useState(false);
  const [downloaded, setDownloaded] = useState(false);
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    document.body.style.overflow = isExpanded ? "hidden" : "";
    return () => { document.body.style.overflow = ""; };
  }, [isExpanded]);

  useEffect(() => {
    if (!current) return;
    const refresh = () => {
      setLiked(isLiked(current.id));
      setDownloaded(isDownloaded(current.id));
    };
    refresh();
    return subscribeLibraryChange(refresh);
  }, [current]);

  if (!current || !isExpanded) return null;

  const totalDuration = duration > 0 ? duration : 0;
  const progress = totalDuration > 0 ? (currentTime / totalDuration) * 100 : 0;

  const handleSeek = (clientX: number) => {
    if (!progressRef.current || totalDuration <= 0) return;
    const rect = progressRef.current.getBoundingClientRect();
    const ratio = Math.min(1, Math.max(0, (clientX - rect.left) / rect.width));
    seek(ratio * totalDuration);
  };

  const handleLike = () => {
    const nowLiked = toggleLike(current);
    setLiked(nowLiked);
  };

  const handleDownload = async () => {
    if (downloaded || downloading) return;
    setDownloading(true);
    try {
      await downloadSong(current);
      setDownloaded(true);
    } catch (err) {
      window.alert(err instanceof Error ? err.message : "Download failed");
    } finally {
      setDownloading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 mx-auto max-w-[460px] animate-fade-up overflow-hidden">
      <div className="absolute inset-0">
        <img src={current.cover} alt="" className="h-full w-full scale-125 object-cover opacity-50 blur-3xl" />
        <div className="absolute inset-0 bg-gradient-to-b from-background/40 via-background/70 to-background" />
      </div>

      <div className="relative flex h-full flex-col px-6 pt-6 pb-10">
        <div className="flex items-center justify-between">
          <button onClick={() => setExpanded(false)} className="grid h-10 w-10 place-items-center rounded-full glass">
            <ChevronDown className="h-5 w-5" />
          </button>
          <div className="text-center">
            <p className="text-[10px] uppercase tracking-widest text-muted-foreground">Now Playing</p>
            <p className="text-xs font-medium">RASICK-EX</p>
          </div>
          <button className="grid h-10 w-10 place-items-center rounded-full glass">
            <MoreHorizontal className="h-5 w-5" />
          </button>
        </div>

        <div className="mt-10 grid place-items-center">
          <div className="relative">
            <div className="absolute -inset-6 rounded-3xl bg-primary/20 blur-3xl" />
            <img src={current.cover} alt={current.title} className="relative h-72 w-72 rounded-3xl object-cover shadow-elevated" />
          </div>
        </div>

        <div className="mt-8">
          <div className="flex items-start justify-between gap-4">
            <div className="min-w-0">
              <h2 className="truncate text-2xl font-bold">{current.title}</h2>
              <p className="truncate text-sm text-muted-foreground">{current.artist}</p>
            </div>
            <button onClick={handleLike} className={`mt-2 ${liked ? "text-primary" : "text-muted-foreground"}`}>
              <Heart className={`h-6 w-6 ${liked ? "fill-current" : ""}`} />
            </button>
          </div>

          <div className="mt-6">
            <div
              ref={progressRef}
              role="slider"
              aria-label="Seek"
              aria-valuemin={0}
              aria-valuemax={totalDuration}
              aria-valuenow={currentTime}
              tabIndex={0}
              onClick={(e) => handleSeek(e.clientX)}
              onKeyDown={(e) => {
                if (e.key === "ArrowRight") seek(currentTime + 5);
                if (e.key === "ArrowLeft") seek(currentTime - 5);
              }}
              className="h-1.5 w-full cursor-pointer overflow-hidden rounded-full bg-muted"
            >
              <div className="h-full rounded-full bg-gradient-primary transition-[width] duration-150" style={{ width: `${progress}%` }} />
            </div>
            <div className="mt-1.5 flex justify-between text-[11px] text-muted-foreground">
              <span>{formatPlaybackTime(currentTime)}</span>
              <span>{formatPlaybackTime(totalDuration || 0) || current.duration}</span>
            </div>
          </div>

          <div className="mt-6 flex items-center justify-between">
            <button className="text-muted-foreground hover:text-foreground"><Shuffle className="h-5 w-5" /></button>
            <button onClick={prev} className="text-foreground"><SkipBack className="h-7 w-7 fill-current" /></button>
            <button
              onClick={togglePlay}
              className="grid h-16 w-16 place-items-center rounded-full bg-gradient-primary text-primary-foreground shadow-glow"
            >
              {isPlaying ? <Pause className="h-7 w-7 fill-current" /> : <Play className="h-7 w-7 fill-current" />}
            </button>
            <button onClick={next} className="text-foreground"><SkipForward className="h-7 w-7 fill-current" /></button>
            <button className="text-muted-foreground hover:text-foreground"><Repeat className="h-5 w-5" /></button>
          </div>

          <div className="mt-8 flex items-center justify-around text-muted-foreground">
            <button
              onClick={handleDownload}
              disabled={downloading || downloaded}
              className="flex flex-col items-center gap-1 hover:text-foreground disabled:opacity-50"
            >
              <Download className="h-5 w-5" />
              <span className="text-[10px]">{downloaded ? "Saved" : downloading ? "Saving…" : "Download"}</span>
            </button>
            <button className="flex flex-col items-center gap-1 hover:text-foreground"><ListMusic className="h-5 w-5" /><span className="text-[10px]">Queue</span></button>
            <button className="flex flex-col items-center gap-1 hover:text-foreground"><Share2 className="h-5 w-5" /><span className="text-[10px]">Share</span></button>
          </div>
        </div>
      </div>
    </div>
  );
}
