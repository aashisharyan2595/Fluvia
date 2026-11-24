/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.metrolist.android.ui.fluvia.FluviaIconPacks
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.close
import com.metrolist.shared.generated.resources.credits_fonts_title
import com.metrolist.shared.generated.resources.credits_icon_pack_entry
import com.metrolist.shared.generated.resources.credits_icon_packs_title
import com.metrolist.shared.generated.resources.error_occurred
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import kotlin.coroutines.cancellation.CancellationException

private const val FLUVIA_PACK_ID = "fluvia"
private const val FONT_LICENSES_PATH = "files/licenses/fonts/LICENSES.md"

private data class LicenseDialog(
    val title: String,
    val path: String,
)

/** Licence rows for the bundled third-party icon packs and fonts; each opens its full licence text. */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun ColumnScope.IconPackCredits() {
    var dialog by remember { mutableStateOf<LicenseDialog?>(null) }

    Text(
        text = stringResource(Res.string.credits_icon_packs_title),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    FluviaIconPacks.filter { it.id != FLUVIA_PACK_ID }.forEach { pack ->
        val packTitle = stringResource(Res.string.credits_icon_pack_entry, pack.label, pack.licence)
        Text(
            text = packTitle,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(role = Role.Button) {
                        dialog =
                            LicenseDialog(
                                title = packTitle,
                                path = "files/licenses/icon-packs/${pack.id}/LICENSE",
                            )
                    }.padding(12.dp),
        )
    }
    val fontsTitle = stringResource(Res.string.credits_fonts_title)
    Text(
        text = fontsTitle,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable(role = Role.Button) { dialog = LicenseDialog(fontsTitle, FONT_LICENSES_PATH) }
                .padding(12.dp),
    )

    dialog?.let { current ->
        val license by produceState<Result<String>?>(null, current.path) {
            value =
                try {
                    Result.success(Res.readBytes(current.path).decodeToString())
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Result.failure(e)
                }
        }
        val failedText = stringResource(Res.string.error_occurred)
        AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(current.title) },
            text = {
                Text(
                    text = license?.let { it.getOrNull() ?: failedText }.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                )
            },
            confirmButton = {
                TextButton(onClick = { dialog = null }) { Text(stringResource(Res.string.close)) }
            },
        )
    }
}
