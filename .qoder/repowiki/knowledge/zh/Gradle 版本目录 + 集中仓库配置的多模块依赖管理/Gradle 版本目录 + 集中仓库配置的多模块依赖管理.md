---
kind: dependency_management
name: Gradle 版本目录 + 集中仓库配置的多模块依赖管理
category: dependency_management
scope:
    - '**'
source_files:
    - settings.gradle
    - gradle/libs.versions.toml
    - build.gradle
    - app/build.gradle
    - utillibrary/build.gradle
---

## 1. 使用的系统与方案

本项目采用 **Android Gradle Plugin (AGP) 8.8.2** 的聚合工程（多模块）方式管理依赖，核心手段包括：
- **Version Catalog（版本目录）**：根目录 `gradle/libs.versions.toml` 集中声明所有第三方库的版本号与坐标，供各模块引用。
- **集中式仓库配置**：通过根 `settings.gradle` 的 `pluginManagement.repositories` 与 `dependencyResolutionManagement.repositories` 统一声明 Google、Maven Central、Gradle Plugin Portal 以及多个阿里云镜像（jcenter/central/google/gradle-plugin/public），并额外引入 JitPack。
- **强制集中仓库策略**：`dependencyResolutionManagement.repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)` 禁止子模块自行声明 `repositories { ... }`，所有仓库来源必须集中在根配置中。
- **本地 jar 包（libs 目录）**：`app/libs` 下直接放置 `BaiduLBS_Android.jar`、`bluetooth_kit.jar`，通过 `implementation fileTree(dir: 'libs', include: ['*.jar'])` 引入；`utillibrary/libs` 也使用相同方式引入本地 jar。
- **模块间依赖**：`app` 通过 `api project(path: ':utillibrary')` 依赖工具库模块，使工具库暴露给上层。

## 2. 关键文件

- `gradle/libs.versions.toml`：版本目录，定义 `[versions]`、`[libraries]` 条目（如 `androidx-appcompat`、`retrofit`、`glide`、`objectbox`、`okhttp`、`eventbus`、`commons-math3` 等）。
- `settings.gradle`：声明插件仓库与依赖仓库，启用 `FAIL_ON_PROJECT_REPOS` 模式，包含 `:app` 和 `:utillibrary` 两个模块。
- `build.gradle`（根）：通过 `plugins` 块声明 AGP 8.8.2 应用/库插件，并通过 `buildscript.dependencies` 引入 `org.greenrobot:greendao-gradle-plugin:3.3.1`。
- `app/build.gradle`：应用模块依赖声明，同时存在 Version Catalog 引用（未使用）与硬编码坐标混用；通过 `sourceSets.main.jniLibs.srcDirs = ['libs']` 将 `libs` 作为 JNI 库目录；通过 `packaging.resources.excludes` 排除重复 META-INF 文件。
- `utillibrary/build.gradle`：库模块依赖声明，通过 `api 'com.github.mjdev:libaums:0.7.4'` 向外部暴露 USB 能力。

## 3. 架构与约定

- **版本集中化**：大部分公共依赖在 `gradle/libs.versions.toml` 中以 `version.ref = xxxVersion` 形式引用，便于统一升级。
- **仓库优先级**：Google 仓库通过 `google { content { includeGroupByRegex(...) } }` 限定仅匹配 Android/Google 相关 group，其余走 Maven Central / 阿里云镜像 / JitPack，提升下载速度。
- **私有/本地二进制**：硬件 SDK 以 `*.jar` 形式随源码提交到 `app/libs` 与 `utillibrary/libs`，不经过远程仓库；构建时由 `fileTree` 自动纳入。
- **GreenDao 代码生成**：根 `build.gradle` 通过 `apply plugin: 'org.greenrobot.greendao'` 并在 `app/greendao` 块中指定 schemaVersion 与生成目录，属于构建期依赖管理的一部分。
- **签名与构建产物**：`app/build.gradle` 中 release/debug 均使用根目录 `android.keystore` 进行签名，keystore 密码明文写在构建脚本中。

## 4. 约定与约束

- **禁止子模块自定义仓库**：`RepositoriesMode.FAIL_ON_PROJECT_REPOS` 会阻止任何子模块在自身 `build.gradle` 中添加 `repositories {}`，否则构建失败。所有仓库必须在根 `settings.gradle` 中声明。
- **JNI 库目录固定为 `libs`**：`app` 模块通过 `jniLibs.srcDirs = ['libs']` 约定原生库放在该目录下。
- **重复 META-INF 资源被显式排除**：`packaging.resources.excludes` 中列出了 `META-INF/{AL2.0,LGPL2.1}`、`DEPENDENCIES`、`LICENSE*`、`notice*`、`*.kotlin_module` 等，避免打包冲突。
- **Java 版本统一为 17**：`app` 与 `utillibrary` 均设置 `sourceCompatibility` 与 `targetCompatibility` 为 `VERSION_17`。
- **测试依赖隔离**：`testImplementation` 与 `androidTestImplementation` 分别用于单元测试与仪器测试，避免污染主 APK。
- **Version Catalog 尚未完全落地**：虽然定义了 `gradle/libs.versions.toml`，但 `app/build.gradle` 与 `utillibrary/build.gradle` 中仍大量使用硬编码坐标字符串（如 `implementation 'androidx.appcompat:appcompat:1.7.1'`），说明版本目录当前仅部分生效，尚未成为唯一依赖声明入口。
- **JitPack 作为可选第三方源**：`settings.gradle` 中启用了 `jitpack.io`，因此可通过 GitHub 仓库坐标直接引入开源项目（如 `BaseRecyclerViewAdapterHelper`、`picture_library` 等）。