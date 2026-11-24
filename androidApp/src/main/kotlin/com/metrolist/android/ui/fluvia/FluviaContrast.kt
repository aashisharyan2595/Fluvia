/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.fluvia

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import com.metrolist.shared.settings.GlassTier
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

const val MIN_TEXT_CONTRAST = 4.5
private const val GLASS_TINT_STEP = 0.02f
private const val COLOR_ADJUST_STEP = 0.04f

private const val EPSILON = 0.001f

// Layers that sit above the tint on each tier, taken from the prototype's glass recipe.
private const val FULL_DARK_SHEEN = 0.11f
private const val FULL_LIGHT_SHEEN = 0.27f
private const val FROSTED_DARK_SHEEN = 0.10f
private const val FROSTED_LIGHT_SHEEN = 0.46f

private val FullDarkTint = Color(0xFF0E0C1E)
private val FrostedDarkTint = Color(0xFF1E1A38)
private val FrostedLightTint = Color(0xFFF0ECF8)

fun contrastRatio(
    a: Color,
    b: Color,
): Double {
    val first = wcagLuminance(a)
    val second = wcagLuminance(b)
    return (max(first, second) + 0.05) / (min(first, second) + 0.05)
}

private fun wcagLuminance(color: Color): Double {
    fun channel(value: Float): Double {
        val v = value.toDouble()
        return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
}

/**
 * Moves [text] towards [towards] in [COLOR_ADJUST_STEP] steps until it reaches [min] against [background].
 * Returns [text] unchanged when it already passes, and [towards] when no step is enough.
 */
fun fitTextColor(
    text: Color,
    background: Color,
    towards: Color,
    min: Double = MIN_TEXT_CONTRAST,
): Color {
    if (contrastRatio(text, background) >= min) return text
    val steps = (1f / COLOR_ADJUST_STEP).roundToInt()
    for (index in 1 until steps) {
        val candidate = lerp(text, towards, index * COLOR_ADJUST_STEP)
        if (contrastRatio(candidate, background) >= min) return candidate
    }
    return towards
}

data class GlassResult(
    val tier: GlassTier,
    val tint: Float,
)

/** Highest tint alpha the guard may reach before the tier gives way. */
fun glassTintCap(
    tier: GlassTier,
    dark: Boolean,
): Float =
    when (tier) {
        GlassTier.FULL -> if (dark) 0.70f else 0.60f
        GlassTier.FROSTED -> if (dark) 0.80f else 0.70f
        GlassTier.SOLID -> 1f
    }

/** Tint alpha a tier starts from when the user has not set one. */
fun defaultGlassTint(
    tier: GlassTier,
    dark: Boolean,
): Float =
    when (tier) {
        GlassTier.FULL -> if (dark) 0.38f else 0.04f
        GlassTier.FROSTED -> if (dark) 0.50f else 0.40f
        GlassTier.SOLID -> 1f
    }

/** Colour a tier lays over the backdrop at the tint alpha. */
fun defaultGlassTintColor(
    tier: GlassTier,
    dark: Boolean,
): Color =
    when (tier) {
        GlassTier.FULL -> if (dark) FullDarkTint else Color.White
        GlassTier.FROSTED -> if (dark) FrostedDarkTint else FrostedLightTint
        GlassTier.SOLID -> Color.Unspecified
    }

private fun glassSheen(
    tier: GlassTier,
    dark: Boolean,
): Float =
    when (tier) {
        GlassTier.FULL -> if (dark) FULL_DARK_SHEEN else FULL_LIGHT_SHEEN
        GlassTier.FROSTED -> if (dark) FROSTED_DARK_SHEEN else FROSTED_LIGHT_SHEEN
        GlassTier.SOLID -> 0f
    }

/**
 * Raises the tint in [GLASS_TINT_STEP] steps from [startTint] until [text] reaches [MIN_TEXT_CONTRAST] on the blended surface,
 * up to the tier cap. When the cap is not enough the tier drops (Full, Frosted, Solid) and the search restarts there at the
 * higher of [startTint] and that tier's default. [text] may carry alpha; it is composited over the surface before measuring.
 */
fun guardGlass(
    tier: GlassTier,
    startTint: Float,
    dark: Boolean,
    text: Color,
    backdrop: Color,
    surfaceTint: Color,
): GlassResult {
    if (tier == GlassTier.SOLID) return GlassResult(GlassTier.SOLID, 1f)

    val cap = max(glassTintCap(tier, dark), startTint)
    val sheen = Color.White.copy(alpha = glassSheen(tier, dark))
    val steps = ((cap - startTint) / GLASS_TINT_STEP + EPSILON).toInt()
    for (index in 0..steps) {
        val tint = ((startTint + index * GLASS_TINT_STEP) * 100f).roundToInt() / 100f
        val surface = sheen.compositeOver(surfaceTint.copy(alpha = tint).compositeOver(backdrop))
        if (contrastRatio(text.compositeOver(surface), surface) >= MIN_TEXT_CONTRAST) {
            return GlassResult(tier, tint)
        }
    }

    val next = if (tier == GlassTier.FULL) GlassTier.FROSTED else GlassTier.SOLID
    return guardGlass(
        tier = next,
        startTint = max(startTint, defaultGlassTint(next, dark)),
        dark = dark,
        text = text,
        backdrop = backdrop,
        surfaceTint = defaultGlassTintColor(next, dark),
    )
}
