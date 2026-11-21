<p align="center">
  <img src="docs/logo.png" alt="SoftPower logo" width="140"/>
</p>

<h1 align="center">SoftPower</h1>

<p align="center">
  Wake your Android screen with a double press of the volume-up button, no power button needed.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Android"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin"/>
  <a href="https://github.com/omarmadany/softpower/releases/latest">
    <img src="https://img.shields.io/github/v/release/omarmadany/softpower?label=Download" alt="Latest release"/>
  </a>
</p>

---

## About

**SoftPower** is a lightweight Android app that lets you turn the screen on by pressing **Volume Up twice**. It is built for people whose power button is worn out, broken, or hard to reach, and for anyone who wants to protect it from daily wear.

It runs as an Accessibility Service, so it works in the background without a heavy footprint, and it uses no internet connection and collects no data.

## Demo

<p align="center">
  <img src="docs/demo.gif" alt="SoftPower demo" width="280"/>
</p>

## How it works

1. Enable **SoftPower** in *Settings > Accessibility*.
2. The service listens for hardware key events.
3. When it detects two Volume Up presses within a short time window, it wakes the screen using `PowerManager`.

## Tech Stack

| Tool | Purpose |
|------|---------|
| Kotlin | Main language |
| Android Studio | IDE |
| AccessibilityService API | Listening to key events in the background |
| PowerManager | Waking the screen |
| Gradle | Build system |
| Git and GitHub | Version control |

## Project Structure

```
softpower/
├── app/
│   └── src/main/
│       ├── java/com/softpower/
│       │   └── service/
│       │       └── PowerAccessibilityService.kt   # Core logic: double-press detection and wake
│       ├── res/                                    # Layouts, strings, icons, accessibility config
│       └── AndroidManifest.xml                     # Service declaration and permissions
├── docs/                                           # Logo and demo GIF
├── build.gradle.kts
└── README.md
```

## Download and Try

1. Go to the [**Releases page**](https://github.com/omarmadany/softpower/releases/latest).
2. Download the latest `.apk` file.
3. Open it on your phone and allow installation from unknown sources if asked.
4. Open the app and enable the service from *Settings > Accessibility > SoftPower*.
5. Turn off the screen, then press **Volume Up twice**.

## Build from Source

```bash
git clone https://github.com/omarmadany/softpower.git
cd softpower
```

Open the project in Android Studio, let Gradle sync, then press **Run**.

## Contact

Built by **Omar Madany**.

- Email: [omar.madanni@gmail.com](mailto:omar.madanni@gmail.com)
- WhatsApp: [wa.me/201021374317](https://wa.me/201021374317)
