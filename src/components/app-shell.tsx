import type { ReactNode } from "react";
import { BottomNav } from "./bottom-nav";
import { MiniPlayer } from "./mini-player";
import { FullPlayer } from "./full-player";

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="relative min-h-screen pb-36">
      {children}
      <MiniPlayer />
      <BottomNav />
      <FullPlayer />
    </div>
  );
}