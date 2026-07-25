<img width="1188" height="763" alt="image" src="https://github.com/user-attachments/assets/cffb4361-cc82-4029-81d3-f3c17200f942" /># Intel 8080
[![Android CI](https://github.com/fireclouu/space_invaders_android/actions/workflows/android.yml/badge.svg?branch=master)](https://github.com/fireclouu/space_invaders_intel_8080_emu/actions/workflows/android.yml)

Functional **Intel 8080 CPU** emulation running **Space Invaders** arcade game system, written in **Java**, with built-in graphical debug features intended for multiplatform systems support in future.

## Screenshots

Android
<img width="300" alt="android-1" src="https://github.com/user-attachments/assets/7d8c1c05-9644-4210-8b8e-bdd2a4ab809f" />
<img width="300" alt="android-2" src="https://github.com/user-attachments/assets/8b35ddc7-0ba2-422f-997b-cce66c0d35ff" />

Terminal
<img width="300" alt="terminal" src="https://github.com/user-attachments/assets/c49f2507-cf94-4a41-a817-7604517d6bd4" />


## Building
Clone this repository:

```
git clone --recurse-submodules https://github.com/fireclouu/intel8080-emulator
```

and follow instructions below for specific platform.

## Requirements

Your machine should install the following:

- Java 17
- For Android: SDK 26, NDK 25.x
- Gradle 9.x

### Android

```sh
WITH_ANDROID=1 ./gradlew assembleDebug
```
It will automate the initial build process for you, assuming you have proper tooling installed.

### Terminal (tests)

```sh
./gradlew :platform:terminal:run [--args="/assets/tests/(test-file)"]
```

It will run shipped test suite files located at `assets/tests` directory of `assets` project.

## Docker

I personally recommend to build files thru docker. It is easy to setup, and separates properly from my host machine instead of spinning new VM. The `docker-compose.yml` on this repo is mainly for my gradle inits.

## Download
Binaries are available in [Releases](https://github.com/fireclouu/intel_8080_java/releases) tab. You can also get latest builds via [Actions](https://github.com/fireclouu/space_invaders_intel_8080_emu/actions) tab.

## Resources

- [Sound assets](https://samples.mameworld.info/)
- [Button assets](https://ya-webdesign.com)
- [emulator101](http://emulator101.com/)
- [superzazu](https://github.com/superzazu/8080)
- [imgui](https://github.com/ocornut/imgui)
- <a href="https://www.flaticon.com/free-icons/space-invaders" title="space invaders icons">Space
  invaders icons created by IconMark - Flaticon</a>

