/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.theme

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.metrolist.shared.ui.theme.ThemeColorExtractor

fun Bitmap.extractThemeColor(): Color = ThemeColorExtractor().extractSeedColor(this)

internal object SystemBarAppearance {
    var imageOwners by mutableIntStateOf(0)
    var playerLightStatusBars by mutableStateOf<Boolean?>(null)
}
