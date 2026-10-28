# Feature Guide

## Home screen
Shows apps visible to InstallSentry through Android's package visibility APIs. Search by display name or package ID, refresh the list, and tap an item to open its details.

## New-install notifications
When Android sends `ACTION_PACKAGE_ADDED` for another package and the app can read that package's metadata, InstallSentry attempts to post a notification. Tapping the notification opens the corresponding overview screen. Updates are not intended to be treated as fresh installs.

## Overview fields
- **App name / package:** label and unique package identifier reported by Android.
- **Version:** version name and numeric version code.
- **App size:** length of the base APK file, when accessible.
- **Installer source:** installer package name reported by Android, if available.
- **First installed / last updated:** timestamps reported by PackageManager.
- **SDK levels:** target SDK and minimum SDK when available.
- **APK path:** local path returned by Android.
- **Signing SHA-256:** digest of the signing certificate bytes.
- **Declared permissions:** permission names requested in package metadata.

## What it does not do
- Does not determine an app's purpose using AI.
- Does not scan app code for malware.
- Does not promise to identify the exact website or person that supplied an APK.
- Does not guarantee alerts on every Android device or after force-stop.
- Does not claim declared permissions are currently granted or used.
- Does not send app inventory to a server.

## Planned, not implemented
- Optional local install-event history.
- APK file SHA-256 calculation with explicit user action.
- Better app icons and filtering for system/user apps.
- Instrumented tests across Android versions.
