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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
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

private const val LUMINANCE_DARK_LIMIT = 0.5f
private const val LENS_MAX_HEIGHT_DP = 16f
private const val LENS_AMOUNT_FACTOR = 2f

/** Content that glass surfaces blur. Null outside a [FluviaBackdropHost], where glass falls back to a solid surface. */
val LocalFluviaBackdrop: ProvidableCompositionLocal<Backdrop?> = staticCompositionLocalOf { null }

/** Draws [background] and [content] into a backdrop that [fluviaGlass] surfaces below it can sample. */
@Composable
fun FluviaBackdropHost(
    background: Color,
    content: @Composable () -> Unit,
) {
    val backdrop =
        rememberLayerBackdrop {
            drawRect(background)
            drawContent()
        }
    CompositionLocalProvider(LocalFluviaBackdrop provides backdrop) {
        Box(Modifier.layerBackdrop(backdrop)) {
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
        val colors = tokens.colors
        val dark = colors.background.luminance() < LUMINANCE_DARK_LIMIT
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

        if (guarded.tier == GlassTier.SOLID || backdrop == null) {
            Modifier.background(colors.surface, shape)
        } else {
            val tier = guarded.tier
            val tint = defaultGlassTintColor(tier, dark).copy(alpha = guarded.tint)
            val sheen = Color.White.copy(alpha = glassSheen(tier, dark))
            val edge = Color.White.copy(alpha = tokens.glass.edge)
            val blurRadius = tokens.glass.blur
            val lensShape = shape as? CornerBasedShape
            val useLens = tier == GlassTier.FULL && lensShape != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            Modifier.drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(blurRadius.toPx())
                    if (useLens && lensShape != null) {
                        val radius =
                            min(
                                min(lensShape.topStart.toPx(size, this), lensShape.topEnd.toPx(size, this)),
                                min(lensShape.bottomStart.toPx(size, this), lensShape.bottomEnd.toPx(size, this)),
                            )
                        val height = min(radius, LENS_MAX_HEIGHT_DP.dp.toPx())
                        if (height > 0f) {
                            lens(height, height * LENS_AMOUNT_FACTOR)
                        }
                    }
                },
                onDrawSurface = {
                    drawRect(tint)
                    drawRect(sheen)
                    if (edge.alpha > 0f) {
                        drawOutline(
                            outline = shape.createOutline(size, layoutDirection, this),
                            color = edge,
                            style = Stroke(width = 1.dp.toPx()),
                        )
                    }
                },
            )
        }
    }
