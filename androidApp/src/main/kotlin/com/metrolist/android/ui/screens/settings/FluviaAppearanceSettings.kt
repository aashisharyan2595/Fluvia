/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.metrolist.android.ui.fluvia.FluviaAccentPresets
import com.metrolist.android.ui.fluvia.FluviaFont
import com.metrolist.android.ui.fluvia.FluviaFontList
import com.metrolist.android.ui.fluvia.FluviaIconPacks
import com.metrolist.android.ui.fluvia.FluviaLookPreset
import com.metrolist.android.ui.fluvia.FluviaLookPresets
import com.metrolist.android.ui.fluvia.FontGroup
import com.metrolist.android.ui.fluvia.IconPackData
import com.metrolist.android.ui.fluvia.LocalFluviaTokens
import com.metrolist.android.ui.fluvia.applyLook
import com.metrolist.android.ui.fluvia.currentLook
import com.metrolist.android.ui.fluvia.fluviaFont
import com.metrolist.android.ui.fluvia.fluviaIconVector
import com.metrolist.android.ui.fluvia.parseHexColor
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.close
import com.metrolist.shared.generated.resources.fluvia_accent
import com.metrolist.shared.generated.resources.fluvia_accent_amber
import com.metrolist.shared.generated.resources.fluvia_accent_blue
import com.metrolist.shared.generated.resources.fluvia_accent_coral
import com.metrolist.shared.generated.resources.fluvia_accent_custom
import com.metrolist.shared.generated.resources.fluvia_accent_pink
import com.metrolist.shared.generated.resources.fluvia_accent_teal
import com.metrolist.shared.generated.resources.fluvia_accent_violet
import com.metrolist.shared.generated.resources.fluvia_background
import com.metrolist.shared.generated.resources.fluvia_background_album_art
import com.metrolist.shared.generated.resources.fluvia_background_aurora
import com.metrolist.shared.generated.resources.fluvia_background_custom
import com.metrolist.shared.generated.resources.fluvia_background_mesh
import com.metrolist.shared.generated.resources.fluvia_background_solid
import com.metrolist.shared.generated.resources.fluvia_colour_adjusted
import com.metrolist.shared.generated.resources.fluvia_colours_need_manual
import com.metrolist.shared.generated.resources.fluvia_corners
import com.metrolist.shared.generated.resources.fluvia_corners_pill
import com.metrolist.shared.generated.resources.fluvia_corners_sharp
import com.metrolist.shared.generated.resources.fluvia_corners_soft
import com.metrolist.shared.generated.resources.fluvia_display_size
import com.metrolist.shared.generated.resources.fluvia_display_size_large
import com.metrolist.shared.generated.resources.fluvia_display_size_medium
import com.metrolist.shared.generated.resources.fluvia_display_size_note
import com.metrolist.shared.generated.resources.fluvia_display_size_small
import com.metrolist.shared.generated.resources.fluvia_display_size_xs
import com.metrolist.shared.generated.resources.fluvia_font
import com.metrolist.shared.generated.resources.fluvia_font_group_display
import com.metrolist.shared.generated.resources.fluvia_font_group_handwriting
import com.metrolist.shared.generated.resources.fluvia_font_group_mono
import com.metrolist.shared.generated.resources.fluvia_font_group_rounded
import com.metrolist.shared.generated.resources.fluvia_font_group_sans
import com.metrolist.shared.generated.resources.fluvia_font_group_serif
import com.metrolist.shared.generated.resources.fluvia_glass_auto
import com.metrolist.shared.generated.resources.fluvia_glass_blur
import com.metrolist.shared.generated.resources.fluvia_glass_edge
import com.metrolist.shared.generated.resources.fluvia_glass_frosted
import com.metrolist.shared.generated.resources.fluvia_glass_frosted_note
import com.metrolist.shared.generated.resources.fluvia_glass_full
import com.metrolist.shared.generated.resources.fluvia_glass_full_note
import com.metrolist.shared.generated.resources.fluvia_glass_solid
import com.metrolist.shared.generated.resources.fluvia_glass_solid_note
import com.metrolist.shared.generated.resources.fluvia_glass_tier
import com.metrolist.shared.generated.resources.fluvia_glass_tint
import com.metrolist.shared.generated.resources.fluvia_hex_example
import com.metrolist.shared.generated.resources.fluvia_hex_invalid
import com.metrolist.shared.generated.resources.fluvia_icon_pack
import com.metrolist.shared.generated.resources.fluvia_icon_style
import com.metrolist.shared.generated.resources.fluvia_icon_style_duo
import com.metrolist.shared.generated.resources.fluvia_icon_style_line
import com.metrolist.shared.generated.resources.fluvia_icon_style_soft
import com.metrolist.shared.generated.resources.fluvia_icon_style_solid
import com.metrolist.shared.generated.resources.fluvia_look_custom
import com.metrolist.shared.generated.resources.fluvia_look_custom_description
import com.metrolist.shared.generated.resources.fluvia_looks
import com.metrolist.shared.generated.resources.fluvia_looks_description
import com.metrolist.shared.generated.resources.fluvia_now_playing
import com.metrolist.shared.generated.resources.fluvia_now_playing_desc
import com.metrolist.shared.generated.resources.fluvia_spacing
import com.metrolist.shared.generated.resources.fluvia_spacing_comfortable
import com.metrolist.shared.generated.resources.fluvia_spacing_compact
import com.metrolist.shared.generated.resources.fluvia_spacing_spacious
import com.metrolist.shared.generated.resources.fluvia_value_dp
import com.metrolist.shared.generated.resources.fluvia_value_percent
import com.metrolist.shared.generated.resources.percent_symbol
import com.metrolist.shared.generated.resources.song_artwork_colors
import com.metrolist.shared.settings.CornerStyle
import com.metrolist.shared.settings.DisplaySize
import com.metrolist.shared.settings.FluviaBackground
import com.metrolist.shared.settings.FluviaLook
import com.metrolist.shared.settings.GlassTier
import com.metrolist.shared.settings.IconStyle
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.settings.SpacingDensity
import com.metrolist.shared.ui.settings.SettingsChoice
import com.metrolist.shared.ui.settings.SettingsGroupRegistryBuilder
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private const val BLUR_MAX = 60
private const val BLUR_STEP = 2
private const val TINT_MAX = 85
private const val EDGE_MIN = 5
private const val EDGE_MAX = 100
private const val PERCENT = 100f
private const val DISABLED_ALPHA = 0.38f
private val PreviewIconKeys = listOf("home", "search", "heart", "queue")
private const val STYLE_PREVIEW_ICON = "heart"

