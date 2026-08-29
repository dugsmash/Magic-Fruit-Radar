# 🔮 魔力果雷达 · RocoMerchant

> **Your Traveling-Merchant Alarm for 《洛克王国：世界》** — add the items you want to your wishlist, and get a system notification (sound + vibration) the moment they appear on the in-game merchant's shelf, so you never miss a rare drop again.
>
> **《洛克王国：世界》远行商人商品提醒器** —— 把想要的道具加进许愿单，商人上架的那一刻，手机以「消息 + 声音 + 震动」通知你上线抢购，稀有道具不再错过。

<p align="center">
  <img src="https://img.shields.io/badge/version-v0.3.1-blue" alt="version">
  <img src="https://img.shields.io/badge/Android-8.0%2B-green" alt="android">
  <img src="https://img.shields.io/badge/Kotlin-1.9-blueviolet" alt="kotlin">
  <img src="https://img.shields.io/badge/license-MIT-yellow" alt="license">
</p>

---

## 📖 Introduction / 项目简介

**EN** — RocoMerchant is a lightweight, offline-first Android alarm app for the game *Roco Kingdom: World* (《洛克王国：世界》). The in-game **Traveling Merchant** (远行商人) restocks 4 times a day (08:00 / 12:00 / 16:00 / 20:00 Beijing Time) with a small, random inventory and short sale windows — rare items like the Prism Orb (棱镜球), Blessing Necklace (祝福项链) or Shiny Pet Egg (炫彩精灵蛋) can vanish in minutes. This app watches the merchant's shelf in the background, matches it against your **wishlist**, and pushes a notification the instant a wished item is in stock. It bundles the **full item-name atlas (2,500+ items, works fully offline)**; item **icons** are downloaded on demand from the official wiki resources (free, no credits) and cached locally — they survive app updates. A clickable **web prototype** is included for product validation.

**中文** — RocoMerchant（魔力果雷达）是一款面向《洛克王国：世界》玩家的轻量、离线优先的 Android 提醒应用，图标为手绘「魔力果」。游戏内「远行商人」每天按北京时间 **08/12/16/20 点** 4 轮随机上架少量商品，售卖窗口短、库存少，稀有道具（棱镜球、祝福项链、炫彩精灵蛋等）往往几分钟内就被抢空。本应用在后台自动盯梢商人货架，与你的**许愿单**比对，愿望商品一上架立即推送通知叫你上线。App 内置**全量道具名称图鉴（2500+ 件，完全离线可用）**；道具**图标**联网按需下载（官方资源、不消耗积分）并缓存在本地，软件更新不会删除。另附带可点击的**网页原型**用于产品验证。

---

## ✨ Features / 功能特性

### 📱 Android App（v0.3.1 · versionCode 11）

