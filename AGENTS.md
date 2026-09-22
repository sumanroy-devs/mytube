# Working with Flow as an AI agent

Flow (`io.github.aedev.flow`) — Android YouTube / YouTube Music client: Kotlin, Jetpack Compose Material 3, Hilt, Media3/ExoPlayer, native InnerTube client with NewPipeExtractor fallback, on-device recommendation engine (FlowNeuro). `CONTRIBUTING.md` is the full human-facing policy; the § names below point into it for rationale. This file is the operative digest.

## Layout

- `app/` — the whole app. One Gradle module; sources `app/src/main/java/io/github/aedev/flow/`. Entrypoints: `MainActivity`, `FlowApplication`.
- Packages: `ui/screens/<feature>/` (route + ViewModel), `ui/components/<feature>/`, `ui/components/shared/`, `ui/components/layout/`, `ui/theme/`, `data/`, `player/`, `di/`, `utils/`.
- `benchmark/` — `android.test` module: `BaselineProfileGenerator`, `StartupBenchmark`. There is no `baselineprofile/` module (older docs say so).
- Flavors `github` (default) / `foss`; build types `debug` / `nightly` (release-level minify, debug signing, sideloadable) / `release`. **Always flavor-prefixed tasks** — `:app:assembleGithubDebug`, `:app:compileFossDebugKotlin`, `:app:installGithubDebug` — never bare `assembleDebug`/`compileDebugKotlin`.
- Generated baseline profile is committed at `app/src/githubRelease/generated/baselineProfiles/`.
- `legacy/` — gitignored owner archive, not compiled. Never edit.
- `graphify-out/` — gitignored local knowledge graph. When `graph.json` exists: `graphify query "<question>"` first, `graphify path`/`graphify explain` for relationships, `graphify update .` after code changes.

## Verify — exact commands

```bash
./gradlew :app:assembleGithubDebug
```

- Single test: `./gradlew :app:testGithubDebugUnitTest --tests '*FooTest*'`.
- Compose UI tests run in that JVM unit task via Robolectric — no emulator needed. Instrumentation tests are **compile-only** in CI (`:app:compileGithubDebugAndroidTestKotlin`) and never run there; Room migration tests (androidTest) only run on a device you connect. Say so when you rely on them.
- Android lint never fails local builds (`abortOnError = false`); CI runs `:app:lintGithubNightly` / `:app:lintGithubRelease` explicitly.
- JDK 17; configuration cache + build cache on; 4 GB heap (`gradle.properties`).
- Baseline profile — regenerate **only** for cold-start-path changes (`MainActivity`, `FlowApplication`, app-level DI, theme resolution, player/cache init), generator-journey changes, Compose/Media3/AGP upgrades, or before a release tag: `./gradlew :app:generateGithubReleaseBaselineProfile` (~20 min on a physical device, or `-PbaselineProfileEmulator=true`). Never for routine work — thousands of lines of churn. Measure startup with `:benchmark:connectedBenchmarkReleaseAndroidTest` (`StartupBenchmark`), not profile size.

## Tooling (Ubuntu bash — verified on PATH)

- Search with `rg` (never `grep`) and `fdfind` (Ubuntu name for `fd`, never `find`); diffs via `delta`. File edits only via `read`/`edit`/`write` — never `cat`/`sed`/`echo` redirection into files.
- `gh` CLI is authenticated (`sumanroy-devs`) — use it for all GitHub operations.
- Device: `adb` + `scrcpy`, SDK `~/Android/Sdk` (`ANDROID_HOME`/`ANDROID_SDK_ROOT` set); JDK 17 at `/usr/lib/jvm/java-17-openjdk-amd64`. Install with `./gradlew :app:installGithubDebug`, never hand-built APK + `adb install`. UI changes must be exercised on a device/emulator when one is available — a passing build is not a working feature.
- `uv`/`uvx` for Python; `fzf` is interactive-only (needs a TTY) — never in agent commands.
- There is no dev server (the Ktor server code is in-app device sync). Verification is lint/build/test only; never boot release/signing builds or create keystores.

