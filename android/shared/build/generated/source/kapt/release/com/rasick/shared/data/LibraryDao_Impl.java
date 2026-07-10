package com.rasick.shared.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class LibraryDao_Impl implements LibraryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FavoriteSong> __insertionAdapterOfFavoriteSong;

  private final EntityInsertionAdapter<RecentSong> __insertionAdapterOfRecentSong;

  private final EntityInsertionAdapter<PlaybackHistory> __insertionAdapterOfPlaybackHistory;

  private final EntityInsertionAdapter<SearchHistory> __insertionAdapterOfSearchHistory;

  private final EntityInsertionAdapter<Playlist> __insertionAdapterOfPlaylist;

  private final EntityInsertionAdapter<PlaylistSong> __insertionAdapterOfPlaylistSong;

  private final EntityInsertionAdapter<QueueItem> __insertionAdapterOfQueueItem;

  private final SharedSQLiteStatement __preparedStmtOfDeleteFavoriteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteRecentById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSearch;

  private final SharedSQLiteStatement __preparedStmtOfClearSearchHistory;

  private final SharedSQLiteStatement __preparedStmtOfDeletePlaylistById;

  private final SharedSQLiteStatement __preparedStmtOfRenamePlaylist;

  private final SharedSQLiteStatement __preparedStmtOfUpdatePlaylistCover;

  private final SharedSQLiteStatement __preparedStmtOfDeletePlaylistSong;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSongsByPlaylistId;

  private final SharedSQLiteStatement __preparedStmtOfClearQueue;

  public LibraryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFavoriteSong = new EntityInsertionAdapter<FavoriteSong>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `favorite_songs` (`id`,`title`,`artist`,`cover`,`audioUrl`,`category`,`addedDate`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FavoriteSong entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTitle());
        }
        if (entity.getArtist() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getArtist());
        }
        if (entity.getCover() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCover());
        }
        if (entity.getAudioUrl() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAudioUrl());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCategory());
        }
        statement.bindLong(7, entity.getAddedDate());
      }
    };
    this.__insertionAdapterOfRecentSong = new EntityInsertionAdapter<RecentSong>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `recent_songs` (`id`,`title`,`artist`,`cover`,`audioUrl`,`category`,`playedDate`,`playbackPosition`,`duration`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecentSong entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTitle());
        }
        if (entity.getArtist() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getArtist());
        }
        if (entity.getCover() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCover());
        }
        if (entity.getAudioUrl() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAudioUrl());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCategory());
        }
        statement.bindLong(7, entity.getPlayedDate());
        statement.bindLong(8, entity.getPlaybackPosition());
        statement.bindLong(9, entity.getDuration());
      }
    };
    this.__insertionAdapterOfPlaybackHistory = new EntityInsertionAdapter<PlaybackHistory>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `playback_history` (`songId`,`title`,`artist`,`cover`,`audioUrl`,`category`,`lastPlayedDate`,`playCount`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlaybackHistory entity) {
        if (entity.getSongId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getSongId());
        }
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTitle());
        }
        if (entity.getArtist() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getArtist());
        }
        if (entity.getCover() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCover());
        }
        if (entity.getAudioUrl() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAudioUrl());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCategory());
        }
        statement.bindLong(7, entity.getLastPlayedDate());
        statement.bindLong(8, entity.getPlayCount());
      }
    };
    this.__insertionAdapterOfSearchHistory = new EntityInsertionAdapter<SearchHistory>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `search_history` (`query`,`searchDate`) VALUES (?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SearchHistory entity) {
        if (entity.getQuery() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getQuery());
        }
        statement.bindLong(2, entity.getSearchDate());
      }
    };
    this.__insertionAdapterOfPlaylist = new EntityInsertionAdapter<Playlist>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `playlists` (`id`,`name`,`coverImage`,`createdDate`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Playlist entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getCoverImage() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCoverImage());
        }
        statement.bindLong(4, entity.getCreatedDate());
      }
    };
    this.__insertionAdapterOfPlaylistSong = new EntityInsertionAdapter<PlaylistSong>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `playlist_songs` (`localId`,`playlistId`,`songId`,`title`,`artist`,`cover`,`audioUrl`,`category`,`orderIndex`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlaylistSong entity) {
        statement.bindLong(1, entity.getLocalId());
        if (entity.getPlaylistId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPlaylistId());
        }
        if (entity.getSongId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSongId());
        }
        if (entity.getTitle() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getTitle());
        }
        if (entity.getArtist() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getArtist());
        }
        if (entity.getCover() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCover());
        }
        if (entity.getAudioUrl() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getAudioUrl());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getCategory());
        }
        statement.bindLong(9, entity.getOrderIndex());
      }
    };
    this.__insertionAdapterOfQueueItem = new EntityInsertionAdapter<QueueItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `queue_items` (`songId`,`title`,`artist`,`cover`,`audioUrl`,`category`,`orderIndex`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final QueueItem entity) {
        if (entity.getSongId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getSongId());
        }
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTitle());
        }
        if (entity.getArtist() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getArtist());
        }
        if (entity.getCover() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCover());
        }
        if (entity.getAudioUrl() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAudioUrl());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCategory());
        }
        statement.bindLong(7, entity.getOrderIndex());
      }
    };
    this.__preparedStmtOfDeleteFavoriteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM favorite_songs WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteRecentById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM recent_songs WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteSearch = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM search_history WHERE `query` = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearSearchHistory = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM search_history";
        return _query;
      }
    };
    this.__preparedStmtOfDeletePlaylistById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM playlists WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRenamePlaylist = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE playlists SET name = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdatePlaylistCover = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE playlists SET coverImage = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeletePlaylistSong = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM playlist_songs WHERE playlistId = ? AND songId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteSongsByPlaylistId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM playlist_songs WHERE playlistId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearQueue = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM queue_items";
        return _query;
      }
    };
  }

  @Override
  public Object insertFavorite(final FavoriteSong song,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFavoriteSong.insert(song);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertRecent(final RecentSong song, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRecentSong.insert(song);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertHistory(final PlaybackHistory history,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlaybackHistory.insert(history);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSearch(final SearchHistory search,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSearchHistory.insert(search);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertPlaylist(final Playlist playlist,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlaylist.insert(playlist);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertPlaylistSong(final PlaylistSong song,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlaylistSong.insert(song);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertQueueItems(final List<QueueItem> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfQueueItem.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteFavoriteById(final String songId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteFavoriteById.acquire();
        int _argIndex = 1;
        if (songId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, songId);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteFavoriteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRecentById(final String songId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteRecentById.acquire();
        int _argIndex = 1;
        if (songId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, songId);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteRecentById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSearch(final String searchQuery,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSearch.acquire();
        int _argIndex = 1;
        if (searchQuery == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, searchQuery);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSearch.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearSearchHistory(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearSearchHistory.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearSearchHistory.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePlaylistById(final String playlistId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePlaylistById.acquire();
        int _argIndex = 1;
        if (playlistId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, playlistId);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeletePlaylistById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object renamePlaylist(final String playlistId, final String newName,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRenamePlaylist.acquire();
        int _argIndex = 1;
        if (newName == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, newName);
        }
        _argIndex = 2;
        if (playlistId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, playlistId);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfRenamePlaylist.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePlaylistCover(final String playlistId, final String coverUrl,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdatePlaylistCover.acquire();
        int _argIndex = 1;
        if (coverUrl == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, coverUrl);
        }
        _argIndex = 2;
        if (playlistId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, playlistId);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdatePlaylistCover.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePlaylistSong(final String playlistId, final String songId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePlaylistSong.acquire();
        int _argIndex = 1;
        if (playlistId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, playlistId);
        }
        _argIndex = 2;
        if (songId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, songId);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeletePlaylistSong.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSongsByPlaylistId(final String playlistId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSongsByPlaylistId.acquire();
        int _argIndex = 1;
        if (playlistId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, playlistId);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSongsByPlaylistId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearQueue(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearQueue.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearQueue.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<FavoriteSong>> getFavoriteSongs() {
    final String _sql = "SELECT * FROM favorite_songs ORDER BY addedDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"favorite_songs"}, new Callable<List<FavoriteSong>>() {
      @Override
      @NonNull
      public List<FavoriteSong> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfCover = CursorUtil.getColumnIndexOrThrow(_cursor, "cover");
          final int _cursorIndexOfAudioUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "audioUrl");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfAddedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "addedDate");
          final List<FavoriteSong> _result = new ArrayList<FavoriteSong>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FavoriteSong _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpArtist;
            if (_cursor.isNull(_cursorIndexOfArtist)) {
              _tmpArtist = null;
            } else {
              _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            }
            final String _tmpCover;
            if (_cursor.isNull(_cursorIndexOfCover)) {
              _tmpCover = null;
            } else {
              _tmpCover = _cursor.getString(_cursorIndexOfCover);
            }
            final String _tmpAudioUrl;
            if (_cursor.isNull(_cursorIndexOfAudioUrl)) {
              _tmpAudioUrl = null;
            } else {
              _tmpAudioUrl = _cursor.getString(_cursorIndexOfAudioUrl);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final long _tmpAddedDate;
            _tmpAddedDate = _cursor.getLong(_cursorIndexOfAddedDate);
            _item = new FavoriteSong(_tmpId,_tmpTitle,_tmpArtist,_tmpCover,_tmpAudioUrl,_tmpCategory,_tmpAddedDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getFavoriteById(final String songId,
      final Continuation<? super FavoriteSong> $completion) {
    final String _sql = "SELECT * FROM favorite_songs WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (songId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, songId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<FavoriteSong>() {
      @Override
      @Nullable
      public FavoriteSong call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfCover = CursorUtil.getColumnIndexOrThrow(_cursor, "cover");
          final int _cursorIndexOfAudioUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "audioUrl");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfAddedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "addedDate");
          final FavoriteSong _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpArtist;
            if (_cursor.isNull(_cursorIndexOfArtist)) {
              _tmpArtist = null;
            } else {
              _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            }
            final String _tmpCover;
            if (_cursor.isNull(_cursorIndexOfCover)) {
              _tmpCover = null;
            } else {
              _tmpCover = _cursor.getString(_cursorIndexOfCover);
            }
            final String _tmpAudioUrl;
            if (_cursor.isNull(_cursorIndexOfAudioUrl)) {
              _tmpAudioUrl = null;
            } else {
              _tmpAudioUrl = _cursor.getString(_cursorIndexOfAudioUrl);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final long _tmpAddedDate;
            _tmpAddedDate = _cursor.getLong(_cursorIndexOfAddedDate);
            _result = new FavoriteSong(_tmpId,_tmpTitle,_tmpArtist,_tmpCover,_tmpAudioUrl,_tmpCategory,_tmpAddedDate);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<RecentSong>> getRecentSongs() {
    final String _sql = "SELECT * FROM recent_songs ORDER BY playedDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"recent_songs"}, new Callable<List<RecentSong>>() {
      @Override
      @NonNull
      public List<RecentSong> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfCover = CursorUtil.getColumnIndexOrThrow(_cursor, "cover");
          final int _cursorIndexOfAudioUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "audioUrl");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfPlayedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "playedDate");
          final int _cursorIndexOfPlaybackPosition = CursorUtil.getColumnIndexOrThrow(_cursor, "playbackPosition");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final List<RecentSong> _result = new ArrayList<RecentSong>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecentSong _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpArtist;
            if (_cursor.isNull(_cursorIndexOfArtist)) {
              _tmpArtist = null;
            } else {
              _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            }
            final String _tmpCover;
            if (_cursor.isNull(_cursorIndexOfCover)) {
              _tmpCover = null;
            } else {
              _tmpCover = _cursor.getString(_cursorIndexOfCover);
            }
            final String _tmpAudioUrl;
            if (_cursor.isNull(_cursorIndexOfAudioUrl)) {
              _tmpAudioUrl = null;
            } else {
              _tmpAudioUrl = _cursor.getString(_cursorIndexOfAudioUrl);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final long _tmpPlayedDate;
            _tmpPlayedDate = _cursor.getLong(_cursorIndexOfPlayedDate);
            final long _tmpPlaybackPosition;
            _tmpPlaybackPosition = _cursor.getLong(_cursorIndexOfPlaybackPosition);
            final long _tmpDuration;
            _tmpDuration = _cursor.getLong(_cursorIndexOfDuration);
            _item = new RecentSong(_tmpId,_tmpTitle,_tmpArtist,_tmpCover,_tmpAudioUrl,_tmpCategory,_tmpPlayedDate,_tmpPlaybackPosition,_tmpDuration);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getRecentById(final String songId,
      final Continuation<? super RecentSong> $completion) {
    final String _sql = "SELECT * FROM recent_songs WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (songId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, songId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RecentSong>() {
      @Override
      @Nullable
      public RecentSong call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfCover = CursorUtil.getColumnIndexOrThrow(_cursor, "cover");
          final int _cursorIndexOfAudioUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "audioUrl");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfPlayedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "playedDate");
          final int _cursorIndexOfPlaybackPosition = CursorUtil.getColumnIndexOrThrow(_cursor, "playbackPosition");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final RecentSong _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpArtist;
            if (_cursor.isNull(_cursorIndexOfArtist)) {
              _tmpArtist = null;
            } else {
              _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            }
            final String _tmpCover;
            if (_cursor.isNull(_cursorIndexOfCover)) {
              _tmpCover = null;
            } else {
              _tmpCover = _cursor.getString(_cursorIndexOfCover);
            }
            final String _tmpAudioUrl;
            if (_cursor.isNull(_cursorIndexOfAudioUrl)) {
              _tmpAudioUrl = null;
            } else {
              _tmpAudioUrl = _cursor.getString(_cursorIndexOfAudioUrl);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final long _tmpPlayedDate;
            _tmpPlayedDate = _cursor.getLong(_cursorIndexOfPlayedDate);
            final long _tmpPlaybackPosition;
            _tmpPlaybackPosition = _cursor.getLong(_cursorIndexOfPlaybackPosition);
            final long _tmpDuration;
            _tmpDuration = _cursor.getLong(_cursorIndexOfDuration);
            _result = new RecentSong(_tmpId,_tmpTitle,_tmpArtist,_tmpCover,_tmpAudioUrl,_tmpCategory,_tmpPlayedDate,_tmpPlaybackPosition,_tmpDuration);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PlaybackHistory>> getPlaybackHistory() {
    final String _sql = "SELECT * FROM playback_history ORDER BY playCount DESC, lastPlayedDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"playback_history"}, new Callable<List<PlaybackHistory>>() {
      @Override
      @NonNull
      public List<PlaybackHistory> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSongId = CursorUtil.getColumnIndexOrThrow(_cursor, "songId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfCover = CursorUtil.getColumnIndexOrThrow(_cursor, "cover");
          final int _cursorIndexOfAudioUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "audioUrl");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfLastPlayedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastPlayedDate");
          final int _cursorIndexOfPlayCount = CursorUtil.getColumnIndexOrThrow(_cursor, "playCount");
          final List<PlaybackHistory> _result = new ArrayList<PlaybackHistory>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PlaybackHistory _item;
            final String _tmpSongId;
            if (_cursor.isNull(_cursorIndexOfSongId)) {
              _tmpSongId = null;
            } else {
              _tmpSongId = _cursor.getString(_cursorIndexOfSongId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpArtist;
            if (_cursor.isNull(_cursorIndexOfArtist)) {
              _tmpArtist = null;
            } else {
              _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            }
            final String _tmpCover;
            if (_cursor.isNull(_cursorIndexOfCover)) {
              _tmpCover = null;
            } else {
              _tmpCover = _cursor.getString(_cursorIndexOfCover);
            }
            final String _tmpAudioUrl;
            if (_cursor.isNull(_cursorIndexOfAudioUrl)) {
              _tmpAudioUrl = null;
            } else {
              _tmpAudioUrl = _cursor.getString(_cursorIndexOfAudioUrl);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final long _tmpLastPlayedDate;
            _tmpLastPlayedDate = _cursor.getLong(_cursorIndexOfLastPlayedDate);
            final int _tmpPlayCount;
            _tmpPlayCount = _cursor.getInt(_cursorIndexOfPlayCount);
            _item = new PlaybackHistory(_tmpSongId,_tmpTitle,_tmpArtist,_tmpCover,_tmpAudioUrl,_tmpCategory,_tmpLastPlayedDate,_tmpPlayCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getHistoryById(final String songId,
      final Continuation<? super PlaybackHistory> $completion) {
    final String _sql = "SELECT * FROM playback_history WHERE songId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (songId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, songId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PlaybackHistory>() {
      @Override
      @Nullable
      public PlaybackHistory call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSongId = CursorUtil.getColumnIndexOrThrow(_cursor, "songId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfCover = CursorUtil.getColumnIndexOrThrow(_cursor, "cover");
          final int _cursorIndexOfAudioUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "audioUrl");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfLastPlayedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastPlayedDate");
          final int _cursorIndexOfPlayCount = CursorUtil.getColumnIndexOrThrow(_cursor, "playCount");
          final PlaybackHistory _result;
          if (_cursor.moveToFirst()) {
            final String _tmpSongId;
            if (_cursor.isNull(_cursorIndexOfSongId)) {
              _tmpSongId = null;
            } else {
              _tmpSongId = _cursor.getString(_cursorIndexOfSongId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpArtist;
            if (_cursor.isNull(_cursorIndexOfArtist)) {
              _tmpArtist = null;
            } else {
              _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            }
            final String _tmpCover;
            if (_cursor.isNull(_cursorIndexOfCover)) {
              _tmpCover = null;
            } else {
              _tmpCover = _cursor.getString(_cursorIndexOfCover);
            }
            final String _tmpAudioUrl;
            if (_cursor.isNull(_cursorIndexOfAudioUrl)) {
              _tmpAudioUrl = null;
            } else {
              _tmpAudioUrl = _cursor.getString(_cursorIndexOfAudioUrl);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final long _tmpLastPlayedDate;
            _tmpLastPlayedDate = _cursor.getLong(_cursorIndexOfLastPlayedDate);
            final int _tmpPlayCount;
            _tmpPlayCount = _cursor.getInt(_cursorIndexOfPlayCount);
            _result = new PlaybackHistory(_tmpSongId,_tmpTitle,_tmpArtist,_tmpCover,_tmpAudioUrl,_tmpCategory,_tmpLastPlayedDate,_tmpPlayCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SearchHistory>> getSearchHistory() {
    final String _sql = "SELECT * FROM search_history ORDER BY searchDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"search_history"}, new Callable<List<SearchHistory>>() {
      @Override
      @NonNull
      public List<SearchHistory> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfQuery = CursorUtil.getColumnIndexOrThrow(_cursor, "query");
          final int _cursorIndexOfSearchDate = CursorUtil.getColumnIndexOrThrow(_cursor, "searchDate");
          final List<SearchHistory> _result = new ArrayList<SearchHistory>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SearchHistory _item;
            final String _tmpQuery;
            if (_cursor.isNull(_cursorIndexOfQuery)) {
              _tmpQuery = null;
            } else {
              _tmpQuery = _cursor.getString(_cursorIndexOfQuery);
            }
            final long _tmpSearchDate;
            _tmpSearchDate = _cursor.getLong(_cursorIndexOfSearchDate);
            _item = new SearchHistory(_tmpQuery,_tmpSearchDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Playlist>> getPlaylists() {
    final String _sql = "SELECT * FROM playlists ORDER BY createdDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"playlists"}, new Callable<List<Playlist>>() {
      @Override
      @NonNull
      public List<Playlist> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCoverImage = CursorUtil.getColumnIndexOrThrow(_cursor, "coverImage");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "createdDate");
          final List<Playlist> _result = new ArrayList<Playlist>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Playlist _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpCoverImage;
            if (_cursor.isNull(_cursorIndexOfCoverImage)) {
              _tmpCoverImage = null;
            } else {
              _tmpCoverImage = _cursor.getString(_cursorIndexOfCoverImage);
            }
            final long _tmpCreatedDate;
            _tmpCreatedDate = _cursor.getLong(_cursorIndexOfCreatedDate);
            _item = new Playlist(_tmpId,_tmpName,_tmpCoverImage,_tmpCreatedDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getPlaylistById(final String playlistId,
      final Continuation<? super Playlist> $completion) {
    final String _sql = "SELECT * FROM playlists WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (playlistId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, playlistId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Playlist>() {
      @Override
      @Nullable
      public Playlist call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCoverImage = CursorUtil.getColumnIndexOrThrow(_cursor, "coverImage");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "createdDate");
          final Playlist _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpCoverImage;
            if (_cursor.isNull(_cursorIndexOfCoverImage)) {
              _tmpCoverImage = null;
            } else {
              _tmpCoverImage = _cursor.getString(_cursorIndexOfCoverImage);
            }
            final long _tmpCreatedDate;
            _tmpCreatedDate = _cursor.getLong(_cursorIndexOfCreatedDate);
            _result = new Playlist(_tmpId,_tmpName,_tmpCoverImage,_tmpCreatedDate);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PlaylistSong>> getPlaylistSongs(final String playlistId) {
    final String _sql = "SELECT * FROM playlist_songs WHERE playlistId = ? ORDER BY orderIndex ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (playlistId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, playlistId);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"playlist_songs"}, new Callable<List<PlaylistSong>>() {
      @Override
      @NonNull
      public List<PlaylistSong> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfLocalId = CursorUtil.getColumnIndexOrThrow(_cursor, "localId");
          final int _cursorIndexOfPlaylistId = CursorUtil.getColumnIndexOrThrow(_cursor, "playlistId");
          final int _cursorIndexOfSongId = CursorUtil.getColumnIndexOrThrow(_cursor, "songId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfCover = CursorUtil.getColumnIndexOrThrow(_cursor, "cover");
          final int _cursorIndexOfAudioUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "audioUrl");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfOrderIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "orderIndex");
          final List<PlaylistSong> _result = new ArrayList<PlaylistSong>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PlaylistSong _item;
            final long _tmpLocalId;
            _tmpLocalId = _cursor.getLong(_cursorIndexOfLocalId);
            final String _tmpPlaylistId;
            if (_cursor.isNull(_cursorIndexOfPlaylistId)) {
              _tmpPlaylistId = null;
            } else {
              _tmpPlaylistId = _cursor.getString(_cursorIndexOfPlaylistId);
            }
            final String _tmpSongId;
            if (_cursor.isNull(_cursorIndexOfSongId)) {
              _tmpSongId = null;
            } else {
              _tmpSongId = _cursor.getString(_cursorIndexOfSongId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpArtist;
            if (_cursor.isNull(_cursorIndexOfArtist)) {
              _tmpArtist = null;
            } else {
              _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            }
            final String _tmpCover;
            if (_cursor.isNull(_cursorIndexOfCover)) {
              _tmpCover = null;
            } else {
              _tmpCover = _cursor.getString(_cursorIndexOfCover);
            }
            final String _tmpAudioUrl;
            if (_cursor.isNull(_cursorIndexOfAudioUrl)) {
              _tmpAudioUrl = null;
            } else {
              _tmpAudioUrl = _cursor.getString(_cursorIndexOfAudioUrl);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final int _tmpOrderIndex;
            _tmpOrderIndex = _cursor.getInt(_cursorIndexOfOrderIndex);
            _item = new PlaylistSong(_tmpLocalId,_tmpPlaylistId,_tmpSongId,_tmpTitle,_tmpArtist,_tmpCover,_tmpAudioUrl,_tmpCategory,_tmpOrderIndex);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<QueueItem>> getQueueItems() {
    final String _sql = "SELECT * FROM queue_items ORDER BY orderIndex ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"queue_items"}, new Callable<List<QueueItem>>() {
      @Override
      @NonNull
      public List<QueueItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSongId = CursorUtil.getColumnIndexOrThrow(_cursor, "songId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfCover = CursorUtil.getColumnIndexOrThrow(_cursor, "cover");
          final int _cursorIndexOfAudioUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "audioUrl");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfOrderIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "orderIndex");
          final List<QueueItem> _result = new ArrayList<QueueItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final QueueItem _item;
            final String _tmpSongId;
            if (_cursor.isNull(_cursorIndexOfSongId)) {
              _tmpSongId = null;
            } else {
              _tmpSongId = _cursor.getString(_cursorIndexOfSongId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpArtist;
            if (_cursor.isNull(_cursorIndexOfArtist)) {
              _tmpArtist = null;
            } else {
              _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            }
            final String _tmpCover;
            if (_cursor.isNull(_cursorIndexOfCover)) {
              _tmpCover = null;
            } else {
              _tmpCover = _cursor.getString(_cursorIndexOfCover);
            }
            final String _tmpAudioUrl;
            if (_cursor.isNull(_cursorIndexOfAudioUrl)) {
              _tmpAudioUrl = null;
            } else {
              _tmpAudioUrl = _cursor.getString(_cursorIndexOfAudioUrl);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final int _tmpOrderIndex;
            _tmpOrderIndex = _cursor.getInt(_cursorIndexOfOrderIndex);
            _item = new QueueItem(_tmpSongId,_tmpTitle,_tmpArtist,_tmpCover,_tmpAudioUrl,_tmpCategory,_tmpOrderIndex);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getQueueItemsSync(final Continuation<? super List<QueueItem>> $completion) {
    final String _sql = "SELECT * FROM queue_items ORDER BY orderIndex ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<QueueItem>>() {
      @Override
      @NonNull
      public List<QueueItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSongId = CursorUtil.getColumnIndexOrThrow(_cursor, "songId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfCover = CursorUtil.getColumnIndexOrThrow(_cursor, "cover");
          final int _cursorIndexOfAudioUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "audioUrl");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfOrderIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "orderIndex");
          final List<QueueItem> _result = new ArrayList<QueueItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final QueueItem _item;
            final String _tmpSongId;
            if (_cursor.isNull(_cursorIndexOfSongId)) {
              _tmpSongId = null;
            } else {
              _tmpSongId = _cursor.getString(_cursorIndexOfSongId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpArtist;
            if (_cursor.isNull(_cursorIndexOfArtist)) {
              _tmpArtist = null;
            } else {
              _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            }
            final String _tmpCover;
            if (_cursor.isNull(_cursorIndexOfCover)) {
              _tmpCover = null;
            } else {
              _tmpCover = _cursor.getString(_cursorIndexOfCover);
            }
            final String _tmpAudioUrl;
            if (_cursor.isNull(_cursorIndexOfAudioUrl)) {
              _tmpAudioUrl = null;
            } else {
              _tmpAudioUrl = _cursor.getString(_cursorIndexOfAudioUrl);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final int _tmpOrderIndex;
            _tmpOrderIndex = _cursor.getInt(_cursorIndexOfOrderIndex);
            _item = new QueueItem(_tmpSongId,_tmpTitle,_tmpArtist,_tmpCover,_tmpAudioUrl,_tmpCategory,_tmpOrderIndex);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
