plugins {
    id("com.android.application")
}

android {
    namespace = "com.fireclouu.intel8080emu"
    compileSdk = 36
    ndkVersion = "25.2.9519653"

    defaultConfig {
        applicationId = "com.fireclouu.intel8080emu"
        minSdk = 23
        targetSdk = 36
        versionCode = 6
        versionName = "alpha-0.6"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("../native/CMakeLists.txt")
            version = "3.22.1"
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
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
