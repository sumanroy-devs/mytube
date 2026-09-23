# Security Policy

## Supported versions

Security fixes land on the latest release line only. Older versions do not
receive backported patches.

| Version | Supported |
| ------- | --------- |
| 2.2.x   | ✅ |
| < 2.2.0 | ❌ |

Always update to the newest release before reporting a security issue.

## Reporting a vulnerability

**Do not report security vulnerabilities through public GitHub issues.**

Report privately through
[GitHub Security Advisories](https://github.com/sumanroy-devs/mytube/security/advisories/new).
If you cannot use that form, email <sumanroy.devs@outlook.com>.

Please include:

- Type of issue (for example: credential exposure, path traversal, injection).
- Full paths of the source files involved.
- The affected tag, branch, or commit.
- Any configuration required to reproduce the issue.
- Step-by-step reproduction instructions.
- Proof-of-concept or exploit code, if available.
- Impact, including how an attacker might exploit it.

You will receive an acknowledgement within 48 hours and a timeline for a fix.
Please do not disclose the issue publicly until a fix has shipped.

## Verifying release APKs

Official MyTube builds are signed with a single release key. Any APK that does
not match the fingerprint below is not an official build, regardless of where
it was downloaded.

```
SHA-256: DE:74:50:2F:9B:58:00:A5:E2:60:5C:D7:01:54:4C:8D:17:E4:7D:F5:51:E6:21:45:B6:4F:4A:3B:45:DD:94:68
```

Verify a downloaded APK with the Android SDK build tools:

```bash
apksigner verify --print-certs mytube-foss.apk
```

The reported `Signer #1 certificate SHA-256 digest` must equal the fingerprint
above (lower case, without colons).

Official distribution is the
[GitHub Releases page](https://github.com/sumanroy-devs/mytube/releases). Builds
obtained anywhere else are unverified.
