---
kind: configuration_system
name: 基于静态常量、SharedPreferences 与 GreenDao 的 Android 应用配置体系
category: configuration_system
scope:
    - '**'
source_files:
    - app/src/main/java/com/example/gtj_sd_dg240/StaticConstant.java
    - app/src/main/java/com/example/gtj_sd_dg240/GTJApplication.java
    - app/build.gradle
    - gradle.properties
    - app/src/main/java/com/example/gtj_sd_dg240/activity/setting/ScanningBlueboothActivity.java
    - app/src/main/java/com/example/gtj_sd_dg240/activity/setting/CalibrationDeviceActivity.java
    - app/src/main/java/com/example/gtj_sd_dg240/activity/setting/MeteringDeviceActivity.java
    - app/src/main/java/com/example/gtj_sd_dg240/activity/testing/ConfigManagerActivity.java
    - app/src/main/java/com/example/gtj_sd_dg240/model/ConfigBean.java
    - app/src/main/java/com/example/gtj_sd_dg240/storage/GreenDaoHelper.java
    - app/src/main/assets/RebarMinMax.csv
---

## 1. 系统/方法概述

该工程没有引入统一的配置框架（如 Hilt Config、KSP 配置生成、集中式 YAML/JSON 加载器），而是采用 **Android 原生 + 轻量工具库** 的组合方式，将配置分为三层：

- **编译期/构建期配置**：通过 `app/build.gradle` 的 `defaultConfig`、`buildTypes`、`signingConfigs` 以及根 `gradle.properties` 控制 SDK、签名、JVM 参数、AndroidX 开关等。
- **硬编码全局常量**：集中在 `StaticConstant.java`，定义网络地址、数据库名、蓝牙 UUID、电杆类型/结构/规范枚举、默认品牌标识、崩溃日志路径等“应用级”常量。
- **运行时用户/设备配置**：使用 `com.blankj.utilcodex.SPUtils`（封装 SharedPreferences）持久化蓝牙 MAC/名称、校准列表、计量设备列表、纵筋直径选择、语言类型等；业务配置数据（电杆配置、项目、曲线等）通过 GreenDao 持久化到 SQLite。

此外，`assets/` 下存放若干 CSV 文件（`6.csv`~`18.csv`、`RebarMinMax.csv`），作为内置基准/阈值数据随包分发，属于不可变的静态配置数据源。

## 2. 关键文件与位置

| 作用 | 关键文件 |
|---|---|
| 应用初始化入口 | `app/src/main/java/com/example/gtj_sd_dg240/GTJApplication.java` |
| 全局常量 & 语言偏好 | `app/src/main/java/com/example/gtj_sd_dg240/StaticConstant.java` |
| 构建/签名/Gradle 属性 | `app/build.gradle`、`gradle.properties`、`android.keystore` |
| 用户/设备持久化配置（SP） | `ScanningBlueboothActivity.java`、`CalibrationDeviceActivity.java`、`MeteringDeviceActivity.java`、`MainActivity.java`、`DetectActivity.java` |
| 业务配置数据（GreenDao） | `model/ConfigBean.java`、`functions/database/greenDao/db/ConfigBeanDao.java`、`storage/GreenDaoHelper.java` |
| 内置 CSV 基准数据 | `app/src/main/assets/*.csv` |
| 配置管理 UI | `activity/testing/ConfigManagerActivity.java` |

## 3. 架构与约定

### 3.1 构建期配置（Gradle）
- `app/build.gradle` 中 `defaultConfig` 固定 `applicationId`、`minSdk 26`、`targetSdk 35`、`versionCode 1`、`versionName "1.0"`。
- `signingConfigs.release` 使用仓库根目录的 `../android.keystore`，别名与密码硬编码为 `android.keystore` / `123456`。
- `debug` 与 `release` 均关闭混淆（`minifyEnabled false`），但都启用 ProGuard 规则文件。
- `buildFeatures.buildConfig = true`，配合 `gradle.properties` 中 `android.defaults.buildfeatures.buildconfig=false`（被模块覆盖），实际仍生成 `BuildConfig`。
- `gradle.properties` 集中 JVM 内存、并行构建、缓存、AndroidX、非传递 R 类等构建优化开关。

