# Privacy Policy

_Last updated: 2026-10-28_

InstallSentry is designed to work offline and process app metadata locally on the device.

## Data collected and transmitted

The application does not intentionally collect or transmit personal data to a developer-operated server. It does not include analytics, advertising, account sign-in, or telemetry code. The Android manifest does not request the `INTERNET` permission.

## Local processing

To provide its core features, the app reads package metadata exposed by Android, such as app labels, package names, version information, installer-source metadata, APK file size/path, signing-certificate fingerprint, and declared permissions. It may display this information and create a local Android notification when a package-added broadcast is delivered. The app does not upload this information.

## Notifications

On Android 13 and later, Android requires notification permission. If permission is denied, install notifications cannot be displayed. The app can still show the installed-app list when opened.

## Third-party services

The app itself does not connect to third-party services. Builds downloaded from third-party distribution sites or modified by other parties may differ from this source code; verify provenance and checksums when available.

## Data retention and deletion

The current MVP does not maintain a server-side database or a separate persistent history of install events. Android manages the notification and package metadata. Uninstalling the app removes its app-private data according to Android behavior.

## Children's privacy

The project does not intentionally collect personal data from children or adults.

## Changes

This policy should be updated whenever new features, permissions, dependencies, or data flows are introduced. Open a repository issue if you believe the implementation does not match this policy.

## Contact

For privacy questions, use the issue tracker associated with the repository where you obtained this source. The repository owner should replace this instruction with a monitored contact method before public distribution.
