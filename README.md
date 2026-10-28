# 📱 MyPhoneWidgets

Custom home-screen widgets designed specifically for **Samsung Galaxy M35 5G** (Samsung One UI).

---

## 📂 Included Widgets

### 1. [⏳ Days Left (Anti-Procrastination Widget)](./DaysLeftWidget)
* **Goal**: Shows days remaining in the year + percentage gone + blunt anti-procrastination quotes to fight delay habits.
* **Size**: 4×2 / 5×2 grid (~28%–30% screen height, full width) with smooth 26dp curved corners.
* **Battery**: AMOLED deep black, 0% idle battery drain.

### 2. [🖼️ Photo Cycle Widget (Oval Home Screen Frame)](./PhotoWidget)
* **Goal**: Cycles through your favorite photos inside an elegant **Oval frame** (or Circle / Rounded Card) with an obsidian rim bezel.
* **Intervals**: Selectable auto-rotation at **15 min, 20 min, 30 min, 60 min**, 3h, 6h, or Daily.
* **Features**:
  * Multi-image picker from system gallery.
  * Tap on widget to immediately switch to the next photo.
  * In-app Photos Vault to preview, reorder, set active, or remove photos.
  * 1-Tap Home Screen Placement (`requestPinAppWidget`).
* **Size**: 2×2 grid cell (fits side-by-side with 2×2 One UI app folders like NetBanking/UPI).
* **Battery**: 0% idle battery drain using efficient wake-while-idle alarms.

---

## ⚡ Automated Cloud Build (No Android Studio Needed!)

This repository has **GitHub Actions CI/CD** configured for both widgets:
* Every time you push changes to `main`, GitHub automatically builds both debug APKs in the cloud:
  * **`DaysLeftWidget-debug-apk`**
  * **`PhotoWidget-debug-apk`**
* **To download your APKs**:
  1. Open this repository on GitHub (on your phone or PC).
  2. Tap the **Actions** tab at the top.
  3. Tap the latest workflow run for the widget you want (e.g. **Build Photo Cycle Widget APK** or **Build Days Left Widget APK**).
  4. Under **Artifacts**, tap the APK artifact name to download your ready-to-install APK file!
