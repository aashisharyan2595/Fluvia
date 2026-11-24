/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.fluvia

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.metrolist.shared.settings.CornerStyle
import com.metrolist.shared.settings.DisplaySize
import com.metrolist.shared.settings.GlassTier
import com.metrolist.shared.settings.IconStyle
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.settings.SpacingDensity

private val Ink = Color(0xFF1A0F2E)
private val DarkBase = Color(0xFF0E0E14)
private val LightBase = Color(0xFFF6F2F8)
private val DarkSurface = Color(0xFF1D1A30)

private const val DARK_ARTWORK_BACKGROUND_MIX = 0.14f
private const val LIGHT_ARTWORK_BACKGROUND_MIX = 0.10f
private const val HEX_DIGITS = 6
private const val HEX_RADIX = 16
private const val PERCENT = 100f

/** Accent presets in the order [Settings.fluviaAccentIndex] refers to. Labels belong to the Appearance screen. */
val FluviaAccentPresets: List<Color> =
    listOf(
        Color(0xFFFF5A5F),
        Color(0xFF6C4DF2),
        Color(0xFF12C2A9),
        Color(0xFFFFB020),
        Color(0xFFFF4FA3),
        Color(0xFF3B82F6),
    )

@Immutable
data class FluviaColors(
    val background: Color,
    val surface: Color,
    val tile: Color,
    val chip: Color,
    val line: Color,
    val text: Color,
    val text2: Color,
    val accent: Color,
    val onAccent: Color,
    /** True when a custom accent or custom background colour had to be moved to keep text readable. */
    val adjusted: Boolean,
)

@Immutable
data class FluviaGlass(
    val tier: GlassTier,
    val blur: Dp,
    val tint: Float,
    val edge: Float,
    /** True when the user set the blur, so a tier downgrade keeps it. */
    val blurSet: Boolean = false,
    /** True when the user set the edge, so a tier downgrade keeps it. */
    val edgeSet: Boolean = false,
)

@Immutable
data class FluviaTokens(
    val dark: Boolean,
    val colors: FluviaColors,
    val glass: FluviaGlass,
    val shapeScale: Float,
    val spacingScale: Float,
    val textScale: Float,
    val fontId: String,
    val iconPackId: String,
    val iconStyle: IconStyle,
)

val LocalFluviaTokens =
    staticCompositionLocalOf<FluviaTokens> { error("FluviaTokens are only available inside FluviaTheme") }

@Composable
fun rememberFluviaTokens(
    darkTheme: Boolean,
    artworkSeed: Color?,
): FluviaTokens {
    val settings = Settings.shared
    val glassTier = settings.glassTier
    val glassBlur = settings.glassBlur
    val glassTint = settings.glassTint
    val glassEdge = settings.glassEdge
    val cornerStyle = settings.cornerStyle
    val spacingDensity = settings.spacingDensity
    val displaySize = settings.displaySize
    val backgroundHex = settings.fluviaBackgroundHex
    val accentIndex = settings.fluviaAccentIndex
    val accentHex = settings.fluviaAccentHex
    val font = settings.fluviaFont
    val iconPack = settings.iconPack
    val iconStyle = settings.iconStyle

    return remember(
        darkTheme,
        artworkSeed,
        glassTier,
        glassBlur,
        glassTint,
        glassEdge,
        cornerStyle,
        spacingDensity,
        displaySize,
        backgroundHex,
        accentIndex,
        accentHex,
        font,
        iconPack,
        iconStyle,
    ) {
        FluviaTokens(
            dark = darkTheme,
            colors =
                buildFluviaColors(
                    dark = darkTheme,
                    presetAccent = FluviaAccentPresets[accentIndex.coerceIn(FluviaAccentPresets.indices)],
                    customAccent = accentHex?.let(::parseHexColor),
                    customBackground = backgroundHex?.let(::parseHexColor),
                    artworkSeed = artworkSeed,
                ),
            glass = buildFluviaGlass(darkTheme, glassTier, glassBlur, glassTint, glassEdge),
            shapeScale = cornerStyle.scale(),
            spacingScale = spacingDensity.scale(),
            textScale = displaySize.scale(),
            fontId = font,
            iconPackId = iconPack,
            iconStyle = iconStyle,
        )
    }
}

@Composable
@ReadOnlyComposable
fun Dp.fluviaSpacing(): Dp = this * LocalFluviaTokens.current.spacingScale

@Composable
@ReadOnlyComposable
fun Dp.fluviaShape(): Dp = this * LocalFluviaTokens.current.shapeScale

