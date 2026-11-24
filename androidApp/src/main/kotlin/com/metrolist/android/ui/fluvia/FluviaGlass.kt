/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.fluvia

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.metrolist.shared.settings.GlassTier
import kotlin.math.min

private const val LENS_MAX_HEIGHT_DP = 16f
private const val LENS_AMOUNT_FACTOR = 2f
private const val EDGE_WIDTH_DP = 1f

/** Content that glass surfaces blur. Null outside a [FluviaBackdropHost], where glass falls back to a solid surface. */
val LocalFluviaBackdrop: ProvidableCompositionLocal<Backdrop?> = staticCompositionLocalOf { null }

/**
 * Captures [backdropContent] over [background] as the backdrop and draws [content] above it as a sibling. Glass
 * surfaces inside [content] sample the backdrop; they must not be part of [backdropContent], which would make the
 * backdrop contain itself.
 */
@Composable
fun FluviaBackdropHost(
    background: Color,
    backdropContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val backdrop =
        rememberLayerBackdrop {
            drawRect(background)
            drawContent()
        }
    Box {
        Box(Modifier.layerBackdrop(backdrop)) {
            backdropContent()
        }
        CompositionLocalProvider(LocalFluviaBackdrop provides backdrop) {
            content()
        }
    }
}

/**
 * Glass surface for [shape], with [text] as the colour of the text it will hold. Tier, blur, tint and edge come from
 * [LocalFluviaTokens]; [guardGlass] may raise the tint or drop the tier so [text] stays readable. Below API 31, without a
 * backdrop, or on the Solid tier it draws an opaque surface instead.
 */
fun Modifier.fluviaGlass(
    shape: Shape,
    text: Color,
): Modifier =
    composed {
        val tokens = LocalFluviaTokens.current
        val backdrop = LocalFluviaBackdrop.current
        val density = LocalDensity.current
        var surfaceSize by remember { mutableStateOf(IntSize.Zero) }
        val colors = tokens.colors
        val dark = tokens.dark
        val requested = tokens.glass.tier
        val canBlur = backdrop != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val guarded =
            if (requested == GlassTier.SOLID || !canBlur) {
                GlassResult(GlassTier.SOLID, 1f)
            } else {
                guardGlass(
                    tier = requested,
                    startTint = tokens.glass.tint,
                    dark = dark,
                    text = text,
                    backdrop = colors.background,
                    surfaceTint = defaultGlassTintColor(requested, dark),
                )
            }
        val tier = guarded.tier
        val edgeAlpha =
            if (tokens.glass.edgeSet || tier == requested) tokens.glass.edge else defaultEdge(tier, dark)

        if (tier == GlassTier.SOLID || backdrop == null) {
            val solidEdge = if (tokens.glass.edgeSet) tokens.glass.edge else defaultEdge(GlassTier.SOLID, dark)
            Modifier
                .background(colors.surface, shape)
                .drawEdge(shape, colors.text.copy(alpha = solidEdge))
        } else {
            val tint = defaultGlassTintColor(tier, dark).copy(alpha = guarded.tint)
            val sheen = Color.White.copy(alpha = glassSheen(tier, dark))
            val blurRadius =
                if (tokens.glass.blurSet || tier == requested) tokens.glass.blur else defaultBlur(tier)
            val lensHeight =
                lensHeightPx(tier, shape, Size(surfaceSize.width.toFloat(), surfaceSize.height.toFloat()), density)
            Modifier
                .onSizeChanged { surfaceSize = it }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { shape },
                    effects = {
                        vibrancy()
                        blur(blurRadius.toPx())
                        if (lensHeight > 0f) {
                            lens(lensHeight, lensHeight * LENS_AMOUNT_FACTOR)
                        }
                    },
                    onDrawSurface = {
                        drawRect(tint)
                        drawRect(sheen)
                    },
                ).drawEdge(shape, Color.White.copy(alpha = edgeAlpha))
        }
    }

/** Lens refraction height in px, or 0 when the lens does not apply (tier, API level or shape). */
private fun lensHeightPx(
    tier: GlassTier,
    shape: Shape,
    size: Size,
    density: Density,
): Float {
    if (tier != GlassTier.FULL || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return 0f
    if (shape !is CornerBasedShape || size.minDimension <= 0f) return 0f
    val radius =
        min(
            min(shape.topStart.toPx(size, density), shape.topEnd.toPx(size, density)),
            min(shape.bottomStart.toPx(size, density), shape.bottomEnd.toPx(size, density)),
        )
    return min(radius, with(density) { LENS_MAX_HEIGHT_DP.dp.toPx() })
}

/** 1dp outline kept inside [shape] by insetting it half the stroke width. */
private fun Modifier.drawEdge(
    shape: Shape,
    color: Color,
): Modifier =
    drawBehind {
        if (color.alpha <= 0f) return@drawBehind
        val half = EDGE_WIDTH_DP.dp.toPx() / 2f
        inset(half) {
            drawOutline(
                outline = shape.createOutline(this.size, layoutDirection, this),
                color = color,
                style = Stroke(width = EDGE_WIDTH_DP.dp.toPx()),
            )
        }
    }
