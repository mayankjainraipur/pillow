<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React 19" />
  <img src="https://img.shields.io/badge/Capacitor-8-119EFF?style=for-the-badge&logo=capacitor&logoColor=white" alt="Capacitor 8" />
  <img src="https://img.shields.io/badge/Vite-6-646CFF?style=for-the-badge&logo=vite&logoColor=white" alt="Vite 6" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-orange?style=for-the-badge" alt="License" />
</p>

<h1 align="center">🛏️ Pillow</h1>

<p align="center">
  <strong>A premium, tactile notes application crafted for comfort and security.</strong>
  <br />
  <em>Every pixel designed to feel soft, responsive, and secure — like resting on your favourite pillow.</em>
</p>

<p align="center">
  <a href="#-features">Features</a> •
  <a href="#-screenshots">Screenshots</a> •
  <a href="#%EF%B8%8F-tech-stack">Tech Stack</a> •
  <a href="#-getting-started">Getting Started</a> •
  <a href="#-project-structure">Project Structure</a> •
  <a href="#-architecture">Architecture</a> •
  <a href="#-contributing">Contributing</a> •
  <a href="#-license">License</a>
</p>

---

## ✨ Features

### 📝 Rich Note-Taking
- **Dual-mode editor** — seamlessly switch between freeform text and interactive checklists
- **Auto-save** — notes persist to local storage automatically on every change
- **Smart titles** — auto-generates titles from content when left blank
- **Read time estimation** — word count and estimated reading time displayed in the editor

### 🎙️ Voice Memos
- **In-note voice recording** — capture speech thoughts directly inside any note
- **Audio playback** — listen to attached recordings with a built-in audio player
- **Multiple attachments** — attach as many voice clips as needed per note

### 🔐 PIN Security & Vault
- **4-digit PIN lock** — set up a master PIN to protect the entire app on launch
- **Per-note locking** — mark individual notes as confidential; requires PIN verification to view
- **Session locking** — lock the app at any time without closing it
- **Secure vault view** — browse all locked notes from a dedicated "Confidential Vault" folder

### 🎨 Pastel Theming
Seven carefully curated wallpaper themes for each note:

| Theme | Description |
|-------|-------------|
| 🍦 **Warm Cream** | Cozy and neutral warm tones |
| 🍑 **Cozy Peach** | Soft coral and sunset warmth |
| 🌿 **Fresh Mint** | Cool and calming green tones |
| 💜 **Dreamy Lavender** | Gentle purple and violet hues |
| ☁️ **Breezy Sky** | Light blue, airy atmosphere |
| 🌸 **Dusty Rose** | Muted pink elegance |
| 🌑 **Night Sleep** | Dark mode with charcoal tones |

### 🗂️ Organization
- **Pin notes** to the top of your workspace for quick access
- **Archive** notes to a deep-rest drawer without deleting them
- **Trash** with permanent delete — two-step deletion for safety
- **Tag system** — label notes with custom tags and filter by them instantly
- **Quick-suggestion tags** — predefined labels like *Idea*, *Work*, *Personal*, *Travel*
- **Search** — real-time filtering across titles and content
- **Sort options** — by date (newest/oldest) or alphabetical (A–Z / Z–A)
- **Grid & List views** — toggle between masonry grid and compact list layouts

### 💾 Backup & Restore
- **JSON export** — download a full backup of all notes and settings as a portable `.json` file
- **Import** — restore from a previously exported backup seamlessly

### 🎭 Premium UX
- **Spring-physics animations** powered by Motion (Framer Motion)
- **Slide-in sidebar** with backdrop blur overlay
- **Device frame wrapper** for previewing the mobile experience on desktop
- **Responsive design** — works beautifully on every screen size
- **Glassmorphic UI** — frosted-glass headers and footers with `backdrop-blur`

---

## 📸 Screenshots

> _Screenshots coming soon — build and run the app to experience the full interface._

---

## 🛠️ Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **UI Framework** | React | 19.x |
| **Language** | TypeScript | 5.8 |
| **Build Tool** | Vite | 6.x |
| **Styling** | Tailwind CSS | 4.x |
| **Animations** | Motion (Framer Motion) | 12.x |
| **Icons** | Lucide React | 0.546 |
| **Native Bridge** | Capacitor | 8.x |
| **Platform** | Android (SDK 24–36) | — |
| **AI Integration** | Google GenAI SDK | 2.x |

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Minimum Version |
|-------------|----------------|
| **Node.js** | 18+ |
| **npm** | 9+ |
| **Android Studio** | Latest stable |
| **JDK** | 17+ |
| **Android SDK** | API 36 (compile), API 24 (min) |

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/mayankjainraipur/pillow.git
cd pillow

