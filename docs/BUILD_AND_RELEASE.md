# 构建与发布

## 日常验证

```bash
./gradlew clean assembleDebug assembleRelease
```

GitHub Actions 会在 `main`、`develop` 以及面向它们的 Pull Request 上执行同样的编译，并检查仓库中是否误提交 JKS、keystore、`keystore.properties` 或 `local.properties`。

## 正式发布

1. 提升 `versionCode`，并按版本计划修改 `versionName`。
2. 从 `develop` 向 `main` 提交发布 Pull Request。
3. 等待 Android CI 通过。
4. 使用离线保存的原长期 JKS 在本地生成正式签名 APK。
5. 核对包名、版本号、签名证书、APK 对齐和 SHA-256。
6. 将正式 APK、SHA-256 和更新日志上传到 GitHub Release。

## 安全边界

GitHub 只保存源码、原创资源、构建脚本和不含秘密的未签名构建产物。长期签名 JKS、别名密码和密钥密码不得进入仓库或 GitHub Actions Secret；正式签名继续在可信本地环境完成。
