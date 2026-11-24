/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.fluvia.nowplaying

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.android.ui.fluvia.FluviaBackdropHost
import com.metrolist.android.ui.fluvia.FluviaBackgroundLayer
import com.metrolist.android.ui.fluvia.LocalFluviaTokens
import com.metrolist.android.ui.fluvia.fluviaGlass
import com.metrolist.android.ui.fluvia.fluviaIcon
import com.metrolist.shared.cast.CastState
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.fluvia_nothing_playing
import com.metrolist.shared.generated.resources.fluvia_playing_from
import com.metrolist.shared.generated.resources.more_options
import com.metrolist.shared.generated.resources.now_playing
import com.metrolist.shared.generated.resources.share
import com.metrolist.shared.generated.resources.view_album
import com.metrolist.shared.generated.resources.view_artist
import com.metrolist.shared.playback.LocalPlayerSession
import com.metrolist.shared.playback.SleepTimerState
import com.metrolist.shared.ui.components.SleepTimerDialog
import com.metrolist.shared.ui.player.LocalPlayerPlatform
import com.metrolist.shared.ui.player.LyricsViewWithPosition
import com.metrolist.shared.ui.player.QueueScreen
import com.metrolist.shared.viewmodels.PlayerViewModel
import org.jetbrains.compose.resources.stringResource

private const val DOT_WINDOW = 5
private const val BLOB_ALPHA = 0.45f

