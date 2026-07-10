# 方块矿境 Block Mine

原生 Android Java 下落方块游戏，采用 `View + Canvas` 绘制，完全离线、无广告、无联网权限。

## 当前稳定版本

- `versionName`: 1.4
- `versionCode`: 7
- 包名：`com.ame.blocktetris`
- minSdk：21
- targetSdk / compileSdk：35
- Android Gradle Plugin：8.6.1
- Gradle Wrapper：8.7

## v1.4 核心内容

- 手机平滑封顶速度：休闲 210ms/格、标准 165ms/格、极限 125ms/格。
- 触底缓冲约 280–650ms，高分阶段仍可横移和旋转。
- 三套技能配置、单局任务、9 套皮肤。
- `GameBalance`、`RunMissions`、`ThemeCatalog` 分离平衡、任务和主题数据。
- 支持从 0 分、历史最高分、最近一局或当前分数继续练习。

## 构建

1. 安装 Android Studio 和 Android SDK 35。
2. 克隆仓库并打开根目录。
3. 等待 Gradle 同步完成。
4. 调试构建：`./gradlew assembleDebug`
5. Release 构建：`./gradlew assembleRelease`

音频均由 `tools/generate_audio.py` 离线合成，仓库中已包含生成后的 WAV；需要重新生成时运行：

```bash
python3 tools/generate_audio.py
```

## 分支规则

- `main`：可发布、可编译的稳定版本。
- `develop`：下一版本集成分支。
- `feature/*`：单项功能分支。
- `fix/*`：问题修复分支。

详细流程见 `docs/长期开发指南.md`，规划见 `docs/ROADMAP.md`。

## 安全

仓库禁止提交 JKS、keystore、密码、`local.properties` 或私人 SDK 路径。正式签名密钥只离线保存。

v1.4.2 新增可切换的 v1.2 经典触控与 v1.4.1 稳健触控；保留原有四首背景音乐，在播放端削弱高频底噪，并修复快速落地与锁定音叠加。