## Gotchas (verified, non-obvious)

- **Gitignored test fixtures**: `app/src/test/resources/{explore,shorts}/` are absent on a clean checkout, and their tests **skip** (JUnit `Assume`) — so a green unit-test run does *not* cover the explore/reel parsers. Regenerate only with the owner-local `notes/innertube-video-responses/*.py` probes against live YouTube. **Never hand-write a fixture** — a hand-written one once let four empty channel tabs ship. The tracked fixtures (`search/`, `channel/`, `sync/`, root `*.json`) stay tracked; don't relocate or gitignore them.
- DataStore is pinned to 1.1.1: 1.2.1 breaks DataStore unit tests on Windows. Don't bump it.
- Release signing: with no `release.keystore`, release builds silently fall back to **unsigned** APKs; CI pins the signer SHA-256 and hard-fails tag builds. Never regenerate the keystore, rotate the signing secrets, or rename `flow.apk`/`flow-foss.apk` (a public IzzyOnDroid contract). Details: CONTRIBUTING § Release and Signing Invariants.
- Room schemas are committed at `app/schemas/`. Schema edits need explicit instruction + version bump + migration (migration tests live in androidTest).
- Spotless uses `ratchetFrom`: any file you touch or move becomes fully ktlint-eligible (140 cols, import order, property naming) — the usual cause of a surprise red build during a refactor.
- JSON is `kotlinx.serialization`; Gson exists only for legacy DTOs and carries an R8 reflection hazard — don't add Gson code. `re2j` is declared but referenced from no first-party source — verify before using *or* removing it.
- No `.git` in this workspace copy — branch/pull/commit commands fail here; check `git rev-parse` first.

## Hard rules (never, unless explicitly asked)

- No commits, pushes, merges, or history rewrites; no version bumps; no Room schema edits; no edits to non-English `strings.xml` (Weblate owns them); no edits to markdown docs (README, CONTRIBUTING, this file).
- No hardcoded user-facing strings: declare in `app/src/main/res/values/strings.xml`, reference via `stringResource(...)`.
- Ask instead of guessing when a task is ambiguous. State exactly what was verified and what was not; never call a change "safe" or "behaviorally identical" off compilation/unit tests alone.

## UI and design (CONTRIBUTING § Material 3, § Anti-Slop)

- Material 3 only — never Material 2 components. All color/type/shape from `MaterialTheme` tokens. Consult current docs before implementing; training data lags this alpha M3 stack.
- Zero tolerance, will be reverted: gradients as surfaces/backgrounds; new glassmorphism or blur surfaces (existing player ambience only); hand-rolled glow/fake shadows; decorative colored card borders (use `outline`/`outlineVariant`); any inline `Color(0xFF…)`.

## Use the platform (CONTRIBUTING § Use the Platform)

- Before building anything: `graphify query`, read `ui/components/shared/` + `utils/`, read `gradle/libs.versions.toml` — re-implementing something already shipped is the commonest waste.
- Reach for first: M3 via the alpha `compose-bom-alpha`, `MaterialTheme.motionScheme`, `HapticFeedbackType`, `PullToRefreshBox`, `sh.calvin.reorderable`, Paging 3, Media3, WorkManager, Room + DataStore, `kotlinx.serialization`, OkHttp/Ktor, `java.time` (desugaring on, minSdk 26 — no `SimpleDateFormat`/`Calendar` in new code), Coil, Glance. Fix `SimpleDateFormat`/`Calendar` debt opportunistically when already in a file.
- Before using an alpha API, confirm it against the actual cached artifact (`unzip`/`javap` over the AAR/JAR; `./gradlew :app:dependencies` for what resolves) and the official docs.
- Hand-roll only when the API is verifiably absent (`ui/components/shared/FastScrollbar.kt` is the reference case) and say what you checked. Never fork/vendor library source, add overlapping dependencies, hand-roll crypto/TLS/security code, or re-invent Media3 player/session/notification wiring.

## Performance — non-negotiable (CONTRIBUTING § Performance; each rule is a shipped regression)

