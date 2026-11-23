plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.chaquo.python")
}

android {
    namespace = "com.kara.omega"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kara.omega"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"

        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

chaquopy {
    defaultConfig {
        version = "3.10"
    }
}
