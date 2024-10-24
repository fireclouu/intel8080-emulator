plugins {
    id("com.android.application")
}

android {
    namespace = "com.fireclouu.intel8080emu"
    compileSdk = 34
    defaultConfig {

        // Uniquely identifies the package for publishing.
        applicationId = "com.fireclouu.intel8080emu"

        // Defines the minimum API level required to run the app.
        minSdk = 23

        // Specifies the API level used to test the app.
        targetSdk = 33

        // Defines the version number of your app.
        versionCode = 4

        // Defines a user-friendly version name for your app.
        versionName = "2.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true // Enables code shrinking for the release build type.
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
