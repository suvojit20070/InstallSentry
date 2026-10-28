# InstallSentry

**An offline-first Android app installation monitor built with Kotlin and Jetpack Compose.**

InstallSentry displays installed-app metadata and can show a notification when Android delivers a package-added broadcast while the app is installed and notifications are permitted.

> **Project status:** early open-source MVP. Build and test on your target Android versions before relying on it. This is not an antivirus, malware detector, or guaranteed system-wide event logger.

## Features

- Offline UI; no app account, analytics SDK, advertising SDK, or network permission.
- Installed app list with name, package ID, version and reported base APK size.
- Overview screen with installer source (when available), first-install/update timestamps, SDK target, APK path, declared permissions and signing-certificate SHA-256 fingerprint.
- Notification on supported `PACKAGE_ADDED` broadcasts, opening the matching overview.
- GitHub Actions workflow builds debug and unsigned release APK artifacts.
- No root, Accessibility Service, VPN, or device-admin privileges.

## Build

Requirements: JDK 17 and Android SDK / Android Studio with Android SDK Platform 35.

```bash
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

The included GitHub Actions workflow runs on push, pull requests and manual dispatch. Open **Actions → Android CI → workflow run → Artifacts** to retrieve APKs. The release APK is unsigned; sign it with your own private key before distribution. Never commit keystores or signing passwords.

## Install-source and hash semantics

- Android's installer-source API may return no value or an installer package name, not a human-friendly store name. It does not always prove the original download URL or source.
- The displayed SHA-256 is the SHA-256 fingerprint of the app signing certificate, **not** the APK file hash. APK file hashing can be added separately if needed.
- Reported APK size is the base APK file size. Split APKs, app data, cache, and downloaded assets may not be included.
- Permission list contains requested/declaration metadata; it does not mean each permission is granted or currently used.

## Platform limitations

Android package broadcasts are subject to platform and device behavior. Notification permission is required on Android 13+. Force-stop, restricted background execution, vendor battery management, work profiles, app visibility restrictions, and Android-version differences can affect alerts or visible metadata. This app intentionally does not run a persistent foreground service. It cannot guarantee an immediate alert for every install on every device. Launching the app refreshes the visible installed-app list.

`QUERY_ALL_PACKAGES` is declared because the app's core function is to display a local installed-app inventory. Distribution stores may require a policy declaration or may reject broad package visibility depending on their rules. Review the store policy before publishing there.

## Privacy and security

See [PRIVACY.md](PRIVACY.md), [SECURITY.md](SECURITY.md), and [docs/FEATURES.md](docs/FEATURES.md). The app has no `INTERNET` permission and contains no network client. App metadata is read locally from Android's PackageManager. Please audit the source and dependencies before shipping builds.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Please open an issue for bugs and feature proposals before submitting large changes.

## License

MIT — see [LICENSE](LICENSE).
