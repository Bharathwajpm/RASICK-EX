package com.rasick.shared.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0017\u001a\u00020\tJ\b\u0010\u0018\u001a\u00020\u0011H\u0002J\u000e\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\tR\u0011\u0010\u0003\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\n\u001a\u00020\u000b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000e\u0010\u000f\u001a\u0004\b\f\u0010\rR\u0010\u0010\u0010\u001a\u0004\u0018\u00010\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0012\u001a\u00020\u00138F\u00a2\u0006\u0006\u001a\u0004\b\u0014\u0010\u0015R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcom/rasick/shared/api/RetrofitClient;", "", "()V", "authService", "Lcom/rasick/shared/api/AuthService;", "getAuthService", "()Lcom/rasick/shared/api/AuthService;", "authServiceInstance", "baseUrl", "", "okHttpClient", "Lokhttp3/OkHttpClient;", "getOkHttpClient", "()Lokhttp3/OkHttpClient;", "okHttpClient$delegate", "Lkotlin/Lazy;", "retrofitInstance", "Lretrofit2/Retrofit;", "songService", "Lcom/rasick/shared/api/SongService;", "getSongService", "()Lcom/rasick/shared/api/SongService;", "songServiceInstance", "getBaseUrl", "getRetrofit", "setBaseUrl", "", "url", "shared_release"})
public final class RetrofitClient {
    @org.jetbrains.annotations.NotNull()
    private static java.lang.String baseUrl = "https://your-domain.com/";
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy okHttpClient$delegate = null;
    @org.jetbrains.annotations.Nullable()
    private static retrofit2.Retrofit retrofitInstance;
    @org.jetbrains.annotations.Nullable()
    private static com.rasick.shared.api.AuthService authServiceInstance;
    @org.jetbrains.annotations.Nullable()
    private static com.rasick.shared.api.SongService songServiceInstance;
    @org.jetbrains.annotations.NotNull()
    public static final com.rasick.shared.api.RetrofitClient INSTANCE = null;
    
    private RetrofitClient() {
        super();
    }
    
    public final void setBaseUrl(@org.jetbrains.annotations.NotNull()
    java.lang.String url) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getBaseUrl() {
        return null;
    }
    
    private final okhttp3.OkHttpClient getOkHttpClient() {
        return null;
    }
    
    private final retrofit2.Retrofit getRetrofit() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rasick.shared.api.AuthService getAuthService() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rasick.shared.api.SongService getSongService() {
        return null;
    }
}