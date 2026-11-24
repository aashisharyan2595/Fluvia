/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.fluvia

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.android.utils.isAndroidAutomotive
import com.metrolist.android.utils.isAndroidTv
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.loading
import com.metrolist.shared.generated.resources.mute
import com.metrolist.shared.generated.resources.next
import com.metrolist.shared.generated.resources.pause
import com.metrolist.shared.generated.resources.play
import com.metrolist.shared.generated.resources.unmute
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.components.images.AsyncImage
import com.metrolist.shared.ui.icons.MetrolistIcons
import org.jetbrains.compose.resources.stringResource
import android.provider.Settings as AndroidSettings

private const val FULL_TURN = 360f
private const val SPIN_MILLIS = 9_000
private const val DISABLED_ALPHA = 0.38f
private const val TRACK_ALPHA = 0.18f

/** Whether the floating dock applies: a phone-style layout with the setting on, never TV, car or desktop mode. */
@Composable
fun rememberFluviaDock(useDesktopLayout: Boolean): Boolean {
    val context = LocalContext.current
    val directional = remember(context) { context.isAndroidTv() || context.isAndroidAutomotive() }
    return Settings.shared.fluviaDock && !useDesktopLayout && !directional
}

/** True unless the user turned system animations off. */
@Composable
private fun animationsEnabled(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        AndroidSettings.Global.getFloat(resolver, AndroidSettings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
}

/** The mini player row of the Fluvia dock: spinning artwork, title, accent play button, next, thin progress bar. */
@Composable
fun FluviaMiniPlayerContent(
    metadataModifier: Modifier,
    marqueeModifier: Modifier,
    title: String,
    artist: String,
    artworkUrl: String?,
    isPlaying: Boolean,
    isPlaybackLoading: Boolean,
    progress: () -> Float,
    isListenTogetherGuest: Boolean,
    isMuted: Boolean,
    canSkipNext: Boolean,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalFluviaTokens.current.colors
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
    val actionLabel =
        stringResource(
            when {
                isListenTogetherGuest && isMuted -> Res.string.unmute
                isListenTogetherGuest -> Res.string.mute
                isPlaying -> Res.string.pause
                else -> Res.string.play
            },
        )
    val loadingLabel = stringResource(Res.string.loading)

    Box(modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxSize().padding(start = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .graphicsLayer { rotationZ = angle.value }
                    .clip(CircleShape)
                    .background(colors.tile),
            ) {
                if (artworkUrl != null) AsyncImage(url = artworkUrl, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f).then(metadataModifier)) {
                Text(
                    title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = marqueeModifier,
                )
                Text(
                    artist,
                    fontSize = 13.sp,
                    color = colors.text2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = marqueeModifier,
                )
            }
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.accent)
                    .clickable(role = Role.Button, onClick = onTogglePlayPause),
                contentAlignment = Alignment.Center,
            ) {
                if (isPlaybackLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).semantics { contentDescription = loadingLabel },
                        color = colors.onAccent,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector =
                            when {
                                isListenTogetherGuest && isMuted -> MetrolistIcons.AutoMirrored.Filled.VolumeUp
                                isListenTogetherGuest -> MetrolistIcons.AutoMirrored.Filled.VolumeOff
                                isPlaying -> fluviaIcon("pause", filled = true)
                                else -> fluviaIcon("play", filled = true)
                            },
                        contentDescription = actionLabel,
                        tint = colors.onAccent,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Box(
                Modifier
                    .size(48.dp)
                    .alpha(if (canSkipNext) 1f else DISABLED_ALPHA)
                    .clip(CircleShape)
                    .clickable(enabled = canSkipNext, role = Role.Button, onClick = onSkipNext),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    fluviaIcon("next", filled = true),
                    contentDescription = stringResource(Res.string.next),
                    tint = colors.text,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        // Read inside the draw scope so playback progress redraws only this bar.
        Canvas(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(3.dp),
        ) {
            val radius = CornerRadius(size.height / 2f)
            drawRoundRect(colors.text.copy(alpha = TRACK_ALPHA), size = size, cornerRadius = radius)
            drawRoundRect(colors.accent, size = Size(size.width * progress().coerceIn(0f, 1f), size.height), cornerRadius = radius)
        }
    }
}
