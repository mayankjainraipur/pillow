<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Kotlin-1.9-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-orange?style=for-the-badge" alt="License" />
</p>

<h1 align="center">🛏️ Pillow</h1>

<p align="center">
  <strong>A premium, native Android notes app — comfortable, fast, and secure.</strong>
  <br />
  <em>Local-first storage, biometric app lock, and a Material 3 interface.</em>
</p>

---

## ✨ Features

- **📝 Notes CRUD** — create, read, update, and delete notes
- **🔍 Search** — real-time filtering across titles and content
- **🗂️ Organization** — categories, tags, pinning, and archiving
- **🎨 Pastel themes** — seven per-note wallpaper themes (Warm Cream, Cozy Peach, Fresh Mint, Dreamy Lavender, Breezy Sky, Dusty Rose, Night Sleep)
- **🗑️ Trash** — soft-delete notes to a Trash, then restore them or empty the Trash permanently
- **🎙️ Voice memos** — record and attach audio clips inside a note, with in-app playback
- **🔐 Biometric app lock** — optional fingerprint / device-credential lock on launch
- **🌙 Dark mode** — manual toggle with Material 3 dynamic color (Android 12+)
- **💾 Local-first** — all data stored on-device in a Room (SQLite) database; no network required

> Roadmap features (not yet implemented): per-note PIN vault, and backup & restore.

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + Repository |
| **Local storage** | Room (SQLite) |
| **Dependency Injection** | Hilt |
| **Navigation** | Navigation Compose |
| **Async** | Coroutines + Flow |
| **Authentication** | AndroidX Biometric |
| **Preferences** | DataStore |
| **Min / Target SDK** | API 24 (Android 7.0) / API 36 |

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|-------------|---------|
| **Android Studio** | Latest stable |
| **JDK** | 17+ (Android Studio's bundled JBR works) |
| **Android SDK** | Platform 36 (compile/target), API 24 (min) |

### Build & Run

```bash
# Create local.properties pointing at your Android SDK, e.g.:
#   sdk.dir=C:\\Users\\<you>\\AppData\\Local\\Android\\Sdk

# Build a debug APK
./gradlew assembleDebug

# Install on a connected device / running emulator
./gradlew installDebug

# Run unit tests
./gradlew testDebugUnitTest
```

Or open the project in Android Studio and press **Run**. See [SETUP.md](SETUP.md) for a detailed environment guide.

---

## 📁 Project Structure

```
app/src/main/java/com/pillow/
├── audio/            # AudioRecorder — MediaRecorder wrapper for voice memos
├── biometric/        # BiometricAuthManager — fingerprint / device-credential lock
├── data/
│   ├── db/           # Room entities, DAOs, and the database
│   └── repository/   # Note / Category / Tag / VoiceMemo repositories
├── di/               # Hilt modules (DatabaseModule)
├── domain/model/     # Domain models
├── presentation/
│   └── viewmodel/    # NoteViewModel, CategoryViewModel, SettingsViewModel, VoiceMemoViewModel
├── ui/
│   ├── navigation/   # PillowNavGraph
│   ├── screen/       # HomeScreen, NoteEditorScreen, SettingsScreen, TrashScreen, VoiceMemoSection
│   ├── theme/        # Color, Theme, Typography, NoteTheme (Material 3)
│   └── MainActivity.kt
└── PillowApp.kt      # Hilt application entry point
```

---

## 📄 License

Licensed under the **Apache License 2.0** — see [LICENSE](LICENSE).
