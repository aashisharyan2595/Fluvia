# Fluvia Now Playing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (inline) to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Replace the Android expanded player with the Fluvia Now Playing screen from the approved hi-fi mockup, behind a setting, without touching the shared `PlayerScreen`.

**Architecture:** New Android-only composable `FluviaNowPlaying` in `androidApp/.../ui/fluvia/nowplaying/`. `MetrolistApp.kt` renders it instead of `PlayerScreen` when `Settings.shared.fluviaNowPlaying` is on. It reads `PlayerViewModel` state and calls its existing actions. Queue and sleep timer reuse the shared `QueueScreen(viewModel=…)` and `SleepTimerDialog`; lyrics reuse the existing lyrics view.

**Tech Stack:** Kotlin, Compose Material 3, Kyant0 Backdrop (via `fluviaGlass`), Coil via shared `AsyncImage`, shared `PlayerViewModel`.

**Spec:** `docs/superpowers/specs/2026-11-25-fluvia-now-playing-design.md`

## Global Constraints

- Android only; shared `PlayerScreen` and desktop/iOS untouched.
- No hardcoded user-facing strings: add English to `shared/src/commonMain/composeResources/values/strings.xml` only, use `stringResource(Res.string.<key>)`; never edit `values-*/strings.xml`.
- Preference key in `shared/.../constants/PreferenceKeys.kt`, accessed as `Settings.shared.<name>`.
- Reuse existing string keys where they exist: `lyrics`, `queue`, `cast`, `sleep_timer`, `play`, `pause`, `previous`, `next`, `shuffle`, `repeat`, `action_like`, `unlike`, `more_options`, `close`, `now_playing`.
- Glass via `Modifier.fluviaGlass(shape, text)`; Frosted/Solid fallback is built into it.
- Standard player's stock Material 3 `Slider` is not modified. Fluvia seek uses a hidden Material 3 `Slider` for TalkBack.
- Decorative waveform bars come from the song id, not audio. Spin and tonearm motion obey the system remove-animations setting.
- Imports, no inline fully qualified names; comments only for non-obvious logic; format with `ktlint --format`.
- Do not commit beyond local commits, push, or open a PR without the user's go-ahead.

## Review Focus

- No current song (queue empty): screen shows empty state, controls disabled, no crash.
- Song with null thumbnail: record label falls back to the Fluvia accent, no spin crash.
- Duration 0 or live stream: waveform inert, time labels show a dash, scrub does nothing.
- Listen Together guest: transport controls disabled as in the existing player.
- Rapid next/previous and swipe on the record: only one skip per gesture.

## Task 1: Setting and wiring

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/metrolist/shared/constants/PreferenceKeys.kt`, `shared/.../settings/Settings.kt`, `shared/src/commonMain/composeResources/values/strings.xml`
- Modify: `androidApp/.../ui/screens/settings/FluviaAppearanceSettings.kt` (a toggle in `fluviaThemeItems()`)
- Modify: `androidApp/.../ui/MetrolistApp.kt` near line 1984 (call site)

**Interfaces:**
- Produces: `Settings.shared.fluviaNowPlaying: Boolean` (default true); string keys `fluvia_now_playing`, `fluvia_now_playing_desc`.

- [ ] Add `FluviaNowPlayingKey = "fluviaNowPlaying"` and `var fluviaNowPlaying by preference(FluviaNowPlayingKey, true)`.
- [ ] Add the toggle row in `fluviaThemeItems()` using the same row helper as neighbouring switches.
- [ ] In `MetrolistApp.kt`, branch: when on, call `FluviaNowPlaying(viewModel, bottomSheetState, onClose, onNavigateToAlbum, onNavigateToArtist, onNavigateToListenTogether, onNavigateToIntegrations, onLyricsVisibleChange, isScreenVisible)`, else the existing `PlayerScreen(...)` unchanged.
- [ ] Build: `./gradlew :androidApp:compileDebugKotlin` passes.

## Task 2: Building blocks

**Files:** Create in `androidApp/.../ui/fluvia/nowplaying/`: `FluviaWaveform.kt`, `FluviaRecord.kt`, `FluviaControls.kt`

**Interfaces:**
- `fun waveformHeights(seed: String, count: Int = 48): List<Float>` (0.2..1.0, deterministic per seed)
- `@Composable fun FluviaWaveform(heights: List<Float>, progress: Float, enabled: Boolean, onScrub: (Float) -> Unit, onScrubEnd: (Float) -> Unit, durationMs: Long, modifier: Modifier)` shows glowing playhead and a glass scrub bubble with target time and delta; hidden `Slider` carries semantics.
- `@Composable fun FluviaRecord(artworkUrl: String?, isPlaying: Boolean, onSwipePrevious: () -> Unit, onSwipeNext: () -> Unit, modifier: Modifier)`: 276dp record, 124dp art label, tonearm drops on play.
- `@Composable fun FluviaTransport(isPlaying, isShuffleOn, repeatMode, enabled, onToggleShuffle, onPrevious, onPlayPause, onNext, onToggleRepeat)`: 48/56/88/56/48dp buttons, 88dp accent rounded-square play.

- [ ] Write a unit test for `waveformHeights`: same seed gives same list, size 48, all values within 0.2..1.0 (parser-like determinism; keep inside an existing androidApp test file if one covers fluvia, otherwise skip per CLAUDE.md Tests).
- [ ] Implement the three files per the interfaces and the mockup geometry in the spec.
- [ ] Build passes.

## Task 3: Screen

**Files:** Create `androidApp/.../ui/fluvia/nowplaying/FluviaNowPlaying.kt`

**Interfaces:**
- Consumes: Task 2 composables, `PlayerViewModel` (`uiState`, `currentPosition`, `togglePlayPause`, `next`, `previous`, `seekTo(progress, durationMs, songId)`, `toggleLike`, `toggleShuffle`, `toggleRepeat`, `setLyricsVisible`, `setQueueVisible`), `PlayerColorExtractor`.
- Produces: `@Composable fun FluviaNowPlaying(...)` with the signature in Task 1.

- [ ] Layout per spec: ambient album-colour blobs over `FluviaBackgroundLayer`, top bar (chevron, "Playing from" label, more), glass record card with page dots, title 28sp bold, artist 17sp, like button, waveform with time labels, transport row, bottom glass tray Lyrics/Queue/Cast/Sleep.
- [ ] Queue: `ModalBottomSheet` hosting `QueueScreen(onClose, onShowSleepTimer, viewModel)` when `uiState.isQueueVisible`.
- [ ] Sleep: `SleepTimerDialog` fed from `playerConnection.sleepTimerState` as in `PlayerScreen.kt` lines 524 and 2562.
- [ ] Lyrics: host the existing lyrics view when `uiState.isLyricsVisible`; call `viewModel.loadLyrics(song)` on song change as `PlayerScreen` does.
- [ ] Cast: invoke the same platform cast entry the existing player uses via `LocalPlayerPlatform`.
- [ ] Empty state, null artwork, duration 0 and guest handling per Review Focus.
- [ ] Build passes; `ktlint --format` on changed Kotlin.

## Task 4: Verify

- [ ] `./gradlew :androidApp:assembleDebug` exit 0; report ktlint and diff review (`git diff origin/main --stat`), no secrets or debug code.
- [ ] Ask the user before pushing; PR body per CLAUDE.md (few sentences, `AI usage:` line, no lists).
