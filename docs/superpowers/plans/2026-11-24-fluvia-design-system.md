# Fluvia Design System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the Android app's Metrolist theme with a Fluvia theme driven by user Appearance settings (glass, accent, background, shapes, fonts, icon packs), with a text-contrast guard.

**Architecture:** `FluviaTokens` is built from `Settings.shared` and provided through a `CompositionLocal`. `FluviaTheme` replaces the Android `MetrolistTheme` and derives Material's `colorScheme`, `typography` and `shapes` from the tokens, so unmigrated Material components follow. Shared settings keys live in `shared`; Fluvia styling lives in `androidApp/.../ui/fluvia/`. The shared (desktop/iOS) `MetrolistTheme` is untouched.

**Tech Stack:** Kotlin, Jetpack Compose Material 3 Expressive, `Settings.shared` preferences, Gradle, ktlint. Glass library chosen in Task 4 (Kyant0 Backdrop or Haze).

**Spec:** `docs/superpowers/specs/2026-11-24-fluvia-design-system-design.md`

## Global Constraints

- No hardcoded user-facing strings. English only in `shared/src/commonMain/composeResources/values/strings.xml`; never edit `values-*/strings.xml`.
- Preference keys in `shared/.../constants/PreferenceKeys.kt`, read as `Settings.shared.<name>`.
- No hardcoded language or country defaults; no inline fully qualified class names; match existing style.
- No automated tests. `CLAUDE.md` allows tests only for parsers, migrations, backup/restore, crypto and protocols. Verification is build, ktlint and device checks.
- Do not commit, push or open a PR without explicit human approval. Do not bump `appVersion`. Three PRs: Tasks 1–3 (theme and tokens), Tasks 4–6 (glass, fonts, icon packs), Tasks 7–8 (Appearance screen and Looks, which list the fonts and packs). Conventional Commit titles; PR bodies need an `AI usage:` line.
- Glass `Full` needs API 31 (blur) and API 33 (lens). Contrast: text at 4.5:1 or better. Contrast caps: Full dark 0.70, light 0.60; Frosted dark 0.80, light 0.70; tint step 0.02; colour adjust step 4%.
- Shape scale: Sharp 0.25, Soft 1.0, Pill 1.8. Spacing: Compact 0.8, Comfortable 1.0, Spacious 1.22. Text: XS 0.86, Small 0.93, Medium 1.0, Large 1.12.
- The default player seek bar stays the stock Material 3 `Slider`.
- Common code changes need Android, desktop and iOS verification (`docs/checks.md`); iOS needs macOS and may be reported as unverified.

## Review Focus

- Light theme with a light accent and a light custom background: text stays at 4.5:1 or better, and the Appearance screen says the colour was adjusted.
- API 30 or lower, or system transparency reduced: `Full` and `Frosted` fall back to `Solid` instead of drawing unreadable text over unblurred content.
- Restoring a backup that lacks the new keys, or contains the removed-from-UI keys: no crash, defaults apply.
- Album-art colours with a very dark or very light cover: accent and surfaces stay readable.
- Switching icon pack or font while a screen is open: no restart, no missing-icon blank, and unknown stored values fall back to the first pack or font rather than crashing.

---

