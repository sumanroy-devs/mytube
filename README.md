## Why MyTube?

Most open-source YouTube clients give you playback but no way to discover new content. You either use the official app and get tracked, or you use an alternative and lose recommendations entirely.

MyTube gives you both. The recommendation engine learns what you like by analyzing your watch behavior locally. It never leaves your devices. You can inspect everything it knows about you, adjust it, or wipe it at any time.

---

## Features

### Video

- High-quality playback via ExoPlayer (Media3) with resolution switching (1080p, 720p, 480p, 360p)
- SponsorBlock — automatically skips sponsors, intros, outros, and filler
- DeArrow — replaces clickbait thumbnails and titles with community-sourced alternatives
- Return Youtube Dislike (RYD)
- Background playback — listen to audio with the screen off
- Picture-in-Picture — keep watching while using other apps
- Casting to smart TVs and streaming devices
- Playback speed control (0.25x to 2x)
- Video chapters with seek jumping
- Gesture controls for brightness, volume, and seeking
- Subtitles with customizable font size, color, and background
- Downloads with VP9, AV1, and standard format support
- Resume playback from where you left off

### Music

- Dedicated music player with album art and audio visualizations
- Queue management with add, remove, and reorder
- Shuffle and repeat (single/all)
- Persistent mini player across the app
- Synchronized lyrics display
- Fetches tracks from YouTube Music

### Recommendations (FlowNeuro Engine)

- Runs 100% on-device — no server, no telemetry, no account needed
- Learns from what you watch, skip, like, dislike, search for, and how long you watch
- Distinguishes weekday and weekend patterns, morning and night preferences
- Detects when you're getting bored of a topic and mixes in new content
- Prevents your feed from collapsing into the same 2-3 topics
- Surfaces related videos from your recent watches to create natural topic transitions
- Uses engagement signals (like-to-view ratios) to filter out low-quality content
- Full transparency dashboard — see what the algorithm knows and why it recommended something
- Export/import your entire recommendation profile as a file

### Library

- Local watch history
- Favorites and custom playlists
- Shorts feed with bookmarking
- Continue watching shelf
- Subscription management with cached feeds

### Privacy

- No Google account required
- No ads, analytics, or tracking
- All data stored locally on your device
- Import subscriptions and history from NewPipe
- Export or delete everything at any time

### Appearance

- 11 themes: Light, Dark, OLED Black, Ocean Blue, Forest Green, Sunset Orange, Purple Nebula, Midnight Black, Rose Gold, Arctic Ice, Crimson Red
- Built entirely with Jetpack Compose and Material 3

---

## Screenshots

<div align="center">
  <table>
    <tr>
      <td align="center"><b>Home Feed</b><br><img src="Assets/Home.jpeg" width="240"></td>
      <td align="center"><b>Video Player</b><br><img src="Assets/VideoPlayer.jpeg" width="240"></td>
      <td align="center"><b>Personality Screen</b><br><img src="Assets/Personality.jpeg" width="240"></td>
    </tr>
    <tr>
      <td align="center"><b>Music Player</b><br><img src="Assets/MusicPlayer.jpeg" width="240"></td>
      <td align="center"><b>Music Hub</b><br><img src="Assets/Music.jpeg" width="240"></td>
      <td align="center"><b>Your Library</b><br><img src="Assets/Library.jpeg" width="240"></td>
    </tr>
    <tr>
      <td align="center"><b>Shorts</b><br><img src="Assets/Shorts.jpeg" width="240"></td>
      <td align="center"><b>Subscriptions</b><br><img src="Assets/Subscriptions.jpeg" width="240"></td>
      <td align="center"><b>Channel View</b><br><img src="Assets/Channel.jpeg" width="240"></td>
    </tr>
    <tr>
      <td align="center"><b>Artist Page</b><br><img src="Assets/Artist.jpeg" width="240"></td>
      <td align="center"></td>
      <td align="center"></td>
    </tr>
  </table>
</div>

---

## Download

### Stable Release

<div align="center">
  <table border="0">
    <tr>
      <td align="center" style="vertical-align: middle; padding: 10px;">
        <a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/sumanroy-devs/mytube/">
          <img src="https://github.com/ImranR98/Obtainium/blob/main/assets/graphics/badge_obtainium.png" alt="Obtainium" height="60">
        </a>
      </td>
      <td align="center" style="vertical-align: middle; padding: 10px;">
        <a href="https://github.com/sumanroy-devs/mytube/releases/latest">
          <img src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" alt="GitHub" height="90">
        </a>
      </td>
    </tr>
  </table>
