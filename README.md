# 🔮 Magic Fruit Radar

**[English](README.md) | [Chinese](README_CN.md)**

> **Your Traveling-Merchant Alarm for Roco Kingdom: World** — add the items you want to your wishlist, and get a system notification (sound + vibration) the moment they appear on the in-game merchant's shelf, so you never miss a rare drop again.

<p align="center">
  <img src="https://img.shields.io/badge/version-v0.3.1-blue" alt="version">
  <img src="https://img.shields.io/badge/Android-8.0%2B-green" alt="android">
  <img src="https://img.shields.io/badge/Kotlin-1.9-blueviolet" alt="kotlin">
  <img src="https://img.shields.io/badge/license-MIT-yellow" alt="license">
</p>

---

## 📖 Introduction

Magic Fruit Radar is a lightweight, **offline-first** Android alarm app for the game *Roco Kingdom: World*. The in-game **Traveling Merchant** restocks 4 times a day (08:00 / 12:00 / 16:00 / 20:00 Beijing Time) with a small, random inventory and short sale windows — rare items like the Prism Orb, Blessing Necklace or Shiny Pet Egg can vanish in minutes. This app watches the merchant's shelf in the background, matches it against your **wishlist**, and pushes a notification the instant a wished item is in stock.

The app bundles the **full item-name atlas (2,500+ items, works fully offline)**; item **icons** are downloaded on demand from the official wiki resources (free, no credits consumed) and cached locally — they **survive app updates**. The launcher icon is a hand-drawn magic fruit. A clickable **web prototype** is included for product validation.

---

## ✨ Features

### 📱 Android App (v0.3.1 · versionCode 11)

| Feature | Description |
|---|---|
| **Wishlist with built-in atlas** | 2,500+ item *names* browsable/searchable offline; default wishlist pre-loaded; per-item on/off switch |
| **Item icons on demand** | The APK bundles **no icons** (small install). Icons are downloaded **free** from the official wiki resources and cached in the app data dir; they **survive app updates** (removed only on uninstall). A one-time dialog prompts icon download after the first **valid** API key; missing icons fall back to name + emoji |
| **Automatic background checks** | One poll per merchant round (08/12/16/20 +5 min, Beijing Time) via WorkManager; self-healing watchdog every 30 min; auto re-schedule after reboot / upgrade / timezone change |
| **Rich notifications** | Sound + vibration by default; DND mode mutes them while keeping the banner; second alert 15 min before the merchant packs up |
| **Merchant shelf view** | Live round countdown, rare-item star marks, one-tap "Claimed" (🎯) |
| **Sale records** | Auto-log every round's inventory, searchable, with "last seen" for rare items; 0:00–8:00 closed hours show the previous-day shelf review |
| **Wish-star leveling** | +1 star per rare item claimed; level-up curve 3/5/7/9… |
| **Background reliability suite** | Boot receiver, battery-optimization whitelist guide, per-brand (MIUI/EMUI/ColorOS/vivo/Samsung) whitelist instructions, one-tap disable auto-start |
| **Privacy-first** | `allowBackup=false`, backup/transfer excluded via data-extraction rules; uninstalling wipes everything (Key, wishlist, records, jobs, whitelist) |
| **Manual atlas sync** | Refresh the item-name atlas & download all icons anytime, with progress bar and clear error messages |

### 🎨 Web Prototype (zero-dependency)

- 4-screen clickable prototype: **Wishlist / Catalog / Records / Settings**, data in `localStorage`, **no network required**
- Full notification demo: system-style banner + Web Audio chime + `navigator.vibrate` + class-mode (mute)

---

## 📸 Screenshots

> Real-device captures, app UI only.

| Wishlist | Shelf | Records |
|---|---|---|
| ![wishlist](test_shots/48_app_wishlist.png) | ![shelf](test_shots/49_app_shelf.png) | ![records](test_shots/50_app_records.png) |

> More evidence: see the [test report](roco-merchant-app/TEST_REPORT.md) and the full set of screenshots in [`test_shots/`](test_shots/).

---

## 📂 Repository Structure