| EN | 中文 |
|---|---|
| **Wishlist with built-in atlas** — 2,500+ item *names* browsable/searchable offline; default wishlist pre-loaded; per-item on/off switch | **许愿单 + 内置图鉴**：2500+ 道具名称离线浏览/搜索；默认预置愿望；逐件开关「盯梢/暂停」 |
| **Item icons on demand** — APK bundles **no icons** (small install); icons are downloaded free from the official wiki resources and cached in the app data dir; they **survive app updates** (deleted only on uninstall); a one-time dialog prompts icon download after the first valid API key; missing icons fall back to name + emoji | **图标按需下载**：安装包不含图标（体积更小）；图标免费（公共资源）下载并缓存于应用数据目录，**软件更新不删除**（仅卸载清除）；首次填入正确 API 后弹窗引导下载；未下载时以名称 + emoji 显示 |
| **Automatic background checks** — polls once per merchant round (08/12/16/20 +5 min, Beijing Time) via WorkManager; self-healing watchdog every 30 min; auto re-schedule after reboot / upgrade / timezone change | **后台自动检测**：每轮商人刷新后 5 分钟检测一次（WorkManager）；每 30 分钟自愈守护；开机/升级/改时区自动重排 |
| **Rich notifications** — sound + vibration by default; DND mode (mute) for class/meetings; second alert 15 min before the merchant packs up | **到货通知**：声音+震动（默认开启）；免打扰模式（静音）；收摊前 15 分钟二次提醒 |
| **Merchant shelf view** — live round countdown, rare-item star marks, one-tap "Claimed" (🎯 抢到) | **货架页**：本轮倒计时、稀有星标、一键「抢到」 |
| **Sale records** — auto-log every round's inventory, searchable, with "last seen" for rare items; 0:00–8:00 closed hours show previous-day shelf review | **记录页**：自动记录每轮售卖（可搜索、稀有最近出现时间）；凌晨 0–8 点休市显示前一日货架回顾 |
| **Wish-star leveling** — +1 star per rare item claimed; level-up curve 3/5/7/9… | **许愿星等级**：每抢到 1 件稀有 +1 星，3/5/7/9… 递增升级 |
| **Background reliability suite** — boot receiver, battery-optimization whitelist guide, per-brand (MIUI/EMUI/ColorOS/vivo/Samsung) whitelist instructions, one-tap disable auto-start | **后台可靠性专项**：开机自恢复、电池优化白名单引导、各品牌后台白名单指引、一键解除自启动 |
| **Privacy-first** — `allowBackup=false`, backup/transfer excluded via data-extraction rules; uninstalling wipes everything (Key, wishlist, records, jobs, whitelist) | **隐私优先**：禁止系统备份与迁移；卸载即彻底清除（Key/许愿单/记录/后台任务/白名单） |
| **Manual atlas sync** — refresh the item-name atlas & download all icons from the wiki API anytime, with progress bar and clear error messages | **手动图鉴同步**：一键刷新名称图鉴并补全全部图标（带进度条与明确错误提示） |

### 🎨 Web Prototype（纯前端，零依赖）

- 4-screen clickable prototype: **Wishlist / Catalog / Records / Settings**, data stored in `localStorage`, **no network required**
- Full notification demo: system-style banner + Web Audio chime + `navigator.vibrate` vibration + class-mode (mute)
- 四屏可点击原型：**许愿单 / 商品图鉴 / 提醒记录 / 我的**；数据存 `localStorage`，无需网络；完整演示「横幅 + 声音 + 震动 + 上课免打扰」通知链路

---

## 📸 Screenshots / 界面预览

| Launch 启动 | Shelf 货架 | Records 记录 | Settings 设置 |
|---|---|---|---|
| ![launch](test_shots/01_launch.png) | ![shelf](test_shots/28_final_shelf.png) | ![records](test_shots/31_filtered_records.png) | ![settings](test_shots/33_settings_simplified.png) |

> More evidence: see the [test report](roco-merchant-app/TEST_REPORT.md) and the full set of 36 screenshots in [`test_shots/`](test_shots/).
> 更多测试证据见 [测试报表](roco-merchant-app/TEST_REPORT.md) 与 [`test_shots/`](test_shots/) 下全部 36 张截图。

---

## 📂 Repository Structure / 项目结构

```
luoke-market/
├── README.md                        # This file · 本说明
├── roco-merchant-app/               # 📱 Native Android app (Kotlin)
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/roco/merchant/
│   │   │   │   ├── MainActivity.kt            # Single-activity host · 单 Activity 宿主
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
│   │   │   │   └── atlas.json                 # Full item-name atlas (offline) · 全量道具名称图鉴
│   │   │   └── res/                           # Layouts, drawables, strings, themes
│   │   ├── build.gradle.kts                   # App module config (v0.3.1 · versionCode 11)
│   │   └── proguard-rules.pro
│   ├── build.gradle.kts                       # Root build (AGP 8.5.2, Kotlin 1.9.24)
│   ├── settings.gradle.kts
│   ├── gradle/ gradlew/ gradlew.bat           # Gradle 8.14.5 wrapper
│   ├── dist/                                  # 📦 Versioned release APKs (0.2.5 → 0.3.1)
│   │   ├── RocoMerchant_v0.2.5_release.apk
│   │   ├── RocoMerchant_v0.2.6_release.apk
│   │   ├── RocoMerchant_v0.2.7_release.apk
│   │   ├── RocoMerchant_v0.3.0_release.apk    # Icons not bundled; atlas names offline
│   │   └── RocoMerchant_v0.3.1_release.apk    # Renamed 魔力果雷达 + hand-drawn icon
│   ├── TEST_REPORT.md                         # Test report · 测试报表
│   └── poster.html                            # Promotional poster (HTML) · 宣传海报
├── 手绘魔力果.jpeg                            # 🎨 Hand-drawn icon source art · 图标原图
├── roco-merchant-prototype/                  # 🎨 Clickable web prototype (zero-dependency)
│   ├── index.html · app.js · styles.css
│   ├── preview.png
│   └── README.md
└── test_shots/                               # 🧪 36 test screenshots (emulator + OPPO Find X5)
```

