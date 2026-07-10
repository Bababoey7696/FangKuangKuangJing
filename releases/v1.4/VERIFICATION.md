# 《方块矿境》v1.4 手机平衡版交付与核验报告

## 交付信息

- 应用名称：方块矿境 / Block Mine
- 包名：`com.ame.blocktetris`
- versionName：`1.4`
- versionCode：`7`
- minSdk：21
- targetSdk / compileSdk：35
- Android Gradle Plugin：8.6.1
- Gradle Wrapper：8.7
- 正式 APK 大小：2688817 bytes
- 完整工程 ZIP 大小：2664080 bytes

## 本次核心优化

### 1. 代码结构

- 新增 `GameBalance.java`，集中管理速度曲线、落地缓冲、技能消耗、冷却和技能配置。
- 新增 `ThemeCatalog.java`，集中管理皮肤名称、解锁条件和世界配色。
- 新增 `RunMissions.java`，单独管理本局任务进度、奖励和去重。
- 主 `BlockTetrisView` 继续负责 Canvas 绘制与状态协调，平衡参数不再大面积散落。

### 2. 手机高分阶段速度

采用类似 v1.2 手感的平滑分段曲线，不再使用 v1.3 后期 1 帧/格的主机极限速度：

| 等级 | 休闲模式 | 标准模式 | 极限模式 |
|---:|---:|---:|---:|
| 1 | 1098ms/格 | 900ms/格 | 738ms/格 |
| 10 | 494ms/格 | 405ms/格 | 332ms/格 |
| 20 | 278ms/格 | 228ms/格 | 187ms/格 |
| 30 | 214ms/格 | 175ms/格 | 144ms/格 |
| 40 | 210ms/格 | 165ms/格 | 127ms/格 |
| 高等级封顶 | 210ms/格 | 165ms/格 | 125ms/格 |

新增触底缓冲：高等级方块落地后仍保留约 280–650ms 的横移和旋转时间；横移和旋转可有限次数重置缓冲，兼顾手机容错与游戏公平。

### 3. 新技能和成长

- 新主动技能“稳速器”：60% 能量，8 秒降低自动下落速度。
- 新主动技能“震荡波”：85% 能量，震碎最高堆叠处最多 10 块。
- 三套技能配置：均衡探索、生存稳速、爆破采掘。
- 新被动能力：稳固落地、遗迹护盾、能量回收。
- 新本局任务：消行、连击、使用技能；每项完成奖励 1 枚遗迹碎片。

### 4. 皮肤和显示修复

- 新增月影矿坑、樱晶洞窟、矩阵矿井，总计 9 套皮肤。
- 皮肤页面支持上下滑动。
- 开局页面直接显示当前模式的本局任务和技能配置。
- 修复旧版本号文字、设置文字越界、按钮长文字显示和小屏布局问题。

## 构建与兼容核验

- GitHub Actions 使用 Java 17、Gradle 8.7、Android SDK 35 执行 Debug 与 Release 构建成功。
- APK 元数据：`com.ame.blocktetris` / versionCode 7 / versionName 1.4。
- 源码未声明 `android.permission.INTERNET`，保持完全离线。
- 完整工程包含 `gradlew`、`gradlew.bat`、`gradle-wrapper.jar` 和 `gradle-wrapper.properties`。
- 工程不包含 JKS、keystore、密码、`keystore.properties` 或私人 `local.properties`。
- APK 所有未压缩条目通过 4 字节对齐检查。
- v1、v2、v3 APK 签名验证全部通过。
- 新版证书 SHA-256 与 v1.3 正式版完全一致：`0E:84:A1:F4:2B:2D:91:86:42:0D:C0:04:70:3D:62:18:68:4C:CE:58:8D:36:B5:A7:51:42:7E:9E:8E:9F:BA:A1`
- 包名、签名证书保持一致，versionCode 从 6 提升到 7，因此可以覆盖安装 v1.3 并保留同一应用数据目录中的记录。

## 自动测试结果

- 速度曲线单调加速和三个模式封顶值：通过。
- 落地缓冲上下限：通过。
- 本局任务触发与重复奖励拦截：通过。
- 皮肤数量与索引范围：通过。
- Gradle Wrapper 完整性：通过。
- GitHub Android Debug/Release 编译：通过。

## 测试边界

当前构建环境没有连接实体 Android 手机，因此无法替代实际触屏手感测试。建议首次安装后重点体验标准模式 20–40 级、暂停恢复、冻结结束、触底横移/旋转、皮肤列表滑动和三套技能配置。

## SHA-256

- 正式 APK：`4cca83a10ce55df1d39923d4804caff94d2c2e717a6657081fffa8bf9d8bfde5`
- 完整工程 ZIP：`40f7987c1b5995531cf84f9ebbd0f2bd96812f653832319c072458389fa5caa2`