```
luoke-market/
├── README.md                        # English README
├── README_CN.md                     # Chinese README
├── roco-merchant-app/               # 📱 Native Android app (Kotlin)
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/roco/merchant/
│   │   │   │   ├── MainActivity.kt            # Single-activity host
│   │   │   │   ├── data/                      # API clients, atlas, prefs, models
│   │   │   │   │   ├── MerchantApi.kt         # Merchant shelf API client
│   │   │   │   │   ├── WikiItemsApi.kt        # Item-atlas / icon API client
│   │   │   │   │   ├── AtlasRepository.kt     # Atlas sync (paged, rate-limited)
│   │   │   │   │   └── Prefs.kt               # SharedPreferences persistence
│   │   │   │   ├── notify/Notifier.kt         # Notifications (sound/vibration/DND)
│   │   │   │   ├── ui/                        # Fragments: wishlist/shelf/records/settings
│   │   │   │   ├── util/BatteryOptimizer.kt   # Ignore-battery-optimization helper
│   │   │   │   └── worker/                    # WorkManager: check/watchdog/boot-receiver
│   │   │   ├── assets/
│   │   │   │   └── atlas.json                 # Full item-name atlas (offline)
│   │   │   └── res/                           # Layouts, drawables, strings, themes
│   │   ├── build.gradle.kts                   # App module config (v0.3.1 · versionCode 11)
│   │   └── proguard-rules.pro
│   ├── build.gradle.kts                       # Root build (AGP 8.5.2, Kotlin 1.9.24)
│   ├── settings.gradle.kts
│   ├── gradle/ gradlew/ gradlew.bat           # Gradle 8.14.5 wrapper
│   ├── dist/                                  # 📦 Versioned release APKs (0.2.5 → 0.3.1)
│   │   ├── MagicFruitRadar_v0.2.5_release.apk
│   │   ├── MagicFruitRadar_v0.2.6_release.apk
│   │   ├── MagicFruitRadar_v0.2.7_release.apk
│   │   ├── MagicFruitRadar_v0.3.0_release.apk    # Icons not bundled; atlas names offline
│   │   └── MagicFruitRadar_v0.3.1_release.apk    # Renamed + hand-drawn magic-fruit icon
│   ├── TEST_REPORT.md                         # Test report
│   └── poster.html                            # Promotional poster (HTML)
├── magic-fruit-art.png                       # 🎨 Hand-drawn icon source art
├── roco-merchant-prototype/                  # 🎨 Clickable web prototype (zero-dependency)
│   ├── index.html · app.js · styles.css
│   ├── preview.png
│   └── README.md
└── test_shots/                               # 🧪 Test screenshots (emulator + real device, app UI only)
```

> ⚠️ Local-only files excluded from git: `keystore.properties`, `keystore/` (release signing key), `local.properties` (SDK path), Gradle build outputs. **Release APKs of every version are versioned in `dist/`** (each < 50 MB, safe for GitHub).

---

## 🚀 Getting Started

### 1) Web Prototype (no build needed)

Open the prototype directly in any modern browser (Chrome / Edge recommended):

```bash
# Option A: just double-click index.html
# Option B: local server
cd roco-merchant-prototype
python -m http.server 8000     # then visit http://localhost:8000
```

> Pure front-end (HTML/CSS/JS, zero dependencies), data in `localStorage`, works fully offline. Use DevTools mobile viewport or open it on a phone for the full experience.

### 2) Android App (build from source)

**Requirements**

| Tool | Version |
|---|---|
| JDK | 17 |
| Android SDK | compileSdk 34 / targetSdk 34 / minSdk 26 (Android 8.0+) |
| Android Studio | Ladybug or newer (or command-line Gradle) |
| Gradle | 8.14.5 (bundled wrapper — no manual install needed) |

**Build**

```bash
cd roco-merchant-app

# Debug APK
./gradlew :app:assembleDebug          # Windows: gradlew.bat :app:assembleDebug

# Release APK (signed)
./gradlew :app:assembleRelease
```

**Signing notes** — Release builds are signed with `keystore/roco-release.jks`, configured via a local `keystore.properties` (template below). This file and the keystore are **git-ignored** — generate your own keystore for your own builds:

```properties
# roco-merchant-app/keystore.properties  (local only, never commit)
storeFile=keystore/roco-release.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=roco
keyPassword=YOUR_KEY_PASSWORD
```

Without a keystore you can still build and install the **debug** APK, which uses the default debug signing.

**Install**

1. Transfer the APK to your phone (WeChat / QQ / USB / cloud drive) and open it.
2. Allow "Install unknown apps" when prompted (OPPO/OnePlus: Settings → Other settings → Device & privacy → Install unknown apps).
3. Grant the **Notification** permission on first launch — it is required for arrival alerts.

> Release APKs of every version are versioned in `roco-merchant-app/dist/`. For public distribution we also recommend attaching them to [GitHub Releases](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository) for direct one-click downloads.

