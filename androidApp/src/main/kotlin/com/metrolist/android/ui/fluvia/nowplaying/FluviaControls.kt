/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.fluvia.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.android.ui.fluvia.LocalFluviaTokens
import com.metrolist.android.ui.fluvia.fluviaGlass
import com.metrolist.android.ui.fluvia.fluviaIcon
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.action_like
import com.metrolist.shared.generated.resources.cast
import com.metrolist.shared.generated.resources.lyrics
import com.metrolist.shared.generated.resources.next
import com.metrolist.shared.generated.resources.pause
import com.metrolist.shared.generated.resources.play
import com.metrolist.shared.generated.resources.previous
import com.metrolist.shared.generated.resources.queue
import com.metrolist.shared.generated.resources.repeat
import com.metrolist.shared.generated.resources.shuffle
import com.metrolist.shared.generated.resources.sleep_timer
import com.metrolist.shared.generated.resources.unlike
import org.jetbrains.compose.resources.stringResource

private const val REPEAT_ONE = 1
private const val REPEAT_ALL = 2
private const val DISABLED_ALPHA = 0.38f

@Composable
private fun TapIcon(
    icon: ImageVector,
    description: String,
    size: Dp,
    tint: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = Color.Transparent,
    shape: Shape = CircleShape,
    iconSize: Dp = 24.dp,
) {
    Box(
        modifier
            .size(size)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clip(shape)
            .background(background)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun FluviaLikeButton(
    liked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalFluviaTokens.current.colors
    TapIcon(
        icon = fluviaIcon("heart", filled = liked),
        description = stringResource(if (liked) Res.string.unlike else Res.string.action_like),
        size = 48.dp,
        tint = if (liked) colors.accent else colors.text,
        enabled = enabled,
        onClick = onToggle,
        modifier = modifier,
    )
}

@Composable
fun FluviaTransport(
    isPlaying: Boolean,
    isShuffleOn: Boolean,
    repeatMode: Int,
    controlsEnabled: Boolean,
    onToggleShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalFluviaTokens.current.colors
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TapIcon(
            fluviaIcon("shuffle"),
            stringResource(Res.string.shuffle),
            48.dp,
            if (isShuffleOn) colors.accent else colors.text2,
            controlsEnabled,
            onToggleShuffle,
        )
        TapIcon(fluviaIcon("prev", filled = true), stringResource(Res.string.previous), 56.dp, colors.text, controlsEnabled, onPrevious)
        TapIcon(
            icon = fluviaIcon(if (isPlaying) "pause" else "play", filled = true),
            description = stringResource(if (isPlaying) Res.string.pause else Res.string.play),
            size = 88.dp,
            tint = colors.onAccent,
            enabled = controlsEnabled,
            onClick = onPlayPause,
            background = colors.accent,
            shape = RoundedCornerShape(30.dp),
            iconSize = 36.dp,
        )
        TapIcon(fluviaIcon("next", filled = true), stringResource(Res.string.next), 56.dp, colors.text, controlsEnabled, onNext)
        Box(contentAlignment = Alignment.Center) {
            TapIcon(
                fluviaIcon("repeat"),
                stringResource(Res.string.repeat),
                48.dp,
                if (repeatMode == REPEAT_ONE || repeatMode == REPEAT_ALL) colors.accent else colors.text2,
                controlsEnabled,
                onToggleRepeat,
            )
            if (repeatMode == REPEAT_ONE) {
                Text("1", fontSize = 9.sp, color = colors.accent, modifier = Modifier.padding(top = 1.dp))
            }
        }
    }
}

@Composable
fun FluviaTray(
    onLyrics: () -> Unit,
    onQueue: () -> Unit,
    onCast: (() -> Unit)?,
    onSleep: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalFluviaTokens.current.colors
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .fluviaGlass(RoundedCornerShape(28.dp), colors.text),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TrayItem(fluviaIcon("lyrics"), stringResource(Res.string.lyrics), onLyrics)
        TrayItem(fluviaIcon("queue"), stringResource(Res.string.queue), onQueue)
        if (onCast != null) TrayItem(fluviaIcon("cast"), stringResource(Res.string.cast), onCast)
        TrayItem(fluviaIcon("moon"), stringResource(Res.string.sleep_timer), onSleep)
    }
}

@Composable
private fun TrayItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val colors = LocalFluviaTokens.current.colors
    Column(
        Modifier
            .clip(
                RoundedCornerShape(16.dp),
            ).clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = colors.text, modifier = Modifier.size(22.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.text2, maxLines = 1)
    }
}
