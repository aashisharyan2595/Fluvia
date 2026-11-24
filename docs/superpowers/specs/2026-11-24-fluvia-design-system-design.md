# Fluvia design system and Appearance settings

Status: draft v2 for approval (revised: replace the Metrolist theme, do not layer on it). Nothing in this document is built yet.

## Intent

Fluvia is Aashish Pandey's Android-only fork of Metrolist. It keeps the YouTube Music playback and library features and replaces the look with a playful, bold, glass-based interface, with Spotify-style structure. The user can change how it looks. The interactive prototype (Fluvia hi-fi mockups, version 1795527551-ba73) is the visual reference.

This first slice delivers the design system and the Appearance settings that control it. Every later screen (Home, Now Playing, widgets) depends on it, so it ships first.

## Goals

- One token layer that every screen reads: colour, glass, shape, density, type size, font, icons.
- Appearance settings that change the token layer live: theme (dark, light, auto), accent (presets, custom hex, album-art colours), background style (aurora, mesh, solid, album art, custom colour), glass tier and fine-tune (blur, tint, edge), corner roundness, density, display size, font, icon pack, haptics.
- A contrast guard: text on glass stays at 4.5:1 or better, with automatic dimming and tier fallback.
- Six Look presets that set several options at once.

## Non-goals (this slice)

- Redesigning each screen's layout (Home, Search, Library, Now Playing). Each is a later task. They get the new colours, shapes and fonts from the theme immediately, but keep their current layouts until redesigned.
- Home editing, widgets, record-player Now Playing.
- Exporting and importing Looks.
- Custom app icon or splash (dropped by the owner).
- Desktop and iOS. Fluvia is Android-only; shared code stays compiling but gets no Fluvia styling.

## Anti-goals

- Hardcoded English strings or locale defaults.
- Glass that makes text unreadable. A good-looking blur that fails 4.5:1 is a failure.
- Keeping two parallel theme systems. `MetrolistTheme` is removed, not wrapped.

## Decisions already made

- Liquid Glass is on the upstream rejected list. The owner decided Fluvia builds it anyway because Fluvia is its own product. Upstream-bound fixes go on separate branches from fork-only work.
- Android-only, custom UI, design system first.
- Package ID `io.github.aashisharyan2595.fluvia`. Renaming the application ID is a separate task and is not part of this slice.

## Upstream merges

- Edits are confined to the UI layer (theme, settings screens, composables, resources). Playback, InnerTube, database, Listen Together and other non-UI code stay untouched, so upstream fixes there merge cleanly.
- Fluvia-owned styling lives in a `fluvia` package and a small set of files, so conflicts stay local.
- Merge upstream on a regular schedule in small steps, not in large batches.

## Constraints from the repository

- No hardcoded user-facing strings. English goes only in `shared/src/commonMain/composeResources/values/strings.xml`. Never edit `values-*/strings.xml`.
- Preference keys live in `PreferenceKeys.kt` and are read through `Settings.shared`.
- No hardcoded language or country defaults. Fonts and locale come from system or settings.
- No inline fully qualified class names. Format changed Kotlin with `ktlint --format`.
- Tests only for parsers, migrations, backup and restore, crypto and protocols. The contrast maths is neither, so this slice adds no tests; verify on a device.
- Nothing is committed, pushed or opened as a PR without explicit approval. No version bump.
- The default player seek bar stays the stock Material 3 `Slider`.

## Approaches considered

1. **Add a token layer beside `MetrolistTheme`.** Safest for existing screens, but leaves two systems alive and the old look in the code. Rejected by the owner.
2. **Replace `MetrolistTheme` with `FluviaTheme` (chosen).** One theme builds `FluviaTokens` from settings, then feeds Material's `colorScheme`, `typography` and `shapes` from those tokens so stock Material components follow it. Every screen gets the new look at once. Cost: a large first change. Upstream UI changes will conflict where they touch the theme, settings and screen files; resolve those case by case and re-skin the result. Fluvia keeps merging upstream everything outside the UI.
3. **New design-system Gradle module.** Extra build wiring for one app. Not chosen.