### 3.2 全局常量层（`StaticConstant`）
- 所有“应用级”不可变或半可变配置集中在一个类中：HTTP 基址 `httpUrl`、数据库名 `DB_NAME`、崩溃开关 `CRASH_DEBUG`、崩溃路径 `crashFilePath`、蓝牙服务/特征 UUID、电杆类型/结构/规范/钢筋直径/螺旋筋直径/检测部位等枚举集合。
- 品牌切换通过 `APP_DEFAULT` 字段在 `APP_LIERDA` / `APP_DIANKEYUAN` / `APP_MINGSHENGHENGZHUO` / `APP_GTJ` 之间选择，注释明确标注“修改 APP_DEFAULT → 修改 colors.xml → 修改 drawable.xml”，形成多品牌打包约定。
- 语言类型通过 `setLanguage()` / `getLanguage()` 读写 SP 键 `languageType`，默认值 1（中文）。

### 3.3 运行时配置（SharedPreferences via SPUtils）
- 蓝牙设备信息：`bluetooth-mac`、`bluetooth-name`（写入于 `ScanningBlueboothActivity`，读取于 `MainActivity`、`BaseActivity`、`AboutUsActivity`）。
- 校准相关：`calibration-standard-list`、`calibration-list`（以 JSON 字符串形式序列化后存入，由 `CalibrationDeviceActivity` 读写，`DetectActivity`、`MainActivity` 读取）。
- 计量设备：`strMeteringDeviceList`、`spDiameter`（`MeteringDeviceActivity` 维护）。
- 其他：`last-config-ZoneBean`、`pwd`/`pwd1` 等。
- 所有 SP 操作统一通过 `SPUtils.getInstance().put()/getString()/getInt()` 完成，无自定义封装层。

### 3.4 业务配置数据（GreenDao + assets CSV）
- 用户创建的“电杆配置”以 `ConfigBean` 实体持久化到 GreenDao（SQLite），按 `PoleType`（普通/预应力/部分预应力）分类查询。
- `ConfigManagerActivity` 提供创建/删除/选择配置的 UI，删除时若配置名不在 `StaticConstant.configList` 白名单内才允许物理删除，否则仅取消选中。
- `assets/` 下的 `*.csv` 与 `RebarMinMax.csv` 作为内置基准/阈值数据，随 APK 分发，供计算逻辑读取。

### 3.5 应用启动装配
`GTJApplication.onCreate()` 顺序初始化：蓝牙管理器（传入 `StaticConstant` 中的 UUID）、崩溃处理器、Orhanobut Logger、BlankUtil Utils、GreenDao、百度定位、Toast 样式。所有外部依赖的初始配置均来自 `StaticConstant` 或硬编码。

## 4. 约定与约束

- **常量集中化**：所有跨模块共享的 URL、数据库名、蓝牙 UUID、枚举集合必须放在 `StaticConstant`，禁止在 Activity 中散落声明。
- **用户偏好走 SP**：任何可被用户修改且需跨页面/重启保留的值（蓝牙设备、校准列表、计量设置、语言）一律通过 `SPUtils` 存取，键名使用字符串字面量（如 `"bluetooth-mac"`），未建立集中键常量表。
- **业务配置走 GreenDao**：电杆配置、项目、曲线等结构化数据通过 GreenDao 实体持久化，新增/删除/查询均经 `GreenDaoHelper.getDaoSession().getXxxDao()`。
- **多品牌打包约定**：切换 `APP_DEFAULT` 时需同步修改 `colors.xml` 与 `drawable.xml`，这是代码注释显式要求的约定。
- **CSV 基准数据只读**：`assets/` 下的 CSV 文件随包分发，代码中未见写入逻辑，视为不可变配置。
- **构建产物签名固定**：debug/release 共用同一 keystore 及密码，便于调试与发布一致性。
- **无环境变量/配置文件机制**：工程中未发现 `.env`、`.properties`、`.yaml`、`.toml` 等外部配置文件加载逻辑，也不存在 Feature Flag 开关系统；所有行为分支通过 `StaticConstant` 布尔/字符串常量或用户 SP 值驱动。