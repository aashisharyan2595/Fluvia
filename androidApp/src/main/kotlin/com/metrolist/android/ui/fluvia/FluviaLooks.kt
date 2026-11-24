/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.fluvia

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.graphics.Color
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.fluvia_look_album_glow
import com.metrolist.shared.generated.resources.fluvia_look_album_glow_description
import com.metrolist.shared.generated.resources.fluvia_look_forest_quiet
import com.metrolist.shared.generated.resources.fluvia_look_forest_quiet_description
import com.metrolist.shared.generated.resources.fluvia_look_midnight_frost
import com.metrolist.shared.generated.resources.fluvia_look_midnight_frost_description
import com.metrolist.shared.generated.resources.fluvia_look_neon_night
import com.metrolist.shared.generated.resources.fluvia_look_neon_night_description
import com.metrolist.shared.generated.resources.fluvia_look_paper_light
import com.metrolist.shared.generated.resources.fluvia_look_paper_light_description
import com.metrolist.shared.generated.resources.fluvia_look_sunset_pop
import com.metrolist.shared.generated.resources.fluvia_look_sunset_pop_description
import com.metrolist.shared.settings.CornerStyle
import com.metrolist.shared.settings.DisplaySize
import com.metrolist.shared.settings.FluviaBackground
import com.metrolist.shared.settings.FluviaLook
import com.metrolist.shared.settings.GlassTier
import com.metrolist.shared.settings.IconStyle
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.settings.SpacingDensity
import com.metrolist.shared.settings.ThemeMode
import org.jetbrains.compose.resources.StringResource

/**
 * Every Appearance setting a Look controls. Blur, tint and edge are null (automatic), the accent and background colours
 * are the presets rather than custom hex values, and spacing, display size and icon pack return to their defaults, as
 * the prototype's Look does.
 */
class FluviaLookPreset(
    val look: FluviaLook,
    val name: StringResource,
    val description: StringResource,
    val themeMode: ThemeMode,
    val glassTier: GlassTier,
    val accentIndex: Int,
    val background: FluviaBackground,
    val albumArtColours: Boolean,
    val cornerStyle: CornerStyle,
    val fontId: String,
    val iconStyle: IconStyle,
    /** Backdrop of the Look's tile in the picker; display only. */
    val previewBackground: Color,
    val spacingDensity: SpacingDensity = SpacingDensity.COMFORTABLE,
    val displaySize: DisplaySize = DisplaySize.MEDIUM,
    val iconPack: String = DEFAULT_ICON_PACK,
) {
    val isDark: Boolean get() = themeMode == ThemeMode.DARK

    /** Writes every setting of the Look. Callers batch the writes. */
    fun write(settings: Settings) {
        settings.themeMode = themeMode
        settings.glassTier = glassTier
        settings.glassBlur = null
        settings.glassTint = null
        settings.glassEdge = null
        settings.dynamicTheme = albumArtColours
        settings.fluviaAccentIndex = accentIndex
        settings.fluviaAccentHex = null
        settings.fluviaBackground = background
        settings.fluviaBackgroundHex = null
        settings.cornerStyle = cornerStyle
        settings.spacingDensity = spacingDensity
        settings.displaySize = displaySize
        settings.fluviaFont = fontId
        settings.iconPack = iconPack
        settings.iconStyle = iconStyle
        settings.fluviaLook = look
    }

    /** Whether the stored settings still hold exactly this Look. */
    fun matches(settings: Settings): Boolean =
        settings.themeMode == themeMode &&
            settings.glassTier == glassTier &&
            settings.glassBlur == null &&
            settings.glassTint == null &&
            settings.glassEdge == null &&
            settings.dynamicTheme == albumArtColours &&
            settings.fluviaAccentIndex == accentIndex &&
            settings.fluviaAccentHex == null &&
            settings.fluviaBackground == background &&
            settings.fluviaBackgroundHex == null &&
            settings.cornerStyle == cornerStyle &&
            settings.spacingDensity == spacingDensity &&
            settings.displaySize == displaySize &&
            settings.fluviaFont == fontId &&
            settings.iconPack == iconPack &&
            settings.iconStyle == iconStyle
}

private const val DEFAULT_ICON_PACK = "fluvia"

