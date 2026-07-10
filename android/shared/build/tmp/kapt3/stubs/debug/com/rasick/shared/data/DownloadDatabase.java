package com.rasick.shared.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u00072\u00020\u0001:\u0001\u0007B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&\u00a8\u0006\b"}, d2 = {"Lcom/rasick/shared/data/DownloadDatabase;", "Landroidx/room/RoomDatabase;", "()V", "downloadedSongDao", "Lcom/rasick/shared/data/DownloadedSongDao;", "libraryDao", "Lcom/rasick/shared/data/LibraryDao;", "Companion", "shared_debug"})
@androidx.room.Database(entities = {com.rasick.shared.data.DownloadedSong.class, com.rasick.shared.data.FavoriteSong.class, com.rasick.shared.data.Playlist.class, com.rasick.shared.data.PlaylistSong.class, com.rasick.shared.data.RecentSong.class, com.rasick.shared.data.PlaybackHistory.class, com.rasick.shared.data.SearchHistory.class, com.rasick.shared.data.QueueItem.class}, version = 3, exportSchema = false)
public abstract class DownloadDatabase extends androidx.room.RoomDatabase {
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.rasick.shared.data.DownloadDatabase INSTANCE;
    @org.jetbrains.annotations.NotNull()
    public static final com.rasick.shared.data.DownloadDatabase.Companion Companion = null;
    
    public DownloadDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.rasick.shared.data.DownloadedSongDao downloadedSongDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.rasick.shared.data.LibraryDao libraryDao();
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0007R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/rasick/shared/data/DownloadDatabase$Companion;", "", "()V", "INSTANCE", "Lcom/rasick/shared/data/DownloadDatabase;", "getDatabase", "context", "Landroid/content/Context;", "shared_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.rasick.shared.data.DownloadDatabase getDatabase(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}