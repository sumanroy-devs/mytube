# MyTube v2.2.1

**Release date:** 2026-09-23

MyTube is a free, open-source, ad-free YouTube and YouTube Music client for Android. This is the first release published under the MyTube name, based on [Flow 2.2.1](https://github.com/a-edev/Flow).

## What's new in MyTube

- Rebrand from Flow to MyTube throughout the app, docs and APK names; onboarding, the in-app splash and the About/Support/Donations screens are removed
- New release signing key — verify installs against the certificate fingerprint below
- Dedicated Library search across history, playlists, likes and downloads, with the search icon leading every Library top bar
- Search the media stored on your device: the Local media screen gets its own tab-aware search (Videos tab searches videos, Music tab searches music)
- Search, Notifications and Settings actions in the Shorts top bar
- Tighter Shorts action rail and auto play-next enabled by default
- Smarter defaults out of the box: background play, auto Picture-in-Picture, overlay play/pause/lock/speed buttons, bottom nav always visible, no feed refresh on Home re-tap
- Developer section in Settings; local media file URIs now survive manual next/previous
- CI restored: Spotless/ktlint formatting checks run on every push

## Highlights from Flow 2.2.1

- Native YouTube and YouTube Music playback with a NewPipeExtractor fallback
- Shorts player with comments, description, downloads, Picture-in-Picture and configurable playback modes
- SponsorBlock and DeArrow support, multiple audio tracks and full quality ladders
- Device sync across your devices, TV UI, Discord RPC
- Faster cold start, lower idle drain and baseline-profile performance work throughout
- Material 3 design with dynamic color and theme customization

## Verification

Release certificate SHA-256 fingerprint:
`DE:74:50:2F:9B:58:00:A5:E2:60:5C:D7:01:54:4C:8D:17:E4:7D:F5:51:E6:21:45:B6:4F:4A:3B:45:DD:94:68`

Every APK is signed with this certificate. `checksums.txt` in the assets lists the SHA-256 of each APK.