/**
 * Fluvia rows of the Appearance theme group, in the order the registry sorts them. Theme mode, album-art colours and
 * haptics are the existing Metrolist rows; this adds the rest.
 */
@Composable
fun SettingsGroupRegistryBuilder.fluviaThemeItems() {
    val settings = Settings.shared
    val tokens = LocalFluviaTokens.current
    // A song's artwork overrides the accent and background colours, so those rows only apply with the switch off.
    val manualColours = !settings.dynamicTheme
    val manualHint =
        if (manualColours) null else stringResource(Res.string.fluvia_colours_need_manual, stringResource(Res.string.song_artwork_colors))

    looksRow()

    switch(
        id = "fluviaNowPlaying",
        title = stringResource(Res.string.fluvia_now_playing),
        description = stringResource(Res.string.fluvia_now_playing_desc),
        checked = settings.fluviaNowPlaying,
    ) { settings.fluviaNowPlaying = it }

    val tier = settings.glassTier
    val tierLabel = glassTierLabel(tier)
    choice(
        id = "fluviaGlassTier",
        title = stringResource(Res.string.fluvia_glass_tier),
        description = stringResource(glassTierNote(tier)),
        valueLabel = tierLabel,
        choices = GlassTier.entries.map { SettingsChoice(it.name, glassTierLabel(it)) },
        selectedChoiceId = tier.name,
    ) { settings.glassTier = GlassTier.valueOf(it) }

    if (tier != GlassTier.SOLID) {
        glassSlider(
            id = "fluviaGlassBlur",
            title = stringResource(Res.string.fluvia_glass_blur),
            stored = settings.glassBlur,
            effective = tokens.glass.blur.value,
            min = 0,
            max = BLUR_MAX,
            step = BLUR_STEP,
            format = { stringResource(Res.string.fluvia_value_dp, it) },
        ) { settings.glassBlur = it }
        glassSlider(
            id = "fluviaGlassTint",
            title = stringResource(Res.string.fluvia_glass_tint),
            stored = settings.glassTint,
            effective = tokens.glass.tint * PERCENT,
            min = 0,
            max = TINT_MAX,
            step = 1,
            format = { stringResource(Res.string.fluvia_value_percent, it, stringResource(Res.string.percent_symbol)) },
        ) { settings.glassTint = it }
        glassSlider(
            id = "fluviaGlassEdge",
            title = stringResource(Res.string.fluvia_glass_edge),
            stored = settings.glassEdge,
            effective = tokens.glass.edge * PERCENT,
            min = EDGE_MIN,
            max = EDGE_MAX,
            step = 1,
            format = { stringResource(Res.string.fluvia_value_percent, it, stringResource(Res.string.percent_symbol)) },
        ) { settings.glassEdge = it }
    }

    val adjusted = tokens.colors.adjusted

    val accentTitle = stringResource(Res.string.fluvia_accent)
    val accentNames = accentNames()
    custom(id = "fluviaAccent", title = accentTitle, enabled = manualColours) { onChanged ->
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(accentTitle, style = MaterialTheme.typography.bodyLarge)
            manualHint?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FluviaAccentPresets.forEachIndexed { index, color ->
                    AccentSwatch(
                        color = color,
                        name = accentNames[index],
                        selected = settings.fluviaAccentHex == null && settings.fluviaAccentIndex == index,
                        enabled = manualColours,
                    ) {
                        // A preset replaces any custom colour, which would otherwise keep winning.
                        settings.fluviaAccentIndex = index
                        settings.fluviaAccentHex = null
                        onChanged()
                    }
                }
            }
        }
    }
    hexRow(
        id = "fluviaAccentHex",
        title = stringResource(Res.string.fluvia_accent_custom),
        stored = settings.fluviaAccentHex,
        enabled = manualColours,
        hint = manualHint,
        adjusted = adjusted,
    ) { settings.fluviaAccentHex = it }

    val background = settings.fluviaBackground
    val backgroundLabels = FluviaBackground.entries.associateWith { backgroundLabel(it) }
    choice(
        id = "fluviaBackground",
        title = stringResource(Res.string.fluvia_background),
        valueLabel = backgroundLabels.getValue(background),
        choices = FluviaBackground.entries.map { SettingsChoice(it.name, backgroundLabels.getValue(it)) },
        selectedChoiceId = background.name,
    ) { settings.fluviaBackground = FluviaBackground.valueOf(it) }
    hexRow(
        id = "fluviaBackgroundHex",
        title = stringResource(Res.string.fluvia_background_custom),
        stored = settings.fluviaBackgroundHex,
        enabled = manualColours,
        hint = manualHint,
        adjusted = adjusted,
    ) { settings.fluviaBackgroundHex = it }

    val corners = settings.cornerStyle
    val cornerLabels = CornerStyle.entries.associateWith { cornerLabel(it) }
    choice(
        id = "fluviaCorners",
        title = stringResource(Res.string.fluvia_corners),
        valueLabel = cornerLabels.getValue(corners),
        choices = CornerStyle.entries.map { SettingsChoice(it.name, cornerLabels.getValue(it)) },
        selectedChoiceId = corners.name,
    ) { settings.cornerStyle = CornerStyle.valueOf(it) }

    val spacing = settings.spacingDensity
    val spacingLabels = SpacingDensity.entries.associateWith { spacingLabel(it) }
    choice(
        id = "fluviaSpacing",
        title = stringResource(Res.string.fluvia_spacing),
        valueLabel = spacingLabels.getValue(spacing),
        choices = SpacingDensity.entries.map { SettingsChoice(it.name, spacingLabels.getValue(it)) },
        selectedChoiceId = spacing.name,
    ) { settings.spacingDensity = SpacingDensity.valueOf(it) }

    val size = settings.displaySize
    val sizeLabels = DisplaySize.entries.associateWith { displaySizeLabel(it) }
    choice(
        id = "fluviaDisplaySize",
        title = stringResource(Res.string.fluvia_display_size),
        description = stringResource(Res.string.fluvia_display_size_note),
        valueLabel = sizeLabels.getValue(size),
        choices = DisplaySize.entries.map { SettingsChoice(it.name, sizeLabels.getValue(it)) },
        selectedChoiceId = size.name,
    ) { settings.displaySize = DisplaySize.valueOf(it) }

    val fontTitle = stringResource(Res.string.fluvia_font)
    val selectedFont = fluviaFont(settings.fluviaFont)
    custom(id = "fluviaFont", title = fontTitle) { onChanged ->
        var open by remember { mutableStateOf(false) }
        PickerRow(title = fontTitle, onClick = { open = true }) {
            Text(
                text = selectedFont.label,
                style = MaterialTheme.typography.labelLarge,
                fontFamily = selectedFont.family,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (open) {
            FontPickerDialog(
                title = fontTitle,
                selectedId = selectedFont.id,
                onDismiss = { open = false },
            ) {
                settings.fluviaFont = it
                onChanged()
                open = false
            }
        }
    }

    val packTitle = stringResource(Res.string.fluvia_icon_pack)
    val selectedPack = FluviaIconPacks.firstOrNull { it.id == settings.iconPack } ?: FluviaIconPacks.first()
    custom(id = "fluviaIconPack", title = packTitle) { onChanged ->
        var open by remember { mutableStateOf(false) }
        PickerRow(title = packTitle, onClick = { open = true }) {
            Text(
                text = selectedPack.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (open) {
            IconPackPickerDialog(
                title = packTitle,
                selectedId = selectedPack.id,
                onDismiss = { open = false },
            ) {
                settings.iconPack = it
                onChanged()
                open = false
            }
        }
    }

    val style = settings.iconStyle
    val styleLabels = IconStyle.entries.associateWith { iconStyleLabel(it) }
    choice(
        id = "fluviaIconStyle",
        title = stringResource(Res.string.fluvia_icon_style),
        valueLabel = styleLabels.getValue(style),
        choices =
            IconStyle.entries.map { entry ->
                SettingsChoice(
                    id = entry.name,
                    label = styleLabels.getValue(entry),
                    preview = {
                        Icon(
                            imageVector = fluviaIconVector(tokens.iconPackId, STYLE_PREVIEW_ICON, false, entry),
                            contentDescription = null,
                        )
                    },
                )
            },
        selectedChoiceId = style.name,
    ) { settings.iconStyle = IconStyle.valueOf(it) }
}

/**
 * Tiles for the six Looks and a Custom chip. The selection comes from [currentLook], which reads the settings, so it
 * moves to Custom as soon as any single option stops matching the Look.
 */
@Composable
private fun SettingsGroupRegistryBuilder.looksRow() {
    val title = stringResource(Res.string.fluvia_looks)
    val summary = stringResource(Res.string.fluvia_looks_description)
    custom(id = "fluviaLook", title = title, description = summary) { onChanged ->
        val selected = currentLook()
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 16.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .selectableGroup()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FluviaLookPresets.forEach { preset ->
                    val name = stringResource(preset.name)
                    LookTile(
                        name = name,
                        selected = selected == preset.look,
                        onClick = {
                            applyLook(preset.look)
                            onChanged()
                        },
                    ) { LookPreview(preset) }
                }
                // Custom is a state, not a preset: choosing it keeps the settings as they are.
                LookTile(
                    name = stringResource(Res.string.fluvia_look_custom),
                    selected = selected == FluviaLook.CUSTOM,
                    enabled = false,
                    onClick = {},
                ) { CustomPreview() }
            }
            val shown = FluviaLookPresets.firstOrNull { it.look == selected }
            Text(
                text = stringResource(shown?.description ?: Res.string.fluvia_look_custom_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun LookTile(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    preview: @Composable () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .width(112.dp)
                .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(width = 112.dp, height = 80.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    BorderStroke(
                        width = if (selected) 3.dp else 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    ),
                    RoundedCornerShape(20.dp),
                ),
        ) { preview() }
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/** A miniature of the Look: its backdrop, font sample, glass strip and accent dot. */
@Composable
private fun LookPreview(preset: FluviaLookPreset) {
    val ink = if (preset.isDark) Color.White else Color(0xFF1A0F2E)
    val accent = FluviaAccentPresets[preset.accentIndex.coerceIn(FluviaAccentPresets.indices)]
    Box(Modifier.fillMaxWidth().height(80.dp).background(preset.previewBackground)) {
        Text(
            text = "Aa",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = fluviaFont(preset.fontId).family,
            color = ink,
            modifier = Modifier.padding(start = 12.dp, top = 8.dp).clearAndSetSemantics {},
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(10.dp)
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ink.copy(alpha = 0.14f)),
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
                .size(12.dp)
                .clip(CircleShape)
                .background(accent),
        )
    }
}

@Composable
private fun CustomPreview() {
    Box(
        Modifier.fillMaxWidth().height(80.dp).background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Aa",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

@Composable
private fun glassTierLabel(tier: GlassTier): String =
    stringResource(
        when (tier) {
            GlassTier.FULL -> Res.string.fluvia_glass_full
            GlassTier.FROSTED -> Res.string.fluvia_glass_frosted
            GlassTier.SOLID -> Res.string.fluvia_glass_solid
        },
    )

private fun glassTierNote(tier: GlassTier) =
    when (tier) {
        GlassTier.FULL -> Res.string.fluvia_glass_full_note
        GlassTier.FROSTED -> Res.string.fluvia_glass_frosted_note
        GlassTier.SOLID -> Res.string.fluvia_glass_solid_note
    }

@Composable
private fun backgroundLabel(background: FluviaBackground): String =
    stringResource(
        when (background) {
            FluviaBackground.AURORA -> Res.string.fluvia_background_aurora
            FluviaBackground.MESH -> Res.string.fluvia_background_mesh
            FluviaBackground.SOLID -> Res.string.fluvia_background_solid
            FluviaBackground.ALBUM_ART -> Res.string.fluvia_background_album_art
        },
    )

@Composable
private fun cornerLabel(style: CornerStyle): String =
    stringResource(
        when (style) {
            CornerStyle.SHARP -> Res.string.fluvia_corners_sharp
            CornerStyle.SOFT -> Res.string.fluvia_corners_soft
            CornerStyle.PILL -> Res.string.fluvia_corners_pill
        },
    )

@Composable
private fun spacingLabel(density: SpacingDensity): String =
    stringResource(
        when (density) {
            SpacingDensity.COMPACT -> Res.string.fluvia_spacing_compact
            SpacingDensity.COMFORTABLE -> Res.string.fluvia_spacing_comfortable
            SpacingDensity.SPACIOUS -> Res.string.fluvia_spacing_spacious
        },
    )

@Composable
private fun displaySizeLabel(size: DisplaySize): String =
    stringResource(
        when (size) {
            DisplaySize.XS -> Res.string.fluvia_display_size_xs
            DisplaySize.SMALL -> Res.string.fluvia_display_size_small
            DisplaySize.MEDIUM -> Res.string.fluvia_display_size_medium
            DisplaySize.LARGE -> Res.string.fluvia_display_size_large
        },
    )

@Composable
private fun iconStyleLabel(style: IconStyle): String =
    stringResource(
        when (style) {
            IconStyle.LINE -> Res.string.fluvia_icon_style_line
            IconStyle.SOLID -> Res.string.fluvia_icon_style_solid
            IconStyle.SOFT -> Res.string.fluvia_icon_style_soft
            IconStyle.DUO -> Res.string.fluvia_icon_style_duo
        },
    )

@Composable
private fun fontGroupLabel(group: FontGroup): String =
    stringResource(
        when (group) {
            FontGroup.Sans -> Res.string.fluvia_font_group_sans
            FontGroup.Rounded -> Res.string.fluvia_font_group_rounded
            FontGroup.Serif -> Res.string.fluvia_font_group_serif
            FontGroup.Display -> Res.string.fluvia_font_group_display
            FontGroup.Mono -> Res.string.fluvia_font_group_mono
            FontGroup.Handwriting -> Res.string.fluvia_font_group_handwriting
        },
    )

/** One name per entry of [FluviaAccentPresets], in the same order. */
@Composable
private fun accentNames(): List<String> =
    listOf(
        stringResource(Res.string.fluvia_accent_coral),
        stringResource(Res.string.fluvia_accent_violet),
        stringResource(Res.string.fluvia_accent_teal),
        stringResource(Res.string.fluvia_accent_amber),
        stringResource(Res.string.fluvia_accent_pink),
        stringResource(Res.string.fluvia_accent_blue),
    )

/**
 * A slider over an integer setting where null means automatic. While automatic the thumb sits on the value the glass
 * level currently resolves to; the Auto button returns the setting to null.
 */
@Composable
private fun SettingsGroupRegistryBuilder.glassSlider(
    id: String,
    title: String,
    stored: Int?,
    effective: Float,
    min: Int,
    max: Int,
    step: Int,
    format: @Composable (Int) -> String,
    onChange: (Int?) -> Unit,
) {
    val autoLabel = stringResource(Res.string.fluvia_glass_auto)
    custom(id = id, title = title) { onChanged ->
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = if (stored == null) autoLabel else format(stored),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                TextButton(
                    onClick = {
                        onChange(null)
                        onChanged()
                    },
                    enabled = stored != null,
                ) { Text(autoLabel) }
            }
            Slider(
                value = (stored?.toFloat() ?: effective).coerceIn(min.toFloat(), max.toFloat()),
                onValueChange = {
                    onChange(it.roundToInt())
                    onChanged()
                },
                valueRange = min.toFloat()..max.toFloat(),
                steps = (max - min) / step - 1,
                // Dense steps draw a row of touching dots; keep the snapping but hide the ticks.
                colors =
                    SliderDefaults.colors(
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp)
                        .semantics { contentDescription = title },
            )
        }
    }
}

@Composable
private fun AccentSwatch(
    color: Color,
    name: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(48.dp)
                .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
                .semantics { contentDescription = name },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (selected) {
                        Modifier.border(BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface), CircleShape)
                    } else {
                        Modifier
                    },
                ),
        )
    }
}

/**
 * A hex colour field. Only a valid 6-digit code (with or without `#`) is written, as `#RRGGBB`; an empty field clears
 * the custom colour. Invalid text stays in the field with an error and leaves the stored value alone.
 */
@Composable
private fun SettingsGroupRegistryBuilder.hexRow(
    id: String,
    title: String,
    stored: String?,
    enabled: Boolean,
    hint: String?,
    adjusted: Boolean,
    onCommit: (String?) -> Unit,
) {
    val example = stringResource(Res.string.fluvia_hex_example)
    val invalidMessage = stringResource(Res.string.fluvia_hex_invalid, example)
    val adjustedMessage = stringResource(Res.string.fluvia_colour_adjusted)
    custom(id = id, title = title, enabled = enabled) { onChanged ->
        var text by remember(stored) { mutableStateOf(stored.orEmpty()) }
        val invalid = text.isNotBlank() && parseHexColor(text.trim()) == null
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            hint?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            OutlinedTextField(
                value = text,
                onValueChange = { input ->
                    text = input
                    val trimmed = input.trim()
                    val cleared = trimmed.isEmpty()
                    val valid = parseHexColor(trimmed) != null
                    if (cleared) {
                        onCommit(null)
                    } else if (valid) {
                        onCommit(normalizeHex(trimmed))
                    }
                    if (cleared || valid) onChanged()
                },
                label = { Text(title) },
                placeholder = { Text(example) },
                singleLine = true,
                enabled = enabled,
                isError = invalid,
                supportingText =
                    when {
                        invalid -> {
                            { Text(invalidMessage) }
                        }

                        adjusted && stored != null -> {
                            { Text(adjustedMessage) }
                        }

                        else -> {
                            null
                        }
                    },
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Ascii,
                    ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun normalizeHex(input: String): String = "#" + input.trim().removePrefix("#").uppercase()

@Composable
private fun PickerRow(
    title: String,
    onClick: () -> Unit,
    value: @Composable () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        value()
    }
}

@Composable
private fun FontPickerDialog(
    title: String,
    selectedId: String,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
) {
    val groups = remember { FluviaFontList.groupBy(FluviaFont::group) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                FontGroup.entries.forEach { group ->
                    val fonts = groups[group].orEmpty()
                    if (fonts.isEmpty()) return@forEach
                    item(key = "group_${group.name}") {
                        Text(
                            text = fontGroupLabel(group),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        )
                    }
                    items(fonts.size, key = { fonts[it].id }) { index ->
                        val font = fonts[index]
                        PickerOption(selected = font.id == selectedId, onClick = { onSelected(font.id) }) {
                            Text(
                                text = font.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = font.family,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.close)) }
        },
    )
}

@Composable
private fun IconPackPickerDialog(
    title: String,
    selectedId: String,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
) {
    val tokens = LocalFluviaTokens.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                items(FluviaIconPacks.size, key = { FluviaIconPacks[it].id }) { index ->
                    val pack = FluviaIconPacks[index]
                    PickerOption(selected = pack.id == selectedId, onClick = { onSelected(pack.id) }) {
                        Column(Modifier.weight(1f)) {
                            Text(pack.label, style = MaterialTheme.typography.titleMedium)
                            IconPackPreview(pack, tokens.iconStyle)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.close)) }
        },
    )
}

@Composable
private fun IconPackPreview(
    pack: IconPackData,
    style: IconStyle,
) {
    Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PreviewIconKeys.forEach { key ->
            Icon(
                imageVector = fluviaIconVector(pack.id, key, false, style),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun PickerOption(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(12.dp))
        content()
    }
}
