import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useCallback, useEffect, useState } from "react";
import { Upload, Trash2, Edit, FolderPlus, ArrowLeft, LogOut, LayoutDashboard } from "lucide-react";
import { useAuth } from "@/contexts/auth-context";
import { LOGO_URL } from "@/lib/mock-data";
import { deleteSong, fetchCategories, fetchSongs, updateSong, fetchAdminDashboard, type AdminDashboardData } from "@/lib/api";
import type { Song } from "@/lib/mock-data";

export const Route = createFileRoute("/admin/dashboard")({
  head: () => ({ meta: [{ title: "Admin Dashboard — RASICK-EX" }] }),
  component: AdminDashboard,
});

function AdminDashboard() {
  const { isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [songs, setSongs] = useState<Song[]>([]);
  const [categoryCount, setCategoryCount] = useState(0);
  const [dashboardData, setDashboardData] = useState<AdminDashboardData | null>(null);

  const loadDashboard = useCallback(async () => {
    try {
      const data = await fetchAdminDashboard();
      setDashboardData(data);
      setSongs(data.songs);
    } catch {
      // Fallback to fetchSongs if stats API fails
      try {
        const data = await fetchSongs();
        setSongs(data);
      } catch {
        setSongs([]);
      }
    }
  }, []);

  useEffect(() => {
    if (!isAdmin) navigate({ to: "/login-selection" });
  }, [isAdmin, navigate]);

  useEffect(() => {
    if (isAdmin) {
      loadDashboard();
      fetchCategories()
        .then((cats) => setCategoryCount(cats.length))
        .catch(() => setCategoryCount(0));

      const interval = setInterval(loadDashboard, 30000); // Auto refresh every 30 seconds
      return () => clearInterval(interval);
    }
  }, [isAdmin, loadDashboard]);

  const handleLogout = () => {
    logout();
    navigate({ to: "/login-selection" });
  };

  const handleEditSong = async (song: Song) => {
    const title = window.prompt("Edit song title:", song.title);
    if (title === null || !title.trim()) return;
    try {
      await updateSong(song.id, { title: title.trim() });
      await loadDashboard();
    } catch (err) {
      window.alert(err instanceof Error ? err.message : "Update failed");
    }
  };

  const handleDeleteSong = async (song: Song) => {
    if (!window.confirm(`Delete "${song.title}"?`)) return;
    try {
      await deleteSong(song.id);
      await loadDashboard();
    } catch (err) {
      window.alert(err instanceof Error ? err.message : "Delete failed");
    }
  };

  const handleQuickEdit = () => {
    if (songs.length === 0) return;
    handleEditSong(songs[0]);
  };

  const handleQuickDelete = () => {
    if (songs.length === 0) return;
    handleDeleteSong(songs[0]);
  };

  if (!isAdmin) return null;

  return (
    <main className="relative min-h-screen bg-background pb-10">
      <div
        className="pointer-events-none absolute inset-x-0 top-0 h-64"
        style={{ background: "var(--gradient-glow)", opacity: 0.5 }}
      />

      {/* Header */}
      <header className="relative z-10 flex items-center justify-between px-5 pt-10">
        <button
          onClick={() => navigate({ to: "/home" })}
          className="flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors"
        >
          <ArrowLeft className="h-4 w-4" /> Home
        </button>
        <button
          onClick={handleLogout}
          className="flex items-center gap-1.5 rounded-full border border-destructive/40 px-3 py-1.5 text-xs font-medium text-destructive hover:bg-destructive/10 transition-colors"
        >
          <LogOut className="h-3.5 w-3.5" /> Logout
        </button>
      </header>

      {/* Title */}
      <div className="relative z-10 flex flex-col items-center px-5 pt-6">
        <div className="flex items-center gap-2 rounded-full border border-primary/30 bg-primary/10 px-4 py-1.5">
          <LayoutDashboard className="h-3.5 w-3.5 text-primary" />
          <span className="text-xs font-medium text-primary">Admin Dashboard</span>
        </div>
        <img src={LOGO_URL} alt="" className="mt-4 h-14 w-14 rounded-2xl object-contain" />
        <h1 className="mt-3 text-xl font-bold">RASICK-EX</h1>
        <p className="text-xs text-muted-foreground">Content Management Panel</p>
      </div>

      {/* Storage Usage */}
      {dashboardData?.stats.storage && (
        <section className="relative z-10 mt-6 px-5">
          <div className="glass rounded-2xl p-4">
            <h2 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-3">Storage Usage</h2>
            
            {dashboardData.stats.storage.warning && (
              <div className="mb-3 rounded-xl bg-destructive/10 border border-destructive/20 p-2.5 text-[10px] text-destructive font-medium text-center">
                ⚠ Storage Almost Full!
              </div>
            )}
            
            <div className="flex items-center justify-between text-xs font-semibold">
              <span>Used Space</span>
              <span>{dashboardData.stats.storage.percentage}%</span>
            </div>
            
            {/* Progress bar */}
            <div className="mt-2 h-2.5 w-full rounded-full bg-secondary overflow-hidden">
              <div 
                className={`h-full rounded-full transition-all duration-500 ${
                  dashboardData.stats.storage.percentage > 90 
                    ? "bg-destructive" 
                    : dashboardData.stats.storage.percentage > 70 
                    ? "bg-amber-400" 
                    : "bg-emerald-400"
                }`}
                style={{ width: `${Math.min(100, dashboardData.stats.storage.percentage)}%` }}
              />
            </div>
            
            <div className="mt-3 flex items-center justify-between text-[10px] text-muted-foreground">
              <span>{dashboardData.stats.storage.used.toFixed(2)} GB / {dashboardData.stats.storage.capacity.toFixed(2)} GB</span>
              <span>Remaining: {dashboardData.stats.storage.remaining.toFixed(2)} GB</span>
            </div>
          </div>
        </section>
      )}

      {/* Quick Actions */}
      <section className="relative z-10 mt-6 px-5">
        <h2 className="mb-3 text-sm font-semibold text-muted-foreground uppercase tracking-wider">Quick Actions</h2>
        <div className="grid grid-cols-2 gap-3">
          <button
            onClick={() => navigate({ to: "/admin/upload" })}
            className="glass group flex flex-col items-center gap-2 rounded-2xl p-5 transition-all hover:bg-secondary/60 hover:shadow-glow"
          >
            <div className="grid h-12 w-12 place-items-center rounded-xl bg-gradient-primary shadow-glow transition-transform group-hover:scale-110">
              <Upload className="h-6 w-6 text-primary-foreground" />
            </div>
            <span className="text-xs font-semibold">Upload Song</span>
          </button>

          <button
            onClick={() => navigate({ to: "/admin/categories" })}
            className="glass group flex flex-col items-center gap-2 rounded-2xl p-5 transition-all hover:bg-secondary/60"
          >
            <div className="grid h-12 w-12 place-items-center rounded-xl bg-secondary transition-transform group-hover:scale-110">
              <FolderPlus className="h-6 w-6 text-primary" />
            </div>
            <span className="text-xs font-semibold">Categories</span>
          </button>

          <button
            onClick={handleQuickEdit}
            className="glass group flex flex-col items-center gap-2 rounded-2xl p-5 transition-all hover:bg-secondary/60"
          >
            <div className="grid h-12 w-12 place-items-center rounded-xl bg-secondary transition-transform group-hover:scale-110">
              <Edit className="h-6 w-6 text-amber-400" />
            </div>
            <span className="text-xs font-semibold">Edit Song</span>
          </button>

          <button
            onClick={handleQuickDelete}
            className="glass group flex flex-col items-center gap-2 rounded-2xl p-5 transition-all hover:bg-secondary/60"
          >
            <div className="grid h-12 w-12 place-items-center rounded-xl bg-secondary transition-transform group-hover:scale-110">
              <Trash2 className="h-6 w-6 text-destructive" />
            </div>
            <span className="text-xs font-semibold">Delete Song</span>
          </button>
        </div>
      </section>

      {/* Uploaded Songs */}
      <section className="relative z-10 mt-8 px-5">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-sm font-semibold">Uploaded Songs</h2>
          <span className="text-xs text-muted-foreground">{songs.length} total</span>
        </div>
        <ul className="space-y-2">
          {songs.map((s) => (
            <li key={s.id} className="glass flex items-center gap-3 rounded-2xl p-3">
              <img src={s.cover} alt="" className="h-12 w-12 rounded-xl object-cover" />
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-semibold">{s.title}</p>
                <p className="truncate text-xs text-muted-foreground">{s.artist} · {s.duration}</p>
                {s.category && (
                  <span className="mt-0.5 inline-block rounded-full bg-primary/10 px-2 py-0.5 text-[10px] text-primary">
                    {s.category}
                  </span>
                )}
              </div>
              <div className="flex items-center gap-1.5">
                <button
                  onClick={() => handleEditSong(s)}
                  className="grid h-8 w-8 place-items-center rounded-lg hover:bg-secondary/80 text-muted-foreground hover:text-amber-400 transition-colors"
                >
                  <Edit className="h-3.5 w-3.5" />
                </button>
                <button
                  onClick={() => handleDeleteSong(s)}
                  className="grid h-8 w-8 place-items-center rounded-lg hover:bg-secondary/80 text-muted-foreground hover:text-destructive transition-colors"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                </button>
              </div>
            </li>
          ))}
        </ul>
      </section>
    </main>
  );
}
