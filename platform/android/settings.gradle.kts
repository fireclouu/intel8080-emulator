pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
}

dependencyResolutionManagement {

    /**
      * The dependencyResolutionManagement.repositories
      * block is where you configure the repositories and dependencies used by
      * all modules in your project, such as libraries that you are using to
      * create your application. However, you should configure module-specific
      * dependencies in each module-level build.gradle file. For new projects,
      * Android Studio includes Google's Maven repository and the Maven Central
      * Repository by default, but it does not configure any dependencies (unless
      * you select a template that requires some).
      */

}
rootProject.name = "Space Invaders"
include(":app")
include(":spaceinvaders")
project(":spaceinvaders").projectDir = file("../../spaceinvaders")
