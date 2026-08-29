# 🔮 魔力果雷达 · RocoMerchant

**[English](README.md) | [中文](README_CN.md)**

> **《洛克王国：世界》远行商人商品提醒器** —— 把想要的道具加进许愿单，商人上架的那一刻，手机以「消息 + 声音 + 震动」通知你上线抢购，稀有道具不再错过。

<p align="center">
  <img src="https://img.shields.io/badge/version-v0.3.1-blue" alt="version">
  <img src="https://img.shields.io/badge/Android-8.0%2B-green" alt="android">
  <img src="https://img.shields.io/badge/Kotlin-1.9-blueviolet" alt="kotlin">
  <img src="https://img.shields.io/badge/license-MIT-yellow" alt="license">
</p>

---

## 📖 项目简介

魔力果雷达（RocoMerchant）是一款面向《洛克王国：世界》玩家的轻量、**离线优先**的 Android 提醒应用，应用图标为手绘「魔力果」。游戏内「远行商人」每天按北京时间 **08/12/16/20 点** 4 轮随机上架少量商品，售卖窗口短、库存少，稀有道具（棱镜球、祝福项链、炫彩精灵蛋等）往往几分钟内就被抢空。本应用在后台自动盯梢商人货架，与你的**许愿单**比对，愿望商品一上架立即推送通知叫你上线。

App 内置**全量道具名称图鉴（2500+ 件，完全离线可用）**；道具**图标**联网按需下载（官方公共资源、不消耗积分）并缓存在本地，**软件更新不会删除**。另附带可点击的**网页原型**用于产品验证。

---

## ✨ 功能特性

### 📱 Android 应用（v0.3.1 · versionCode 11）

| 功能 | 说明 |
|---|---|
| **许愿单 + 内置图鉴** | 2500+ 道具名称离线浏览/搜索；默认预置愿望；逐件开关「盯梢/暂停」 |
| **图标按需下载** | 安装包**不含图标**（体积更小）；图标免费（官方公共资源）下载并缓存于应用数据目录，**软件更新不删除**（仅卸载清除）；首次填入正确 API 后弹窗引导下载；未下载时以名称 + emoji 显示 |
| **后台自动检测** | 每轮商人刷新后 5 分钟检测一次（WorkManager）；每 30 分钟自愈守护；开机/升级/改时区自动重排 |
| **到货通知** | 声音+震动（默认开启）；免打扰模式（静音）；收摊前 15 分钟二次提醒 |
| **货架页** | 本轮倒计时、稀有星标、一键「抢到」（🎯） |
| **记录页** | 自动记录每轮售卖（可搜索、稀有最近出现时间）；凌晨 0–8 点休市显示前一日货架回顾 |
| **许愿星等级** | 每抢到 1 件稀有 +1 星，3/5/7/9… 递增升级 |
| **后台可靠性专项** | 开机自恢复、电池优化白名单引导、各品牌（MIUI/EMUI/ColorOS/vivo/三星）后台白名单指引、一键解除自启动 |
| **隐私优先** | 禁止系统备份与迁移；卸载即彻底清除（Key/许愿单/记录/后台任务/白名单） |
| **手动图鉴同步** | 一键刷新名称图鉴并补全全部图标（带进度条与明确错误提示） |

### 🎨 网页原型（纯前端，零依赖）

- 四屏可点击原型：**许愿单 / 商品图鉴 / 提醒记录 / 我的**；数据存 `localStorage`，**无需网络**
- 完整通知演示：系统风格横幅 + Web Audio 提示音 + `navigator.vibrate` 震动 + 上课免打扰（静音）

---

## 📸 界面预览

> 真机实拍截图（OPPO Find X5），仅含应用界面。

| 许愿单 | 货架 | 记录 |
|---|---|---|
| ![许愿单](test_shots/48_app_wishlist.png) | ![货架](test_shots/49_app_shelf.png) | ![记录](test_shots/50_app_records.png) |

> 更多测试证据见 [测试报表](roco-merchant-app/TEST_REPORT.md) 与 [`test_shots/`](test_shots/) 下全部截图。

---

## 📂 项目结构

