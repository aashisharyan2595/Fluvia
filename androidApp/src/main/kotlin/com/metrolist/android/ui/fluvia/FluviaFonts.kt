/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.fluvia

import androidx.annotation.FontRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.metrolist.android.R

enum class FontGroup { Sans, Rounded, Serif, Display, Mono, Handwriting }

/** A bundled font. [label] is the family name, a proper noun that is not translated. */
@Immutable
data class FluviaFont(
    val id: String,
    val label: String,
    val group: FontGroup,
    val family: FontFamily,
)

// Fonts with one published weight pass the same file for both, so Compose does not synthesise a bold.
private fun family(
    @FontRes regular: Int,
    @FontRes bold: Int,
): FontFamily = FontFamily(Font(regular, FontWeight.Normal), Font(bold, FontWeight.Bold))

val FluviaFontList: List<FluviaFont> =
    listOf(
        FluviaFont(
            id = "outfit",
            label = "Outfit",
            group = FontGroup.Sans,
            family = family(R.font.outfit_regular, R.font.outfit_bold),
        ),
        FluviaFont(
            id = "inter",
            label = "Inter",
            group = FontGroup.Sans,
            family = family(R.font.inter_regular, R.font.inter_bold),
        ),
        FluviaFont(
            id = "dm_sans",
            label = "DM Sans",
            group = FontGroup.Sans,
            family = family(R.font.dm_sans_regular, R.font.dm_sans_bold),
        ),
        FluviaFont(
            id = "space_grotesk",
            label = "Space Grotesk",
            group = FontGroup.Sans,
            family = family(R.font.space_grotesk_regular, R.font.space_grotesk_bold),
        ),
        FluviaFont(
            id = "sora",
            label = "Sora",
            group = FontGroup.Sans,
            family = family(R.font.sora_regular, R.font.sora_bold),
        ),
        FluviaFont(
            id = "fraunces",
            label = "Fraunces",
            group = FontGroup.Serif,
            family = family(R.font.fraunces_regular, R.font.fraunces_bold),
        ),
        FluviaFont(
            id = "plus_jakarta_sans",
            label = "Plus Jakarta Sans",
            group = FontGroup.Sans,
            family = family(R.font.plus_jakarta_sans_regular, R.font.plus_jakarta_sans_bold),
        ),
        FluviaFont(
            id = "manrope",
            label = "Manrope",
            group = FontGroup.Sans,
            family = family(R.font.manrope_regular, R.font.manrope_bold),
        ),
        FluviaFont(
            id = "poppins",
            label = "Poppins",
            group = FontGroup.Sans,
            family = family(R.font.poppins_regular, R.font.poppins_bold),
        ),
        FluviaFont(
            id = "montserrat",
            label = "Montserrat",
            group = FontGroup.Sans,
            family = family(R.font.montserrat_regular, R.font.montserrat_bold),
        ),
        FluviaFont(
            id = "urbanist",
            label = "Urbanist",
            group = FontGroup.Sans,
            family = family(R.font.urbanist_regular, R.font.urbanist_bold),
        ),
        FluviaFont(
            id = "lexend",
            label = "Lexend",
            group = FontGroup.Sans,
            family = family(R.font.lexend_regular, R.font.lexend_bold),
        ),
        FluviaFont(
            id = "rubik",
            label = "Rubik",
            group = FontGroup.Sans,
            family = family(R.font.rubik_regular, R.font.rubik_bold),
        ),
        FluviaFont(
            id = "work_sans",
            label = "Work Sans",
            group = FontGroup.Sans,
            family = family(R.font.work_sans_regular, R.font.work_sans_bold),
        ),
        FluviaFont(
            id = "figtree",
            label = "Figtree",
            group = FontGroup.Sans,
            family = family(R.font.figtree_regular, R.font.figtree_bold),
        ),
        FluviaFont(
            id = "nunito",
            label = "Nunito",
            group = FontGroup.Rounded,
            family = family(R.font.nunito_regular, R.font.nunito_bold),
        ),
        FluviaFont(
            id = "quicksand",
            label = "Quicksand",
            group = FontGroup.Rounded,
            family = family(R.font.quicksand_regular, R.font.quicksand_bold),
        ),
        FluviaFont(
            id = "comfortaa",
            label = "Comfortaa",
            group = FontGroup.Rounded,
            family = family(R.font.comfortaa_regular, R.font.comfortaa_bold),
        ),
        FluviaFont(
            id = "fredoka",
            label = "Fredoka",
            group = FontGroup.Rounded,
            family = family(R.font.fredoka_regular, R.font.fredoka_bold),
        ),
        FluviaFont(
            id = "playfair_display",
            label = "Playfair Display",
            group = FontGroup.Serif,
            family = family(R.font.playfair_display_regular, R.font.playfair_display_bold),
        ),
        FluviaFont(
            id = "lora",
            label = "Lora",
            group = FontGroup.Serif,
            family = family(R.font.lora_regular, R.font.lora_bold),
        ),
        FluviaFont(
            id = "merriweather",
            label = "Merriweather",
            group = FontGroup.Serif,
            family = family(R.font.merriweather_regular, R.font.merriweather_bold),
        ),
        FluviaFont(
            id = "dm_serif_display",
            label = "DM Serif Display",
            group = FontGroup.Serif,
            family = family(R.font.dm_serif_display_regular, R.font.dm_serif_display_regular),
        ),
        FluviaFont(
            id = "cormorant_garamond",
            label = "Cormorant Garamond",
            group = FontGroup.Serif,
            family = family(R.font.cormorant_garamond_regular, R.font.cormorant_garamond_bold),
        ),
        FluviaFont(
            id = "syne",
            label = "Syne",
            group = FontGroup.Display,
            family = family(R.font.syne_regular, R.font.syne_bold),
        ),
        FluviaFont(
            id = "unbounded",
            label = "Unbounded",
            group = FontGroup.Display,
            family = family(R.font.unbounded_regular, R.font.unbounded_bold),
        ),
        FluviaFont(
            id = "righteous",
            label = "Righteous",
            group = FontGroup.Display,
            family = family(R.font.righteous_regular, R.font.righteous_regular),
        ),
        FluviaFont(
            id = "bricolage_grotesque",
            label = "Bricolage Grotesque",
            group = FontGroup.Display,
            family = family(R.font.bricolage_grotesque_regular, R.font.bricolage_grotesque_bold),
        ),
        FluviaFont(
            id = "bebas_neue",
            label = "Bebas Neue",
            group = FontGroup.Display,
            family = family(R.font.bebas_neue_regular, R.font.bebas_neue_regular),
        ),
        FluviaFont(
            id = "archivo_black",
            label = "Archivo Black",
            group = FontGroup.Display,
            family = family(R.font.archivo_black_regular, R.font.archivo_black_regular),
        ),
        FluviaFont(
            id = "jetbrains_mono",
            label = "JetBrains Mono",
            group = FontGroup.Mono,
            family = family(R.font.jetbrains_mono_regular, R.font.jetbrains_mono_bold),
        ),
        FluviaFont(
            id = "space_mono",
            label = "Space Mono",
            group = FontGroup.Mono,
            family = family(R.font.space_mono_regular, R.font.space_mono_bold),
        ),
        FluviaFont(
            id = "ibm_plex_mono",
            label = "IBM Plex Mono",
            group = FontGroup.Mono,
            family = family(R.font.ibm_plex_mono_regular, R.font.ibm_plex_mono_bold),
        ),
        FluviaFont(
            id = "caveat",
            label = "Caveat",
            group = FontGroup.Handwriting,
            family = family(R.font.caveat_regular, R.font.caveat_bold),
        ),
        FluviaFont(
            id = "pacifico",
            label = "Pacifico",
            group = FontGroup.Handwriting,
            family = family(R.font.pacifico_regular, R.font.pacifico_regular),
        ),
        FluviaFont(
            id = "shantell_sans",
            label = "Shantell Sans",
            group = FontGroup.Handwriting,
            family = family(R.font.shantell_sans_regular, R.font.shantell_sans_bold),
        ),
    )

/** Returns the font with [id], or the first entry when the id is unknown. */
fun fluviaFont(id: String): FluviaFont = FluviaFontList.firstOrNull { it.id == id } ?: FluviaFontList.first()
