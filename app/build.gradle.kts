plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Firma opcional: si el workflow define KEYSTORE_FILE se usa; si no, firma con la clave debug.
val keystorePath: String? = System.getenv("KEYSTORE_FILE")

android {
    namespace = "com.tvlauncher"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tvlauncher"
        minSdk = 21
        targetSdk = 34
        versionCode = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1
        versionName = System.getenv("VERSION_NAME") ?: "1.0.0"
    }

    signingConfigs {
        if (keystorePath != null && File(keystorePath).exists()) {
            create("release") {
                storeFile = File(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
