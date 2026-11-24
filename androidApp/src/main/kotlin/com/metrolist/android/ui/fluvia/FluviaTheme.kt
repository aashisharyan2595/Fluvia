/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.fluvia

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.metrolist.android.extensions.findActivity
import com.metrolist.android.ui.theme.SystemBarAppearance
import com.metrolist.android.utils.isAndroidTv
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.LocalIsTv
import com.metrolist.shared.ui.components.FocusOutlineProvider
import com.metrolist.shared.ui.haptics.MetrolistHapticsProvider
import com.metrolist.shared.ui.theme.MetrolistTypography
import com.metrolist.shared.ui.theme.animateColorScheme
import com.metrolist.shared.ui.theme.pureBlack

private const val PRIMARY_CONTAINER_MIX = 0.28f
private const val SURFACE_CONTAINER_LOW_MIX = 0.5f
private const val SURFACE_CONTAINER_HIGH_MIX = 0.04f
private const val SURFACE_CONTAINER_HIGHEST_MIX = 0.08f
private const val OUTLINE_ALPHA = 0.5f

/**
 * Applies the Fluvia tokens to Material 3. Dynamic colour and the palette style are ignored; the accent comes from the
 * Fluvia settings, or from [artworkSeed] when the caller supplies one.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FluviaTheme(
    darkTheme: Boolean,
    artworkSeed: Color?,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val lightStatusBars = SystemBarAppearance.playerLightStatusBars ?: (!darkTheme && SystemBarAppearance.imageOwners == 0)
    SideEffect {
        context.findActivity()?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = lightStatusBars
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    val isTv = remember(context) { context.isAndroidTv() }

    val tokens = rememberFluviaTokens(darkTheme, artworkSeed)
    val pureBlack = Settings.shared.isPureBlackTheme(darkTheme)
    val colorScheme =
        remember(tokens.colors, darkTheme, pureBlack) {
            val scheme = fluviaColorScheme(tokens.colors, darkTheme)
            if (darkTheme && pureBlack) scheme.pureBlack() else scheme
        }

    val motionScheme = remember { MotionScheme.expressive() }
    val animatedColorScheme = animateColorScheme(colorScheme, motionScheme)
    val background = colorScheme.surface.toArgb()
    DisposableEffect(context, background) {
        context.findActivity()?.window?.setBackgroundDrawable(ColorDrawable(background))
        onDispose { }
    }

    val baseTypography = MetrolistTypography()
    val typography =
        remember(baseTypography, tokens.fontId, tokens.textScale) {
            baseTypography.fluvia(fluviaFontFamily(tokens.fontId), tokens.textScale)
        }
    val shapes = remember(tokens.shapeScale) { fluviaShapes(tokens.shapeScale) }

    MaterialExpressiveTheme(
        colorScheme = animatedColorScheme,
        typography = typography,
        shapes = shapes,
        motionScheme = motionScheme,
    ) {
        CompositionLocalProvider(
            LocalFluviaTokens provides tokens,
            LocalIsTv provides isTv,
        ) {
            MetrolistHapticsProvider {
                FocusOutlineProvider(enabled = isTv, content = content)
            }
        }
    }
}

// Seam for font loading: replace this body, keep the signature.
private fun fluviaFontFamily(id: String): FontFamily = FontFamily.Default

private fun Typography.fluvia(
    fontFamily: FontFamily,
    scale: Float,
): Typography =
    Typography(
        displayLarge = displayLarge.fluvia(fontFamily, scale),
        displayMedium = displayMedium.fluvia(fontFamily, scale),
        displaySmall = displaySmall.fluvia(fontFamily, scale),
        headlineLarge = headlineLarge.fluvia(fontFamily, scale),
        headlineMedium = headlineMedium.fluvia(fontFamily, scale),
        headlineSmall = headlineSmall.fluvia(fontFamily, scale),
        titleLarge = titleLarge.fluvia(fontFamily, scale),
        titleMedium = titleMedium.fluvia(fontFamily, scale),
        titleSmall = titleSmall.fluvia(fontFamily, scale),
        bodyLarge = bodyLarge.fluvia(fontFamily, scale),
        bodyMedium = bodyMedium.fluvia(fontFamily, scale),
        bodySmall = bodySmall.fluvia(fontFamily, scale),
        labelLarge = labelLarge.fluvia(fontFamily, scale),
        labelMedium = labelMedium.fluvia(fontFamily, scale),
        labelSmall = labelSmall.fluvia(fontFamily, scale),
    )

private fun TextStyle.fluvia(
    fontFamily: FontFamily,
    scale: Float,
): TextStyle = copy(fontFamily = fontFamily, fontSize = fontSize * scale, lineHeight = lineHeight * scale)

private fun fluviaShapes(scale: Float): Shapes =
    Shapes(
        extraSmall = RoundedCornerShape(16.dp * scale),
        small = RoundedCornerShape(20.dp * scale),
        medium = RoundedCornerShape(24.dp * scale),
        large = RoundedCornerShape(28.dp * scale),
        extraLarge = RoundedCornerShape(32.dp * scale),
    )

private fun fluviaColorScheme(
    colors: FluviaColors,
    dark: Boolean,
): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    val surface = colors.background
    val tile = colors.surface
    val primaryContainer = lerp(tile, colors.accent, PRIMARY_CONTAINER_MIX)
    // The container mixes the accent into the tile, so text on it is re-fitted instead of assumed to pass.
    val onPrimaryContainer =
        fitTextColor(colors.text, primaryContainer, if (dark) Color.Black else Color.White, MIN_TEXT_CONTRAST)
    val secondaryContainer = colors.chip.compositeOver(tile)
    val onSurfaceVariant = colors.text2.compositeOver(surface)

    return base.copy(
        primary = colors.accent,
        onPrimary = colors.onAccent,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = colors.accent,
        secondary = colors.accent,
        onSecondary = colors.onAccent,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = colors.text,
        tertiary = colors.accent,
        onTertiary = colors.onAccent,
        tertiaryContainer = primaryContainer,
        onTertiaryContainer = onPrimaryContainer,
        background = surface,
        onBackground = colors.text,
        surface = surface,
        onSurface = colors.text,
        surfaceVariant = colors.chip.compositeOver(surface),
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = colors.accent,
        inverseSurface = colors.text,
        inverseOnSurface = surface,
        outline = colors.text.copy(alpha = OUTLINE_ALPHA).compositeOver(surface),
        outlineVariant = colors.line.compositeOver(surface),
        surfaceDim = surface,
        surfaceBright = tile,
        surfaceContainerLowest = surface,
        surfaceContainerLow = lerp(surface, tile, SURFACE_CONTAINER_LOW_MIX),
        surfaceContainer = tile,
        surfaceContainerHigh = lerp(tile, colors.text, SURFACE_CONTAINER_HIGH_MIX),
        surfaceContainerHighest = lerp(tile, colors.text, SURFACE_CONTAINER_HIGHEST_MIX),
    )
}
