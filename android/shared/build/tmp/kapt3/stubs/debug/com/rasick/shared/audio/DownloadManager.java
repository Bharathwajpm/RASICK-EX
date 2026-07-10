package com.rasick.shared.audio;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0005J\u000e\u0010\u0012\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0005J\u0006\u0010\u0013\u001a\u00020\u0014J\b\u0010\u0015\u001a\u00020\nH\u0002J\b\u0010\u0016\u001a\u00020\fH\u0002J\u0006\u0010\u0017\u001a\u00020\u0014J\u000e\u0010\u0018\u001a\u00020\u00102\u0006\u0010\u0019\u001a\u00020\u001aJ\u000e\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u0011\u001a\u00020\u0005J\u000e\u0010\u001d\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0005J\u000e\u0010\u001e\u001a\u00020\u00102\u0006\u0010\u001f\u001a\u00020 R\u001a\u0010\u0003\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lcom/rasick/shared/audio/DownloadManager;", "", "()V", "activeCalls", "Ljava/util/concurrent/ConcurrentHashMap;", "", "Lokhttp3/Call;", "client", "Lokhttp3/OkHttpClient;", "database", "Lcom/rasick/shared/data/DownloadDatabase;", "downloadDir", "Ljava/io/File;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "cancelDownload", "", "songId", "deleteDownload", "getAvailableInternalMemorySize", "", "getDb", "getDir", "getUsedStorageSize", "initialize", "context", "Landroid/content/Context;", "isDownloaded", "", "pauseDownload", "startDownload", "song", "Lcom/rasick/shared/model/Song;", "shared_debug"})
public final class DownloadManager {
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.OkHttpClient client = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.concurrent.ConcurrentHashMap<java.lang.String, okhttp3.Call> activeCalls = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.Nullable()
    private static com.rasick.shared.data.DownloadDatabase database;
    @org.jetbrains.annotations.Nullable()
    private static java.io.File downloadDir;
    @org.jetbrains.annotations.NotNull()
    public static final com.rasick.shared.audio.DownloadManager INSTANCE = null;
    
    private DownloadManager() {
        super();
    }
    
    public final void initialize(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    private final com.rasick.shared.data.DownloadDatabase getDb() {
        return null;
    }
    
    private final java.io.File getDir() {
        return null;
    }
    
    public final void startDownload(@org.jetbrains.annotations.NotNull()
    com.rasick.shared.model.Song song) {
    }
    
    public final void pauseDownload(@org.jetbrains.annotations.NotNull()
    java.lang.String songId) {
    }
    
    public final void cancelDownload(@org.jetbrains.annotations.NotNull()
    java.lang.String songId) {
    }
    
    public final void deleteDownload(@org.jetbrains.annotations.NotNull()
    java.lang.String songId) {
    }
    
    public final long getAvailableInternalMemorySize() {
        return 0L;
    }
    
    public final long getUsedStorageSize() {
        return 0L;
    }
    
    public final boolean isDownloaded(@org.jetbrains.annotations.NotNull()
    java.lang.String songId) {
        return false;
    }
}