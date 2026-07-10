package com.rasick.shared.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DownloadDatabase_Impl extends DownloadDatabase {
  private volatile DownloadedSongDao _downloadedSongDao;

  private volatile LibraryDao _libraryDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `downloaded_songs` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `artist` TEXT NOT NULL, `category` TEXT, `cover` TEXT NOT NULL, `originalUrl` TEXT NOT NULL, `localPath` TEXT NOT NULL, `fileSize` INTEGER NOT NULL, `downloadDate` INTEGER NOT NULL, `status` TEXT NOT NULL, `progress` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `favorite_songs` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `artist` TEXT NOT NULL, `cover` TEXT NOT NULL, `audioUrl` TEXT NOT NULL, `category` TEXT, `addedDate` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `playlists` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `coverImage` TEXT, `createdDate` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `playlist_songs` (`localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `playlistId` TEXT NOT NULL, `songId` TEXT NOT NULL, `title` TEXT NOT NULL, `artist` TEXT NOT NULL, `cover` TEXT NOT NULL, `audioUrl` TEXT NOT NULL, `category` TEXT, `orderIndex` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_playlist_songs_playlistId` ON `playlist_songs` (`playlistId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `recent_songs` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `artist` TEXT NOT NULL, `cover` TEXT NOT NULL, `audioUrl` TEXT NOT NULL, `category` TEXT, `playedDate` INTEGER NOT NULL, `playbackPosition` INTEGER NOT NULL, `duration` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `playback_history` (`songId` TEXT NOT NULL, `title` TEXT NOT NULL, `artist` TEXT NOT NULL, `cover` TEXT NOT NULL, `audioUrl` TEXT NOT NULL, `category` TEXT, `lastPlayedDate` INTEGER NOT NULL, `playCount` INTEGER NOT NULL, PRIMARY KEY(`songId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `search_history` (`query` TEXT NOT NULL, `searchDate` INTEGER NOT NULL, PRIMARY KEY(`query`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `queue_items` (`songId` TEXT NOT NULL, `title` TEXT NOT NULL, `artist` TEXT NOT NULL, `cover` TEXT NOT NULL, `audioUrl` TEXT NOT NULL, `category` TEXT, `orderIndex` INTEGER NOT NULL, PRIMARY KEY(`songId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'c8f3cd04f0e45be72ac2eb0baedd7cbe')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `downloaded_songs`");
        db.execSQL("DROP TABLE IF EXISTS `favorite_songs`");
        db.execSQL("DROP TABLE IF EXISTS `playlists`");
        db.execSQL("DROP TABLE IF EXISTS `playlist_songs`");
        db.execSQL("DROP TABLE IF EXISTS `recent_songs`");
        db.execSQL("DROP TABLE IF EXISTS `playback_history`");
        db.execSQL("DROP TABLE IF EXISTS `search_history`");
        db.execSQL("DROP TABLE IF EXISTS `queue_items`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsDownloadedSongs = new HashMap<String, TableInfo.Column>(11);
        _columnsDownloadedSongs.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedSongs.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedSongs.put("artist", new TableInfo.Column("artist", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedSongs.put("category", new TableInfo.Column("category", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedSongs.put("cover", new TableInfo.Column("cover", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedSongs.put("originalUrl", new TableInfo.Column("originalUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedSongs.put("localPath", new TableInfo.Column("localPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedSongs.put("fileSize", new TableInfo.Column("fileSize", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedSongs.put("downloadDate", new TableInfo.Column("downloadDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedSongs.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedSongs.put("progress", new TableInfo.Column("progress", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDownloadedSongs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDownloadedSongs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDownloadedSongs = new TableInfo("downloaded_songs", _columnsDownloadedSongs, _foreignKeysDownloadedSongs, _indicesDownloadedSongs);
        final TableInfo _existingDownloadedSongs = TableInfo.read(db, "downloaded_songs");
        if (!_infoDownloadedSongs.equals(_existingDownloadedSongs)) {
          return new RoomOpenHelper.ValidationResult(false, "downloaded_songs(com.rasick.shared.data.DownloadedSong).\n"
                  + " Expected:\n" + _infoDownloadedSongs + "\n"
                  + " Found:\n" + _existingDownloadedSongs);
        }
        final HashMap<String, TableInfo.Column> _columnsFavoriteSongs = new HashMap<String, TableInfo.Column>(7);
        _columnsFavoriteSongs.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFavoriteSongs.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFavoriteSongs.put("artist", new TableInfo.Column("artist", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFavoriteSongs.put("cover", new TableInfo.Column("cover", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFavoriteSongs.put("audioUrl", new TableInfo.Column("audioUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFavoriteSongs.put("category", new TableInfo.Column("category", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFavoriteSongs.put("addedDate", new TableInfo.Column("addedDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFavoriteSongs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFavoriteSongs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFavoriteSongs = new TableInfo("favorite_songs", _columnsFavoriteSongs, _foreignKeysFavoriteSongs, _indicesFavoriteSongs);
        final TableInfo _existingFavoriteSongs = TableInfo.read(db, "favorite_songs");
        if (!_infoFavoriteSongs.equals(_existingFavoriteSongs)) {
          return new RoomOpenHelper.ValidationResult(false, "favorite_songs(com.rasick.shared.data.FavoriteSong).\n"
                  + " Expected:\n" + _infoFavoriteSongs + "\n"
                  + " Found:\n" + _existingFavoriteSongs);
        }
        final HashMap<String, TableInfo.Column> _columnsPlaylists = new HashMap<String, TableInfo.Column>(4);
        _columnsPlaylists.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaylists.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaylists.put("coverImage", new TableInfo.Column("coverImage", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaylists.put("createdDate", new TableInfo.Column("createdDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlaylists = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlaylists = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPlaylists = new TableInfo("playlists", _columnsPlaylists, _foreignKeysPlaylists, _indicesPlaylists);
        final TableInfo _existingPlaylists = TableInfo.read(db, "playlists");
        if (!_infoPlaylists.equals(_existingPlaylists)) {
          return new RoomOpenHelper.ValidationResult(false, "playlists(com.rasick.shared.data.Playlist).\n"
                  + " Expected:\n" + _infoPlaylists + "\n"
                  + " Found:\n" + _existingPlaylists);
        }
        final HashMap<String, TableInfo.Column> _columnsPlaylistSongs = new HashMap<String, TableInfo.Column>(9);
        _columnsPlaylistSongs.put("localId", new TableInfo.Column("localId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaylistSongs.put("playlistId", new TableInfo.Column("playlistId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaylistSongs.put("songId", new TableInfo.Column("songId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaylistSongs.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaylistSongs.put("artist", new TableInfo.Column("artist", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaylistSongs.put("cover", new TableInfo.Column("cover", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaylistSongs.put("audioUrl", new TableInfo.Column("audioUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaylistSongs.put("category", new TableInfo.Column("category", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaylistSongs.put("orderIndex", new TableInfo.Column("orderIndex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlaylistSongs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlaylistSongs = new HashSet<TableInfo.Index>(1);
        _indicesPlaylistSongs.add(new TableInfo.Index("index_playlist_songs_playlistId", false, Arrays.asList("playlistId"), Arrays.asList("ASC")));
        final TableInfo _infoPlaylistSongs = new TableInfo("playlist_songs", _columnsPlaylistSongs, _foreignKeysPlaylistSongs, _indicesPlaylistSongs);
        final TableInfo _existingPlaylistSongs = TableInfo.read(db, "playlist_songs");
        if (!_infoPlaylistSongs.equals(_existingPlaylistSongs)) {
          return new RoomOpenHelper.ValidationResult(false, "playlist_songs(com.rasick.shared.data.PlaylistSong).\n"
                  + " Expected:\n" + _infoPlaylistSongs + "\n"
                  + " Found:\n" + _existingPlaylistSongs);
        }
        final HashMap<String, TableInfo.Column> _columnsRecentSongs = new HashMap<String, TableInfo.Column>(9);
        _columnsRecentSongs.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecentSongs.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecentSongs.put("artist", new TableInfo.Column("artist", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecentSongs.put("cover", new TableInfo.Column("cover", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecentSongs.put("audioUrl", new TableInfo.Column("audioUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecentSongs.put("category", new TableInfo.Column("category", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecentSongs.put("playedDate", new TableInfo.Column("playedDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecentSongs.put("playbackPosition", new TableInfo.Column("playbackPosition", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecentSongs.put("duration", new TableInfo.Column("duration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRecentSongs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRecentSongs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRecentSongs = new TableInfo("recent_songs", _columnsRecentSongs, _foreignKeysRecentSongs, _indicesRecentSongs);
        final TableInfo _existingRecentSongs = TableInfo.read(db, "recent_songs");
        if (!_infoRecentSongs.equals(_existingRecentSongs)) {
          return new RoomOpenHelper.ValidationResult(false, "recent_songs(com.rasick.shared.data.RecentSong).\n"
                  + " Expected:\n" + _infoRecentSongs + "\n"
                  + " Found:\n" + _existingRecentSongs);
        }
        final HashMap<String, TableInfo.Column> _columnsPlaybackHistory = new HashMap<String, TableInfo.Column>(8);
        _columnsPlaybackHistory.put("songId", new TableInfo.Column("songId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaybackHistory.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaybackHistory.put("artist", new TableInfo.Column("artist", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaybackHistory.put("cover", new TableInfo.Column("cover", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaybackHistory.put("audioUrl", new TableInfo.Column("audioUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaybackHistory.put("category", new TableInfo.Column("category", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaybackHistory.put("lastPlayedDate", new TableInfo.Column("lastPlayedDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaybackHistory.put("playCount", new TableInfo.Column("playCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlaybackHistory = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlaybackHistory = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPlaybackHistory = new TableInfo("playback_history", _columnsPlaybackHistory, _foreignKeysPlaybackHistory, _indicesPlaybackHistory);
        final TableInfo _existingPlaybackHistory = TableInfo.read(db, "playback_history");
        if (!_infoPlaybackHistory.equals(_existingPlaybackHistory)) {
          return new RoomOpenHelper.ValidationResult(false, "playback_history(com.rasick.shared.data.PlaybackHistory).\n"
                  + " Expected:\n" + _infoPlaybackHistory + "\n"
                  + " Found:\n" + _existingPlaybackHistory);
        }
        final HashMap<String, TableInfo.Column> _columnsSearchHistory = new HashMap<String, TableInfo.Column>(2);
        _columnsSearchHistory.put("query", new TableInfo.Column("query", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSearchHistory.put("searchDate", new TableInfo.Column("searchDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSearchHistory = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSearchHistory = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSearchHistory = new TableInfo("search_history", _columnsSearchHistory, _foreignKeysSearchHistory, _indicesSearchHistory);
        final TableInfo _existingSearchHistory = TableInfo.read(db, "search_history");
        if (!_infoSearchHistory.equals(_existingSearchHistory)) {
          return new RoomOpenHelper.ValidationResult(false, "search_history(com.rasick.shared.data.SearchHistory).\n"
                  + " Expected:\n" + _infoSearchHistory + "\n"
                  + " Found:\n" + _existingSearchHistory);
        }
        final HashMap<String, TableInfo.Column> _columnsQueueItems = new HashMap<String, TableInfo.Column>(7);
        _columnsQueueItems.put("songId", new TableInfo.Column("songId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueueItems.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueueItems.put("artist", new TableInfo.Column("artist", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueueItems.put("cover", new TableInfo.Column("cover", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueueItems.put("audioUrl", new TableInfo.Column("audioUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueueItems.put("category", new TableInfo.Column("category", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueueItems.put("orderIndex", new TableInfo.Column("orderIndex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysQueueItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesQueueItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoQueueItems = new TableInfo("queue_items", _columnsQueueItems, _foreignKeysQueueItems, _indicesQueueItems);
        final TableInfo _existingQueueItems = TableInfo.read(db, "queue_items");
        if (!_infoQueueItems.equals(_existingQueueItems)) {
          return new RoomOpenHelper.ValidationResult(false, "queue_items(com.rasick.shared.data.QueueItem).\n"
                  + " Expected:\n" + _infoQueueItems + "\n"
                  + " Found:\n" + _existingQueueItems);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "c8f3cd04f0e45be72ac2eb0baedd7cbe", "9b8d8ae598ed81f84c35c6a54aa6ec2e");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "downloaded_songs","favorite_songs","playlists","playlist_songs","recent_songs","playback_history","search_history","queue_items");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `downloaded_songs`");
      _db.execSQL("DELETE FROM `favorite_songs`");
      _db.execSQL("DELETE FROM `playlists`");
      _db.execSQL("DELETE FROM `playlist_songs`");
      _db.execSQL("DELETE FROM `recent_songs`");
      _db.execSQL("DELETE FROM `playback_history`");
      _db.execSQL("DELETE FROM `search_history`");
      _db.execSQL("DELETE FROM `queue_items`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(DownloadedSongDao.class, DownloadedSongDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(LibraryDao.class, LibraryDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public DownloadedSongDao downloadedSongDao() {
    if (_downloadedSongDao != null) {
      return _downloadedSongDao;
    } else {
      synchronized(this) {
        if(_downloadedSongDao == null) {
          _downloadedSongDao = new DownloadedSongDao_Impl(this);
        }
        return _downloadedSongDao;
      }
    }
  }

  @Override
  public LibraryDao libraryDao() {
    if (_libraryDao != null) {
      return _libraryDao;
    } else {
      synchronized(this) {
        if(_libraryDao == null) {
          _libraryDao = new LibraryDao_Impl(this);
        }
        return _libraryDao;
      }
    }
  }
}