> ⚠️ Local-only files excluded from git: `keystore.properties`, `keystore/` (release signing key), `local.properties` (SDK path), Gradle build outputs. **Release APKs of every version are versioned in `dist/`** (each < 50 MB, safe for GitHub).
>
> ⚠️ 本地私有文件不入库：签名配置 `keystore.properties`、签名文件 `keystore/`、SDK 路径 `local.properties`、构建产物目录。**各版本正式 APK 已纳入版本控制存放于 `dist/`**（单文件 < 50 MB，符合 GitHub 限制）。

---

## 🚀 Getting Started / 快速开始

### 1) Web Prototype — 网页原型（无需构建）

Open the prototype directly in any modern browser (Chrome / Edge recommended):

```bash
# Option A: just double-click index.html
# Option B: local server
cd roco-merchant-prototype
python -m http.server 8000     # then visit http://localhost:8000
```

> Pure front-end (HTML/CSS/JS, zero dependencies), data in `localStorage`, works fully offline. Use DevTools mobile viewport or open it on a phone for the full experience.

### 2) Android App — Android 应用（构建）

**Requirements / 环境要求**

| Tool | Version |
|---|---|
| JDK | 17 |
| Android SDK | compileSdk 34 / targetSdk 34 / minSdk 26 (Android 8.0+) |
| Android Studio | Ladybug or newer (or command-line Gradle) |
| Gradle | 8.14.5 (bundled wrapper — no manual install needed) |

**Build / 构建**

```bash
cd roco-merchant-app

# Debug APK（调试包）
./gradlew :app:assembleDebug          # Windows: gradlew.bat :app:assembleDebug

# Release APK（正式签名包）
./gradlew :app:assembleRelease
```

**Signing notes / 签名说明** — Release builds are signed with `keystore/roco-release.jks`, configured via a local `keystore.properties` (template below). This file and the keystore are **git-ignored** — generate your own keystore for your own builds:

```properties
# roco-merchant-app/keystore.properties  (local only, never commit)
storeFile=keystore/roco-release.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=roco
keyPassword=YOUR_KEY_PASSWORD
```

Without a keystore you can still build and install the **debug** APK, which uses the default debug signing.

**Install / 安装**

1. Transfer the APK to your phone (WeChat / QQ / USB / cloud drive) and open it.
2. Allow "Install unknown apps" when prompted (OPPO/OnePlus: Settings → Other settings → Device & privacy → Install unknown apps).
3. Grant the **Notification** permission on first launch — it is required for arrival alerts.

> Release APKs of every version are versioned in `roco-merchant-app/dist/`. For public distribution we also recommend attaching them to [GitHub Releases](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository) for direct one-click downloads.

---

## 📲 Usage Guide / 使用说明

### First-run setup / 首次使用