## Design

### Tokens

`FluviaTheme` replaces `MetrolistTheme` in `Theme.kt` and `MetrolistApp.kt`. It builds `FluviaTokens`, an immutable class built from settings: colours (surface, tile, chip, line, text, text2, accent, on-accent), `glass` (tier, blur radius, tint alpha, edge alpha), `shapes` (scale factor from Sharp 0.25, Soft 1, Pill 1.8), `density` (0.8, 1, 1.22), `textScale` (0.86, 0.93, 1, 1.12), `fontFamily`, `iconPack`. Material's `colorScheme`, `typography` and `shapes` are derived from the tokens, so unmigrated Material components still look right. Components multiply their base dp and sp by these factors. Fully round shapes (circles) are never scaled.

### Settings

New keys in `PreferenceKeys.kt`: look, themeMode, accentIndex, accentHex, backgroundStyle, backgroundHex, glassTier, glassBlur, glassTint, glassEdge, cornerRadius, density, displaySize, fontFamily, iconPack, iconStyle, haptics. A `null` fine-tune value means automatic. Applying a Look writes all of them and clears the custom colours.

### Glass tiers and device support

- Full: backdrop blur plus edge light and lens effect. The blur needs API 31, the lens effect API 33.
- Frosted: heavy blur with a grainy tint. Works on API 31 and newer.
- Solid: opaque. Used below API 31, when the system's "remove animations" or reduce-transparency settings are on, and as the contrast fallback.
- Implementation is chosen by a spike (see open items): Kyant0 Backdrop or Haze.

### Contrast guard

For each surface that holds text: start at the tint alpha (the user's value or the tier default), raise alpha in steps of 0.02 until the text colour reaches 4.5:1 against the blended backdrop, up to a cap (Full: 0.70 dark and 0.60 light; Frosted: 0.80 dark and 0.70 light). If the cap is not enough, drop one tier (Full to Frosted to Solid). Accent and custom background colours are adjusted toward black or white in 4% steps until text passes, and the Appearance screen says when a colour was adjusted. The reference implementation is `guardGlass` in the prototype.

### Fonts

Fonts are bundled in the app (not downloaded) and chosen from a list of 36 in six groups. Licences are checked per font before bundling. The default is the first font in the list; no locale-based font defaults.

### Icon packs

Six packs: Fluvia (original), Lucide, Feather, Tabler, Phosphor, Material Symbols. Each icon is stored as one path string per pack and built into an `ImageVector` at start-up, so switching packs needs no restart. Stroke-style packs support the Line, Solid, Soft and Duo styles. Fill-style packs ignore the style setting. Licences (ISC, MIT, MIT, MIT, Apache 2.0, as published in each package) go in the Credits screen and must be re-checked before release. Path data comes from the npm packages and is generated by a script kept under `scripts/`.

### Appearance screen

Built from the prototype's Appearance board: Looks, theme, glass and fine-tune, album-art colours, accent, background, corners, spacing, display size, font, icon pack and style, and the contrast notes. Strings are new English keys.

## Verification

- Build the Android app and run the checks listed in `docs/checks.md`.
- Install on one API 31+ device and one API 33+ device, and check each glass tier and the guard with the 12 album covers. Report anything not checked.

## Left to the builder

- Exact Kotlin structure and file split.
- Which of Backdrop or Haze to use, after the spike.
- How `FluviaTokens` maps onto Material colour roles where screens still use Material components.

## Open items (not blocking this slice)

- Spike Kyant0 Backdrop versus Haze for blur and lens effects.
- Verify the lock-screen widget `keyguard` category (later slice).
- Check font and icon licences individually.
- Check that the name Fluvia is available.
- PR split: (1) `FluviaTheme` and tokens replacing `MetrolistTheme`, with the settings keys; (2) glass surfaces, fonts and icon packs; (3) Appearance screen and Look presets, which list the fonts and packs.
- Existing Metrolist theme options (Material You dynamic colour, palette style, pure black) are removed unless you want some kept. Decide before the plan.
