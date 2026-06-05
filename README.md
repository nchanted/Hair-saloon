# ✂️ Salon Tycoon

A mobile management game for Android. Run a hair salon: seat walk-in clients, assign your stylists, keep everyone happy before their patience runs out, and reinvest your profits. Every day the clients arrive faster, expect more, and lose patience quicker — survive long enough and you can renovate your way from a cramped Corner Shop up to a Luxury Spa.

Built with **Kotlin + Jetpack Compose**. No game engine, no external assets — it compiles to a single small APK.

---

## ⚠️ Important: why there's no APK in this folder

This project was generated in a sandbox **with no internet access**, so the Android SDK and Gradle dependencies could not be downloaded and the APK could **not** be compiled here. That's expected and fine — the project is complete and correct, and it's wired to build itself automatically on GitHub (which *does* have network access). See the build options below.

---

## 🎮 How to play

- Clients walk in and wait in the queue at the bottom. **Tap a waiting client** to seat them at a free chair.
- Your best free, rested stylist takes them automatically.
- Each client has a **patience bar** — if it empties before they're seated, they storm out and your **reputation** drops.
- Finish a service at **quality ≥ their expectation** to earn the full fee plus a tip and a reputation bump. Fall short and you get paid less and lose reputation.
- Stylists **tire as they work** and recover while idle. Don't run a one-person show.
- At the end of each day you pay **rent + wages**. Spend whatever's left in the **Shop**.
- If reputation hits **0**, or you go **bankrupt**, it's game over.
- **Late game:** once your reputation is high enough, **Renovate** to a bigger tier. Higher tiers mean pricier clients, more chairs, a higher reputation ceiling, and premium services (Updos, Makeovers).

### Difficulty curve
Clients spawn faster, expect higher quality, and lose patience more quickly with each passing day — so you must keep hiring, training, and upgrading just to keep up.

---

## 🛠 Build options

### Option A — Build locally in Android Studio (easiest)
1. Install [Android Studio](https://developer.android.com/studio) (Hedgehog or newer).
2. **File → Open** and select this project folder.
3. Android Studio will sync Gradle and generate the Gradle wrapper automatically.
4. Click **Run** ▶ to install on an emulator or a connected device, or **Build → Build Bundle(s) / APK(s) → Build APK(s)** to get an installable APK.

### Option B — Build locally from the command line
You need a JDK 17 and a local Gradle 8.7 install (or let Android Studio generate the wrapper first).
```bash
gradle wrapper --gradle-version 8.7   # one-time, generates ./gradlew
./gradlew assembleDebug                # outputs app/build/outputs/apk/debug/app-debug.apk
```

### Option C — Build the APK on GitHub (no local tools needed)
This repo ships with a GitHub Actions workflow that builds the APK in the cloud.
1. Create a new GitHub repository and push this project to it:
   ```bash
   git init
   git add .
   git commit -m "Salon Tycoon"
   git branch -M main
   git remote add origin https://github.com/<you>/<repo>.git
   git push -u origin main
   ```
2. Go to the **Actions** tab. The **Build Debug APK** workflow runs automatically on every push (and can be run manually via **Run workflow**).
3. When it finishes, open the run and download the **`salon-tycoon-debug-apk`** artifact. Unzip it and install the `.apk` on your phone (enable "Install unknown apps").

---

## 🏪 Publishing to the Google Play Store

The Play Store requires a **signed** build (an `.aab` App Bundle). The included `release.yml` workflow builds and signs both an APK and an AAB for you when you push a version tag.

### 1. Create a signing keystore (one time, keep it safe forever)
```bash
keytool -genkey -v -keystore salon-tycoon.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias salon
```
You'll choose a **keystore password**, a **key password**, and the **alias** (`salon` above). Remember all three.

> ⚠️ Never commit the `.jks` file. If you lose it you can't update your app on Play. Back it up.

### 2. Base64-encode the keystore so it can live in a GitHub secret
```bash
base64 -i salon-tycoon.jks | tr -d '\n' > keystore.b64   # macOS/Linux
# or: base64 -w 0 salon-tycoon.jks > keystore.b64        # Linux (GNU)
```

### 3. Add four repository secrets
In your GitHub repo: **Settings → Secrets and variables → Actions → New repository secret**. Add:

| Secret name           | Value                                        |
|-----------------------|----------------------------------------------|
| `SIGNING_KEY`         | the full contents of `keystore.b64`          |
| `KEY_ALIAS`           | your alias (e.g. `salon`)                     |
| `KEY_STORE_PASSWORD`  | the keystore password                        |
| `KEY_PASSWORD`        | the key password                             |

### 4. Tag a release to trigger the signed build
```bash
git tag v1.0.0
git push origin v1.0.0
```
The **Release (Signed APK + AAB)** workflow runs, signs the build, and uploads both the signed `.apk` and the signed `.aab` as artifacts (and attaches them to a GitHub Release).

### 5. Upload to Play
1. Create the app in the [Google Play Console](https://play.google.com/console).
2. Download the signed **`.aab`** artifact from the workflow run.
3. Create a release in your chosen track (internal testing is a good start) and upload the `.aab`.
4. Fill out the store listing, content rating, and data-safety forms, then roll out.

> Tip: bump `versionCode` (and usually `versionName`) in `app/build.gradle.kts` before each new upload — Play rejects duplicate version codes.

---

## 📁 Project structure
```
HairSalonTycoon/
├─ app/
│  ├─ build.gradle.kts            # module config + Compose dependencies
│  ├─ proguard-rules.pro
│  └─ src/main/
│     ├─ AndroidManifest.xml
│     ├─ java/com/hairsalon/tycoon/
│     │  ├─ MainActivity.kt
│     │  ├─ game/                 # pure game logic (no Android UI)
│     │  │  ├─ Models.kt          # Client, Stylist, ServiceType, etc.
│     │  │  ├─ Balance.kt         # all tuning: economy + difficulty curve
│     │  │  ├─ GameState.kt       # immutable snapshot of the whole game
│     │  │  ├─ GameEngine.kt      # pure state transitions (tick, seat, shop…)
│     │  │  └─ GameViewModel.kt   # real-time loop + state holder
│     │  └─ ui/                   # Jetpack Compose screens
│     │     ├─ GameApp.kt         # menu / game-over / router
│     │     ├─ GameScreen.kt      # the live salon floor
│     │     ├─ ShopScreen.kt      # between-days upgrades
│     │     └─ theme/             # colors + Material 3 theme
│     └─ res/                     # icons, strings, theme
├─ gradle/wrapper/                # pins Gradle 8.7
├─ settings.gradle.kts
├─ build.gradle.kts               # root
└─ .github/workflows/
   ├─ build.yml                   # debug APK on every push
   └─ release.yml                 # signed APK + AAB on version tag
```

## 🧱 Tech / versions
- Kotlin 1.9.24, Android Gradle Plugin 8.5.2, Gradle 8.7
- Jetpack Compose (BOM 2024.06.00), Material 3
- compileSdk/targetSdk 34, minSdk 26
- Package: `com.hairsalon.tycoon`

Enjoy running your salon! 💇
