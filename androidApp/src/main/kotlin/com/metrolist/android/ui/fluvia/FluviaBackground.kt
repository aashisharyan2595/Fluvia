/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.fluvia

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.metrolist.shared.settings.FluviaBackground
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.components.images.AsyncImage

private const val AURORA_ALPHA_DARK = 0.30f
private const val AURORA_ALPHA_LIGHT = 0.22f
private const val MESH_ALPHA_DARK = 0.26f
private const val MESH_ALPHA_LIGHT = 0.20f
private const val SCRIM_ALPHA_DARK = 0.55f
private const val SCRIM_ALPHA_LIGHT = 0.62f
private const val ARTWORK_BLUR_DP = 48
private const val MESH_COLUMNS = 3
private const val MESH_ROWS = 3

/** A soft colour pool: centre and radius are fractions of the layer, so the shape follows any screen size. */
private data class Blob(
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Color,
)

/**
 * Fills its bounds with the background style from the Appearance settings. [artworkUrl] is the current song's artwork
 * and is only used by [FluviaBackground.ALBUM_ART]; without it, or below API 31 where blur is unavailable, that style
 * draws the aurora, whose colours come from the accent token (the artwork seed only when song-artwork colours are enabled).
 */
@Composable
fun FluviaBackgroundLayer(
    modifier: Modifier = Modifier,
    artworkUrl: String? = null,
) {
    val tokens = LocalFluviaTokens.current
    val colors = tokens.colors
    val style = Settings.shared.fluviaBackground
    val canBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    when {
        style == FluviaBackground.SOLID -> {
            Box(modifier.background(colors.background))
        }

        style == FluviaBackground.MESH -> {
            val blobs = remember(colors.background, colors.accent, colors.text) { meshBlobs(colors) }
            BlobCanvas(modifier, colors.background, blobs, if (tokens.dark) MESH_ALPHA_DARK else MESH_ALPHA_LIGHT)
        }

        style == FluviaBackground.ALBUM_ART && artworkUrl != null && canBlur -> {
            Box(modifier.clipToBounds()) {
                AsyncImage(
                    url = artworkUrl,
                    modifier = Modifier.fillMaxSize().blur(ARTWORK_BLUR_DP.dp, BlurredEdgeTreatment.Rectangle),
                    contentScale = ContentScale.Crop,
                )
                // Neutral scrim keeps text legible over any artwork.
                val scrim = if (tokens.dark) Color.Black.copy(alpha = SCRIM_ALPHA_DARK) else Color.White.copy(alpha = SCRIM_ALPHA_LIGHT)
                Box(Modifier.fillMaxSize().background(scrim))
            }
        }

        else -> {
            val blobs = remember(colors.background, colors.accent, colors.text) { auroraBlobs(colors) }
            BlobCanvas(modifier, colors.background, blobs, if (tokens.dark) AURORA_ALPHA_DARK else AURORA_ALPHA_LIGHT)
        }
    }
}

@Composable
private fun BlobCanvas(
    modifier: Modifier,
    base: Color,
    blobs: List<Blob>,
    alpha: Float,
) {
    Canvas(modifier) {
        drawRect(base)
        val longest = maxOf(size.width, size.height)
        blobs.forEach { blob ->
            drawRect(
                Brush.radialGradient(
                    colors = listOf(blob.color.copy(alpha = alpha), Color.Transparent),
                    center = Offset(size.width * blob.x, size.height * blob.y),
                    radius = longest * blob.radius,
                ),
            )
        }
    }
}

private fun auroraBlobs(colors: FluviaColors): List<Blob> =
    listOf(
        Blob(x = 0.15f, y = 0.05f, radius = 0.75f, color = colors.accent),
        Blob(x = 0.95f, y = 0.40f, radius = 0.65f, color = lerp(colors.accent, colors.text, 0.45f)),
        Blob(x = 0.30f, y = 0.95f, radius = 0.80f, color = lerp(colors.accent, colors.background, 0.35f)),
    )

/** Compose has no mesh gradient, so a grid of colour pools over the base approximates one. */
private fun meshBlobs(colors: FluviaColors): List<Blob> {
    val mixes = floatArrayOf(0f, 0.45f, 0.2f, 0.6f, 0f, 0.35f, 0.25f, 0.5f, 0f)
    return List(MESH_COLUMNS * MESH_ROWS) { index ->
        val column = index % MESH_COLUMNS
        val row = index / MESH_COLUMNS
        Blob(
            x = column / (MESH_COLUMNS - 1f),
            y = row / (MESH_ROWS - 1f),
            radius = 0.55f,
            color = lerp(colors.accent, colors.text, mixes[index]),
        )
    }
}
