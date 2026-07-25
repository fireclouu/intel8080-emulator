plugins {
    application
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    implementation(project(":core"))
    runtimeOnly(project(":assets"))
}

application {
    mainClass = "com.fireclouu.intel8080.spaceinvaders.Main"
}