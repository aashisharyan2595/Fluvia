# Fluvia design system and Appearance settings

Status: tasks 1, 2, 3, 5, 6, 7 and 8 are implemented on branch `feat/fluvia-design-system` as local commits. Nothing has been compiled yet; CI is the first build. Task 4 (glass surface) is not implemented: the provisional choice is Kyant0 Backdrop 2.0.0 (Apache-2.0), picked from search results because its docs were unreachable, with Haze rejected because its v2 glass module is alpha; device validation is pending.

Known gaps that need follow-up tasks:

- The glass settings have no consumer yet. Background style rendering (aurora, mesh, solid, album art) and the spacing scale are also unused: `fluviaSpacing` and `fluviaShape` have no callers.
- The system high-contrast setting (`UiModeManager` contrast, API 34+) was honoured by the old theme and is not carried over.
- Widgets still gate on `materialYouEnabled && dynamicTheme`.
- The Look tile backdrops (Plum, Forest, Blush, Ink) are not applied as backgrounds, because no real setting models them.
- `PURE_BLACK` is overwritten by dark Looks.
- Album-art colours default to on and dim the accent rows.

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
- Implementation uses Kyant0 Backdrop 2.0.0 (Apache-2.0, `io.github.kyant0:backdrop`), chosen over Haze after reading its documentation; no on-device spike was run. Its effects work on API 31 and newer, and lens needs API 33 (RuntimeShader). Lens also needs a corner-based shape. The library documents no fallback below API 31, so `fluviaGlass` draws a solid surface there. Android exposes no reduce-transparency setting, so only the Solid tier covers that case. The frosted grain texture is not implemented. Validation on API 31, 33 and 30 devices is still owed.

### Contrast guard

