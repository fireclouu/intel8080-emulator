plugins {
    id("com.android.application")
}

android {
    namespace = "com.fireclouu.intel8080emu"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.fireclouu.intel8080emu"
        minSdk = 23
        targetSdk = 34
        versionCode = 4
        versionName = "alpha-0.4"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    implementation(project(":spaceinvaders"))
    implementation("androidx.annotation:annotation:1.9.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}