# Architecture

- `MainActivity.kt`: Jetpack Compose app inventory and search.
- `OverviewActivity.kt`: Compose metadata overview.
- `AppInfo.kt`: PackageManager metadata mapping and display formatting.
- `PackageChangeReceiver.kt`: receives package-added/replaced broadcasts and avoids treating replacements as new installs.
- `InstallNotifications.kt`: notification channel and overview PendingIntent.
- `.github/workflows/android.yml`: CI build and APK artifact publishing.

The implementation intentionally avoids an always-running foreground service. Android background execution and package visibility vary by OS version and device vendor, so behavior must be validated on real devices.
