package com.rasick.shared.audio;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004J\u001a\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00040\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00040\n2\u0006\u0010\u0007\u001a\u00020\bJ\u000e\u0010\u000b\u001a\u00020\u00042\u0006\u0010\u0007\u001a\u00020\bJ\u001a\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00040\u00062\u0006\u0010\u0007\u001a\u00020\bJ\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\nJ\u000e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0004J\u000e\u0010\u0012\u001a\u00020\u00102\u0006\u0010\u0007\u001a\u00020\bJ\u000e\u0010\u0013\u001a\u00020\u00102\u0006\u0010\u0007\u001a\u00020\b\u00a8\u0006\u0014"}, d2 = {"Lcom/rasick/shared/audio/DeviceCapabilities;", "", "()V", "getCpuAbi", "", "getDisplayInfo", "Lkotlin/Pair;", "context", "Landroid/content/Context;", "getHdmiSupportedEncodings", "", "getRamInfo", "getStorageInfo", "getSupportedCodecs", "Lcom/rasick/shared/audio/CodecInfo;", "isCodecSupported", "", "mimeType", "isHdmiConnected", "isTv", "shared_release"})
public final class DeviceCapabilities {
    @org.jetbrains.annotations.NotNull()
    public static final com.rasick.shared.audio.DeviceCapabilities INSTANCE = null;
    
    private DeviceCapabilities() {
        super();
    }
    
    public final boolean isTv(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    public final boolean isHdmiConnected(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getHdmiSupportedEncodings(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.rasick.shared.audio.CodecInfo> getSupportedCodecs() {
        return null;
    }
    
    public final boolean isCodecSupported(@org.jetbrains.annotations.NotNull()
    java.lang.String mimeType) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRamInfo(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<java.lang.String, java.lang.String> getStorageInfo(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<java.lang.String, java.lang.String> getDisplayInfo(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCpuAbi() {
        return null;
    }
}