private fun Char.isHexDigit(): Boolean = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'

/** Accepts `#RRGGBB` or `RRGGBB`; anything else is not a colour. */
internal fun parseHexColor(hex: String): Color? {
    val digits = hex.trim().removePrefix("#")
    if (digits.length != HEX_DIGITS || !digits.all { it.isHexDigit() }) return null
    val rgb = digits.toLongOrNull(HEX_RADIX) ?: return null
    return Color(0xFF000000L or rgb)
}

internal fun buildFluviaColors(
    dark: Boolean,
    presetAccent: Color,
    customAccent: Color?,
    customBackground: Color?,
    artworkSeed: Color?,
): FluviaColors {
    val text = if (dark) Color.White else Ink
    val accentRaw = artworkSeed ?: customAccent ?: presetAccent

    val background =
        when {
            artworkSeed != null -> {
                lerp(
                    if (dark) DarkBase else LightBase,
                    accentRaw,
                    if (dark) DARK_ARTWORK_BACKGROUND_MIX else LIGHT_ARTWORK_BACKGROUND_MIX,
                )
            }

            customBackground != null -> {
                // Contrast is symmetric, so fitting the background against the text moves it towards black or white.
                fitTextColor(customBackground, text, if (dark) Color.Black else Color.White, MIN_TEXT_CONTRAST)
            }

            else -> {
                if (dark) DarkBase else LightBase
            }
        }
    val customAccentInUse = artworkSeed == null && customAccent != null
    // Presets and artwork colours are fills and controls, so they only need the non-text ratio; a custom hex is held to text contrast.
    val accent = fitTextColor(accentRaw, background, text, if (customAccentInUse) MIN_TEXT_CONTRAST else MIN_UI_CONTRAST)
    val customBackgroundAdjusted = artworkSeed == null && customBackground != null && background != customBackground
    val adjusted = (customAccentInUse && accent != accentRaw) || customBackgroundAdjusted

    return FluviaColors(
        background = background,
        surface = if (dark) DarkSurface else Color.White,
        tile = if (dark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.85f),
        chip = if (dark) Color.White.copy(alpha = 0.12f) else Ink.copy(alpha = 0.08f),
        line = if (dark) Color.White.copy(alpha = 0.08f) else Ink.copy(alpha = 0.08f),
        text = text,
        text2 = text.copy(alpha = if (dark) 0.78f else 0.72f),
        accent = accent,
        onAccent = if (contrastRatio(Color.White, accent) >= contrastRatio(Ink, accent)) Color.White else Ink,
        adjusted = adjusted,
    )
}

private fun buildFluviaGlass(
    dark: Boolean,
    tier: GlassTier,
    blur: Int?,
    tint: Int?,
    edge: Int?,
): FluviaGlass =
    FluviaGlass(
        tier = tier,
        blur = blur?.dp?.coerceAtLeast(0.dp) ?: defaultBlur(tier),
        tint = tint?.let { (it / PERCENT).coerceIn(0f, 1f) } ?: defaultGlassTint(tier, dark),
        edge = edge?.let { (it / PERCENT).coerceIn(0f, 1f) } ?: defaultEdge(tier, dark),
        blurSet = blur != null,
        edgeSet = edge != null,
    )

internal fun defaultBlur(tier: GlassTier): Dp =
    when (tier) {
        GlassTier.FULL -> 24.dp
        GlassTier.FROSTED -> 40.dp
        GlassTier.SOLID -> 0.dp
    }

internal fun defaultEdge(
    tier: GlassTier,
    dark: Boolean,
): Float =
    when (tier) {
        GlassTier.FULL -> if (dark) 0.28f else 0.85f
        GlassTier.FROSTED -> if (dark) 0.22f else 0.70f
        GlassTier.SOLID -> if (dark) 0.14f else 0.10f
    }

private fun CornerStyle.scale(): Float =
    when (this) {
        CornerStyle.SHARP -> 0.25f
        CornerStyle.SOFT -> 1f
        CornerStyle.PILL -> 1.8f
    }

private fun SpacingDensity.scale(): Float =
    when (this) {
        SpacingDensity.COMPACT -> 0.8f
        SpacingDensity.COMFORTABLE -> 1f
        SpacingDensity.SPACIOUS -> 1.22f
    }

private fun DisplaySize.scale(): Float =
    when (this) {
        DisplaySize.XS -> 0.86f
        DisplaySize.SMALL -> 0.93f
        DisplaySize.MEDIUM -> 1f
        DisplaySize.LARGE -> 1.12f
    }