# 2. Install dependencies
npm install

# 3. Start the development server (web preview)
npm run dev
```

The app will be available at `http://localhost:3000`.

### Building for Android

```bash
# 1. Build the web assets
npm run build

# 2. Sync the build output to the Android project
npx cap sync android

# 3. Open in Android Studio
npx cap open android
```

From Android Studio, run the app on an emulator or a connected device.

### Available Scripts

| Script | Description |
|--------|-------------|
| `npm run dev` | Start the Vite dev server on port 3000 |
| `npm run build` | Production build to `dist/` |
| `npm run preview` | Preview the production build locally |
| `npm run lint` | Type-check with TypeScript (no emit) |
| `npm run clean` | Remove `dist/` and `server.js` artifacts |

---

## 📁 Project Structure

```
pillow/
├── android/                    # Native Android project (Capacitor)
│   ├── app/
│   │   ├── build.gradle        # App-level Gradle config
│   │   └── src/                # Android source & resources
│   ├── build.gradle            # Root Gradle config
│   ├── variables.gradle        # SDK & dependency versions
│   └── ...
├── src/
│   ├── App.tsx                 # Root application — state management & routing
│   ├── main.tsx                # React DOM entry point
│   ├── index.css               # Global stylesheet
│   ├── types.ts                # TypeScript interfaces (Note, AppSettings, etc.)
│   ├── themes.ts               # Pastel theme definitions & seed data
│   └── components/
│       ├── AudioPlayer.tsx     # Voice memo playback widget
│       ├── DeviceFrame.tsx     # Desktop device frame wrapper
│       ├── EditorScreen.tsx    # Full-featured note editor
│       ├── LockScreen.tsx      # PIN entry & security overlay
│       ├── NoteCard.tsx        # Note preview card (grid/list)
│       ├── Sidebar.tsx         # Navigation drawer & settings
│       └── VoiceRecorder.tsx   # Microphone recording interface
├── capacitor.config.ts         # Capacitor configuration
├── vite.config.ts              # Vite build configuration
├── tsconfig.json               # TypeScript compiler options
├── package.json                # Dependencies & scripts
└── metadata.json               # App metadata & capabilities
```

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                    DeviceFrame                       │
│  ┌───────────────────────────────────────────────┐  │
│  │                   App.tsx                      │  │
│  │         (State Management & CRUD Logic)        │  │
│  │                                                │  │
│  │  ┌──────────┐  ┌────────────┐  ┌───────────┐  │  │
│  │  │ Sidebar  │  │  NoteCard  │  │ EditorScr │  │  │
│  │  │  (Nav)   │  │  (Grid)    │  │  (Editor)  │  │  │
│  │  └──────────┘  └────────────┘  └───────────┘  │  │
│  │                                                │  │
│  │  ┌──────────┐  ┌────────────┐  ┌───────────┐  │  │
│  │  │LockScreen│  │VoiceRecord │  │AudioPlayer│  │  │
│  │  │(Security)│  │   (Mic)    │  │ (Playback)│  │  │
│  │  └──────────┘  └────────────┘  └───────────┘  │  │
│  └───────────────────────────────────────────────┘  │
│                                                      │
│  ┌──────────────────────────────────────────────┐   │
│  │           localStorage (Persistence)          │   │
│  └──────────────────────────────────────────────┘   │
│                                                      │
│  ┌──────────────────────────────────────────────┐   │
│  │         Capacitor (Native Android Bridge)     │   │
│  └──────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

### Key Design Decisions

- **Centralized state in `App.tsx`** — all notes, settings, and UI state are managed at the root and passed down via props, keeping the data flow predictable and debuggable.
- **LocalStorage persistence** — notes and settings are synced to `localStorage` on every change via `useEffect`, enabling instant offline access with zero backend dependencies.
- **Capacitor for native delivery** — the web app is wrapped using Capacitor 8, providing native Android capabilities (microphone access, splash screen) while keeping a single codebase.
- **Motion for animations** — spring-physics transitions for sidebar slides, editor overlays, and lock-screen modals create a fluid, native-feeling experience.

---

## 🤝 Contributing

Contributions are welcome! Here's how to get started:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

Please ensure your code:
- Passes type-checking (`npm run lint`)
- Follows existing code style and component patterns
- Includes descriptive commit messages

---

## 📄 License

This project is licensed under the **Apache License 2.0** — see the [LICENSE](LICENSE) file for details.

```
Copyright 2025 Pillow Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

---

<p align="center">
  <sub>Crafted with ☕ and comfort · <strong>Pillow</strong> — rest easy, write freely.</sub>
</p>
