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

function StatCard({ label, value, color }: { label: string; value: string | number; color: string }) {
  return (
    <div className="glass rounded-2xl p-4">
      <p className="text-xs text-muted-foreground">{label}</p>
      <p className={`mt-1 text-2xl font-bold ${color}`}>{value}</p>
    </div>
  );
}

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

      {/* Stats */}
      <section className="relative z-10 mt-6 grid grid-cols-2 gap-3 px-5">
        <StatCard label="Total Songs" value={dashboardData?.stats.totalSongs ?? songs.length} color="text-primary" />
        <StatCard label="Categories" value={dashboardData?.stats.totalCategories ?? categoryCount} color="text-emerald-400" />
        <StatCard label="Downloads" value={dashboardData?.stats.downloads ?? "1.2k"} color="text-amber-400" />
        <StatCard label="Users" value={dashboardData?.stats.totalUsers ?? "340"} color="text-pink-400" />
      </section>

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

      {/* Operational Analytics & Extended Stats */}
      {dashboardData?.stats && (
        <section className="relative z-10 mt-6 px-5 space-y-4">
          
          {/* Operational Analytics */}
          <div className="glass rounded-2xl p-4">
            <h2 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-3">Operational Analytics</h2>
            <div className="space-y-2 text-xs">
              <div className="flex justify-between"><span className="text-muted-foreground">Uploads Today</span><span className="font-semibold">{dashboardData.stats.analytics.uploadsToday}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Downloads Today</span><span className="font-semibold">{dashboardData.stats.analytics.downloadsToday}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Total Downloads</span><span className="font-semibold">{dashboardData.stats.analytics.totalDownloads}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Most Played Song</span><span className="font-semibold truncate max-w-[150px] text-right">{dashboardData.stats.analytics.mostPlayedSong}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Largest Song</span><span className="font-semibold truncate max-w-[150px] text-right">{dashboardData.stats.analytics.largestSong}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Largest Cover Image</span><span className="font-semibold truncate max-w-[150px] text-right">{dashboardData.stats.analytics.largestCoverImage}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Average Song Size</span><span className="font-semibold">{dashboardData.stats.analytics.avgSongSize}</span></div>
            </div>
          </div>

          {/* Song Statistics */}
          <div className="glass rounded-2xl p-4">
            <h2 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-3">Song Statistics</h2>
            <div className="space-y-2 text-xs">
              <div className="flex justify-between"><span className="text-muted-foreground">Total Audio Size</span><span className="font-semibold">{dashboardData.stats.songStats.totalAudioSize}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Average Audio Size</span><span className="font-semibold">{dashboardData.stats.songStats.avgAudioSize}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Largest Audio File</span><span className="font-semibold truncate max-w-[150px] text-right">{dashboardData.stats.songStats.largestAudioFile}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Smallest Audio File</span><span className="font-semibold truncate max-w-[150px] text-right">{dashboardData.stats.songStats.smallestAudioFile}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Newest Upload</span><span className="font-semibold truncate max-w-[150px] text-right">{dashboardData.stats.songStats.newestUpload}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Oldest Upload</span><span className="font-semibold truncate max-w-[150px] text-right">{dashboardData.stats.songStats.oldestUpload}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Total Covers</span><span className="font-semibold">{dashboardData.stats.songStats.totalCovers}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Average Cover Size</span><span className="font-semibold">{dashboardData.stats.songStats.avgCoverSize}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Largest Cover</span><span className="font-semibold truncate max-w-[150px] text-right">{dashboardData.stats.songStats.largestCover}</span></div>
            </div>
          </div>

          {/* User Statistics */}
          <div className="glass rounded-2xl p-4">
            <h2 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-3">User Statistics</h2>
            <div className="space-y-2 text-xs">
              <div className="flex justify-between"><span className="text-muted-foreground">Registered Users</span><span className="font-semibold">{dashboardData.stats.userStats.registeredUsers}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Admin Users</span><span className="font-semibold">{dashboardData.stats.userStats.adminUsers}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Normal Users</span><span className="font-semibold">{dashboardData.stats.userStats.normalUsers}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">New Users Today</span><span className="font-semibold">{dashboardData.stats.userStats.newUsersToday}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Most Active User</span><span className="font-semibold">{dashboardData.stats.userStats.mostActiveUser}</span></div>
            </div>
          </div>

          {/* Playback & Download Analytics */}
          <div className="glass rounded-2xl p-4">
            <h2 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-3">Playback & Downloads</h2>
            <div className="space-y-2 text-xs">
              <div className="flex justify-between"><span className="text-muted-foreground">Most Played Song</span><span className="font-semibold truncate max-w-[150px] text-right">{dashboardData.stats.playbackStats.mostPlayedSong}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Least Played Song</span><span className="font-semibold truncate max-w-[150px] text-right">{dashboardData.stats.playbackStats.leastPlayedSong}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Recently Played Count</span><span className="font-semibold">{dashboardData.stats.playbackStats.recentlyPlayedCount}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Favorite Count</span><span className="font-semibold">{dashboardData.stats.playbackStats.favoriteCount}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Playlist Count</span><span className="font-semibold">{dashboardData.stats.playbackStats.playlistCount}</span></div>
              <div className="flex justify-between border-t border-secondary/40 pt-2 mt-2"><span className="text-muted-foreground">Downloaded Songs (Total)</span><span className="font-semibold">{dashboardData.stats.downloadStats.downloadedSongs}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Most Downloaded Song</span><span className="font-semibold truncate max-w-[150px] text-right">{dashboardData.stats.downloadStats.mostDownloadedSong}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Est. Total Offline Storage</span><span className="font-semibold">{dashboardData.stats.downloadStats.totalOfflineStorageUsed}</span></div>
            </div>
          </div>

          {/* System Health */}
          <div className="glass rounded-2xl p-4">
            <h2 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-3">System Health Status</h2>
            <div className="grid grid-cols-2 gap-x-4 gap-y-2 text-xs">
              <div className="flex justify-between"><span className="text-muted-foreground">Backend</span><span className="font-semibold">{dashboardData.stats.systemHealth.backend}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">MongoDB</span><span className="font-semibold">{dashboardData.stats.systemHealth.mongodb}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Filebase</span><span className="font-semibold">{dashboardData.stats.systemHealth.filebase}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Upload</span><span className="font-semibold">{dashboardData.stats.systemHealth.uploadService}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Streaming</span><span className="font-semibold">{dashboardData.stats.systemHealth.streamingService}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Download</span><span className="font-semibold">{dashboardData.stats.systemHealth.downloadService}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Storage</span><span className="font-semibold">{dashboardData.stats.systemHealth.storageService}</span></div>
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
