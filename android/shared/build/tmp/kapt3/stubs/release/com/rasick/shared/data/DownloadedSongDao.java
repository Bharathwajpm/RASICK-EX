package com.rasick.shared.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0014\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\r0\fH\'J\u0018\u0010\u000e\u001a\u0004\u0018\u00010\u00052\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0010"}, d2 = {"Lcom/rasick/shared/data/DownloadedSongDao;", "", "deleteSong", "", "song", "Lcom/rasick/shared/data/DownloadedSong;", "(Lcom/rasick/shared/data/DownloadedSong;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteSongById", "songId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllDownloadedSongs", "Lkotlinx/coroutines/flow/Flow;", "", "getSongById", "insertSong", "shared_release"})
@androidx.room.Dao()
public abstract interface DownloadedSongDao {
    
    @androidx.room.Query(value = "SELECT * FROM downloaded_songs ORDER BY downloadDate DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.rasick.shared.data.DownloadedSong>> getAllDownloadedSongs();
    
    @androidx.room.Query(value = "SELECT * FROM downloaded_songs WHERE id = :songId LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getSongById(@org.jetbrains.annotations.NotNull()
    java.lang.String songId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.rasick.shared.data.DownloadedSong> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertSong(@org.jetbrains.annotations.NotNull()
    com.rasick.shared.data.DownloadedSong song, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteSong(@org.jetbrains.annotations.NotNull()
    com.rasick.shared.data.DownloadedSong song, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM downloaded_songs WHERE id = :songId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteSongById(@org.jetbrains.annotations.NotNull()
    java.lang.String songId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}