```
luoke-market/
├── README.md                        # 英文说明
├── README_CN.md                     # 中文说明（本文件）
├── roco-merchant-app/               # 📱 原生 Android 应用（Kotlin）
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/roco/merchant/
│   │   │   │   ├── MainActivity.kt            # 单 Activity 宿主
│   │   │   │   ├── data/                      # API 客户端、图鉴、偏好、数据模型
│   │   │   │   │   ├── MerchantApi.kt         # 商人货架 API 客户端
│   │   │   │   │   ├── WikiItemsApi.kt        # 道具图鉴/图标 API 客户端
│   │   │   │   │   ├── AtlasRepository.kt     # 图鉴同步（分页、限频）
│   │   │   │   │   └── Prefs.kt               # SharedPreferences 持久化
│   │   │   │   ├── notify/Notifier.kt         # 通知（声音/震动/免打扰）
│   │   │   │   ├── ui/                        # 界面：许愿单/货架/记录/设置
│   │   │   │   ├── util/BatteryOptimizer.kt   # 忽略电池优化辅助
│   │   │   │   └── worker/                    # WorkManager：检测/守护/开机接收器
│   │   │   ├── assets/
│   │   │   │   └── atlas.json                 # 全量道具名称图鉴（离线）
│   │   │   └── res/                           # 布局、图形、字符串、主题
│   │   ├── build.gradle.kts                   # 应用模块配置（v0.3.1 · versionCode 11）
│   │   └── proguard-rules.pro
│   ├── build.gradle.kts                       # 根构建（AGP 8.5.2，Kotlin 1.9.24）
│   ├── settings.gradle.kts
│   ├── gradle/ gradlew/ gradlew.bat           # Gradle 8.14.5 包装器
│   ├── dist/                                  # 📦 各版本正式签名 APK（0.2.5 → 0.3.1）
│   │   ├── RocoMerchant_v0.2.5_release.apk
│   │   ├── RocoMerchant_v0.2.6_release.apk
│   │   ├── RocoMerchant_v0.2.7_release.apk
│   │   ├── RocoMerchant_v0.3.0_release.apk    # 不含图标；保留道具名称图鉴
│   │   └── RocoMerchant_v0.3.1_release.apk    # 更名「魔力果雷达」+ 手绘图标
│   ├── TEST_REPORT.md                         # 测试报表
│   └── poster.html                            # 宣传海报（HTML）
├── 手绘魔力果.png                            # 🎨 手绘图标原图
├── roco-merchant-prototype/                  # 🎨 可点击网页原型（零依赖）
│   ├── index.html · app.js · styles.css
│   ├── preview.png
│   └── README.md
└── test_shots/                               # 🧪 测试截图（模拟器 + OPPO Find X5 真机，仅应用界面）
```

> ⚠️ 本地私有文件不入库：签名配置 `keystore.properties`、签名文件 `keystore/`、SDK 路径 `local.properties`、构建产物目录。**各版本正式 APK 已纳入版本控制存放于 `dist/`**（单文件 < 50 MB，符合 GitHub 限制）。

---

## 🚀 快速开始

### 1) 网页原型（无需构建）

直接用现代浏览器打开即可（推荐 Chrome / Edge）：

```bash
# 方式一：直接双击 index.html
# 方式二：本地服务器
cd roco-merchant-prototype
python -m http.server 8000     # 然后访问 http://localhost:8000
```

> 纯前端（HTML/CSS/JS，零依赖），数据存 `localStorage`，完全离线可用。可用浏览器开发者工具切换手机视口，或直接发到手机打开完整体验。

### 2) Android 应用（源码构建）

**环境要求**

| 工具 | 版本 |
|---|---|
| JDK | 17 |
| Android SDK | compileSdk 34 / targetSdk 34 / minSdk 26（Android 8.0+） |
| Android Studio | Ladybug 或更新（或命令行 Gradle） |
| Gradle | 8.14.5（内置包装器，无需手动安装） |

**构建**

```bash
cd roco-merchant-app

# 调试包
./gradlew :app:assembleDebug          # Windows: gradlew.bat :app:assembleDebug

# 正式签名包
./gradlew :app:assembleRelease
```

**签名说明** —— 正式包使用 `keystore/roco-release.jks` 签名，通过本地 `keystore.properties` 配置（模板如下）。该文件与签名密钥**不入库**——请为自己的构建生成自己的 keystore：

```properties
# roco-merchant-app/keystore.properties  （仅本地，切勿提交）
storeFile=keystore/roco-release.jks
storePassword=你的存储密码
keyAlias=roco
keyPassword=你的密钥密码
```

没有 keystore 也可以构建并安装**调试包**（使用默认 debug 签名）。

**安装**

1. 用微信/QQ/网盘/数据线把 APK 发到手机，点击打开
2. 首次安装允许「安装未知应用」（OPPO/一加：设置 → 其他设置 → 设备与隐私 → 安装未知应用）
3. 首次启动允许「通知」权限（到货提醒必需）

> 各版本正式 APK 已纳入版本控制存放于 `roco-merchant-app/dist/`；对外分发建议同时挂到 [GitHub Releases](https://docs.github.com/zh/repositories/releasing-projects-on-github/managing-releases-in-a-repository) 供用户一键下载。

---

## 📲 使用说明

### 首次使用

1. 打开应用 → 进入**设置** → 填入**洛克魔法书 API Key**（前往 [https://rocom.shallow.ink/](https://rocom.shallow.ink/) 获取）
2. 点**保存设置**；此后每轮商人（北京时间 08/12/16/20 点）刷新后 5 分钟自动检测一次
3. **道具图标不在安装包内**。首次保存**正确的** API Key 后，应用会弹窗引导下载：点**立即下载图标**（约 2 分钟，公共资源不消耗积分），或随时到 设置 → **🔄 立即同步全部图鉴与图片**。图标保存在手机本地，**软件更新不会删除**，仅卸载时清除；未下载时以名称 + emoji 显示
4. 打开**许愿单** → 点右下角 **+** → 在离线名称图鉴中搜索/挑选加入。默认愿望：棱镜球 / 祝福项坠 / 炫彩精灵蛋 / 首领血脉秘药

### 提醒

- 愿望商品上架 → **通知（声音+震动）**
- 设置中的**免打扰**模式可静音+不震动（保留横幅）
- 未抢到的愿望商品在**收摊前 15 分钟**二次提醒

### 后台可靠性（国产系统必读）

手机厂商（MIUI / EMUI / ColorOS / OriginOS / MagicOS）会激进清理后台进程，为保证提醒可靠：

1. 允许**忽略电池优化**（首启引导，或 设置 → 后台运行保护 → 🔋 忽略电池优化）
2. 把应用加入**自启动**白名单
3. 最近任务里把应用卡片**下拉锁定**，防止被划掉
4. 应用内置各品牌分步指引：设置 → 后台运行保护 → 📖 各品牌后台白名单指引

> 手机重启后后台检测**自动恢复**，无需打开应用；也可在设置中关闭「开机自启动」彻底解除。

### 卸载

卸载即彻底清除：API Key、许愿单、记录、通知渠道、Doze 白名单、后台任务全部随包删除；已禁止系统云备份与设备迁移（`allowBackup=false`），重装不会恢复旧设置。**请自行保管好 API Key。**

### 积分与费用 —— 钱付给谁

> 💰 **重要：积分由数据服务方计费，本应用作者不收取任何费用。**

| 问题 | 答案 |
|---|---|
| 钱付给了谁？ | **洛克魔法书服务方**（rocom.shallow.ink / wegame.shallow.ink 的运营方）。你在其官网充值积分并购买 API Key，每次接口调用由**他们**的系统扣费 |
| 作者收费吗？ | **不收费**。本应用作者不参与任何分成，也不代收费用 |
| 应用内有内购吗？ | **没有**。无广告、无内购、无付费功能，应用本身完全免费 |
| 积分怎么消耗？ | 自动检测：每轮商人仅 1 次付费调用（每天最多 4 次，限流防浪费）；手动刷新：约 5 积分/次，且需弹窗确认；道具图鉴与图标：公共资源，**0 积分** |
| Key 无效或积分用完？ | 应用给出明确错误提示，不会盲目重试；充值或更换 Key 请前往服务方官网 |

---

## 🔌 外部服务与数据来源

应用依赖以下第三方服务。**不存在同等可靠性的免费替代**，因此设计上使用付费数据服务：

| 服务 | 用途 | 接入方式 |
|---|---|---|
| **洛克魔法书 · Roco Magic Book** — [rocom.shallow.ink](https://rocom.shallow.ink/) | 商人货架实时数据（每轮在售商品）+ 官方道具图鉴与图标 | 付费 API Key（`X-API-Key` 请求头）。Base URL：`https://wegame.shallow.ink` |

**使用的接口：**

| 接口 | 用途 |
|---|---|
| `GET /api/v1/games/rocom/merchant/info` | 当前商人货架（主数据源） |
| `POST /api/v1/games/rocom/ingame/merchant/info` | 主接口失败时的回退 |
| `GET /api/v1/games/rocom/wiki/items?page_no=&page_size=&q=` | 全量道具图鉴（分页）与按名搜索 |
| `GET /api/v1/resources/wiki/assets/items/bag/*.png` | 道具图标（公共资源，**不消耗积分**） |

> 💡 **为什么用它？** 游戏官方不提供商人数据的公开 API。前期调研（见原型 README）给出的可靠数据源排序为：社区 API（即本服务）→ 页面/协议轮询 → 截图 OCR → 众包上报。该服务是现有最可靠的分钟级数据源，道具图标亦来自官方游戏资源。*推广：如果你在做任何《洛克王国》相关开发，该服务的 API 覆盖商人数据、图鉴等能力，可在 [rocom.shallow.ink](https://rocom.shallow.ink/) 获取 Key。*

**应用内数据与图片来源**（设置页 → 📚 数据与图片来源）：洛克魔法书 API（商人货架 + Wiki 图鉴与图标）。网页原型**不依赖任何外部服务**——提示音由 Web Audio 合成，数据存于 `localStorage`。

---

## 🔒 隐私与安全

- **API Key 仅存本机**：只保存在应用私有 SharedPreferences，App 不会上传到任何地方，仅发送给服务方接口
- **无广告、无统计、无追踪 SDK**，仅使用标准库（AndroidX / OkHttp / Gson / Coroutines / WorkManager）
- **卸载即彻底清除**：已禁止云备份与设备迁移（`allowBackup=false` + data-extraction rules），记录、Key、白名单、后台任务随包删除
- **付费调用需确认**：手动刷新先弹窗确认；自动检测限流为每轮一次

---

## 🧪 测试与验证

完整测试过程见 [TEST_REPORT.md](roco-merchant-app/TEST_REPORT.md)，证据截图见 [`test_shots/`](test_shots/)（模拟器 + OPPO Find X5 真机，仅应用界面）。

| 环境 | 结果 |
|---|---|
| Android 模拟器（xiaomi17promax，Android 37.1）——12 轮功能测试 | ✅ 全部通过 |
| 真机 OPPO Find X5（Android 16 / API 36，arm64）——真实 API，约 10 积分 | ✅ 全部通过，0 崩溃（`com.roco.merchant`） |
| `adb reboot` 后不打开应用 | ✅ 后台任务自动恢复 |
| 卸载审计（任务/白名单/通知渠道/数据目录） | ✅ 彻底清除 |
| 图鉴同步（2500+ 件，43 页） | ✅ 0 失败；名称图鉴内置离线，图标免费下载本地缓存 |

**关键修复记录：** 响应 `data` 包装层未解析 → 货架恒为 0 件（v0.2.5）· 「即将收摊」缺剩余时间校验误报（v0.2.5）· 跨轮次商品混入当前货架（v0.2.5）· 许愿单开关复用 ViewHolder 闪退（v0.2.3）· 重启后后台任务丢失（v0.2.6 开机接收器 + 守护）· 云备份导致卸载后设置残留（v0.2.7 `allowBackup=false`）· **APK 去掉内置图标、首次填 Key 弹窗引导、图标更新不删除（v0.3.0）** · **更名「魔力果雷达」+ 手绘魔力果图标（v0.3.1）**。

---

## 📦 版本历史

各版本正式签名 APK 均存放于 [`roco-merchant-app/dist/`](roco-merchant-app/dist/)，纳入版本控制。

| 版本 | 要点 | APK |
|---|---|---|
| **v0.3.1**（当前） | 更名「魔力果雷达」（Magic-Fruit Radar）；启动图标换为手绘魔力果原图（整幅铺满，无打底色） | `RocoMerchant_v0.3.1_release.apk` |
| **v0.3.0** | 安装包不含图标（保留全部道具名称离线图鉴）；图标免费按需下载、更新不删除；首次填入正确 API 后弹窗引导 | `RocoMerchant_v0.3.0_release.apk` |
| **v0.2.7** | 自启动可解除；卸载彻底清除（`allowBackup=false`） | `RocoMerchant_v0.2.7_release.apk` |
| **v0.2.6** | 后台可靠性专项：开机自恢复、30 分钟守护、电池优化白名单引导 | `RocoMerchant_v0.2.6_release.apk` |
| **v0.2.5** | 修复 3 个真机 Bug：货架 0 件/误报收摊/跨轮次混货 | `RocoMerchant_v0.2.5_release.apk` |

---

## 🗺️ 路线图

```
原型验证 (done) → 社区数据源联调 (done) → 原生 Android App (done)
→ 系统推送通道深化（厂商通道 + FCM） → 多区服订阅 / 课表免打扰 → 官方合作洽谈
```

- 多区服订阅与学生课表免打扰
- 厂商推送通道深化（厂商通道 / FCM），进一步提升国产 ROM 后台送达率
- 与官方洽谈合作

---

## ⚠️ 免责声明

本项目为**独立同人项目**，与腾讯及《洛克王国》官方无任何关联、未获官方认可。游戏道具名称、数据与图片版权归原权利方所有；游戏内价格与商人规则随时可能变动，请以游戏内为准。数据来源为第三方社区 API，使用需遵守该服务条款与游戏用户协议；网页原型仅为概念演示。

---

## 📄 许可证

本项目以 **MIT 许可证**开源，详见 [LICENSE](LICENSE)。
