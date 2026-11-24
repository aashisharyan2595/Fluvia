/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.fluvia.nowplaying

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.metrolist.android.ui.fluvia.LocalFluviaTokens
import com.metrolist.android.ui.fluvia.fluviaGlass
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.fluvia_seek_position
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.roundToInt

private const val BAR_COUNT = 48
private const val MIN_BAR = 0.2f
private const val SEED_MIX = 31
private const val SEED_RANGE = 1000
private const val MS_PER_SECOND = 1000L
private const val SECONDS_PER_MINUTE = 60L
private val WAVEFORM_HEIGHT = 48.dp
private val BUBBLE_WIDTH = 120.dp
private val BUBBLE_LIFT = 44.dp

/** Bar heights in [MIN_BAR]..1 derived from [seed]; decorative, not read from the audio. */
fun waveformHeights(
    seed: String,
    count: Int = BAR_COUNT,
): List<Float> {
    var state = seed.fold(7) { acc, c -> acc * SEED_MIX + c.code }
    return List(count) {
        state = state * 1_103_515_245 + 12_345
        val unit = ((state ushr 8) % SEED_RANGE) / SEED_RANGE.toFloat()
        MIN_BAR + unit * (1f - MIN_BAR)
    }
}

/** `m:ss` for [ms], or a dash when the position is unknown. */
fun formatPlaybackTime(ms: Long): String {
    if (ms < 0) return "–"
    val totalSeconds = ms / MS_PER_SECOND
    return "${totalSeconds / SECONDS_PER_MINUTE}:${(totalSeconds % SECONDS_PER_MINUTE).toString().padStart(2, '0')}"
}

@Composable
fun FluviaWaveform(
    heights: List<Float>,
    progress: Float,
    durationMs: Long,
    enabled: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFluviaTokens.current
    val accent = tokens.colors.accent
    val idle = tokens.colors.text.copy(alpha = 0.22f)
    val seekable = enabled && durationMs > 0L
    var scrub by remember { mutableStateOf<Float?>(null) }
    val shown = (scrub ?: progress).coerceIn(0f, 1f)
    val seekLabel = stringResource(Res.string.fluvia_seek_position)

    BoxWithConstraints(modifier.fillMaxWidth().height(WAVEFORM_HEIGHT)) {
        Canvas(Modifier.fillMaxSize()) {
            val slot = size.width / heights.size
            val barWidth = slot * 0.55f
            heights.forEachIndexed { index, value ->
                val barHeight = size.height * value
                val x = index * slot + (slot - barWidth) / 2f
                val played = (index + 0.5f) / heights.size <= shown
                drawRoundRect(
                    color = if (played && seekable) accent else idle,
                    topLeft = Offset(x, (size.height - barHeight) / 2f),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f),
                )
            }
            if (seekable) {
                val x = size.width * shown
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            listOf(accent.copy(alpha = 0.55f), Color.Transparent),
                            Offset(x, size.height / 2f),
                            18.dp.toPx(),
                        ),
                    radius = 18.dp.toPx(),
                    center = Offset(x, size.height / 2f),
                )
                drawCircle(color = accent, radius = 5.dp.toPx(), center = Offset(x, size.height / 2f))
            }
        }
        // Carries touch and TalkBack semantics; the bars above are the visible control.
        Slider(
            value = shown,
            onValueChange = { scrub = it },
            onValueChangeFinished = {
                scrub?.let(onSeek)
                scrub = null
            },
            enabled = seekable,
            modifier = Modifier.fillMaxSize().alpha(0f).semantics { contentDescription = seekLabel },
        )
        scrub?.let { target ->
            val delta = ((target - progress) * durationMs).toLong()
            val sign = if (delta < 0) "−" else "+"
            val bubbleWidth = with(LocalDensity.current) { BUBBLE_WIDTH.roundToPx() }
            val lift = with(LocalDensity.current) { BUBBLE_LIFT.roundToPx() }
            val widthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
            val x = (widthPx * target - bubbleWidth / 2f).roundToInt().coerceIn(0, (widthPx - bubbleWidth).coerceAtLeast(0))
            Box(
                Modifier
                    .offset { IntOffset(x, -lift) }
                    .width(BUBBLE_WIDTH)
                    .fluviaGlass(RoundedCornerShape(14.dp), tokens.colors.text)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${formatPlaybackTime((target * durationMs).toLong())}  $sign${formatPlaybackTime(abs(delta))}",
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.colors.text,
                )
            }
        }
    }
}