### Task 1: Settings keys and enums (shared)

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/metrolist/shared/settings/SettingEnums.kt`
- Modify: `shared/src/commonMain/kotlin/com/metrolist/shared/constants/PreferenceKeys.kt`
- Modify: `shared/src/commonMain/kotlin/com/metrolist/shared/settings/Settings.kt` (preference properties near `themeMode`, line ~341; backup type map near line ~1478)

**Interfaces:**
- Produces enums in `SettingEnums.kt`: `GlassTier { FULL, FROSTED, SOLID }`, `CornerStyle { SHARP, SOFT, PILL }`, `SpacingDensity { COMPACT, COMFORTABLE, SPACIOUS }`, `DisplaySize { XS, SMALL, MEDIUM, LARGE }`, `FluviaBackground { AURORA, MESH, SOLID, ALBUM_ART }`, `IconStyle { LINE, SOLID, SOFT, DUO }`, `FluviaLook { MIDNIGHT_FROST, PAPER_LIGHT, NEON_NIGHT, ALBUM_GLOW, FOREST_QUIET, SUNSET_POP, CUSTOM }`.
- Produces `Settings` properties: `glassTier: GlassTier`, `glassBlur: Int?`, `glassTint: Int?`, `glassEdge: Int?`, `cornerStyle: CornerStyle`, `spacingDensity: SpacingDensity`, `displaySize: DisplaySize`, `fluviaBackground: FluviaBackground`, `fluviaBackgroundHex: String?`, `fluviaAccentIndex: Int`, `fluviaAccentHex: String?`, `fluviaFont: String`, `iconPack: String`, `iconStyle: IconStyle`, `fluviaLook: FluviaLook`. Blur, tint, edge and hex are `null` for automatic.
- Existing `themeMode` (incl. `PURE_BLACK`), `densityScaleFactor`, `themePaletteStyle` and the Material You flag keep working; they are not removed and old backups still restore.

- [ ] **Step 1: Add the enums** to `SettingEnums.kt` with the members above.
- [ ] **Step 2: Add keys** to `PreferenceKeys.kt` following the existing naming, and the `Settings` properties with `preference(default)`; defaults: `FULL`, `null`, `SOFT`, `COMFORTABLE`, `MEDIUM`, `AURORA`, accent index `0`, `fluviaFont` and `iconPack` are the ids `"outfit"` and `"fluvia"`, `iconStyle` `SOFT`, `fluviaLook` `CUSTOM`.
- [ ] **Step 3: Register the new keys in the backup type map** (`"glassTier" to SettingType.…` pattern at `Settings.kt:~1478`) so backup and restore round-trip them. Unknown enum names fall back to the default.
- [ ] **Step 4: Verify.** Run `./gradlew :shared:ktlintCheck :androidApp:assembleDebug :desktopApp:build`. Expected: all succeed.
- [ ] **Step 5: Commit** (only after approval): `feat(settings): add Fluvia appearance preferences`.

### Task 2: Tokens and contrast guard (androidApp)

**Files:**
- Create: `androidApp/src/main/kotlin/com/metrolist/android/ui/fluvia/FluviaTokens.kt`
- Create: `androidApp/src/main/kotlin/com/metrolist/android/ui/fluvia/FluviaContrast.kt`

**Interfaces:**
- Consumes: Task 1 settings.
- Produces in `FluviaContrast.kt`: `fun contrastRatio(a: Color, b: Color): Double` (WCAG relative luminance); `fun fitTextColor(text: Color, background: Color, towards: Color, min: Double = 4.5): Color` (steps the text colour towards `towards` in 4% increments until `min`); `data class GlassResult(val tier: GlassTier, val tint: Float)`; `fun guardGlass(tier: GlassTier, startTint: Float, dark: Boolean, text: Color, backdrop: Color, surfaceTint: Color): GlassResult` (raise tint by 0.02 up to the tier cap, otherwise drop one tier, ending at `SOLID`).
- Produces in `FluviaTokens.kt`: `@Immutable data class FluviaTokens(val colors: FluviaColors, val glass: FluviaGlass, val shapeScale: Float, val spacingScale: Float, val textScale: Float, val fontId: String, val iconPackId: String, val iconStyle: IconStyle)`; `val LocalFluviaTokens = staticCompositionLocalOf<FluviaTokens>`; `@Composable fun rememberFluviaTokens(darkTheme: Boolean, artworkSeed: Color?): FluviaTokens`; `fun Dp.fluviaSpacing(): Dp`, `fun Dp.fluviaShape(): Dp` (circles are not scaled: callers pass full-round shapes separately).
- Reuse `ColorSchemeLegibility.kt` where it already checks roles; do not duplicate it.

- [ ] **Step 1: Implement `contrastRatio`, `fitTextColor`, `guardGlass`** as specified. Reference behaviour: `guardGlass` in the prototype (`Main.dc.html`). Pure functions, no Compose dependencies.
- [ ] **Step 2: Implement `FluviaTokens` and `rememberFluviaTokens`.** Scales come from the Global Constraints table; accent comes from the 6 preset list, the custom hex, or `artworkSeed`; the custom accent and background hex pass through `fitTextColor`; the result records whether it was adjusted (`adjusted: Boolean` on `FluviaColors`) for the Appearance note.
- [ ] **Step 3: Verify.** `./gradlew :androidApp:ktlintCheck :androidApp:assembleDebug`. Expected: success. Spot check `contrastRatio(Color.White, Color.Black)` equals 21.0 in a scratch run (not committed).
- [ ] **Step 4: Commit** (after approval): `feat(theme): add Fluvia tokens and contrast guard`.

### Task 3: FluviaTheme replaces the Android MetrolistTheme

**Files:**
- Create: `androidApp/src/main/kotlin/com/metrolist/android/ui/fluvia/FluviaTheme.kt`
- Modify: `androidApp/src/main/kotlin/com/metrolist/android/ui/theme/Theme.kt` (remove `MetrolistTheme`, keep helpers still used elsewhere)
- Modify: `androidApp/src/main/kotlin/com/metrolist/android/MainActivity.kt:328` and `androidApp/src/main/kotlin/com/metrolist/android/ui/MetrolistApp.kt:467` (call sites)

**Interfaces:**
- Consumes: `rememberFluviaTokens`, `LocalFluviaTokens`.
- Produces: `@Composable fun FluviaTheme(darkTheme: Boolean, artworkSeed: Color?, content: @Composable () -> Unit)`.

- [ ] **Step 1: Implement `FluviaTheme`.** Keep what `MetrolistTheme` already does: status-bar and navigation-bar appearance, window background, `MotionScheme.expressive()`, `MetrolistHapticsProvider`, `FocusOutlineProvider` on TV. Build `ColorScheme` from the tokens (honouring `ThemeMode.PURE_BLACK`), `Typography` from `fluviaFont` and `textScale`, `Shapes` from `shapeScale`, and provide `LocalFluviaTokens`. Dynamic colour and palette style are ignored by Fluvia and not shown in its Appearance screen (decision: hide, do not delete keys, so backups keep restoring).
- [ ] **Step 2: Swap both call sites** to `FluviaTheme` and delete the old Android `MetrolistTheme`. Leave `shared` `MetrolistTheme` (desktop, iOS) as is.
- [ ] **Step 3: Verify.** `./gradlew :androidApp:assembleDebug :androidApp:lintDebug :androidApp:ktlintCheck :desktopApp:build`. Then on a device or emulator: launch, switch dark, light and auto; confirm Home, Player and Settings are readable. Report anything not checked.
- [ ] **Step 4: Commit** (after approval): `feat(theme): replace the Android theme with FluviaTheme`. Open PR 1 with an `AI usage:` line.

### Task 4: Glass surface and spike decision

**Files:**
- Create: `androidApp/src/main/kotlin/com/metrolist/android/ui/fluvia/FluviaGlass.kt`
- Modify: `androidApp/build.gradle.kts` and `gradle/libs.versions.toml` (one dependency)
- Modify: `docs/superpowers/specs/2026-11-24-fluvia-design-system-design.md` (record the chosen library)

**Interfaces:**
- Consumes: `LocalFluviaTokens`, `guardGlass`.
- Produces: `fun Modifier.fluviaGlass(shape: Shape, text: Color): Modifier` which resolves tier, blur, tint and edge from tokens, applies `guardGlass`, and degrades to `Solid` below API 31 or when system transparency is reduced.

- [ ] **Step 1: Spike.** Build a throwaway screen with Kyant0 Backdrop and with Haze on one API 33 device. Record blur quality, lens support and frame cost in the spec, then pick one and delete the other.
- [ ] **Step 2: Implement `fluviaGlass`** with the chosen library. Draw order: backdrop blur, tint, 1dp edge light, then content. `Full` adds the lens effect only on API 33 and above; `Frosted` adds the grain texture.
- [ ] **Step 3: Verify.** `./gradlew :androidApp:assembleDebug :androidApp:ktlintCheck`. On API 33 and API 31 devices, check each tier over the 12 album covers; on an API 30 emulator confirm `Solid`.
- [ ] **Step 4: Commit** (after approval): `feat(theme): add glass surfaces`.

### Task 5: Fonts

**Files:**
- Create: `androidApp/src/main/res/font/` (font files), `androidApp/src/main/kotlin/com/metrolist/android/ui/fluvia/FluviaFonts.kt`
- Modify: `docs/superpowers/specs/2026-11-24-fluvia-design-system-design.md` (licence table)

**Interfaces:**
- Produces: `data class FluviaFont(val id: String, val label: String, val group: FontGroup, val family: FontFamily)`, `val FluviaFontList: List<FluviaFont>` (36 entries, six groups: sans, rounded, serif, display, mono, handwriting, first entry `outfit`), `fun fluviaFont(id: String): FluviaFont` (unknown id returns the first entry).

- [ ] **Step 1: Check each font's licence** (OFL or Apache) from its source repository, record it in the spec, and drop any font that is not freely bundleable.
- [ ] **Step 2: Add font files** (only the weights used: 400 and 700) and `FluviaFontList`. Check APK size delta; if it exceeds 6 MB, trim the list and tell the owner.
- [ ] **Step 3: Wire `fontId` into `FluviaTheme` typography.**
- [ ] **Step 4: Verify.** `./gradlew :androidApp:assembleDebug :androidApp:ktlintCheck`; change fonts live on a device. **Commit** (after approval): `feat(theme): bundle selectable fonts`.

### Task 6: Icon packs

**Files:**
- Create: `scripts/gen-icon-packs.mjs`, `androidApp/src/main/kotlin/com/metrolist/android/ui/fluvia/FluviaIconPaths.kt` (generated), `androidApp/src/main/kotlin/com/metrolist/android/ui/fluvia/FluviaIcons.kt`

**Interfaces:**
- Produces: `data class IconPackData(val id: String, val label: String, val licence: String, val stroke: Boolean, val viewport: Float, val paths: Map<String, String>)`, `val FluviaIconPacks: List<IconPackData>` (ids `fluvia`, `lucide`, `feather`, `tabler`, `phosphor`, `material`), `@Composable fun fluviaIcon(key: String, filled: Boolean = false): ImageVector` using `LocalFluviaTokens` (unknown pack falls back to `fluvia`, missing key falls back to the `fluvia` path).
- Icon keys (48): user, wifiOff, lock, arrowDown, download, heart, search, close, check, chevL, chevR, chevD, pause, play, next, prev, shuffle, repeat, palette, expand, minus, plus, lyrics, queue, cast, moon, vibrate, more, home, library, settings, alert, share, radio, playlistAdd, queueAdd, queueNext, bookmark, album, mic, users, pin, queueRemove, flag, phone, speaker, monitor, tv, edit.

- [ ] **Step 1: Port the generator.** The prototype's build step (npm packages `lucide-static`, `feather-icons`, `@tabler/icons`, `@phosphor-icons/core`, `@material-symbols/svg-400`; every shape merged into one path; leading relative `m` made absolute) becomes `scripts/gen-icon-packs.mjs`, which writes `FluviaIconPaths.kt`. Package licences are read from each `package.json`.
- [ ] **Step 2: Implement `fluviaIcon`** with a cache per (pack, key, filled, style). Stroke packs honour `IconStyle` (width 1.8 line, fill solid, 2.4 soft, fill at 28% duo).
- [ ] **Step 3: Replace existing icon usage on the screens touched by the theme** with `fluviaIcon` where a Fluvia key exists; leave other icons alone. Add the licence list to the Credits screen as string resources.
- [ ] **Step 4: Verify.** `./gradlew :androidApp:assembleDebug :androidApp:lintDebug :androidApp:ktlintCheck`; switch each of the six packs on a device and check the nav, player controls and menus. **Commit** (after approval): `feat(theme): add selectable icon packs`. Open PR 2 after Tasks 4–6.

### Task 7: Appearance screen

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/metrolist/shared/ui/settings/AppearanceSettingsRegistration.kt` (or an Android-only registration if the registry is shared with desktop; follow how existing Android-only entries are registered)
- Modify: `shared/src/commonMain/composeResources/values/strings.xml`

