# Setup Guide for Pillow Notes App

This guide will help you set up and run the Pillow Notes Android app on your development machine.

## Prerequisites

### Required Software

1. **Android Studio** (latest stable recommended)
   - Download from: https://developer.android.com/studio
   - Bundles a compatible JDK (JBR) and the Gradle plugin

2. **Android SDK**
   - Minimum API 24 (Android 7.0)
   - Compile / Target API 36
   - Components needed:
     - Android SDK Platform 36
     - Android SDK Build-Tools 36.x.x
     - Android Emulator (optional, for testing)

3. **Java Development Kit (JDK)**
   - JDK 17 or newer (Android Studio's bundled JBR works out of the box)
   - Only set JAVA_HOME if you build from the command line outside Android Studio

4. **Gradle**
   - Provided by the Gradle wrapper (`./gradlew`) — version 8.11.1, no separate install needed

## Installation Steps

### Step 1: Install Android Studio

1. Download Android Studio from the official website
2. Run the installer and follow the setup wizard
3. Select "Standard Installation" (includes SDK)
4. Complete the installation

### Step 2: Configure Android SDK

1. Open Android Studio
2. Go to **File → Settings → Appearance & Behavior → System Settings → Android SDK**
3. In the "SDK Platforms" tab:
   - Check **Android 16 (API 36)** ✓
   - Also check **Android 7.0 (API 24)** for compatibility

4. In the "SDK Tools" tab, ensure these are installed:
   - Android SDK Build-Tools 36.x.x
   - Android Emulator
   - Android SDK Platform-Tools
   - Kotlin Plugin

5. Click **Apply** and wait for downloads to complete

### Step 3: Set Up Java Environment

1. **On Windows**:
   - Right-click "This PC" → Properties
   - Click "Advanced system settings"
   - Click "Environment Variables"
   - Click "New" under "System variables"
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Java\jdk-17` (adjust path to your JDK)
   - Click OK

2. **On macOS/Linux**:
   - Open terminal
   - Edit ~/.zshrc or ~/.bash_profile:
     ```bash
     export JAVA_HOME=$(/usr/libexec/java_home -v 17)
     ```
   - Run: `source ~/.zshrc` or `source ~/.bash_profile`

### Step 4: Configure Local Properties

1. Navigate to the project root directory in terminal/command prompt
2. Edit or create `local.properties`:
   ```properties
   sdk.dir=/path/to/android/sdk
   ```

   **Examples:**
   - Windows: `sdk.dir=C:\\Users\\YourName\\AppData\\Local\\Android\\sdk`
   - macOS: `sdk.dir=/Users/YourName/Library/Android/sdk`
   - Linux: `sdk.dir=/home/YourName/Android/sdk`

### Step 5: Build the Project

1. Open the project in Android Studio
2. Go to **Build → Make Project** or press `Ctrl+F9`
3. Wait for the build to complete (first build takes time due to dependency downloads)
4. Check for any errors in the Build window

### Step 6: Run on Emulator

1. **Create an Android Virtual Device (AVD)**:
   - Go to **Tools → Device Manager**
   - Click **Create Device**
   - Choose a device (e.g., Pixel 7)
   - Select an Android system image (API 24–36; API 36 recommended)
   - Click Finish

2. **Launch the Emulator**:
   - Select the created device in Device Manager
   - Click the play button to start the emulator

3. **Run the App**:
   - Click **Run → Run 'app'** or press `Shift+F10`
   - Select the running emulator
   - Click OK

### Step 7: Run on Physical Device

1. **Enable Developer Mode**:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Developer options should appear in Settings

2. **Enable USB Debugging**:
   - Go to Settings → Developer Options
   - Enable "USB Debugging"

3. **Connect via USB**:
   - Connect your device to your computer via USB
   - Allow USB debugging when prompted on the device

4. **Run the App**:
   - Click **Run → Run 'app'** in Android Studio
   - Select your physical device
   - Click OK

## Quick Start (Running the App Day-to-Day)

Once the initial setup is done, these are the only 3 commands you need each session.

> Run these in PowerShell from the project root. Keep the emulator window open while you work.

**Step 1 — Start the emulator** (leave this terminal open):
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -avd Pixel_9 -gpu host -no-snapshot-load
```

Wait ~30–60 seconds for the phone to fully boot.

**Step 2 — Build and install the app** (in a new terminal):
```powershell
.\gradlew installDebug
```

**Step 3 — Launch the app:**
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell monkey -p com.pillow -c android.intent.category.LAUNCHER 1
```

After making code changes, just re-run steps 2 and 3 — no need to restart the emulator.

**To watch live logs:**
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" logcat
```

> **Easier alternative:** Open the project in Android Studio and press the green Run button — it does all 3 steps in one click and shows logs in the Logcat panel automatically.

---

## Gradle Commands

### Building

```bash
# Clean build
./gradlew clean build

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing configuration)
./gradlew assembleRelease

# Build Android App Bundle (for Google Play)
./gradlew bundleRelease
```

### Testing

```bash
# Run unit tests
./gradlew test

# Run instrumentation tests on emulator/device
./gradlew connectedAndroidTest

# Run with code coverage
./gradlew testDebugUnitTestCoverage
```

### Installation

```bash
# Install debug APK on connected device
./gradlew installDebug

# Uninstall app
./gradlew uninstallDebug
```

### Other Useful Commands

```bash
# List all available tasks
./gradlew tasks

# Check Gradle version
./gradlew --version

# Update Gradle wrapper
./gradlew wrapper --gradle-version 8.11.1
```

## Troubleshooting

### Issue: "SDK location not found"

**Solution:**
- Create `local.properties` in project root
- Add: `sdk.dir=/path/to/android/sdk`
- Rebuild project

### Issue: "JAVA_HOME not set"

**Solution:**
```bash
# On Windows (PowerShell):
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# On macOS/Linux:
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### Issue: "Gradle sync failed"

**Solution:**
1. Click "Sync Now" in the notification banner
2. Or go to **File → Sync Project with Gradle Files**
3. If still fails, try:
   ```bash
   ./gradlew clean
   ./gradlew build
   ```

### Issue: "Build fails with 'Module 'kotlin-stdlib' has been compiled with newer compiler"

**Solution:**
- Update Kotlin plugin: **Tools → Kotlin → Configure Kotlin Plugin Updates**
- Or update Android Studio to the latest version

### Issue: "Cannot connect to emulator"

**Solution:**
1. Restart the emulator
2. Run: `adb kill-server && adb start-server`
3. Check device list: `adb devices`

### Issue: App screen is completely black on emulator

The Pixel_9 AVD sometimes renders the app surface pure black due to a GPU snapshot bug — the layout is fine but nothing draws. This is not an app bug.

**Fix:** Always cold-boot the emulator with host GPU (see Quick Start below). Or in Android Studio: Device Manager → Pixel_9 → Cold Boot Now, and set Graphics = Hardware in AVD settings.

### Issue: "Biometric features not available on emulator"

**Solution:**
- Use a physical device with fingerprint sensor for full testing
- Or enable AVD fingerprint in emulator settings if available

## IDE Configuration

### Android Studio Optimization

1. **Increase Memory**:
   - Help → Edit Custom VM Options
   - Increase: `-Xmx4096m` (adjust based on your RAM)

2. **Enable Instant Run**:
   - File → Settings → Build, Execution, Deployment → Instant Run
   - Check "Enable Instant Run"

3. **Faster Gradle Builds**:
   - File → Settings → Build, Execution, Deployment → Compiler
   - Check "Compile independent modules in parallel"

## Next Steps

Once setup is complete:

1. Read the [README.md](README.md) for the project overview and structure
2. Explore the code structure in `app/src/main/java/com/pillow/`
3. Run the app and test core features:
   - Create a note (data persists in the Room database)
   - Search notes and apply a pastel theme
   - Move a note to Trash, then restore it
   - Record a voice memo (physical device — needs a microphone)
   - Enable dark mode
   - Test fingerprint lock (on physical device)

## Additional Resources

- [Android Developer Documentation](https://developer.android.com/docs)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose/documentation)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Material Design 3](https://m3.material.io/)

## Support

For issues:
1. Check Android Studio logcat for error messages
2. Run `./gradlew build -info` for detailed build information
3. Search Android documentation for specific error codes
4. Create an issue in the project repository

---

**Happy coding! 🚀**
