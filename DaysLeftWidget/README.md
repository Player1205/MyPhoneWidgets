# ⏳ Days Left (Anti-Procrastination Widget)

> Custom Android Widget tailored specifically for **Samsung Galaxy M35 5G** (Samsung One UI).

---

## 📱 Widget Overview

* **Screen Proportion**: Designed for a **4×2** or **5×2** home screen grid cell layout, occupying **~25% to 30% of your screen height** and the **full screen width**.
* **Styling**:
  * **26dp curved corners** aligned with Samsung One UI design guidelines.
  * **10dp side padding** providing clean breathing space from screen bezels for a floating card effect.
  * **Deep AMOLED black** (`#0E1118`) with subtle border highlight (`#232838`) for zero battery drain.
* **Core Features**:
  1. **Days Remaining Counter**: Big, bold typography showing exact days left in the current year.
  2. **Year Progress Bar**: Visual progress bar showing how much of the year has already elapsed (`81.6% GONE`).
  3. **Anti-Procrastination Reality Checks**: Hard-hitting quotes to break through scrolling habits and procrastination whenever you unlock your phone.
  4. **Interactive Tap**: Tapping the counter or refresh icon cycles through 12+ blunt anti-procrastination quotes immediately.
  5. **Auto-Update**: Updates daily at midnight and whenever unlocked/tapped without wasting battery.

---

## 🛠️ Project Structure

```
Phone_Widgets/
└── DaysLeftWidget/
    ├── app/
    │   ├── build.gradle.kts
    │   └── src/main/
    │       ├── AndroidManifest.xml
    │       ├── java/com/antiprocrastination/daysleft/
    │       │   ├── DaysLeftWidgetProvider.kt    <-- Widget logic & countdown math
    │       │   ├── QuoteManager.kt              <-- Reality check quotes & cycle state
    │       │   └── MainActivity.kt              <-- In-app preview & 1-tap "Add to Screen" button
    │       └── res/
    │           ├── drawable/
    │           │   ├── widget_card_bg.xml       <-- 26dp curved corners & border
    │           │   ├── custom_progress_bar.xml  <-- Rounded year progress bar
    │           │   ├── pill_badge_bg.xml        <-- "% GONE" status pill
    │           │   └── quote_bg.xml             <-- Quote container card
    │           ├── layout/
    │           │   ├── widget_days_left.xml     <-- Widget RemoteViews layout
    │           │   └── activity_main.xml        <-- App screen layout
    │           └── xml/
    │               └── widget_days_left_info.xml<-- Dimensions & sizing metadata
    ├── gradle/
    ├── build.gradle.kts
    └── settings.gradle.kts
```

---

## 🚀 How to Build & Install onto Your Samsung M35 5G

### Option 1: Open in Android Studio (Easiest)
1. Open **Android Studio**.
2. Select **Open** and choose this folder:
   `c:\Users\vansh\OneDrive\Desktop\Phone_Widgets\DaysLeftWidget`
3. Click **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
4. Android Studio will generate `app-debug.apk`.
5. Transfer `app-debug.apk` to your phone via:
   * **Quick Share** / **Link Sharing**
   * **USB Cable**
   * **Google Drive** or **WhatsApp Web**
6. Tap the `.apk` file on your Samsung M35 to install it.

### Option 2: 1-Tap Home Screen Placement
Once installed on your phone:
1. Open the **Days Left** app from your app drawer.
2. Tap the **⚡ Add Widget to Home Screen** button.
3. Samsung One UI will prompt you to confirm placement!
4. Resize the width handles to span full width if desired.
