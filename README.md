# Fit (纯本地私人减脂 Android 客户端)

<p align="center">
  <b>自律 · 触感 · 纯粹 (Disciplined · Tactile · Pure)</b><br>
  一款数据严格存在本地、零网络权限、遵循 Impeccable 工业实用极简风的减脂运动规划与打卡客户端。
</p>

---

## ✨ 核心特性

- **🔒 100% 离线与绝对隐私**
  - `AndroidManifest.xml` 中**彻底剔除 `INTERNET` 权限**，数据完全存储在本地 SQLite (Room) 数据库中，零网络依赖、零云端同步、零第三方数据收集。
- **🥗 智能饮食录入与热量换算**
  - 内置 50+ 种高频食物数据库，支持拼音与汉字检索；
  - 支持按克重、份量快捷录入（+50g、+100g、1碗、1个），支持自定义新食物并持久化；
  - 采用 Mifflin-St Jeor 代谢公式结合活动系数，自动推导今日需通过运动消耗的目标卡路里。
- **⚖️ 动态守恒运动分解调节舱**
  - 内置跳绳、跑步（400m操场跑道）、游泳、骑行、深蹲等运动，自动按等热量权重均分并规整为离散生活化单位；
  - **热量绝对守恒**：微调某一运动量时，系统根据架构护轨实时联动其余未锁定运动反向对冲，确保总运动消耗严格守恒；
  - 提供单项锁定（Pin）功能与量子残差吸收端（Slack Absorber），消除取整抖动。
- **🎨 Material 3 Expressive 设计系统 (严格遵循 UI.txt 规范)**
  - 专属自然绿意配色（浅色主色 `#2E6A3D`，深色主色 `#96D5A1`），完全跟随 Android 系统深浅色模式切换；
  - 标准 Expressive 弹性动效（`MotionScheme.expressive()`）与圆角规范（胶囊按钮、188dp/14dp 饮食摄入卡片、20dp 推荐运动卡片、28dp 对话框、16dp FAB）；
  - 顶部 64dp “FIT” 应用栏配备 `verified` 本地认证档案与 `calendar_month` 日历历史回溯；
  - 动态波浪（Wavy）线性进度条与运动复选打卡联动；单行非折叠 FAB 组合布局（`edit` 方案调整与向右拉伸的 `exercise` 快速开始）。

---

## 🛠 技术架构

- **语言**: Kotlin 2.0.21
- **UI 框架**: Jetpack Compose + Material 3 (Expressive Tokens)
- **本地存储**: Android Jetpack Room 2.6.1 (SQLite 纯本地持久化)
- **架构模式**: MVI / Clean Architecture (Unidirectional Data Flow via `flatMapLatest`)
- **测试体系**: 严格 DevFlow TDD 规范，单元测试全绿覆盖核心计算与状态转换
- **交付产物**: 已签名 Release APK (`app-release.apk`)

---

## 📥 下载安装

安装最新构建的已签名 Release APK 至安卓手机即可使用：
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## 🏗 构建与测试

要求环境：JDK 17/21，Android SDK 35。

```bash
# 运行全部单元测试
./gradlew testDebugUnitTest

# 构建已签名 Release APK
./gradlew assembleRelease
```

---

## 📜 开源协议

MIT License
