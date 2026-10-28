# Release checklist

- [ ] Build and install debug APK on supported Android versions.
- [ ] Test fresh install notifications with notification permission granted and denied.
- [ ] Test package update, uninstall, force-stop, reboot and battery-restricted states.
- [ ] Verify installer-source labels against Play Store and sideloaded APK test cases.
- [ ] Confirm signing-certificate SHA-256 wording is clear.
- [ ] Review `QUERY_ALL_PACKAGES` policy for intended distribution channel.
- [ ] Replace repository/contact placeholders in documentation.
- [ ] Review dependency and GitHub Actions updates.
- [ ] Sign release APK locally or with a secure CI secret; never commit keystore files.
- [ ] Publish checksums and source tag alongside release artifacts.
- [ ] Update privacy policy if permissions, data storage, or network behavior changes.
