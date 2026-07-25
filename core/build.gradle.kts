plugins {
  `java-library`
}

group = "com.fireclouu"

repositories {
  mavenCentral()
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 11
  options.encoding = "UTF-8"
}