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
- **📋 底部可抽拉任务舱 (Anchored Bottom Sheet)**
  - 确认计划后生成底部常驻卡片，展示今日摄入 vs 目标消耗与液体感完成进度；
  - 向上拖拽平滑展开当日任务清单，子卡片左侧配备自定义触感微振动圆环（`CLOCK_TICK` 反馈），点击平滑划线打卡。
- **🎨 Impeccable 设计系统**
  - 暖骨白/深炭灰中性色调，单个低饱和赤陶焦橙（`#D9532F`）强调色；
  - 全局数据指标采用 **Tabular Monospace（等宽数字）**，动态滑动调节数值时**界面毫秒级零抖动**。

---

## 🛠 技术架构

- **语言**: Kotlin 2.0.21
- **UI 框架**: Jetpack Compose + Material 3 + Impeccable Design Token
- **本地存储**: Android Jetpack Room 2.6.1 (SQLite)
- **架构模式**: MVI / Clean Architecture (Domain, Data, UI 严格分层)
- **测试体系**: TDD 先行，100% 覆盖核心代谢与守恒重平衡用例

---

## 📥 下载安装

前往 [GitHub Releases](https://github.com/yugusu704-lang/Fit/releases) 下载最新版的 `app-debug.apk`，安装至安卓手机即可使用。

或者通过 ADB 快速安装：
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🏗 构建与测试

要求环境：JDK 17/21，Android SDK 35。

```bash
# 运行单元测试
./gradlew testDebugUnitTest

# 打包 Debug APK
./gradlew assembleDebug
```

---

## 📜 开源协议

MIT License
