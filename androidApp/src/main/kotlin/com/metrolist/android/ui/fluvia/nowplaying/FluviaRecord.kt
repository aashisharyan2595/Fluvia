/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.fluvia.nowplaying

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.metrolist.android.ui.fluvia.LocalFluviaTokens
import com.metrolist.android.ui.fluvia.fluviaGlass
import com.metrolist.shared.ui.components.images.AsyncImage
import android.provider.Settings as AndroidSettings

private val CARD_HEIGHT = 360.dp
private val RECORD_SIZE = 276.dp
private val LABEL_SIZE = 124.dp
private const val FULL_TURN = 360f
private const val SPIN_MILLIS = 12_000
private const val SWIPE_THRESHOLD_PX = 120f
private const val ARM_DOWN_DEGREES = 22f
private const val ARM_UP_DEGREES = -8f
private const val GROOVE_COUNT = 9

/** True unless the user turned system animations off (Remove animations / animator scale 0). */
@Composable
private fun animationsEnabled(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        AndroidSettings.Global.getFloat(resolver, AndroidSettings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
}

@Composable
fun FluviaRecord(
    artworkUrl: String?,
    isPlaying: Boolean,
    dotCount: Int,
    dotIndex: Int,
    onSwipePrevious: () -> Unit,
    onSwipeNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFluviaTokens.current
    val animate = animationsEnabled()
    val angle = remember { Animatable(0f) }
    LaunchedEffect(isPlaying, animate) {
        if (isPlaying && animate) {
            while (true) {
                val start = angle.value % FULL_TURN
                angle.snapTo(start)
                angle.animateTo(start + FULL_TURN, tween(SPIN_MILLIS, easing = LinearEasing))
            }
        }
    }
    val armAngle by animateFloatAsState(
        targetValue = if (isPlaying) ARM_DOWN_DEGREES else ARM_UP_DEGREES,
        animationSpec = if (animate) tween(450) else tween(0),
        label = "arm",
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT)
            .fluviaGlass(RoundedCornerShape(44.dp), tokens.colors.text)
            .pointerInput(onSwipePrevious, onSwipeNext) {
                var total = 0f
                detectHorizontalDragGestures(
                    onDragStart = { total = 0f },
                    onDragEnd = {
                        when {
                            total <= -SWIPE_THRESHOLD_PX -> onSwipeNext()
                            total >= SWIPE_THRESHOLD_PX -> onSwipePrevious()
                        }
                    },
                    onHorizontalDrag = { _, delta -> total += delta },
                )
            },
    ) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .size(RECORD_SIZE)
                .graphicsLayer { rotationZ = angle.value },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(Color(0xFF0B0C1B))
                val step = (size.minDimension / 2f - LABEL_SIZE.toPx() / 2f) / (GROOVE_COUNT + 1)
                repeat(GROOVE_COUNT) { i ->
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = LABEL_SIZE.toPx() / 2f + step * (i + 1),
                        style = Stroke(width = 1.dp.toPx()),
                    )
                }
                drawCircle(
                    brush =
                        Brush.sweepGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.12f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.12f),
                                Color.Transparent,
                            ),
                            Offset(size.width / 2f, size.height / 2f),
                        ),
                )
            }
            Box(Modifier.size(LABEL_SIZE).clip(CircleShape).background(tokens.colors.accent)) {
                if (artworkUrl != null) AsyncImage(url = artworkUrl, modifier = Modifier.fillMaxSize())
            }
            Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF0B0C1B)))
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(end = 36.dp, top = 30.dp)
                .size(8.dp, 84.dp)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 0f)
                    rotationZ = armAngle
                }.clip(RoundedCornerShape(4.dp))
                .background(tokens.colors.text.copy(alpha = 0.7f)),
        )
        Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(dotCount) { i ->
                if (i > 0) Spacer(Modifier.width(6.dp))
                Box(
                    Modifier
                        .height(6.dp)
                        .width(if (i == dotIndex) 18.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (i == dotIndex) tokens.colors.accent else tokens.colors.text.copy(alpha = 0.25f)),
                )
            }
        }
    }
}
