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
import com.metrolist.android.ui.fluvia.IconPackData
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.close
import com.metrolist.shared.generated.resources.credits_icon_pack_entry
import com.metrolist.shared.generated.resources.credits_icon_packs_title
import org.jetbrains.compose.resources.stringResource

private const val FLUVIA_PACK_ID = "fluvia"

/** Licence rows for the bundled third-party icon packs; each opens the pack's full licence text. */
@Composable
fun ColumnScope.IconPackCredits() {
    var selected by remember { mutableStateOf<IconPackData?>(null) }

    Text(
        text = stringResource(Res.string.credits_icon_packs_title),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    FluviaIconPacks.filter { it.id != FLUVIA_PACK_ID }.forEach { pack ->
        Text(
            text = stringResource(Res.string.credits_icon_pack_entry, pack.label, pack.licence),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(role = Role.Button) { selected = pack }
                    .padding(12.dp),
        )
    }

    selected?.let { pack ->
        val licenseText by produceState<String?>(null, pack.id) {
            value = runCatching { Res.readBytes("files/licenses/icon-packs/${pack.id}/LICENSE").decodeToString() }.getOrNull()
        }
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(stringResource(Res.string.credits_icon_pack_entry, pack.label, pack.licence)) },
            text = {
                Text(
                    text = licenseText.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                )
            },
            confirmButton = {
                TextButton(onClick = { selected = null }) { Text(stringResource(Res.string.close)) }
            },
        )
    }
}
