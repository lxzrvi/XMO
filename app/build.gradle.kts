plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.xmo.music"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.xmo.music"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(
        platform(
            "androidx.compose:compose-bom:2025.08.00"
        )
    )

    implementation(
        "androidx.activity:activity-compose:1.10.1"
    )

    implementation(
        "androidx.compose.material3:material3"
    )

    implementation(
        "androidx.compose.material:material-icons-core"
    )

    implementation(
        "androidx.datastore:datastore-preferences:1.1.7"
    )

    implementation(
        "io.coil-kt.coil3:coil-compose:3.3.0"
    )

    implementation(
        "androidx.palette:palette-ktx:1.0.0"
    )

    implementation(
        "androidx.media3:media3-exoplayer:1.8.0"
    )

    implementation(
        "androidx.media3:media3-session:1.8.0"
    )

    /*
     * XMO shared Liquid Glass
     */
    implementation("dev.chrisbanes.haze:haze:2.0.0-alpha05")
    implementation("dev.chrisbanes.haze:haze-blur:2.0.0-alpha05")
    implementation("dev.chrisbanes.haze:haze-blur-materials:2.0.0-alpha05")
}