/** The six Looks, from the prototype's `LOOKS` table. [FluviaLook.CUSTOM] has no preset. */
val FluviaLookPresets: List<FluviaLookPreset> =
    listOf(
        FluviaLookPreset(
            look = FluviaLook.MIDNIGHT_FROST,
            name = Res.string.fluvia_look_midnight_frost,
            description = Res.string.fluvia_look_midnight_frost_description,
            themeMode = ThemeMode.DARK,
            glassTier = GlassTier.FROSTED,
            accentIndex = 1,
            background = FluviaBackground.AURORA,
            albumArtColours = false,
            cornerStyle = CornerStyle.SOFT,
            fontId = "outfit",
            iconStyle = IconStyle.SOFT,
            previewBackground = Color(0xFF1F0F2E),
        ),
        FluviaLookPreset(
            look = FluviaLook.PAPER_LIGHT,
            name = Res.string.fluvia_look_paper_light,
            description = Res.string.fluvia_look_paper_light_description,
            themeMode = ThemeMode.LIGHT,
            glassTier = GlassTier.FULL,
            accentIndex = 0,
            background = FluviaBackground.AURORA,
            albumArtColours = false,
            cornerStyle = CornerStyle.SOFT,
            fontId = "inter",
            iconStyle = IconStyle.LINE,
            previewBackground = Color(0xFFFFFFFF),
        ),
        FluviaLookPreset(
            look = FluviaLook.NEON_NIGHT,
            name = Res.string.fluvia_look_neon_night,
            description = Res.string.fluvia_look_neon_night_description,
            themeMode = ThemeMode.DARK,
            glassTier = GlassTier.FULL,
            accentIndex = 4,
            background = FluviaBackground.MESH,
            albumArtColours = false,
            cornerStyle = CornerStyle.SOFT,
            fontId = "space_grotesk",
            iconStyle = IconStyle.DUO,
            previewBackground = Color(0xFF000000),
        ),
        FluviaLookPreset(
            look = FluviaLook.ALBUM_GLOW,
            name = Res.string.fluvia_look_album_glow,
            description = Res.string.fluvia_look_album_glow_description,
            themeMode = ThemeMode.DARK,
            glassTier = GlassTier.FULL,
            accentIndex = 0,
            background = FluviaBackground.ALBUM_ART,
            albumArtColours = true,
            cornerStyle = CornerStyle.SOFT,
            fontId = "outfit",
            iconStyle = IconStyle.SOFT,
            previewBackground = Color(0xFF0E0E14),
        ),
        FluviaLookPreset(
            look = FluviaLook.FOREST_QUIET,
            name = Res.string.fluvia_look_forest_quiet,
            description = Res.string.fluvia_look_forest_quiet_description,
            themeMode = ThemeMode.DARK,
            glassTier = GlassTier.SOLID,
            accentIndex = 2,
            background = FluviaBackground.SOLID,
            albumArtColours = false,
            cornerStyle = CornerStyle.SHARP,
            fontId = "sora",
            iconStyle = IconStyle.LINE,
            previewBackground = Color(0xFF0E2A22),
        ),
        FluviaLookPreset(
            look = FluviaLook.SUNSET_POP,
            name = Res.string.fluvia_look_sunset_pop,
            description = Res.string.fluvia_look_sunset_pop_description,
            themeMode = ThemeMode.LIGHT,
            glassTier = GlassTier.FROSTED,
            accentIndex = 3,
            background = FluviaBackground.AURORA,
            albumArtColours = false,
            cornerStyle = CornerStyle.PILL,
            fontId = "fraunces",
            iconStyle = IconStyle.SOLID,
            previewBackground = Color(0xFFFBEFF3),
        ),
    )

/**
 * Applies [look]: writes every Appearance setting of the preset, clears the custom accent and background colours and
 * stores the Look. [FluviaLook.CUSTOM] means the user's own settings, so it changes nothing.
 */
fun applyLook(look: FluviaLook) {
    val preset = FluviaLookPresets.firstOrNull { it.look == look } ?: return
    // One snapshot so observers see the whole Look at once and never a half-applied mix.
    Snapshot.withMutableSnapshot { preset.write(Settings.shared) }
}

/**
 * The stored Look while every setting still equals its preset, otherwise [FluviaLook.CUSTOM]. Reads settings state, so
 * calling it in composition re-evaluates when any of them changes.
 */
fun currentLook(): FluviaLook {
    val settings = Settings.shared
    val stored = settings.fluviaLook
    val preset = FluviaLookPresets.firstOrNull { it.look == stored } ?: return FluviaLook.CUSTOM
    return if (preset.matches(settings)) stored else FluviaLook.CUSTOM
}
