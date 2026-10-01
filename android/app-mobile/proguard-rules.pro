# Keep Retrofit and Gson annotations
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Gson model structures
-keep class com.rasick.shared.model.** { *; }

# Keep Room entities and DAOs
-keep class com.rasick.shared.data.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Keep Media3 classes from being obfuscated
-keep class androidx.media3.** { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Gson
-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }
