# 魔力果雷达 · Magic Fruit Radar

**[English](README.md) | [中文](README_CN.md)**

> **《洛克王国：世界》远行商人商品提醒器** —— 把想要的道具加进许愿单，商人上架的那一刻，手机以「消息 + 声音 + 震动」通知你上线抢购，稀有道具不再错过。

<p align="center">
  <img src="https://img.shields.io/badge/version-v0.3.1-blue" alt="version">
  <img src="https://img.shields.io/badge/Android-8.0%2B-green" alt="android">
  <img src="https://img.shields.io/badge/Kotlin-1.9-blueviolet" alt="kotlin">
  <img src="https://img.shields.io/badge/license-MIT-yellow" alt="license">
</p>

---

## 项目简介

魔力果雷达是一款面向《洛克王国：世界》玩家的轻量、**离线优先**的 Android 提醒应用，应用图标为手绘「魔力果」。游戏内「远行商人」每天按北京时间 **08/12/16/20 点** 4 轮随机上架少量商品，售卖窗口短、库存少，棱镜球、祝福项链、炫彩精灵蛋等稀有道具往往因未能在售卖时间内购买而错过。本应用在后台自动盯梢商人货架，与你的**许愿单**比对，愿望商品一上架立即推送通知叫你上线。

App 内置全量道具名称图鉴，共 2500+ 件，完全离线可用；道具图标联网按需下载并缓存在本地，软件更新不会删除。

---

## 功能特性

### Android 应用 · v0.3.1 · versionCode 11

| 功能 | 说明 |
|---|---|
| **许愿单 + 内置图鉴** | 2500+ 道具名称离线浏览和搜索；默认预置愿望；逐件开关「盯梢/暂停」 |
| **图标按需下载** | 安装包**不含图标**，体积更小；图标联网下载并缓存于应用数据目录，**软件更新不删除**，仅卸载时移除；首次填入正确 API 后弹窗引导下载；未下载时以名称 + emoji 显示 |
| **后台自动检测** | 每轮商人刷新后 5 分钟检测一次，基于 WorkManager；每 30 分钟自愈守护；开机、升级或改时区自动重排 |
| **到货通知** | 声音+震动，默认开启；免打扰模式可静音；收摊前 15 分钟二次提醒 |
| **货架页** | 本轮倒计时、稀有星标、一键「抢到」 |
| **记录页** | 自动记录每轮售卖，可搜索，含稀有最近出现时间；凌晨 0–8 点休市显示前一日货架回顾 |
| **许愿星等级** | 每抢到 1 件稀有 +1 星，3/5/7/9… 递增升级 |
| **手动图鉴同步** | 一键刷新名称图鉴并补全全部图标，带进度条与明确错误提示 |

---

## 界面预览

> 真机实拍截图，仅含应用界面。

| 许愿单 | 货架 | 记录 |
|---|---|---|
| ![许愿单](docs/48_app_wishlist.png) | ![货架](docs/49_app_shelf.png) | ![记录](docs/50_app_records.png) |

---

## 快速开始

### Android 应用

**环境要求**

| 工具 | 版本 |
|---|---|
| JDK | 17 |
| Android SDK | compileSdk 34 / targetSdk 34 / minSdk 26，Android 8.0+ |
| Android Studio | Ladybug 或更新，或命令行 Gradle |
| Gradle | 8.14.5 内置包装器，无需手动安装 |

**构建**

```bash
cd roco-merchant-app

# 调试包
./gradlew :app:assembleDebug          # Windows: gradlew.bat :app:assembleDebug

# 正式签名包
./gradlew :app:assembleRelease
```

**签名说明** —— 正式包使用 `keystore/roco-release.jks` 签名，通过本地 `keystore.properties` 配置，模板如下。该文件与签名密钥**不入库**，请为自己的构建生成自己的 keystore：

```properties
# roco-merchant-app/keystore.properties  仅本地，切勿提交
storeFile=keystore/roco-release.jks
storePassword=你的存储密码
keyAlias=roco
keyPassword=你的密钥密码
```

没有 keystore 也可以构建并安装**调试包**，使用默认 debug 签名。

**安装**

1. 用微信、QQ、网盘或数据线把 APK 发到手机，点击打开
2. 首次安装允许「安装未知应用」
3. 首次启动允许「通知」权限，到货提醒必需

> 各版本正式 APK 已纳入版本控制存放于 `roco-merchant-app/dist/`；对外分发建议同时挂到 [GitHub Releases](https://docs.github.com/zh/repositories/releasing-projects-on-github/managing-releases-in-a-repository) 供用户一键下载。

---

## 使用说明

### 首次使用

1. 打开应用，进入**设置**，填入**洛克魔法书 API Key**，可前往 [rocom.shallow.ink](https://rocom.shallow.ink/) 获取
2. 点**保存设置**；此后每轮商人刷新后 5 分钟自动检测一次，按北京时间 08/12/16/20 点
3. **道具图标不在安装包内**。首次保存**正确的** API Key 后，应用会弹窗引导下载：点**立即下载图标**，约 2 分钟、不消耗积分，或随时到 设置 → **同步全部图鉴与图片**。图标保存在手机本地，**软件更新不会删除**，仅卸载时移除；未下载时以名称 + emoji 显示
4. 打开**许愿单**，点右下角 **+**，在离线名称图鉴中搜索或挑选加入。默认愿望：棱镜球 / 祝福项坠 / 炫彩精灵蛋 / 首领血脉秘药

### 提醒

- 愿望商品上架 → **通知，声音+震动**
- 设置中的**免打扰**模式可静音且不震动，保留横幅
- 未抢到的愿望商品在**收摊前 15 分钟**二次提醒

### 后台可靠性

手机厂商会激进清理后台进程，为保证提醒可靠：

1. 允许**忽略电池优化**，首启引导，或 设置 → 后台运行保护 → 忽略电池优化
2. 把应用加入**自启动**白名单
3. 最近任务里把应用卡片**下拉锁定**，防止被划掉
4. 应用内置各品牌分步指引：设置 → 后台运行保护 → 各品牌后台白名单指引

> 手机重启后后台检测**自动恢复**，无需打开应用；也可在设置中关闭「开机自启动」彻底解除。

### 卸载

卸载即彻底清除：API Key、许愿单、记录、通知渠道、Doze 白名单、后台任务全部随包删除；已通过 `allowBackup=false` 禁止系统云备份与设备迁移，重装不会恢复旧设置。**请自行保管好 API Key。**

### 积分与费用，钱付给谁

> **重要：积分由数据服务方计费，本应用作者不收取任何费用。**

| 问题 | 答案 |
|---|---|
| 钱付给了谁？ | **洛克魔法书服务方**，即 rocom.shallow.ink / wegame.shallow.ink 的运营方。你在其官网充值积分并购买 API Key，每次接口调用由**他们**的系统扣费 |
| 作者收费吗？ | **不收费**。本应用作者不参与任何分成，也不代收费用 |
| 应用内有内购吗？ | **没有**。无广告、无内购、无付费功能，应用本身完全免费 |
| 积分怎么消耗？ | 自动检测：每轮商人仅 1 次付费调用，每天最多 4 次，限流防浪费；手动刷新：约 5 积分/次，且需弹窗确认；道具图鉴与图标：**不消耗积分** |
| Key 无效或积分用完？ | 应用给出明确错误提示，不会盲目重试；充值或更换 Key 请前往服务方官网 |

---

## 外部服务与数据来源

应用依赖以下第三方服务。**不存在同等可靠性的免费替代**，因此设计上使用付费数据服务：

| 服务 | 用途 | 接入方式 |
|---|---|---|
| **洛克魔法书 · Roco Magic Book** — [rocom.shallow.ink](https://rocom.shallow.ink/) | 商人货架实时数据，每轮在售商品，以及道具图鉴与图标 | 付费 API Key，使用 `X-API-Key` 请求头。Base URL：`https://wegame.shallow.ink` |

---

## 隐私与安全

- **API Key 仅存本机**：只保存在应用私有 SharedPreferences，App 不会上传到任何地方，仅发送给服务方接口
- **无广告、无统计、无追踪 SDK**，仅使用标准库，如 AndroidX、OkHttp、Gson、Coroutines、WorkManager
- **卸载即彻底清除**：已通过 `allowBackup=false` 与 data-extraction rules 禁止云备份与设备迁移，记录、Key、白名单、后台任务随包删除
- **付费调用需确认**：手动刷新先弹窗确认；自动检测限流为每轮一次

---

## 免责声明

本项目为独立同人项目，与腾讯及《洛克王国》官方无任何关联。游戏道具名称、数据与图片版权归原权利方所有；商人规则随时可能变动，请以游戏内为准。数据来源为第三方社区 API，使用需遵守该服务条款与游戏用户协议。

---

## 许可证

本项目以 **MIT 许可证**开源，详见 [LICENSE](LICENSE)。
