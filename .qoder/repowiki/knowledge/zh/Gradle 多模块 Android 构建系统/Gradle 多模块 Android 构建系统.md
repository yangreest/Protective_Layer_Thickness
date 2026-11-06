---
kind: build_system
name: Gradle 多模块 Android 构建系统
category: build_system
scope:
    - '**'
source_files:
    - build.gradle
    - settings.gradle
    - gradle.properties
    - gradle/wrapper/gradle-wrapper.properties
    - gradle/libs.versions.toml
    - app/build.gradle
    - utillibrary/build.gradle
    - android.keystore
---

## 1. 构建系统与工具链

项目采用 **Android Gradle Plugin (AGP) 8.8.2** + **Gradle Wrapper 8.10.2** 的多模块构建体系，通过根 `build.gradle`、`settings.gradle` 统一编排两个子模块：`:app`（应用）和 `:utillibrary`（通用库）。Java 编译目标为 **JDK 17**（`sourceCompatibility/targetCompatibility = VERSION_17`），并通过 `gradle.properties` 指定本地 JDK 路径（`/Users/wd/Library/.../corretto-17.0.14`）。

仓库未包含 Dockerfile、CI 流水线或发布脚本，构建与打包完全依赖 Gradle 命令（如 `./gradlew assembleRelease`），产物为 APK。

## 2. 关键文件与职责

- `build.gradle`（根）：声明 AGP 插件版本、注册 GreenDao 插件 classpath、定义全局 clean 任务。
- `settings.gradle`：集中管理所有 Maven 仓库源（google、mavenCentral、阿里云镜像、jitpack），并启用 `RepositoriesMode.FAIL_ON_PROJECT_REPOS` 强制禁止在子模块中重复声明仓库，确保依赖来源统一可控。
- `gradle.properties`：JVM 内存 `-Xmx4096m`、并行构建、构建缓存、按需配置、VFS 监控等性能开关；同时开启 AndroidX、Jetifier、非传递 RClass，并禁用 buildConfig/aidl/renderScript/resValues/shaders 等未使用特性。
- `gradle/libs.versions.toml`：使用 **Version Catalog** 集中声明第三方库版本（appcompat、retrofit、glide、eventbus、okhttp 等），但当前各模块的 `build.gradle` 仍直接写死版本号，尚未迁移到 catalog 引用。
- `gradle/wrapper/gradle-wrapper.properties`：锁定 Gradle 分发版本为 `8.10.2-all.zip`。
- `app/build.gradle`：应用模块配置，含签名、GreenDao 代码生成、JNI libs 目录、ViewBinding、Lint 策略、依赖声明。
- `utillibrary/build.gradle`：库模块配置，对外暴露 `api project(':utillibrary')` 给 app 使用。
- `android.keystore` / `android.keystore.txt`：APK 签名密钥（release/debug 均使用同一 keystore 与密码 `123456`）。

## 3. 架构与约定

### 模块划分
- `:app` — Android Application，入口 `com.example.gtj_sd_dg240`，compileSdk/targetSdk 均为 35，minSdk 26。
- `:utillibrary` — Android Library，namespace `com.blackhao.utillibrary.library`，minSdk 24，提供 USB OTG、MVP 基类、日志、图片代理等能力。

### 依赖管理策略
- 仓库源集中在根 `settings.gradle`，子模块不得新增仓库。
- 第三方依赖以 `implementation` 为主，仅 `:utillibrary` 对 `libaums` 使用 `api` 暴露给上层。
- 本地二进制通过 `fileTree(dir: 'libs', include: ['*.jar'])` 引入（如 BaiduLBS、bluetooth_kit）。
- 测试依赖使用 `testImplementation` / `androidTestImplementation` 隔离。

### 构建类型与产物
- `debug` 与 `release` 均关闭 minify，启用 ProGuard 规则文件 `proguard-rules.pro`。
- 两者都使用同一个 release 签名配置（keystore 位于根目录 `android.keystore`）。
- JNI 原生库通过 `jniLibs.srcDirs = ['libs']` 打包进 APK，并使用新式 packaging（`useLegacyPackaging = false`）。
- Lint 在 release 构建时不中断（`checkReleaseBuilds false`、`abortOnError false`、`ignoreWarnings true`）。

### 代码生成
- GreenDao 插件 (`org.greenrobot.greendao:greendao-gradle-plugin:3.3.1`) 在 `app` 模块中启用，schemaVersion=1，生成的 DAO 输出到 `src/main/java/com/example/gtj_sd_dg240/functions/database/greenDao/db`。

## 4. 约定与约束

- **Gradle 版本锁定**：通过 wrapper 固定为 8.10.2，保证团队一致。
- **仓库白名单**：`FAIL_ON_PROJECT_REPOS` 模式禁止子模块自行添加仓库，所有依赖必须经根 `settings.gradle` 声明。
- **Java 版本统一**：所有模块 compile/target 均为 Java 17。
- **AndroidX 强制**：`android.useAndroidX=true` + `android.enableJetifier=true`，旧库自动转换。
- **构建性能默认开启**：并行、缓存、按需配置、VFS 监控均在 `gradle.properties` 中启用。
- **未使用特性显式关闭**：aidl、renderScript、resValues、shaders 在根与模块层均设为 false，减少构建产物体积。
- **签名复用**：debug/release 共用同一 keystore 与密码，便于调试与发布一致性。
- **无 CI/容器化**：仓库未发现 GitHub Actions、Jenkinsfile、Dockerfile 等自动化流水线，构建由开发者本地执行。
- **Version Catalog 未生效**：`gradle/libs.versions.toml` 已定义版本表，但 `app/build.gradle` 与 `utillibrary/build.gradle` 仍硬编码版本号，尚未迁移至 `libs.xxx.ref` 引用方式。