For each surface that holds text: start at the tint alpha (the user's value or the tier default), raise alpha in steps of 0.02 until the text colour reaches 4.5:1 against the blended backdrop, up to a cap (Full: 0.70 dark and 0.60 light; Frosted: 0.80 dark and 0.70 light). If the cap is not enough, drop one tier (Full to Frosted to Solid). Accent and custom background colours are adjusted toward black or white in 4% steps until text passes, and the Appearance screen says when a colour was adjusted. The reference implementation is `guardGlass` in the prototype.

### Fonts

Fonts are bundled in the app (not downloaded) and chosen from a list of 36 in six groups. Licences are checked per font before bundling. The default is the first font in the list; no locale-based font defaults.

Licences, checked against the `LICENSE_FONT` file shipped in each package. The files are the unmodified static TrueType weights 400 and 700 from the `@expo-google-fonts/<name>` packages (the wrapper packages are MIT; the fonts are OFL-1.1). Archivo Black, Bebas Neue, DM Serif Display, Pacifico and Righteous publish one weight, which is used for both. The licence text and per-font copyright lines ship in `shared/src/commonMain/composeResources/files/licenses/fonts/LICENSES.md`. The added font files compress to about 5.4 MiB.

| Font | Licence | Source package |
| --- | --- | --- |
| Outfit | OFL-1.1 | @expo-google-fonts/outfit 0.4.3 |
| Inter | OFL-1.1 | @expo-google-fonts/inter 0.4.2 |
| DM Sans | OFL-1.1 | @expo-google-fonts/dm-sans 0.4.2 |
| Space Grotesk | OFL-1.1 | @expo-google-fonts/space-grotesk 0.4.1 |
| Sora | OFL-1.1 | @expo-google-fonts/sora 0.4.2 |
| Fraunces | OFL-1.1 | @expo-google-fonts/fraunces 0.4.1 |
| Plus Jakarta Sans | OFL-1.1 | @expo-google-fonts/plus-jakarta-sans 0.4.2 |
| Manrope | OFL-1.1 | @expo-google-fonts/manrope 0.4.3 |
| Poppins | OFL-1.1 | @expo-google-fonts/poppins 0.4.1 |
| Montserrat | OFL-1.1 | @expo-google-fonts/montserrat 0.4.2 |
| Urbanist | OFL-1.1 | @expo-google-fonts/urbanist 0.4.2 |
| Lexend | OFL-1.1 | @expo-google-fonts/lexend 0.4.1 |
| Rubik | OFL-1.1 | @expo-google-fonts/rubik 0.4.2 |
| Work Sans | OFL-1.1 | @expo-google-fonts/work-sans 0.4.2 |
| Figtree | OFL-1.1 | @expo-google-fonts/figtree 0.4.1 |
| Nunito | OFL-1.1 | @expo-google-fonts/nunito 0.4.2 |
| Quicksand | OFL-1.1 | @expo-google-fonts/quicksand 0.4.1 |
| Comfortaa | OFL-1.1 | @expo-google-fonts/comfortaa 0.4.2 |
| Fredoka | OFL-1.1 | @expo-google-fonts/fredoka 0.4.1 |
| Playfair Display | OFL-1.1 | @expo-google-fonts/playfair-display 0.4.2 |
| Lora | OFL-1.1 | @expo-google-fonts/lora 0.4.2 |
| Merriweather | OFL-1.1 | @expo-google-fonts/merriweather 0.4.2 |
| DM Serif Display | OFL-1.1 | @expo-google-fonts/dm-serif-display 0.4.2 |
| Cormorant Garamond | OFL-1.1 | @expo-google-fonts/cormorant-garamond 0.4.1 |
| Syne | OFL-1.1 | @expo-google-fonts/syne 0.4.2 |
| Unbounded | OFL-1.1 | @expo-google-fonts/unbounded 0.4.1 |
| Righteous | OFL-1.1 | @expo-google-fonts/righteous 0.4.1 |
| Bricolage Grotesque | OFL-1.1 | @expo-google-fonts/bricolage-grotesque 0.4.1 |
| Bebas Neue | OFL-1.1 | @expo-google-fonts/bebas-neue 0.4.1 |
| Archivo Black | OFL-1.1 | @expo-google-fonts/archivo-black 0.4.1 |
| JetBrains Mono | OFL-1.1 | @expo-google-fonts/jetbrains-mono 0.4.1 |
| Space Mono | OFL-1.1 | @expo-google-fonts/space-mono 0.4.2 |
| IBM Plex Mono | OFL-1.1 | @expo-google-fonts/ibm-plex-mono 0.4.2 |
| Caveat | OFL-1.1 | @expo-google-fonts/caveat 0.4.2 |
| Pacifico | OFL-1.1 | @expo-google-fonts/pacifico 0.4.1 |
| Shantell Sans | OFL-1.1 | @expo-google-fonts/shantell-sans 0.4.1 |

### Icon packs

Six packs: Fluvia (original), Lucide, Feather, Tabler, Phosphor, Material Symbols. Each icon is stored as one path string per pack and built into an `ImageVector` at start-up, so switching packs needs no restart. Stroke-style packs support the Line, Solid, Soft and Duo styles. Fill-style packs ignore the style setting. Licences (ISC, MIT, MIT, MIT, Apache 2.0, as published in each package) go in the Credits screen and must be re-checked before release. Path data comes from the npm packages and is generated by a script kept under `scripts/`.

### Appearance screen

Built from the prototype's Appearance board: Looks, theme, glass and fine-tune, album-art colours, accent, background, corners, spacing, display size, font, icon pack and style, and the contrast notes. Strings are new English keys.

## Verification

- Build the Android app and run the checks listed in `docs/checks.md`.
- Install on one API 31+ device and one API 33+ device, and check each glass tier and the guard with the 12 album covers. Report anything not checked.

## Left to the builder

- Exact Kotlin structure and file split.
- Which of Backdrop or Haze to use: decided, Backdrop 2.0.0.
- How `FluviaTokens` maps onto Material colour roles where screens still use Material components.

## Open items (not blocking this slice)

- Device-check Backdrop blur, lens and frame cost on API 31 and 33 devices, and Solid on API 30.
- Verify the lock-screen widget `keyguard` category (later slice).
- Check font and icon licences individually.
- Check that the name Fluvia is available.
- PR split: (1) `FluviaTheme` and tokens replacing `MetrolistTheme`, with the settings keys; (2) glass surfaces, fonts and icon packs; (3) Appearance screen and Look presets, which list the fonts and packs.
- Existing Metrolist theme options (Material You dynamic colour, palette style, pure black) are removed unless you want some kept. Decide before the plan.
