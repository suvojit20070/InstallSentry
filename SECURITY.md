# Security Policy

## Scope

InstallSentry is an early-stage utility, not a security scanner or antivirus. Do not rely on it to detect malware or guarantee complete installation monitoring.

## Reporting a vulnerability

Please do not publish exploitable details in a public issue before maintainers have had a reasonable opportunity to assess the report. Contact the repository maintainer through a private security advisory on GitHub, if enabled. The maintainer should configure a contact channel before publishing a production release.

## Safe development

- Do not commit signing keys, passwords, access tokens, or private data.
- Keep dependencies updated and review dependency changes.
- Do not add network permissions, analytics, accessibility monitoring, or background services without documenting the purpose and privacy impact.
- Verify GitHub Actions permissions and review workflow changes in pull requests.
