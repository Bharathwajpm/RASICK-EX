plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.rasick.shared"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "BACKEND_URL", "\"http://10.0.2.2:5000/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BACKEND_URL", "\"https://your-domain.com/\"")
        }
        debug {
            buildConfigField("String", "BACKEND_URL", "\"http://10.0.2.2:5000/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    
    // Retrofit & Networking
    api("com.squareup.retrofit2:retrofit:2.9.0")
    api("com.squareup.retrofit2:converter-gson:2.9.0")
    api("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Media3 (ExoPlayer)
    api("androidx.media3:media3-exoplayer:1.3.0")
    api("androidx.media3:media3-session:1.3.0")

    // Compose Runtime
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    api(composeBom)
    api("androidx.compose.runtime:runtime")

    // Coroutines
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Room Database
    api("androidx.room:room-runtime:2.6.1")
    api("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Jetpack DataStore
    api("androidx.datastore:datastore-preferences:1.0.0")
}
