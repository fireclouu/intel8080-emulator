# Space Invaders - Intel 8080 emulator
Playable Space Invaders machine emulation with **Intel 8080 CPU interpreter**, written in Java.\
\
[![Android CI](https://github.com/fireclouu/space_invaders_android/actions/workflows/android.yml/badge.svg?branch=master)](https://github.com/fireclouu/space_invaders_android/actions/workflows/android.yml)

---

### Screenshot
<img src="https://i.ibb.co/w7NhRXJ/Screenshot-2024-06-08-11-14-13-913-com-fireclouu-spaceinvadersemu.jpg" width="200"/>

### Platform
[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://github.com/fireclouu/space_invaders_intel_8080_emu/releases/download/v2.0/app-release.apk)

```text
Version 2.0
- Added Player 2 support by fixing and rewriting interrupt handler
- fixes thread issues and input stream handling
- Improved stability when suspending application
- automatically scale and position to wider screen
- MMU feature implemented
- fixed persistent highscore feature
- fixed button drawable statelists
- rewrite and optimized surface draw function
- accurately mapping VRAM pixel points to host
- dyanmic orientation changes
- performance boost by fixing incorrect hardware canvas implementation
- correct aspect ratio display on dynamic windows
- smoother canvas blits
```
```text
Version 1.3
Android
- fixes to few deprecated libraries
- optimizations by removing unused flags
- fixed adaptive display size
```
```text
Version 0.1
Android
- Initial release
- Supports Android 10 and up
```
> [!TIP]
> You can also get latest builds via [Actions](https://github.com/fireclouu/space_invaders_android/actions) tab.

### Assets
- [Sound assets](https://samples.mameworld.info/)
- [Button face Graphics assets](https://ya-webdesign.com)

### Resources
- [emulator101](http://emulator101.com/)
- [superzazu](https://github.com/superzazu/8080)
- <a href="https://www.flaticon.com/free-icons/space-invaders" title="space invaders icons">Space invaders icons created by IconMark - Flaticon</a>