/**
 * Fluvia's expanded player: a record card, waveform seek bar and glass controls. Reads [viewModel] state and calls
 * its existing actions; queue, lyrics, sleep timer and cast reuse the shared components.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluviaNowPlaying(
    viewModel: PlayerViewModel,
    onClose: () -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToIntegrations: () -> Unit,
    onLyricsVisibleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isScreenVisible: Boolean = true,
) {
    val tokens = LocalFluviaTokens.current
    val colors = tokens.colors
    val uiState by viewModel.uiState.collectAsState()
    val song = uiState.currentSong
    val platform = LocalPlayerPlatform.current
    val session = LocalPlayerSession.current
    val isGuest = platform.isListenTogetherGuest()
    val controlsEnabled = song != null && !isGuest
    val sleepTimerState by session?.sleepTimerState?.collectAsState() ?: remember { mutableStateOf(SleepTimerState()) }
    val castState by session?.castState?.collectAsState() ?: remember { mutableStateOf(CastState()) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var showDeviceSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val openCast = platform.rememberCastDialogLauncher { showDeviceSheet = true }

    SideEffect { onLyricsVisibleChange(uiState.isLyricsVisible) }
    LaunchedEffect(song?.id, uiState.isLyricsVisible) {
        if (uiState.isLyricsVisible && song != null) viewModel.loadLyrics(song)
    }
    BackHandler(enabled = isScreenVisible) {
        when {
            uiState.isQueueVisible -> viewModel.setQueueVisible(false)
            uiState.isLyricsVisible -> viewModel.setLyricsVisible(false)
            else -> onClose()
        }
    }

    val ambient = uiState.dominantColor
    FluviaBackdropHost(
        background = colors.background,
        backdropContent = {
            Box(Modifier.fillMaxSize()) {
                FluviaBackgroundLayer(Modifier.fillMaxSize(), artworkUrl = song?.thumbnail)
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        Brush.radialGradient(
                            listOf(ambient.copy(alpha = BLOB_ALPHA), Color.Transparent),
                            Offset(
                                size.width * 0.15f,
                                size.height * 0.2f,
                            ),
                            size.width * 0.8f,
                        ),
                        radius = size.width * 0.8f,
                        center = Offset(size.width * 0.15f, size.height * 0.2f),
                    )
                    drawCircle(
                        Brush.radialGradient(
                            listOf(ambient.copy(alpha = BLOB_ALPHA * 0.7f), Color.Transparent),
                            Offset(
                                size.width * 0.9f,
                                size.height * 0.75f,
                            ),
                            size.width * 0.7f,
                        ),
                        radius = size.width * 0.7f,
                        center = Offset(size.width * 0.9f, size.height * 0.75f),
                    )
                }
            }
        },
    ) {
        Column(
            modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    fluviaIcon("chevD"),
                    contentDescription = stringResource(Res.string.now_playing),
                    tint = colors.text,
                    modifier =
                        Modifier
                            .size(
                                44.dp,
                            ).clip(RoundedCornerShape(22.dp))
                            .clickable(role = Role.Button, onClick = onClose)
                            .padding(10.dp),
                )
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(Res.string.fluvia_playing_from), style = MaterialTheme.typography.labelSmall, color = colors.text2)
                    Text(
                        uiState.queueTitle ?: stringResource(Res.string.now_playing),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Box {
                    Icon(
                        fluviaIcon("more"),
                        contentDescription = stringResource(Res.string.more_options),
                        tint = colors.text,
                        modifier =
                            Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .clickable(enabled = song != null, role = Role.Button) {
                                    showMenu =
                                        true
                                }.padding(10.dp),
                    )
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.share)) },
                            onClick = {
                                showMenu = false
                                viewModel.shareSong()
                            },
                        )
                        song?.albumId?.let { albumId ->
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.view_album)) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToAlbum(albumId)
                                },
                            )
                        }
                        song?.artists?.firstOrNull()?.id?.let { artistId ->
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.view_artist)) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToArtist(artistId)
                                },
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            if (uiState.isLyricsVisible) {
                Box(Modifier.fillMaxWidth().height(360.dp).fluviaGlass(RoundedCornerShape(44.dp), colors.text)) {
                    LyricsViewWithPosition(
                        viewModel = viewModel,
                        lyrics = uiState.lyrics,
                        songwriters = uiState.lyricsSongwriters,
                        agents = uiState.lyricsAgents,
                        provider = uiState.lyricsProvider,
                        timingOffsetMs = uiState.lyricsOffsetMs,
                        onSeek = viewModel::seekToPosition,
                        isLoading = uiState.isLyricsLoading,
                        textColor = colors.text,
                        isCasting = castState.isConnected,
                        isBuffering = castState.isBuffering,
                    )
                }
            } else {
                val index = uiState.currentQueueIndex
                val dots = uiState.queueItems.size.coerceAtMost(DOT_WINDOW)
                val dotIndex =
                    when {
                        dots == 0 || index < 0 -> -1
                        uiState.queueItems.size <= DOT_WINDOW -> index
                        else -> (index * dots / uiState.queueItems.size).coerceIn(0, dots - 1)
                    }
                FluviaRecord(
                    artworkUrl = song?.displayThumbnail,
                    isPlaying = uiState.isPlaying,
                    dotCount = dots,
                    dotIndex = dotIndex,
                    onSwipePrevious = { if (controlsEnabled) viewModel.previous() },
                    onSwipeNext = { if (controlsEnabled) viewModel.next() },
                )
            }
            Spacer(Modifier.height(20.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        song?.title ?: stringResource(Res.string.fluvia_nothing_playing),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        song?.artists?.joinToString { it.name }.orEmpty(),
                        fontSize = 17.sp,
                        color = colors.text2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                FluviaLikeButton(liked = song?.isLiked == true, enabled = song != null, onToggle = viewModel::toggleLike)
            }
            Spacer(Modifier.height(16.dp))

            PositionSection(viewModel, song?.id, uiState.duration, controlsEnabled && !uiState.isLive)
            Spacer(Modifier.height(16.dp))

            FluviaTransport(
                isPlaying = uiState.isPlaying,
                isShuffleOn = uiState.isShuffleOn,
                repeatMode = uiState.repeatMode,
                controlsEnabled = controlsEnabled,
                onToggleShuffle = viewModel::toggleShuffle,
                onPrevious = viewModel::previous,
                onPlayPause = viewModel::togglePlayPause,
                onNext = viewModel::next,
                onToggleRepeat = viewModel::toggleRepeat,
            )
            Spacer(Modifier.height(24.dp))

            FluviaTray(
                onLyrics = { viewModel.setLyricsVisible(!uiState.isLyricsVisible) },
                onQueue = { viewModel.setQueueVisible(true) },
                onCast = if (!isGuest && platform.supportsCasting) openCast else null,
                onSleep = { showSleepTimer = true },
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    if (uiState.isQueueVisible) {
        ModalBottomSheet(onDismissRequest = { viewModel.setQueueVisible(false) }) {
            QueueScreen(
                viewModel = viewModel,
                onClose = { viewModel.setQueueVisible(false) },
                onShowSleepTimer = { showSleepTimer = true },
                onNavigateToAlbum = onNavigateToAlbum,
                onNavigateToArtist = onNavigateToArtist,
            )
        }
    }
    if (showSleepTimer) {
        SleepTimerDialog(
            state = sleepTimerState,
            onDismiss = { showSleepTimer = false },
            onStart = { durationMs, fadeOut, finishCurrentSong -> session?.startSleepTimer(durationMs, fadeOut, finishCurrentSong) },
            onStopAfterCurrentSong = { fadeOut -> session?.stopAfterCurrentSong(fadeOut) },
            onCancelTimer = { session?.cancelSleepTimer() },
        )
    }
    if (showDeviceSheet) {
        platform.DeviceConnectSheet(
            onDismiss = { showDeviceSheet = false },
            onNavigateToIntegrations = {
                showDeviceSheet = false
                onNavigateToIntegrations()
            },
        )
    }
}

/** Collects the position here so only the waveform and time labels recompose as playback advances. */
@Composable
private fun PositionSection(
    viewModel: PlayerViewModel,
    songId: String?,
    durationMs: Long,
    seekEnabled: Boolean,
) {
    val colors = LocalFluviaTokens.current.colors
    val position by viewModel.currentPosition.collectAsState()
    val heights = remember(songId) { waveformHeights(songId.orEmpty()) }
    val progress = if (durationMs > 0L) (position.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    Column {
        FluviaWaveform(
            heights = heights,
            progress = progress,
            durationMs = durationMs,
            enabled = seekEnabled,
            onSeek = { fraction -> viewModel.seekTo(fraction, durationMs, songId) },
        )
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                formatPlaybackTime(
                    if (durationMs >
                        0L
                    ) {
                        position
                    } else {
                        -1L
                    },
                ),
                style = MaterialTheme.typography.labelMedium,
                color = colors.text2,
            )
            Text(
                formatPlaybackTime(
                    if (durationMs >
                        0L
                    ) {
                        durationMs
                    } else {
                        -1L
                    },
                ),
                style = MaterialTheme.typography.labelMedium,
                color = colors.text2,
            )
        }
    }
}
