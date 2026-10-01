package com.rasick.shared.audio

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioFormat
import android.media.AudioManager
import android.media.MediaCodecList
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.view.WindowManager

object DeviceCapabilities {
    
    fun isTv(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? android.app.UiModeManager
        if (uiModeManager != null && uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) {
            return true
        }
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    }

    fun isHdmiConnected(context: Context): Boolean {
        val intent = context.registerReceiver(null, IntentFilter(AudioManager.ACTION_HDMI_AUDIO_PLUG))
        return intent?.getIntExtra(AudioManager.EXTRA_AUDIO_PLUG_STATE, 0) == 1
    }

    fun getHdmiSupportedEncodings(context: Context): List<String> {
        val list = mutableListOf<String>()
        val intent = context.registerReceiver(null, IntentFilter(AudioManager.ACTION_HDMI_AUDIO_PLUG))
        if (intent?.getIntExtra(AudioManager.EXTRA_AUDIO_PLUG_STATE, 0) == 1) {
            val encs = intent.getIntArrayExtra(AudioManager.EXTRA_ENCODINGS)
            encs?.forEach { encoding ->
                val name = when (encoding) {
                    AudioFormat.ENCODING_AC3 -> "AC3 (Dolby Digital)"
                    AudioFormat.ENCODING_E_AC3 -> "E-AC3 (Dolby Digital Plus)"
                    AudioFormat.ENCODING_DTS -> "DTS"
                    4 -> "DTS"
                    14 -> "DTS-HD"
                    AudioFormat.ENCODING_PCM_16BIT -> "PCM 16-bit"
                    else -> "Encoding-$encoding"
                }
                if (!list.contains(name)) {
                    list.add(name)
                }
            }
        }
        return list
    }

    fun getSupportedCodecs(): List<CodecInfo> {
        val list = mutableListOf<CodecInfo>()
        val mcl = MediaCodecList(MediaCodecList.ALL_CODECS)
        mcl.codecInfos.forEach { info ->
            if (!info.isEncoder) {
                val types = info.supportedTypes
                types.forEach { type ->
                    if (type.startsWith("audio/", ignoreCase = true)) {
                        val isHardware = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            info.isHardwareAccelerated
                        } else {
                            !info.name.startsWith("OMX.google", ignoreCase = true) && !info.name.startsWith("c2.android", ignoreCase = true)
                        }
                        list.add(
                            CodecInfo(
                                mimeType = type,
                                decoderName = info.name,
                                isHardware = isHardware
                            )
                        )
                    }
                }
            }
        }
        return list
    }

    fun isCodecSupported(mimeType: String): Boolean {
        return getSupportedCodecs().any { it.mimeType.equals(mimeType, ignoreCase = true) }
    }

    fun isHardwareCodecSupported(mimeType: String): Boolean {
        return getSupportedCodecs().any {
            it.mimeType.equals(mimeType, ignoreCase = true) && it.isHardware
        }
    }

    // Extended metrics for device manager
    fun getRamInfo(context: Context): String {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalGb = memoryInfo.totalMem.toDouble() / (1024 * 1024 * 1024)
        return String.format("%.1f GB", totalGb)
    }

    fun getStorageInfo(context: Context): Pair<String, String> {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        
        val totalGb = (totalBlocks * blockSize).toDouble() / (1024 * 1024 * 1024)
        val freeGb = (availableBlocks * blockSize).toDouble() / (1024 * 1024 * 1024)
        
        return Pair(String.format("%.1f GB", totalGb), String.format("%.1f GB", freeGb))
    }

    fun getDisplayInfo(context: Context): Pair<String, String> {
        val metrics = context.resources.displayMetrics
        val res = "${metrics.widthPixels}x${metrics.heightPixels}"
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val defaultDisplay = windowManager.defaultDisplay
        val refreshRate = "${defaultDisplay.refreshRate.toInt()} Hz"
        return Pair(res, refreshRate)
    }

    fun getCpuAbi(): String {
        return Build.SUPPORTED_ABIS.joinToString(", ")
    }
}

data class CodecInfo(
    val mimeType: String,
    val decoderName: String,
    val isHardware: Boolean
)