**Interfaces:**
- Consumes: Task 1 settings, Task 2 `FluviaColors.adjusted`, Task 6 `FluviaIconPacks`, Task 5 `FluviaFontList`.

- [ ] **Step 1: Read how the registry renders sections and previews** (`ThemeModePreview` pattern near the top of the file) and add Fluvia entries in this order: Looks, theme mode, glass tier, glass fine-tune (blur, tint, edge sliders with an Auto state), album-art colours, accent (6 presets and a hex field), background style and colour, corners, spacing, display size, font, icon pack, icon style, haptics.
- [ ] **Step 2: Add English strings** for every label, note and the "adjusted for contrast" message; none in `values-*`.
- [ ] **Step 3: Hide the Metrolist-only rows** (Material You, palette style) from the Android Appearance screen; desktop keeps them.
- [ ] **Step 4: Verify.** `./gradlew :shared:ktlintCheck :androidApp:assembleDebug :androidApp:lintDebug :desktopApp:build`; on a device change every control and confirm the app restyles live. **Commit** (after approval): `feat(settings): add the Fluvia Appearance screen`.

### Task 8: Look presets

**Files:**
- Create: `androidApp/src/main/kotlin/com/metrolist/android/ui/fluvia/FluviaLooks.kt`

**Interfaces:**
- Produces: `fun applyLook(look: FluviaLook)` writing all Appearance settings and clearing custom hex values. Presets: Midnight Frost, Paper Light, Neon Night, Album Glow, Forest Quiet, Sunset Pop, with the values from the prototype `LOOKS` table.

- [ ] **Step 1: Implement `applyLook`** and `fun currentLook(): FluviaLook` (returns `CUSTOM` when any value differs or a custom hex is set).
- [ ] **Step 2: Verify** on a device by applying each Look and changing one setting (the Look chip must switch to Custom). **Commit** (after approval): `feat(settings): add Look presets`. Open PR 3.

### Task 9: Final verification

- [ ] **Step 1:** Run the full Android and desktop commands from `docs/checks.md`. Expected: all succeed.
- [ ] **Step 2:** Run the Review Focus cases on a device; list any not checked.
- [ ] **Step 3:** Review `git diff` against the remote base for stray files, debug code and secrets.