</div>

### Nightly / Debug Build
>
> ⚠️ Nightly builds are unstable and may contain bugs. Use at your own risk.

<div align="center">
  <a href="https://nightly.link/sumanroy-devs/mytube/workflows/build/main/mytube-nightly-apk.zip">
    <img src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" alt="Download from GitHub" height="75">
  </a>
  <p><b>No GitHub account required</b> — powered by <a href="https://nightly.link">nightly.link</a></p>
</div>

### Requirements

**Minimum Requirement:** Android 8.0+
<a id="cert"></a>

### Verifying Authenticity

To ensure the authenticity of the APK and verify it has not been tampered with, you can check the signing certificate fingerprint using tools like [AppVerifier](https://github.com/soupslurpr/AppVerifier).

**Release Certificate SHA-256 Fingerprint:**
`DE:74:50:2F:9B:58:00:A5:E2:60:5C:D7:01:54:4C:8D:17:E4:7D:F5:51:E6:21:45:B6:4F:4A:3B:45:DD:94:68`

---

<a id="support--donations"></a>

## 💰 Support Development

MyTube is a free and open-source project. As an independent developer without traditional banking access, keeping this project alive relies entirely on community support.

**You can now easily support the project using a Credit Card, Apple Pay, or PayPal via Patreon!** (You can choose to support monthly, or just leave a simple one-time tip in the shop).

<a href="https://patreon.com/A_EDev" target="_blank" rel="noreferrer noopener">
  <img src="https://img.shields.io/badge/Patreon-Support_MyTube-FF424D?style=for-the-badge&amp;logo=patreon&amp;logoColor=white" alt="Support MyTube on Patreon">
</a>

<br>

**Prefer to send Crypto directly?**
If you already use crypto, you can send it directly to my wallets below:

| Coin         | Network      | Address                                                                                           |
| :----------- | :----------- | :------------------------------------------------------------------------------------------------ |
| **USDT**     | TRC20 (Tron) | `TRz7VDrTWwCLCfQmYBEJakqcZgbFNWfUMP`                                                              |
| **Bitcoin**  | BTC          | `bc1qgmkkxxvzvsymtpfazqfl93jw6k4jgy0xmrtnv8`                                                      |
| **Ethereum** | ERC-20       | `0xfbac6f464fec7fe458e318971a42ba45b305b70e`                                                      |
| **Solana**   | SOL          | `7b3SLgiVPb8qQUvERSPGRWoFoiGEDvkFuY98M1GEngug`                                                    |
| **Monero**   | XMR          | `8AgaxZnpEvT8VXJpczpL7BQejwSEw97saJmKYqq4zKErbe9bkYSwUhJ813msPPbdYhF11oz4N7tfEj4Zi6k27fKD83ca1if` |

*Your support helps me maintain the project and add amazing new features!*

---

## 🙏 Acknowledgments

MyTube stands on the shoulders of giants. Special thanks to:

- **[NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor):** The backbone of our data extraction.
- **[NewPipe](https://github.com/TeamNewPipe/NewPipe):** For inspiration from their solid foundation for YouTube data handling.
- **[PipePipe](https://codeberg.org/NullPointerException/PipePipe):** For their SABR and InnerTube playback implementation, which guided MyTube's YouTube streaming pipeline.
- **[PipePipe Developer Docs](https://priveetee.github.io/Docs-PipePipe/):** For their reference documentation on SABR, BotGuard/PoToken attestation, and InnerTube extraction internals.
- **[MetroList](https://github.com/MetrolistGroup/Metrolist):** Inspiration for the Hybrid Music fetching approach, Lyrics handling and some icons design references.
- **[LibreTube](https://github.com/LibreTube/LibreTube):** Inspiration for SponsorBlock and DeArrow handling and some icons design references.
- **[ExoPlayer](https://github.com/google/ExoPlayer):** The gold standard for Android media playback.
- **[Jetpack Compose](https://developer.android.com/jetpack/compose):** For enabling the beautiful, modern UI.
- **[Material Design 3](https://m3.material.io/):** For the design system and guidelines.

---

<div align="center">

<a id="translate"></a>

## Translations

Help translate MyTube into your language!

[![Translation status](https://hosted.weblate.org/widget/flow/strings/287x66-grey.png)](https://hosted.weblate.org/engage/flow/)

[![Translation status](https://hosted.weblate.org/widget/flow/strings/horizontal-auto.svg)](https://hosted.weblate.org/engage/flow/)
</div>