---
kind: error_handling
name: Android 应用全局崩溃捕获与网络错误处理
category: error_handling
scope:
    - '**'
source_files:
    - app/src/main/java/com/example/gtj_sd_dg240/utils/CrashHandler.java
    - app/src/main/java/com/example/gtj_sd_dg240/activity/start/CrashMessageActivity.java
    - app/src/main/java/com/example/gtj_sd_dg240/GTJApplication.java
    - app/src/main/java/com/example/gtj_sd_dg240/net/ApiException.java
    - app/src/main/java/com/example/gtj_sd_dg240/net/MessageResult.java
    - app/src/main/java/com/example/gtj_sd_dg240/net/Result.java
    - app/src/main/java/com/example/gtj_sd_dg240/net/RetrofitPresenter.java
    - app/src/main/java/com/example/gtj_sd_dg240/net/RetryInterceptor.java
---

## 1. 使用的系统/方法

该 Android 工程采用**自定义全局未捕获异常处理器 + Retrofit 回调统一错误上报 + 业务层 try-catch** 的三层错误处理架构：
- **全局崩溃收集**：通过 `CrashHandler` 实现 `Thread.UncaughtExceptionHandler`，在 `GTJApplication.onCreate()` 中注册为默认异常处理器。
- **网络层错误**：基于 Retrofit + OkHttp，使用 `MessageResult<T>`（含 `code`、`msg`、`data`）作为统一响应体；通过 `RetryInterceptor` 对 `SocketTimeoutException` 做指数退避重试，失败后抛出 `RetryFailedException`；所有请求在 `RetrofitPresenter` 的 `onFailure` / `onResponse` 中统一转换为 `RetrofitPresenterResultListener.onError(...)`。
- **业务层异常**：各 Activity / Presenter 使用 `try { ... } catch (NumberFormatException e) { ... }` 等局部捕获，并通过 `ToastUtils` 或 UI 提示用户。

## 2. 关键文件与包

| 文件 | 职责 |
|---|---|
| `app/src/main/java/com/example/gtj_sd_dg240/utils/CrashHandler.java` | 全局未捕获异常处理器，收集设备信息、保存堆栈到文件、启动 `CrashMessageActivity` 展示并退出进程 |
| `app/src/main/java/com/example/gtj_sd_dg240/activity/start/CrashMessageActivity.java` | 崩溃后展示的 Activity，接收堆栈字符串并显示 |
| `app/src/main/java/com/example/gtj_sd_dg240/GTJApplication.java` | 应用入口，初始化 `CrashHandler`、Logger、GreenDao、定位等 |
| `app/src/main/java/com/example/gtj_sd_dg240/net/ApiException.java` | 业务网络异常类型，继承 `RuntimeException`，用于在网络层抛出自定义异常 |
| `app/src/main/java/com/example/gtj_sd_dg240/net/MessageResult.java` | 统一后端响应结构：`code`、`msg`、`data` |
| `app/src/main/java/com/example/gtj_sd_dg240/net/Result.java` | 另一个通用返回包装（`success`、`message`、`data`），部分接口使用 |
| `app/src/main/java/com/example/gtj_sd_dg240/net/RetrofitPresenter.java` | 封装所有 Retrofit 请求，统一判断 `code != 200` 走 `onError`，并将网络异常消息映射为中文提示 |
| `app/src/main/java/com/example/gtj_sd_dg240/net/RetryInterceptor.java` | OkHttp 拦截器，对 `SocketTimeoutException` 最多重试 2 次，间隔按 `2^retryCount * 500ms` 指数退避，最终抛出 `RetryFailedException` |
| `app/src/main/java/com/example/gtj_sd_dg240/net/RetrofitPresenterResultListener.java` | 回调接口，定义 `onSuccess(T data)` / `onError(String error)` |
| `app/src/main/java/com/example/gtj_sd_dg240/net/RetrofitInterfaceServiceUrl.java` | Retrofit API 接口定义 |
| `app/src/main/java/com/example/gtj_sd_dg240/net/CustomGsonConverterFactory.java` / `CustomGsonResponseBodyConverter.java` | 自定义 Gson 转换器（可能用于统一解析错误响应） |

## 3. 架构与约定

### 3.1 全局崩溃处理流程
1. `GTJApplication.onCreate()` 调用 `CrashHandler.getInstance().init(this)`。
2. `CrashHandler.init()` 保存系统默认 `UncaughtExceptionHandler`，并设置自身为默认处理器。
3. 发生未捕获异常时，`uncaughtException()` → `handleException()` → 在新线程中通过 `ToastUtils.showShort("程序崩溃！")` 提示 → 收集 Build/PackageInfo 设备信息 → `saveCrashInfo2File(ex)` 写入 `/storage/emulated/.../crashFilePath/crash-时间戳.txt`（受 `StaticConstant.CRASH_DEBUG` 控制）→ `sendEmail()` 启动 `CrashMessageActivity` 并传入完整堆栈 → 等待 3s 后 `Process.killProcess` + `System.exit(1)` 结束进程。
4. 若 `handleException` 返回 false（如 ex 为 null），则回退到系统默认处理器。

### 3.2 网络错误处理约定
- 所有网络请求统一通过 `RetrofitPresenter` 发起，不直接在 Activity 中调用 Retrofit。
- 响应体统一为 `MessageResult<T>`，约定 `code == 200` 表示成功；否则进入 `onError("请求失败" + msg)`。
- 网络异常（超时、DNS 解析失败等）在 `onFailure` 中通过 `getStrMessage(throwable.getMessage())` 将 `No address associated with hostname` 等英文异常翻译为“检查网络连接”等中文提示后再传给 `onError`。
- `RetryInterceptor` 仅针对 `SocketTimeoutException` 做指数退避重试（最大 2 次，初始 500ms），其他 IO 异常直接抛出；超过重试次数抛出内部 `RetryFailedException`。
- 上层通过 `RetrofitPresenterResultListener.onError(String)` 统一处理错误，通常再转交给 MVP 的 View 层展示。

### 3.3 业务层异常
- 数值解析类异常（如 `NumberFormatException`）在各 Activity 中 `catch` 后忽略或提示。
- 百度地图定位回调通过 `BDLocation.TypeServerError` / `TypeNetWorkException` / `TypeCriteriaException` 等常量分支处理不同定位错误。

## 4. 约定与约束

- **全局异常必须经 CrashHandler 接管**：`CrashHandler` 是唯一被设置为 `Thread.setDefaultUncaughtExceptionHandler` 的地方，确保所有未捕获异常都能落地日志并弹窗。
- **网络错误不走 throw 而是回调**：`RetrofitPresenter` 的所有公开方法均接受 `RetrofitPresenterResultListener`，错误一律通过 `onError(String)` 返回，不在 Presenter 中向上抛异常。
- **后端协议约定 code=200 为成功**：`MessageResult.code != 200` 即视为失败，错误信息拼接为 `"请求失败：" + response.body().msg` 或 `code + "_" + msg`。
- **崩溃日志仅在调试开关开启时落盘**：`StaticConstant.CRASH_DEBUG` 为 true 时才写外部存储文件并通过 `OtherUtils.notifySystemToScan` 通知系统扫描以便分享。
- **UI 提示统一通过 ToastUtils**：崩溃提示、网络错误提示等均使用 `com.blankj.utilcode.util.ToastUtils`，避免裸 `Toast`。
- **日志统一通过 Orhanobut Logger**：`GTJApplication` 中注册 `AndroidLogAdapter(PrettyFormatStrategy)`，网络层和 CrashHandler 中使用 `Logger.d(...)` 输出结构化日志。