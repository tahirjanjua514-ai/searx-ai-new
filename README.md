# SEARX AI — All-In-One Social Media Video Downloader & Media Hub

**SEARX AI** is a professional Android application built using Kotlin, Jetpack Compose, Material 3, AndroidX Media3 (ExoPlayer), Room Database, and DataStore Preferences. It provides ultra-fast, secure video analysis and authorized media downloads for **YouTube**, **TikTok**, **Facebook**, and **Instagram**, with automatic tracking beacon removal, multi-resolution selection, download manager, and a built-in ExoPlayer.

---

## 🚀 Key Features

1. **Smart Link Detection & Platform Routing**:
   - Detects clipboard video links upon tapping the **Paste** button.
   - Cleans tracking parameters (`utm_*`, `si`, `igsh`, `mibextid`, `is_from_webapp`).
   - Automatically identifies target platform (YouTube, TikTok, Facebook, Instagram).

2. **Multi-Quality Download Engine**:
   - 1080p Full HD, 720p HD, 480p SD, and Audio-Only (MP3/M4A) options.
   - Multi-threaded chunked downloads with pause, resume (HTTP Range header), and retry support.
   - Real-time download metrics: percentage, bytes transferred, speed (MB/s), and estimated time remaining (ETA).

3. **Media Library & Downloads Manager**:
   - Tabbed management: **Active**, **Completed**, and **Failed**.
   - In-app search and multi-criteria sorting (Newest Date, Largest Size, Alphabetical).
   - Media actions: Play, Share (via FileProvider), Retry, and Delete.
   - Storage usage calculator and one-tap history cleanup.

4. **Built-in Media3 ExoPlayer**:
   - High-performance playback of downloaded media and live streams.
   - Custom overlay controls: Play/Pause, ±10-second seek, continuous progress scrubber.
   - Variable playback speeds: `0.5x`, `0.75x`, `1.0x`, `1.25x`, `1.5x`, `2.0x`.
   - Fullscreen landscape mode orientation toggle.

5. **Futuristic Cyberpunk / Modern UI**:
   - Deep navy, electric blue, and neon cyan cybernetic aesthetics.
   - Dark mode & Light mode support.
   - Dual language localization: **English** & **Urdu (اردو)**.
   - Wi-Fi only download protection toggle.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework:** Jetpack Compose with Material Design 3 (M3)
- **Programming Language:** Kotlin 2.2.10
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Local Database:** Room Database 2.7.0 (with KSP)
- **Preferences:** AndroidX DataStore Preferences
- **Media Playback:** AndroidX Media3 ExoPlayer 1.5.1
- **Image Loading:** Coil 2.7.0
- **Network Engine:** OkHttp 4.10.0 + Retrofit 2.12.0
- **Concurrency:** Kotlin Coroutines & Flow

---

## 📱 How to Build and Export the APK

### 1. Build Debug APK
To compile and assemble the debug APK locally:
```bash
gradle :app:assembleDebug
```
The resulting APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Run Local Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```

### 3. Build Release APK / App Bundle (AAB)
To generate a release build ready for signing:
```bash
gradle :app:assembleRelease
# Or for Google Play App Bundle:
gradle :app:bundleRelease
```
Outputs are generated in `app/build/outputs/apk/release/` and `app/build/outputs/bundle/release/`.

---

## 🔒 Privacy & Legal Compliance

SEARX AI adheres strictly to platform policies and copyright frameworks:
- **No DRM Circumvention:** Encrypted, digital-rights-managed, or paid content cannot be decrypted or bypassed.
- **Zero Background Tracking:** Clipboard data is never inspected silently in the background; it is read only when the user explicitly triggers the **Paste** action.
- **Local Storage Isolation:** Saved videos remain on user's device and are not uploaded to secondary servers without express consent.
- **Authentication Safety:** Never requests social media credentials or session cookies.
