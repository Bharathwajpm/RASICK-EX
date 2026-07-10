package com.rasick.shared.audio;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0002\b\u000f\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\t\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0012\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b5\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010S\u001a\u00020TJ\b\u0010U\u001a\u0004\u0018\u00010EJ\u0010\u0010V\u001a\u00020T2\u0006\u0010W\u001a\u00020\u001cH\u0007J\u0006\u0010X\u001a\u00020TJ\b\u0010Y\u001a\u00020TH\u0002J\u001c\u0010Z\u001a\u00020T2\u0006\u0010[\u001a\u00020\u001e2\f\u0010\\\u001a\b\u0012\u0004\u0012\u00020\u001e0JJ\u0006\u0010]\u001a\u00020TJ\u0010\u0010^\u001a\u00020T2\u0006\u0010[\u001a\u00020\u001eH\u0002J\u0006\u0010_\u001a\u00020TJ\u000e\u0010`\u001a\u00020T2\u0006\u0010a\u001a\u00020\fJ\u0016\u0010b\u001a\u00020T2\u0006\u0010c\u001a\u00020\u00042\u0006\u0010d\u001a\u00020\u0004J\u0010\u0010e\u001a\u00020T2\u0006\u0010W\u001a\u00020\u001cH\u0002J\u0010\u0010f\u001a\u00020T2\u0006\u0010g\u001a\u00020%H\u0002J\b\u0010h\u001a\u00020TH\u0002J\u000e\u0010i\u001a\u00020T2\u0006\u0010j\u001a\u00020%J\b\u0010k\u001a\u00020TH\u0002J\b\u0010l\u001a\u00020TH\u0002J\u0006\u0010m\u001a\u00020TJ\b\u0010n\u001a\u00020TH\u0002J\u0006\u0010o\u001a\u00020TJ\u0006\u0010p\u001a\u00020TJ\u0006\u0010q\u001a\u00020TJ\u0017\u0010r\u001a\u00020T2\b\u0010s\u001a\u0004\u0018\u00010\fH\u0000\u00a2\u0006\u0002\btJ\'\u0010u\u001a\u00020T2\b\u0010v\u001a\u0004\u0018\u00010\f2\u0006\u0010w\u001a\u00020\u00042\u0006\u0010x\u001a\u00020\u0004H\u0000\u00a2\u0006\u0002\byJ\u0015\u0010z\u001a\u00020T2\u0006\u0010{\u001a\u000200H\u0000\u00a2\u0006\u0002\b|J\u001f\u0010}\u001a\u00020T2\b\u0010[\u001a\u0004\u0018\u00010\u001e2\u0006\u0010~\u001a\u00020\u0004H\u0000\u00a2\u0006\u0002\b\u007fJ\u0018\u0010\u0080\u0001\u001a\u00020T2\u0007\u0010\u0081\u0001\u001a\u00020%H\u0000\u00a2\u0006\u0003\b\u0082\u0001J\u001a\u0010\u0083\u0001\u001a\u00020T2\t\u0010\u0084\u0001\u001a\u0004\u0018\u00010\fH\u0000\u00a2\u0006\u0003\b\u0085\u0001J\u0018\u0010\u0086\u0001\u001a\u00020T2\u0007\u0010\u0087\u0001\u001a\u000200H\u0000\u00a2\u0006\u0003\b\u0088\u0001R+\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u00048F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\n\u0010\u000b\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR/\u0010\r\u001a\u0004\u0018\u00010\f2\b\u0010\u0003\u001a\u0004\u0018\u00010\f8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0012\u0010\u000b\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R/\u0010\u0013\u001a\u0004\u0018\u00010\f2\b\u0010\u0003\u001a\u0004\u0018\u00010\f8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0016\u0010\u000b\u001a\u0004\b\u0014\u0010\u000f\"\u0004\b\u0015\u0010\u0011R+\u0010\u0017\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u00048F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001a\u0010\u000b\u001a\u0004\b\u0018\u0010\u0007\"\u0004\b\u0019\u0010\tR\u0010\u0010\u001b\u001a\u0004\u0018\u00010\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R/\u0010\u001f\u001a\u0004\u0018\u00010\u001e2\b\u0010\u0003\u001a\u0004\u0018\u00010\u001e8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b$\u0010\u000b\u001a\u0004\b \u0010!\"\u0004\b\"\u0010#R+\u0010&\u001a\u00020%2\u0006\u0010\u0003\u001a\u00020%8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b+\u0010\u000b\u001a\u0004\b\'\u0010(\"\u0004\b)\u0010*R/\u0010,\u001a\u0004\u0018\u00010\f2\b\u0010\u0003\u001a\u0004\u0018\u00010\f8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b/\u0010\u000b\u001a\u0004\b-\u0010\u000f\"\u0004\b.\u0010\u0011R+\u00101\u001a\u0002002\u0006\u0010\u0003\u001a\u0002008F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b5\u0010\u000b\u001a\u0004\b1\u00102\"\u0004\b3\u00104R+\u00106\u001a\u0002002\u0006\u0010\u0003\u001a\u0002008F@FX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b8\u0010\u000b\u001a\u0004\b6\u00102\"\u0004\b7\u00104R+\u00109\u001a\u0002002\u0006\u0010\u0003\u001a\u0002008F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b;\u0010\u000b\u001a\u0004\b9\u00102\"\u0004\b:\u00104R+\u0010<\u001a\u0002002\u0006\u0010\u0003\u001a\u0002008F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b>\u0010\u000b\u001a\u0004\b<\u00102\"\u0004\b=\u00104R+\u0010?\u001a\u0002002\u0006\u0010\u0003\u001a\u0002008F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\bA\u0010\u000b\u001a\u0004\b?\u00102\"\u0004\b@\u00104R\u0010\u0010B\u001a\u0004\u0018\u00010CX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010D\u001a\u0004\u0018\u00010EX\u0082\u000e\u00a2\u0006\u0002\n\u0000R+\u0010F\u001a\u00020%2\u0006\u0010\u0003\u001a\u00020%8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\bI\u0010\u000b\u001a\u0004\bG\u0010(\"\u0004\bH\u0010*R7\u0010K\u001a\b\u0012\u0004\u0012\u00020\u001e0J2\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u001e0J8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\bP\u0010\u000b\u001a\u0004\bL\u0010M\"\u0004\bN\u0010OR\u000e\u0010Q\u001a\u00020RX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0089\u0001"}, d2 = {"Lcom/rasick/shared/audio/PlaybackManager;", "", "()V", "<set-?>", "", "activeChannels", "getActiveChannels", "()I", "setActiveChannels", "(I)V", "activeChannels$delegate", "Landroidx/compose/runtime/MutableState;", "", "activeDecoderName", "getActiveDecoderName", "()Ljava/lang/String;", "setActiveDecoderName", "(Ljava/lang/String;)V", "activeDecoderName$delegate", "activeMimeType", "getActiveMimeType", "setActiveMimeType", "activeMimeType$delegate", "activeSampleRate", "getActiveSampleRate", "setActiveSampleRate", "activeSampleRate$delegate", "appContext", "Landroid/content/Context;", "currentIndex", "Lcom/rasick/shared/model/Song;", "currentSong", "getCurrentSong", "()Lcom/rasick/shared/model/Song;", "setCurrentSong", "(Lcom/rasick/shared/model/Song;)V", "currentSong$delegate", "", "duration", "getDuration", "()J", "setDuration", "(J)V", "duration$delegate", "errorMsg", "getErrorMsg", "setErrorMsg", "errorMsg$delegate", "", "isBuffering", "()Z", "setBuffering", "(Z)V", "isBuffering$delegate", "isExpanded", "setExpanded", "isExpanded$delegate", "isPlaying", "setPlaying", "isPlaying$delegate", "isRepeatOne", "setRepeatOne", "isRepeatOne$delegate", "isShuffleEnabled", "setShuffleEnabled", "isShuffleEnabled$delegate", "job", "Lkotlinx/coroutines/Job;", "player", "Landroidx/media3/exoplayer/ExoPlayer;", "position", "getPosition", "setPosition", "position$delegate", "", "queue", "getQueue", "()Ljava/util/List;", "setQueue", "(Ljava/util/List;)V", "queue$delegate", "scope", "Lkotlinx/coroutines/CoroutineScope;", "clearPlaybackQueue", "", "getPlayerInstance", "initialize", "context", "next", "onPlaybackEnded", "play", "song", "newQueue", "prev", "recordPlaybackStart", "release", "removeSongFromQueue", "songId", "reorderQueue", "fromIndex", "toIndex", "restoreQueueState", "savePositionProgress", "pos", "saveQueueState", "seekTo", "ms", "startPlaybackService", "startPositionUpdates", "stop", "stopPositionUpdates", "togglePlay", "toggleRepeatOne", "toggleShuffle", "updateActiveDecoder", "decoder", "updateActiveDecoder$shared_release", "updateActiveFormat", "mime", "sampleRate", "channels", "updateActiveFormat$shared_release", "updateBuffering", "buffering", "updateBuffering$shared_release", "updateCurrentSong", "index", "updateCurrentSong$shared_release", "updateDuration", "dur", "updateDuration$shared_release", "updateError", "error", "updateError$shared_release", "updatePlaying", "playing", "updatePlaying$shared_release", "shared_release"})
public final class PlaybackManager {
    @org.jetbrains.annotations.Nullable()
    private static androidx.media3.exoplayer.ExoPlayer player;
    @org.jetbrains.annotations.Nullable()
    private static kotlinx.coroutines.Job job;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.Nullable()
    private static android.content.Context appContext;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.runtime.MutableState currentSong$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.runtime.MutableState isPlaying$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.runtime.MutableState isBuffering$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.runtime.MutableState position$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.runtime.MutableState duration$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.runtime.MutableState errorMsg$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.runtime.MutableState queue$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.runtime.MutableState isExpanded$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.runtime.MutableState isRepeatOne$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.runtime.MutableState isShuffleEnabled$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.runtime.MutableState activeMimeType$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.runtime.MutableState activeSampleRate$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.runtime.MutableState activeChannels$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.runtime.MutableState activeDecoderName$delegate = null;
    private static int currentIndex = -1;
    @org.jetbrains.annotations.NotNull()
    public static final com.rasick.shared.audio.PlaybackManager INSTANCE = null;
    
    private PlaybackManager() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.rasick.shared.model.Song getCurrentSong() {
        return null;
    }
    
    private final void setCurrentSong(com.rasick.shared.model.Song p0) {
    }
    
    public final boolean isPlaying() {
        return false;
    }
    
    private final void setPlaying(boolean p0) {
    }
    
    public final boolean isBuffering() {
        return false;
    }
    
    private final void setBuffering(boolean p0) {
    }
    
    public final long getPosition() {
        return 0L;
    }
    
    private final void setPosition(long p0) {
    }
    
    public final long getDuration() {
        return 0L;
    }
    
    private final void setDuration(long p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMsg() {
        return null;
    }
    
    private final void setErrorMsg(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.rasick.shared.model.Song> getQueue() {
        return null;
    }
    
    private final void setQueue(java.util.List<com.rasick.shared.model.Song> p0) {
    }
    
    public final boolean isExpanded() {
        return false;
    }
    
    public final void setExpanded(boolean p0) {
    }
    
    public final boolean isRepeatOne() {
        return false;
    }
    
    private final void setRepeatOne(boolean p0) {
    }
    
    public final boolean isShuffleEnabled() {
        return false;
    }
    
    private final void setShuffleEnabled(boolean p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getActiveMimeType() {
        return null;
    }
    
    private final void setActiveMimeType(java.lang.String p0) {
    }
    
    public final int getActiveSampleRate() {
        return 0;
    }
    
    private final void setActiveSampleRate(int p0) {
    }
    
    public final int getActiveChannels() {
        return 0;
    }
    
    private final void setActiveChannels(int p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getActiveDecoderName() {
        return null;
    }
    
    private final void setActiveDecoderName(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final androidx.media3.exoplayer.ExoPlayer getPlayerInstance() {
        return null;
    }
    
    public final void updatePlaying$shared_release(boolean playing) {
    }
    
    public final void updateBuffering$shared_release(boolean buffering) {
    }
    
    public final void updateDuration$shared_release(long dur) {
    }
    
    public final void updateError$shared_release(@org.jetbrains.annotations.Nullable()
    java.lang.String error) {
    }
    
    public final void updateCurrentSong$shared_release(@org.jetbrains.annotations.Nullable()
    com.rasick.shared.model.Song song, int index) {
    }
    
    public final void updateActiveFormat$shared_release(@org.jetbrains.annotations.Nullable()
    java.lang.String mime, int sampleRate, int channels) {
    }
    
    public final void updateActiveDecoder$shared_release(@org.jetbrains.annotations.Nullable()
    java.lang.String decoder) {
    }
    
    @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
    public final void initialize(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    private final void startPlaybackService() {
    }
    
    public final void play(@org.jetbrains.annotations.NotNull()
    com.rasick.shared.model.Song song, @org.jetbrains.annotations.NotNull()
    java.util.List<com.rasick.shared.model.Song> newQueue) {
    }
    
    public final void togglePlay() {
    }
    
    public final void next() {
    }
    
    public final void prev() {
    }
    
    public final void seekTo(long ms) {
    }
    
    public final void toggleRepeatOne() {
    }
    
    public final void toggleShuffle() {
    }
    
    public final void reorderQueue(int fromIndex, int toIndex) {
    }
    
    public final void removeSongFromQueue(@org.jetbrains.annotations.NotNull()
    java.lang.String songId) {
    }
    
    public final void clearPlaybackQueue() {
    }
    
    public final void stop() {
    }
    
    public final void release() {
    }
    
    private final void startPositionUpdates() {
    }
    
    private final void stopPositionUpdates() {
    }
    
    private final void onPlaybackEnded() {
    }
    
    private final void recordPlaybackStart(com.rasick.shared.model.Song song) {
    }
    
    private final void savePositionProgress(long pos) {
    }
    
    private final void saveQueueState() {
    }
    
    private final void restoreQueueState(android.content.Context context) {
    }
}