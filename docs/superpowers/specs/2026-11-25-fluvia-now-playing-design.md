# Fluvia Now Playing design

## Intent

Make the full-screen Now Playing screen match the approved hi-fi mockup ("Fluvia hi-fi mockups", artboards "Now playing, dark" and "Now playing, light"). It is the screen the owner looks at most, and it should read as Apple-style glass with Spotify-style clarity in the Fluvia colours.

## Goals

- A glass record-player layout on Android, built from the mockup's frame (390 by 844 dp reference).
- Everything reads from Fluvia tokens, so Appearance settings (accent, glass tier, corner style, spacing density, text scale, icon pack) change the screen.
- Works in dark and light looks, and falls back cleanly on the Frosted and Solid glass tiers.

## Non-goals and anti-goals

- No change to desktop or iOS. The player code is shared, so the new screen is Android-only and opt-in.
- No change to the default (Standard) player or its seek bar. The Standard seek bar stays the stock Material 3 `Slider`, as the repo rules require.
- Not Home, Search, Library, widgets or the lock screen. They are separate tasks.
- Anti-goal: a screen that looks right in a still image but drops frames or becomes unreadable on a weak device.

## The screen (from the mockup)

- Background: slow ambient colour blobs taken from the album art, over the Fluvia background layer.
- Top bar: minimise chevron, "Playing from" label with the source name, more-options button.
- Record card: a glass card holding a spinning record. The album art is the record label. A tonearm swings onto the record when playback starts and lifts when it pauses. Swiping the card left or right changes the song, with page dots under the record.
- Title (28 sp, bold), artist (17 sp) and a like button.
- Seek bar: a waveform of 48 bars with a glowing playhead. Dragging shows a glass bubble with the target time and the change from the current position.
- Controls: shuffle, previous, a large accent-coloured Play/Pause button, next, repeat.
- Bottom glass tray: Lyrics, Queue, Cast and Sleep timer, each opening the existing sheet.

## Approaches considered

1. New Android-only player composable in `androidApp`, chosen from the existing player-style setting. The shared `PlayerScreen` is untouched. Cost: some duplicated wiring to playback state. Recommended, because it cannot break desktop or iOS.
2. Add Fluvia flags to the shared `PlayerScreen`. Cost: about 2,600 lines of shared code gain Android-only branches and risk regressions elsewhere.
3. Copy `PlayerScreen` into Android and edit it. Cost: two diverging copies of the player logic.

Approach 1 is the call proposed here; it needs the owner's approval with this document.

## Constraints from the repo

- New user-facing text goes in `shared/src/commonMain/composeResources/values/strings.xml` (English only) and is read with `stringResource`.
- Preference keys live in `PreferenceKeys.kt` and are read through `Settings.shared`.
- No hardcoded locale defaults, no inline fully qualified class names, imports only.
- No new tests unless the change falls in the repo's test list. It does not, so verification is a CI build and a check on the emulator.

## Calls made here, to confirm

- The waveform bars are decorative: their heights are generated from the song id so each song looks the same every time. They are not real audio data. If the owner prefers a plain progress bar, the bars become one smooth fill.
- The waveform replaces only the Fluvia player's seek bar. For screen readers the screen keeps an invisible Material 3 `Slider` carrying the seek value and label, so TalkBack users can still seek.
- Blur needs Android 12 and lens effects need Android 13. Below that the card uses the Frosted or Solid tier from the existing glass resolver.
- The record spin and tonearm motion follow the system "remove animations" setting.

## Left to the builder

Which existing setting entry selects the new style, how the album colours are extracted (an extractor already exists), the exact dp-to-layout mapping, and haptics on scrub.
