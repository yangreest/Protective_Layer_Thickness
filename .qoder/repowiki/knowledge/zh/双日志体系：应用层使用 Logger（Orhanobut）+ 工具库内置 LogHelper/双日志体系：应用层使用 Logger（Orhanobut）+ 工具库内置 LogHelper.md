---
kind: logging_system
name: 双日志体系：应用层使用 Logger（Orhanobut）+ 工具库内置 LogHelper
category: logging_system
scope:
    - '**'
source_files:
    - app/src/main/java/com/example/gtj_sd_dg240/GTJApplication.java
    - utillibrary/src/main/java/com/blackhao/utillibrary/log/LogHelper.java
    - utillibrary/src/main/java/com/blackhao/utillibrary/file/FileUtil.java
    - app/src/main/java/com/example/gtj_sd_dg240/net/RetrofitPresenter.java
    - app/src/main/java/com/example/gtj_sd_dg240/utils/DeleteFileUtils.java
    - app/src/main/java/com/example/gtj_sd_dg240/utils/PermissionUtils.java
    - app/src/main/java/com/example/gtj_sd_dg240/utils/CrashHandler.java
---

## 1. 使用的系统/方案

本仓库存在两套并行的日志实现：
- **app 模块**：使用第三方库 `com.orhanobut.logger`，通过 `Logger.addLogAdapter(new AndroidLogAdapter(PrettyFormatStrategy.newBuilder().build()))` 在 `GTJApplication.onCreate()` 中注册一个基于 `PrettyFormatStrategy` 的漂亮格式化输出适配器，所有日志经该适配器统一输出到 Android Logcat。
- **utillibrary 模块**：自研 `com.blackhao.utillibrary.log.LogHelper`，封装 Android 原生 `android.util.Log`，提供 `d/i/w/e/json` 等接口，并通过 `BuildConfig.DEBUG`、全局 `LOG_LEVEL`、`IS_WRITE_FILE` 控制是否打印与是否落盘。

两套体系互不依赖：app 模块未引入 `LogHelper`，utillibrary 也未引用 app 中的 `Logger`。因此不同模块可能混用两种日志方式。

## 2. 关键文件与包
- `app/src/main/java/com/example/gtj_sd_dg240/GTJApplication.java`：应用入口，集中初始化 `Logger` 适配器。
- `utillibrary/src/main/java/com/blackhao/utillibrary/log/LogHelper.java`：工具库内建日志单例，负责 TAG 生成、超长字符串切分、JSON 美化、可选的文件写入。
- `utillibrary/src/main/java/com/blackhao/utillibrary/file/FileUtil.java`：工具库中调用 `LogHelper.getInstance().e(...)` 的典型示例。
- `app/src/main/java/com/example/gtj_sd_dg240/net/RetrofitPresenter.java`、`DeleteFileUtils.java`、`PermissionUtils.java`、`ExportUtils.java`、`CrashHandler.java` 等：app 模块中使用 `Logger.d/e(...)` 的实际调用点。

## 3. 架构与约定
- **初始化位置**：日志系统在 `Application.onCreate()` 中完成（app 模块），符合 Android 生命周期规范，确保后续任意组件均可直接使用 `Logger`。
- **输出格式**：app 模块通过 `PrettyFormatStrategy` 输出带层级缩进的可读日志；utillibrary 的 `LogHelper` 通过 `getTAG()` 动态解析调用栈，自动附加 `(FileName:Line)` 信息，便于定位来源。
- **级别控制**：`LogHelper` 内部维护 `LOG_LEVEL = Log.ERROR` 与 `IS_DEBUG = BuildConfig.DEBUG`，仅当 `IS_DEBUG` 为真或当前级别低于等于阈值时才输出；`Logger` 本身由 Orhanobut 框架按构建变体控制开关（未在代码中显式配置）。`LogHelper` 还预留了 `IS_WRITE_FILE` 与 `initLogFile(Context)`，可将日志按天写入 `context.getCacheDir()/QChatLog/yyyy-MM-dd.txt`，但默认关闭。
- **长消息处理**：`LogHelper.splitStr` 将超过 `MAX_LENGTH = 3 * 1024` 的日志按固定长度切分后逐条输出，避免 Android Logcat 截断。
- **结构化字段**：未采用 JSON 结构化日志；`LogHelper.json()` 仅对传入字符串做 JSON 美化后再以 `Log.e` 输出，不属于结构化字段记录。

## 4. 观察到的约定与约束
- app 模块统一通过 `Logger` 输出，且仅在 `GTJApplication` 中注册一次适配器，不在业务类中重复配置。
- utillibrary 模块统一通过 `LogHelper.getInstance()` 单例输出，避免直接散落 `android.util.Log` 调用。
- 日志级别策略：`LogHelper` 默认 release 下只输出 ERROR 及以上，debug 下全量输出；文件落盘默认关闭，需手动开启并调用 `initLogFile`。
- 未发现统一的日志开关 API（如 `setLogLevel`、`enableFile` 对外暴露），`LogHelper` 的级别与开关均为静态字段，修改方式不够灵活。
- 未发现统一的日志收集/上报机制，日志仅输出到 Logcat 或本地文件，未集成远程日志服务。
- 部分业务类仍直接使用 `android.util.Log.e(...)`（如 `MainActivity.java`、`DataFileManagerActivity.java`、`DataUploadActivity.java`），说明尚未完全迁移到 `Logger`，属于历史遗留混用。