---

## 📲 Usage Guide

### First-run setup

1. Open the app → go to **Settings** → fill in your **Roco Magic Book API key** (get one at [https://rocom.shallow.ink/](https://rocom.shallow.ink/)).
2. Tap **Save**. The app now auto-checks once per merchant round — 5 minutes after each refresh at Beijing Time 08/12/16/20.
3. **Item icons** are *not* bundled in the APK. After you save a **valid** API key for the first time, a dialog appears prompting you to download them: tap **Download icons now** (≈2 minutes, free public resources, no credits consumed), or go to Settings → **🔄 Sync all atlas & icons** anytime. Downloaded icons are stored in the app data dir and **never deleted by app updates** — they are only removed on uninstall. Until downloaded, items show name + emoji.
4. Open **Wishlist** → tap **+** → browse/search the built-in name atlas (offline) → tap items to add. Defaults: Prism Orb / Blessing Pendant / Shiny Pet Egg / Chief Bloodline Potion.

### Alerts

- A wished item on the shelf → **notification (sound + vibration)**.
- **DND** mode in Settings mutes sound & vibration while keeping the banner.
- Second alert **15 minutes before the merchant packs up** if a wished item was not claimed.

### Background reliability (important for Chinese ROMs)

Android manufacturers (MIUI / EMUI / ColorOS / OriginOS / MagicOS) aggressively kill background processes. To keep alerts reliable:

1. Allow **ignore battery optimization** (first-launch guide, or Settings → Background Protection → 🔋 Ignore battery optimization).
2. Whitelist the app in **Auto-start**.
3. **Lock the app card** in Recents (pull down) so it cannot be swiped away.
4. Per-brand step-by-step guides are built into the app: Settings → Background Protection → 📖 Per-brand whitelist guide.

> After a phone reboot the background checks **auto-restore** — no need to open the app. You can also fully disable auto-start (Settings → Auto-start off) so reboots stop restoring jobs.

### Uninstall

Uninstalling wipes everything — API Key, wishlist, records, notification channels, Doze whitelist and background jobs are all removed with the package. Cloud backup & device transfer are disabled (`allowBackup=false`), so old settings never come back after reinstall. **Keep your API Key safe yourself.**

### Credits & cost — who gets paid

> 💰 **Important: credits are charged by the data service, NOT by this app.**

| Question | Answer |
|---|---|
| Where does the money go? | **Roco Magic Book service** — the operator of `rocom.shallow.ink` / `wegame.shallow.ink`. You buy credits and an API key on their website; each API call deducts credits from **their** billing system. |
| Does the developer get paid? | **No.** The author of this app earns nothing from your credits — no commission, no resale, no hidden fees. |
| In-app purchases? | **None.** No ads, no IAP, no paid features — the app is free. |
| How are credits consumed? | Automatic checks: **1 paid call per merchant round** (4/day max, throttled to avoid waste). Manual refresh: **~5 credits per call**, always with a confirmation dialog. Item atlas & icons: **free public resources, 0 credits**. |
| Key invalid / credits run out? | The app shows clear error messages and never retries blindly — you top up or replace the key at the service's website. |

---

## 🔌 External Services & Data Sources

The app relies on the following third-party service. **No free alternatives with equal reliability exist**, so the app uses a paid data service by design:

| Service | Role | Access |
|---|---|---|
| **Roco Magic Book** — [rocom.shallow.ink](https://rocom.shallow.ink/) | Merchant shelf data (what is on sale each round) + official item atlas & icons | Paid API key (`X-API-Key` header). Base URL: `https://wegame.shallow.ink` |

**API endpoints used:**

| Endpoint | Purpose |
|---|---|
| `GET /api/v1/games/rocom/merchant/info` | Current merchant shelf (primary data source) |
| `POST /api/v1/games/rocom/ingame/merchant/info` | Fallback when the primary endpoint fails |
| `GET /api/v1/games/rocom/wiki/items?page_no=&page_size=&q=` | Full item atlas (paged) & name search |
| `GET /api/v1/resources/wiki/assets/items/bag/*.png` | Item icons (public resources, **no credits consumed**) |

> 💡 **Why this service?** The game exposes **no official public API** for the merchant. Community research (see the prototype's README) ranked reliable data sources as: community API (this service) → page/protocol polling → OCR → crowd-sourcing. This service is the most reliable, minute-level source available, and item icons are served from the official game resources. *Promotion: if you are building anything for Roco Kingdom, their API covers merchant data, wiki/atlas, and more — grab a key at [rocom.shallow.ink](https://rocom.shallow.ink/).*

**Data & image sources inside the app** (Settings → Data & image sources): Roco Magic Book API (merchant shelf + Wiki atlas & icons). The web prototype uses **no external service** — sounds are synthesized with Web Audio, and data stays in `localStorage`.

---

## 🔒 Privacy & Security

- **API key stays on-device** — stored only in private `SharedPreferences`, never uploaded anywhere by the app; it is sent only to the service's own endpoints.
- **No ads, no analytics, no tracking SDKs.** Only standard libraries (AndroidX, OkHttp, Gson, Kotlin Coroutines, WorkManager).
- **Uninstall = full wipe** — `allowBackup=false` + data-extraction rules exclude cloud backup and device transfer; records, key, whitelist and jobs all die with the package.
- **Explicit user consent for paid calls** — manual refresh shows a confirmation dialog first; automatic checks are throttled to once per round.

---

## 🧪 Testing

Full details in [TEST_REPORT.md](roco-merchant-app/TEST_REPORT.md), evidence screenshots in [`test_shots/`](test_shots/) (emulator + real device, app UI only).

| Environment | Result |
|---|---|
| Android emulator (Android 37.1) — 12 rounds of feature tests | ✅ All passed |
| Real device (Android 16 / API 36) — real API, ~10 credits | ✅ All passed, 0 crashes (`com.roco.merchant`) |
| `adb reboot` without opening the app | ✅ Background jobs auto-restored |
| Uninstall audit (jobs / whitelist / channels / data dir) | ✅ Fully cleared |
| Atlas sync (2,500+ items, 43 pages) | ✅ 0 failures; name atlas bundled & offline; icons downloaded free & cached locally |

**Key bug fixes along the way:** response `data` wrapper not parsed → shelf always empty (v0.2.5) · premature "packing up" alert without remaining-time check (v0.2.5) · cross-round items leaking into the current shelf (v0.2.5) · wishlist switch crash from ViewHolder reuse (v0.2.3) · background jobs lost after reboot (v0.2.6 boot receiver + watchdog) · settings surviving uninstall via cloud backup (v0.2.7 `allowBackup=false`) · **icons removed from APK, first-key download dialog, icons persist across updates (v0.3.0)** · **rename + hand-drawn magic-fruit icon (v0.3.1)**.

---

## 📦 Release History

All release APKs are versioned under [`roco-merchant-app/dist/`](roco-merchant-app/dist/).

| Version | Highlights | APK |
|---|---|---|
| **v0.3.1** (current) | Renamed to **Magic Fruit Radar**; launcher icon replaced with the hand-drawn magic-fruit artwork (full-bleed, no backing color) | `MagicFruitRadar_v0.3.1_release.apk` |
| **v0.3.0** | Icons no longer bundled — APK keeps the full item-*name* atlas offline; icons download free on demand & persist across updates; one-time dialog after first valid API key | `MagicFruitRadar_v0.3.0_release.apk` |
| **v0.2.7** | Disable auto-start toggle; uninstall fully wipes data (`allowBackup=false`) | `MagicFruitRadar_v0.2.7_release.apk` |
| **v0.2.6** | Background reliability: boot receiver, 30-min watchdog, battery-optimization whitelist guide | `MagicFruitRadar_v0.2.6_release.apk` |
| **v0.2.5** | Fixed 3 bugs: `data` wrapper parsing, premature "packing up" alert, cross-round items | `MagicFruitRadar_v0.2.5_release.apk` |

---

## 🗺️ Roadmap

```
Prototype validation (done) → Community data source integration (done) → Native Android App (done)
→ Deeper push-channel integration (vendor channels + FCM) → Multi-server / timetable DND → Official collaboration
```

- Multi-server subscriptions & timetable-based DND for students
- Deeper push-channel integration (vendor channels / FCM) for even more reliable delivery on aggressive ROMs
- Official collaboration with the game team

---

## ⚠️ Disclaimer

This is an **independent fan project**. It is not affiliated with, endorsed by, or connected to Tencent or the official *Roco Kingdom: World* team in any way. All game item names, data and images belong to their respective owners; prices and merchant rules in-game may change at any time — always verify in game. The data source is a third-party community API; using it may be subject to the service's terms and the game's user agreement. The web prototype is a concept demo only.

---

## 📄 License

Released under the **MIT License** — see [LICENSE](LICENSE).