1. Open the app → go to **设置 (Settings)** → fill in your **洛克魔法书 API Key** (get one at [https://rocom.shallow.ink/](https://rocom.shallow.ink/)).
2. Tap **保存设置 (Save)**. The app now auto-checks once per merchant round — 5 minutes after each refresh at Beijing Time 08/12/16/20.
3. **Item icons** are *not* bundled in the APK. After you save a **valid** API key for the first time, a dialog appears prompting you to download them: tap **立即下载图标** (≈2 minutes, free public resources, no credits consumed), or go to 设置 → **🔄 立即同步全部图鉴与图片** anytime. Downloaded icons are stored in the app data dir and **never deleted by app updates** — they are only removed on uninstall. Until downloaded, items show name + emoji.
4. Open **许愿单 (Wishlist)** → tap **+** → browse/search the built-in name atlas (offline) → tap items to add. Defaults: 棱镜球 / 祝福项坠 / 炫彩精灵蛋 / 首领血脉秘药.

### Alerts / 提醒

- A wished item on the shelf → **notification (sound + vibration)**.
- **免打扰 (DND)** mode in Settings mutes sound & vibration while keeping the banner.
- Second alert **15 minutes before the merchant packs up** if a wished item was not claimed.

### Background reliability (important for Chinese ROMs) / 后台可靠性（国产系统必读）

Android manufacturers (MIUI / EMUI / ColorOS / OriginOS / MagicOS) aggressively kill background processes. To keep alerts reliable:

1. Allow **ignore battery optimization** (first-launch guide, or Settings → 后台运行保护 → 🔋 忽略电池优化).
2. Whitelist the app in **Auto-start / 自启动**.
3. **Lock the app card** in Recents (pull down) so it cannot be swiped away.
4. Per-brand step-by-step guides are built into the app: Settings → 后台运行保护 → 📖 各品牌后台白名单指引.

> After a phone reboot the background checks **auto-restore** — no need to open the app. You can also fully disable auto-start (Settings → 开机自启动 off) so reboots stop restoring jobs.

### Uninstall / 卸载

Uninstalling wipes everything — API Key, wishlist, records, notification channels, Doze whitelist and background jobs are all removed with the package. Cloud backup & device transfer are disabled (`allowBackup=false`), so old settings never come back after reinstall. **Keep your API Key safe yourself.**

### Credits & cost — who gets paid / 积分与费用 — 钱付给谁

> 💰 **Important: credits are charged by the data service, NOT by this app / 重要：积分由数据服务方计费，本应用作者不收取任何费用。**

| Question / 问题 | Answer / 答案 |
|---|---|
| Where does the money go? / 钱付给了谁？ | **洛克魔法书 (Roco Magic Book) service** — the operator of `rocom.shallow.ink` / `wegame.shallow.ink`. You buy credits and an API Key on their website; each API call deducts credits from **their** billing system. **付给洛克魔法书服务方**：你在其官网充值积分并购买 Key，每次接口调用由**他们**的系统扣费。 |
| Does the developer get paid? / 作者收费吗？ | **No.** The author of this app earns nothing from your credits — no commission, no resale, no hidden fees. **不收费**：本应用作者不参与任何分成，也不代收费用。 |
| In-app purchases? / 应用内有内购吗？ | **None.** No ads, no IAP, no paid features — the app is free. **没有任何内购/广告/付费功能**，应用本身完全免费。 |
| How are credits consumed? / 积分怎么消耗？ | Automatic checks: **1 paid call per merchant round** (4/day max, throttled to avoid waste). Manual refresh: **~5 credits per call**, always with a confirmation dialog. Item atlas & icons: **free public resources, 0 credits**. **自动检测每轮仅 1 次付费调用（每天最多 4 次）；手动刷新约 5 积分/次且需弹窗确认；道具图鉴与图标为公共资源，0 积分。** |
| What if the key is invalid / credits run out? / Key 无效或积分用完？ | The app shows clear error messages and never retries blindly — you top up or replace the key at the service's website. **App 会给出明确错误提示，不会盲目重试**；充值或更换 Key 请前往服务方官网。 |

---

## 🔌 External Services & Data Sources / 外部服务与数据来源

The app relies on the following third-party service. **No free alternatives with equal reliability exist**, so the app uses a paid data service by design:

| Service / 服务 | Role / 用途 | Access / 接入方式 |
|---|---|---|
| **洛克魔法书 · Roco Magic Book** — [rocom.shallow.ink](https://rocom.shallow.ink/) | Merchant shelf data (what is on sale each round) + official item atlas & icons · 商人货架实时数据 + 官方道具图鉴与图标 | Paid API key (`X-API-Key` header). Base URL: `https://wegame.shallow.ink` |

**API endpoints used / 使用的接口：**

| Endpoint | Purpose / 用途 |
|---|---|
| `GET /api/v1/games/rocom/merchant/info` | Current merchant shelf · 当前商人货架（主数据源） |
| `POST /api/v1/games/rocom/ingame/merchant/info` | Fallback when the primary endpoint fails · 主接口失败时的回退 |
| `GET /api/v1/games/rocom/wiki/items?page_no=&page_size=&q=` | Full item atlas (paged) & name search · 全量道具图鉴（分页）与按名搜索 |
| `GET /api/v1/resources/wiki/assets/items/bag/*.png` | Item icons (public resources, **no credits consumed**) · 道具图标（公共资源，不消耗积分） |

> 💡 **Why this service?** The game exposes **no official public API** for the merchant. Community research (see the prototype's README) ranked reliable data sources as: community API (this service) → page/protocol polling → OCR → crowd-sourcing. This service is the most reliable, minute-level source available, and item icons are served from the official game resources. *Promotion: if you are building anything for Roco Kingdom, their API covers merchant data, wiki/atlas, and more — grab a key at [rocom.shallow.ink](https://rocom.shallow.ink/).*
>
> 💡 **为什么用它？** 游戏官方不提供商人数据的公开 API。前期调研（见原型 README）给出的可靠数据源排序为：社区 API（即本服务）→ 页面/协议轮询 → 截图 OCR → 众包上报。该服务是现有最可靠的分钟级数据源，道具图标亦来自官方游戏资源。*推广：如果你在做任何《洛克王国》相关开发，该服务的 API 覆盖商人数据、图鉴等能力，可在 [rocom.shallow.ink](https://rocom.shallow.ink/) 获取 Key。*

**Data & image sources inside the app / 应用内数据与图片来源**（设置页 → 📚 数据与图片来源）: 洛克魔法书 API（商人货架 + Wiki 图鉴与图标）。The web prototype uses **no external service** — sounds are synthesized with Web Audio, and data stays in `localStorage`.

---

## 🔒 Privacy & Security / 隐私与安全

- **API Key stays on-device** — stored only in private `SharedPreferences`, never uploaded anywhere by the app; it is sent only to the service's own endpoints.
- **No ads, no analytics, no tracking SDKs.** Only standard libraries (AndroidX, OkHttp, Gson, Kotlin Coroutines, WorkManager).
- **Uninstall = full wipe** — `allowBackup=false` + data-extraction rules exclude cloud backup and device transfer; records, key, whitelist and jobs all die with the package.
- **Explicit user consent for paid calls** — manual refresh shows a confirmation dialog first; automatic checks are throttled to once per round.
- **API Key 仅存本机**：只保存在应用私有存储，App 不会上传到任何地方，仅发送给服务方接口。
- **无广告、无统计、无追踪 SDK**，仅使用标准库（AndroidX / OkHttp / Gson / Coroutines / WorkManager）。
- **卸载即彻底清除**：已禁止云备份与设备迁移，记录、Key、白名单、后台任务随包删除。
- **付费调用需确认**：手动刷新先弹窗确认；自动检测限流为每轮一次。

---

## 🧪 Testing / 测试与验证

Full details in [TEST_REPORT.md](roco-merchant-app/TEST_REPORT.md) (Chinese), 36 evidence screenshots in [`test_shots/`](test_shots/).

| Environment / 环境 | Result / 结果 |
|---|---|
| Android emulator (xiaomi17promax, Android 37.1) — 12 rounds of feature tests | ✅ All passed |
| Real device OPPO Find X5 (Android 16 / API 36, arm64) — real API, ~10 credits | ✅ All passed, 0 crashes (`com.roco.merchant`) |
| `adb reboot` without opening the app | ✅ Background jobs auto-restored |
| Uninstall audit (jobs / whitelist / channels / data dir) | ✅ Fully cleared |
| Atlas sync (2,500+ items, 43 pages) | ✅ 0 failures; name atlas bundled & offline; icons downloaded free & cached locally |

**Key bug fixes along the way / 关键修复：** response `data` wrapper not parsed → shelf always empty (v0.2.5) · premature "packing up" alert without remaining-time check (v0.2.5) · cross-round items leaking into the current shelf (v0.2.5) · wishlist switch crash from ViewHolder reuse (v0.2.3) · background jobs lost after reboot (v0.2.6 boot receiver + watchdog) · settings surviving uninstall via cloud backup (v0.2.7 `allowBackup=false`) · **icons removed from APK, first-key download dialog, icons persist across updates (v0.3.0)**.

---

## 📦 Release History / 版本历史

All release APKs are versioned under [`roco-merchant-app/dist/`](roco-merchant-app/dist/). 各版本正式签名 APK 均存放于 `roco-merchant-app/dist/`，纳入版本控制。

| Version / 版本 | Highlights / 要点 | APK |
|---|---|---|
| **v0.3.1** (current) | Renamed to **魔力果雷达** (Magic-Fruit Radar); launcher icon replaced with the hand-drawn 魔力果 artwork · 应用更名「魔力果雷达」；启动图标换为手绘魔力果原图 | `RocoMerchant_v0.3.1_release.apk` |
| **v0.3.0** | Icons no longer bundled — APK keeps the full item-*name* atlas offline; icons download free on demand & persist across updates; one-time dialog after first valid API key · 安装包不含图标（保留全部道具名称离线图鉴）；图标免费按需下载、更新不删除；首次填入正确 API 后弹窗引导 | `RocoMerchant_v0.3.0_release.apk` |
| **v0.2.7** | Disable auto-start toggle; uninstall fully wipes data (`allowBackup=false`) · 自启动可解除；卸载彻底清除 | `RocoMerchant_v0.2.7_release.apk` |
| **v0.2.6** | Background reliability: boot receiver, 30-min watchdog, battery-optimization whitelist guide · 后台可靠性专项（开机自恢复/守护/白名单引导） | `RocoMerchant_v0.2.6_release.apk` |
| **v0.2.5** | Fixed 3 bugs: `data` wrapper parsing, premature "packing up" alert, cross-round items · 修复 3 个真机 Bug（货架 0 件/误报收摊/跨轮次混货） | `RocoMerchant_v0.2.5_release.apk` |

---

## 🗺️ Roadmap / 路线图

```
原型验证 (done) → 社区数据源联调 (done) → 原生 Android App (done)
→ 系统推送通道深化（厂商通道 + FCM 可选） → 多区服订阅 / 课表免打扰 → 官方合作洽谈
```

- Multi-server (区服) subscriptions & timetable-based DND for students · 多区服订阅与学生课表免打扰
- Deeper push-channel integration (vendor channels / FCM) for even more reliable delivery on aggressive ROMs · 厂商推送通道深化
- Official collaboration with the game team · 与官方洽谈合作

---

## ⚠️ Disclaimer / 免责声明

This is an **independent fan project**. It is not affiliated with, endorsed by, or connected to Tencent or 《洛克王国》 official in any way. All game item names, data and images belong to their respective owners; prices and merchant rules in-game may change at any time — always verify in game. The data source is a third-party community API; using it may be subject to the service's terms and the game's user agreement. The web prototype is a concept demo only.

本项目为**独立同人项目**，与腾讯及《洛克王国》官方无任何关联、未获官方认可。游戏道具名称、数据与图片版权归原权利方所有；游戏内价格与商人规则随时可能变动，请以游戏内为准。数据来源为第三方社区 API，使用需遵守该服务条款与游戏用户协议；网页原型仅为概念演示。

---

## 📄 License / 许可证

Released under the **MIT License** — see [LICENSE](LICENSE). 本项目以 **MIT 许可证**开源，详见 [LICENSE](LICENSE)。
