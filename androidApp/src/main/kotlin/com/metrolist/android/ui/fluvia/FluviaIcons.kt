/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.fluvia

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.metrolist.shared.settings.IconStyle
import java.util.concurrent.ConcurrentHashMap

/**
 * One selectable icon pack. [paths] maps an icon key to SVG path data in a [viewport] square whose top edge is at
 * [minY]; a key suffixed with `F` is the pack's filled variant of that icon.
 */
@Immutable
data class IconPackData(
    val id: String,
    val label: String,
    val licence: String,
    val stroke: Boolean,
    val viewport: Float,
    val paths: Map<String, String>,
    val minY: Float = 0f,
)

private const val DEFAULT_PACK_ID = "fluvia"
private const val FILLED_SUFFIX = "F"
private const val FILLED_STROKE_WIDTH = 1.5f
private const val DUO_FILL_ALPHA = 0.28f

val FluviaIconPacks: List<IconPackData> =
    listOf(
        IconPackData(
            "fluvia",
            FluviaIconPack.LABEL,
            FluviaIconPack.LICENCE,
            FluviaIconPack.STROKE,
            FluviaIconPack.VIEWPORT,
            FluviaIconPack.PATHS,
            FluviaIconPack.MIN_Y,
        ),
        IconPackData(
            "lucide",
            LucideIconPack.LABEL,
            LucideIconPack.LICENCE,
            LucideIconPack.STROKE,
            LucideIconPack.VIEWPORT,
            LucideIconPack.PATHS,
            LucideIconPack.MIN_Y,
        ),
        IconPackData(
            "feather",
            FeatherIconPack.LABEL,
            FeatherIconPack.LICENCE,
            FeatherIconPack.STROKE,
            FeatherIconPack.VIEWPORT,
            FeatherIconPack.PATHS,
            FeatherIconPack.MIN_Y,
        ),
        IconPackData(
            "tabler",
            TablerIconPack.LABEL,
            TablerIconPack.LICENCE,
            TablerIconPack.STROKE,
            TablerIconPack.VIEWPORT,
            TablerIconPack.PATHS,
            TablerIconPack.MIN_Y,
        ),
        IconPackData(
            "phosphor",
            PhosphorIconPack.LABEL,
            PhosphorIconPack.LICENCE,
            PhosphorIconPack.STROKE,
            PhosphorIconPack.VIEWPORT,
            PhosphorIconPack.PATHS,
            PhosphorIconPack.MIN_Y,
        ),
        IconPackData(
            "material",
            MaterialIconPack.LABEL,
            MaterialIconPack.LICENCE,
            MaterialIconPack.STROKE,
            MaterialIconPack.VIEWPORT,
            MaterialIconPack.PATHS,
            MaterialIconPack.MIN_Y,
        ),
    )

private data class IconCacheKey(
    val packId: String,
    val key: String,
    val filled: Boolean,
    val style: IconStyle,
)

private val iconCache = ConcurrentHashMap<IconCacheKey, ImageVector>()

private val defaultPack: IconPackData = FluviaIconPacks.first { it.id == DEFAULT_PACK_ID }

/**
 * The icon [key] in the pack and style chosen in appearance settings. The vector is tint-agnostic: draw it with
 * `Icon`, which tints with `LocalContentColor` unless told otherwise.
 */
@Composable
fun fluviaIcon(
    key: String,
    filled: Boolean = false,
): ImageVector {
    val tokens = LocalFluviaTokens.current
    return fluviaIconVector(tokens.iconPackId, key, filled, tokens.iconStyle)
}

private fun fluviaIconVector(
    packId: String,
    key: String,
    filled: Boolean,
    style: IconStyle,
): ImageVector {
    val pack = FluviaIconPacks.firstOrNull { it.id == packId } ?: defaultPack
    return iconCache.getOrPut(IconCacheKey(pack.id, key, filled, style)) {
        buildIcon(pack, key, filled, style)
    }
}

private fun buildIcon(
    pack: IconPackData,
    key: String,
    filled: Boolean,
    style: IconStyle,
): ImageVector {
    // A key a pack lacks falls back to the Fluvia drawing, which is stroked, so the stroke rules follow that pack.
    val owner = if (pack.paths.containsKey(key)) pack else defaultPack
    val filledPath = if (filled) owner.paths[key + FILLED_SUFFIX] else null
    val data = filledPath ?: owner.paths.getValue(key)
    val nodes: List<PathNode> = PathParser().parsePathString(data).toNodes()

    val builder =
        ImageVector.Builder(
            name = "fluvia.${owner.id}.$key${if (filled) ".filled" else ""}.${style.name.lowercase()}",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = owner.viewport,
            viewportHeight = owner.viewport,
        )
    val black = SolidColor(Color.Black)
    // ImageVector has no viewport origin, so a pack whose viewBox starts above zero is shifted down into place.
    val shifted = owner.minY != 0f
    if (shifted) builder.addGroup(translationY = -owner.minY)

    when {
        !owner.stroke -> builder.addPath(pathData = nodes, fill = black)
        filled ->
            builder.addPath(
                pathData = nodes,
                pathFillType = PathFillType.NonZero,
                fill = black,
                stroke = black.takeIf { owner.id != DEFAULT_PACK_ID },
                strokeLineWidth = FILLED_STROKE_WIDTH,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        else -> {
            val line = style == IconStyle.LINE
            builder.addPath(
                pathData = nodes,
                fill = black.takeIf { style == IconStyle.SOLID || style == IconStyle.DUO },
                fillAlpha = if (style == IconStyle.DUO) DUO_FILL_ALPHA else 1f,
                stroke = black,
                strokeLineWidth = style.strokeWidth(),
                strokeLineCap = if (line) StrokeCap.Butt else StrokeCap.Round,
                strokeLineJoin = if (line) StrokeJoin.Miter else StrokeJoin.Round,
            )
        }
    }

    if (shifted) builder.clearGroup()
    return builder.build()
}

private fun IconStyle.strokeWidth(): Float =
    when (this) {
        IconStyle.LINE -> 1.8f
        IconStyle.SOLID -> 1f
        IconStyle.SOFT -> 2.4f
        IconStyle.DUO -> 1.8f
    }
