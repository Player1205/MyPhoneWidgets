# 🖼️ Photo Cycle Widget (Curved Home Screen Frame)

> Custom Android Photo Widget designed specifically for **Samsung Galaxy M35 5G** (Samsung One UI).
> Displays your favorite photos filling the full **2×2 widget area** inside a curved rectangle (with authentic 28dp One UI rounded corners), cycling automatically through your chosen photos at configurable intervals (15 min, 20 min, 30 min, 60 min, etc.).

---

## 📱 Widget Overview

* **Screen Proportion**: Perfectly sized for a **2×2 home screen grid cell layout** (matching standard Samsung One UI folder/widget dimensions like NetBanking/UPI edge-to-edge).
* **Styling & Aesthetics**:
  * **Curved Rectangle (One UI 28dp)**: Fills the 2×2 widget area with authentic 28dp rounded corners.
  * **Golden Border Rim**: Luxury double-edge golden border with champagne highlight (`#FFB703` + `#FFE58F`).
  * **Soft Ambient Glow**: Elegant golden backlight aura emanating smoothly behind the widget perimeter (subtle, non-overpowering opacity).
  * **Edge-to-Edge Center Crop**: Your photo fills the full card area smoothly with zero letterboxing.
  * **High-DPI Anti-Aliased Rendering**: Software/Canvas center-cropped bitmap rendering eliminates OEM launcher distortion or low-resolution scaling bugs.
* **Core Features**:
  1. **Multi-Photo Selection**: Select multiple images at once directly from your device gallery.
  2. **Configurable Rotation Intervals**:
     * **15 Minutes** (User Preset)
     * **20 Minutes** (User Preset)
     * **30 Minutes** (User Preset)
     * **60 Minutes (1 Hour)** (User Preset)
     * **3 Hours**, **6 Hours**, or **24 Hours (Daily)**
  3. **Instant Tap Rotation**: Tapping the widget on your home screen immediately advances to the next photo in your rotation with a subtle toast indicator (`Photo 2 of 5`).
  4. **Photos Vault Management**:
     * Live preview card in-app with next/previous controls.
     * Horizontal thumbnail gallery showing all photos in rotation.
     * **"Set Active"** button to jump directly to any photo on your home screen.
     * Individual photo deletion (`✕`).
  5. **0% Idle Battery Drain**:
     * Uses Android `AlarmManager` with wake-while-idle alarms rather than battery-draining continuous background services.
     * Survives phone restarts with automatic `BOOT_COMPLETED` rescheduling.

---

## 🛠️ Project Structure

```
Phone_Widgets/
└── PhotoWidget/
    ├── app/
    │   ├── build.gradle.kts
    │   └── src/main/
    │       ├── AndroidManifest.xml
    │       ├── java/com/phone/photowidget/
    │       │   ├── MainActivity.kt           <-- Photos vault, interval picker & 1-tap "Add to Screen"
    │       │   ├── PhotoWidgetProvider.kt    <-- AppWidgetProvider, RemoteViews & tap-to-cycle action
    │       │   ├── PhotoManager.kt           <-- Local private image storage & preferences
    │       │   ├── PhotoBitmapRenderer.kt    <-- Anti-aliased Oval/Circle/Rounded canvas renderer
    │       │   ├── PhotoCycleScheduler.kt    <-- AlarmManager interval scheduling (15, 20, 30, 60 min)
    │       │   ├── PhotoCycleReceiver.kt     <-- Periodic rotation receiver
    │       │   └── BootReceiver.kt           <-- Auto-reschedule on device reboot
    │       └── res/
    │           ├── drawable/                 <-- Vector icons, dark cards, interval chips
    │           ├── layout/
    │           │   ├── activity_main.xml     <-- In-app vault & preview layout
    │           │   ├── item_photo_card.xml   <-- Thumbnail card layout
    │           │   └── widget_photo.xml      <-- 2x2 home screen RemoteViews layout
    │           ├── values/
    │           │   ├── colors.xml            <-- AMOLED dark palette
    │           │   ├── strings.xml           <-- UI strings
    │           │   └── themes.xml            <-- Dark Material theme
    │           └── xml/
    │               └── widget_photo_info.xml <-- 2x2 dimensions & sizing metadata
    ├── gradle/
    ├── build.gradle.kts
    └── settings.gradle.kts
```

---

## 🚀 How to Build & Install onto Your Samsung M35 5G

### Option 1: Automated Cloud Build (No Android Studio Needed!)
1. Push this repository to **GitHub**.
2. Go to the **Actions** tab on your GitHub repository.
3. Select the workflow: **Build Photo Cycle Widget APK**.
4. Once completed, download **`PhotoWidget-debug-apk`** under the **Artifacts** section!
5. Tap the downloaded `.apk` on your Samsung Galaxy M35 5G to install it.

### Option 2: Android Studio
1. Open **Android Studio**.
2. Select **Open** and choose:
   `c:\Users\vansh\OneDrive\Desktop\Phone_Widgets\PhotoWidget`
3. Click **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
4. Android Studio will generate `app-debug.apk`.
5. Transfer and install on your phone.

---

## ⚡ 1-Tap Home Screen Placement
Once installed on your phone:
1. Open the **Photo Cycle Widget** app from your app drawer.
2. Tap **"Add Photos"** to pick your photos from your gallery.
3. Select your desired interval (e.g. **15 Min**, **20 Min**, **30 Min**, or **60 Min**).
4. Tap the **⚡ Add Widget to Home Screen** button.
5. Confirm the Samsung One UI prompt to place your 2×2 oval widget!
