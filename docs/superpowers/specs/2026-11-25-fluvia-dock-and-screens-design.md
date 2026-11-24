# Fluvia dock, Home, Search and Library restyle: design

Source of truth: the Fluvia hi-fi mockup canvas (artifact `9f2ffee6-20bf-439b-a42e-8ce439e485ec`, `project/Main.dc.html`; floating chrome ~line 857, Home 87, Search 356, Library 436). Frame 390x844.

## Intent

Fluvia still looks like Metrolist outside the Now Playing screen. Bring the mini player, navigation, Home, Search and Library to the approved hi-fi design so the whole app reads as one product. Each item ships as its own PR, in this order, each branched from `main`.

## Goals / non-goals

- Goals: match the mockup's layout, sizes and glass treatment; keep all existing behaviour and data; Android only; follow the user's Fluvia settings (glass tier, accent, corner and spacing scale, icon pack, font).
- Non-goals: new features (for example the mockup's Home editor for reordering or removing sections), desktop or iOS changes, Settings and Album screens (later), changes to view models or data loading.
- Anti-goal: a restyle that breaks TV, tablet or landscape layouts or removes any existing control.

## Approaches considered

1. Parallel Android screens (as done for Now Playing): highest fidelity, but copies large amounts of data and menu logic (Home 1,186 lines shared plus 486 Android, Search 2,245, Library 1,222). Rejected.
2. Theme tokens only: already in place (colours, shapes, fonts reach every screen); not enough, because the layout is what differs.
3. Hybrid (recommended): restyle through the Android screen wrappers and small, optional style parameters on shared components, with defaults that keep desktop and iOS exactly as they are.

## PR A: floating dock (mini player plus navigation)

The mockup merges them into one glass card: 342 wide, 24 side margins, radius 36. Top row (64): 40 spinning artwork circle, title 15 bold, artist 13, 48 accent-soft play and pause button, 48 next button. A 3 progress bar sits at the row's bottom edge, with an invisible Material 3 `Slider` over it for touch and TalkBack. Below it, a 68 row holds four items: Home, Search, Library, Settings, each with a 56x30 pill behind the active icon. Tapping the player row opens Now Playing. With nothing playing, the card shows only the nav row.

- Android phone layout only; TV, desktop-mode and rail layouts keep the current bars.
- Needs care in `MetrolistApp.kt`: the player sheet's collapsed bound, snackbar offset and content bottom inset must use the dock height.
- Progress is the thin bar from the mockup, not a waveform; the waveform stays on Now Playing.

## PR B: Home

Header: "Fluvia" 14 semibold above a 32 bold greeting that follows the time of day, plus a 48 profile button. Filter chips: 40 high, radius 20, selected in accent. Quick picks: two-column grid of 56 tiles (56 artwork, bold title, equaliser bars on the playing item). Shelves: 22 bold titles; 144 cards with radius 24 and a soft glow shadow; downloaded badge. Offline and sign-in notices become glass banners with a 44 action button. Loading state uses skeletons.

## PR C: Search

Large 32 title, 56 glass search field, history and suggestion rows at 64, "Browse all" two-column category tiles (96 high, radius 24), result rows at 64 with 56 artwork.

## PR D: Library

32 title, chip row (Songs, Albums, Artists, Playlists), 64 list rows and three-column artwork grid with radius 24 cards, downloaded badge, offline note "Downloads still play".

## Decisions already made (my calls, correctable)

- The dock and the restyled screens have no on/off setting; they are the Fluvia look. Now Playing keeps its toggle because it replaces a whole screen.
- Greeting text is derived from the device clock and localised through string resources.
- Only existing icons from the active icon pack are used.
- Sizes are the mockup's dp, scaled by the user's spacing and text-size settings.

## Left to the builder

Exact composable split, how shared components receive style parameters, animation timing, and haptics, as long as defaults leave desktop and iOS unchanged.

## Verification

Each PR: ktlint, CI debug build green, plus emulator screenshots of the affected screens in Frosted and Solid glass tiers and with large display size. Anything not run is reported as unverified.