- Gate every continuous animation on its own layer's visibility; keep the warm composition (first-expand jank), never animate a hidden layer — this exact pattern caused a 30%-battery-in-90-min overheating. Audit new UI for `rememberInfiniteTransition`, `basicMarquee`, timer-retargeted `animate*AsState`, short-`delay` polling effects; each needs an answer for hidden/paused/screen-off.
- Read per-frame animated values in layout/draw (lambdas inside `Modifier.layout`/`graphicsLayer`/`drawBehind`), never in composition; use `derivedStateOf` for fraction-derived booleans.
- Position cadence is a contract: 1 Hz `playerState`; the 250 ms precise tick only via refcounted `acquirePreciseProgress`/`releasePreciseProgress`. Don't widen it, don't add a new high-frequency position flow.
- Event-driven over polling (polling suspends while paused); no new wakelocks or frame-clock work with the screen off.
- One fetch per cause — dedupe effects in warm-kept trees; read caches through, not around.
- Shared surfaces use `sharedMusicViewModel()`/`sharedMusicPlayerViewModel()` — never a per-route `hiltViewModel<MusicViewModel>()` (measured: ~60-call init re-runs per navigation). New ViewModels load lazily; no network floods from `init`.
- Bounded fan-out on `PerformanceDispatcher.networkIO` (4–16 threads); `stateIn(WhileSubscribed(5_000))` stays; no hot collectors outliving their surface.
- Heat with UI visible = per-frame work; drain with screen off = CPU/network loops — diagnose in that order, never by degrading visible design. No performance claims without measurement: `MusicBenchmarkTest`/`NeuroBenchmarkTest` floors for recommendation changes, `StartupBenchmark` for startup; player-path changes must add no latency, stalls, or flicker.

## Where code goes (CONTRIBUTING § Code Structure)

- Placement: `private` in one file (one use, <~40 lines) → sibling file in same `ui/screens/<feature>/` (`internal`) → `ui/components/<feature>/` (`internal`, ≥2 screens) → `ui/components/shared/` (`public`, ≥2 features). Promote on the **second real consumer**, demote when callers drop to one. A file in `ui/screens/` may never be imported by another feature.
- Budgets (split before landing): screen 250/400, component 300/500, ViewModel 400/600, other 400/600 (target/hard ceiling). Split by responsibility — leaves, pure logic out of ViewModels, group by surface — never `FooScreen2.kt`. Don't split files you aren't otherwise touching.
- Naming: `Flow*` prefix for domainless shared primitives, `Media*` for shared media vocabulary, feature prefix for feature components; file named after its primary export. Private `const` SCREAMING_SNAKE, private `Dp`/`Color` vals PascalCase (ktlint enforces).
- One `@HiltViewModel` per feature in `ui/screens/<feature>/`; extract pure logic to its own unit-testable file.
- Refactor hygiene: no dead code, no new comments (WHY only), and a refactor must not change pixels or behavior — when merging differing components, expose the difference as a parameter whose default preserves current output.

## Dependency injection (CONTRIBUTING § Dependency injection)

- Constructor injection by default; no new app-owned `getInstance()` calls (platform factories like `Calendar.getInstance()`/`WorkManager.getInstance()` are fine).
- Migrate incrementally only when already in scope: preserve scope/cardinality exactly (no second DB, scope, cache, player, or session), no blocking work in constructors/providers.
- The player path (`EnhancedPlayerManager`, `EnhancedMusicPlayerManager`, `ShortsPlayerPool`, services/sessions) is never an opportunistic migration — explicit task + end-to-end playback verification only.

## Testing

- Conventions live in `.agent/skills/unit-testing/SKILL.md`: MockK + `unmockkAll()` in `@After`, Truth `assertThat`, `runTest` + `Dispatchers.setMain`, backtick scenario names, test file mirrors the source under `app/src/test/java/...`.
- Add focused tests with new pure logic; report exactly which commands you ran and which checks you did not.

## Commits (when authorized)

- Pull latest `main` first (when git history is available). Conventional format: `type(scope): short description` — e.g. `feat(player): add gapless playback`.
