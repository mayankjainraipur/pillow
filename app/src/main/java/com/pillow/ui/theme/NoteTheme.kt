package com.pillow.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * A per-note pastel "wallpaper" theme. The [backgroundHex] is what gets persisted
 * in [com.pillow.domain.model.Note.color], so themes need no database column of
 * their own — a note's stored color simply maps back to one of these.
 */
data class NoteTheme(
    val key: String,
    val label: String,
    val backgroundHex: String,
    /** True for dark backgrounds, so text/icons on the note should be light. */
    val isDark: Boolean = false
) {
    val background: Color get() = Color(android.graphics.Color.parseColor(backgroundHex))
    val onBackground: Color get() = if (isDark) Color(0xFFECECEC) else Color(0xFF2B2B2B)
}

object NoteThemes {
    val WarmCream = NoteTheme("warm_cream", "Warm Cream", "#FFF8E7")
    val CozyPeach = NoteTheme("cozy_peach", "Cozy Peach", "#FFE0CC")
    val FreshMint = NoteTheme("fresh_mint", "Fresh Mint", "#D6F5E3")
    val DreamyLavender = NoteTheme("dreamy_lavender", "Dreamy Lavender", "#E8DFF5")
    val BreezySky = NoteTheme("breezy_sky", "Breezy Sky", "#D8ECFB")
    val DustyRose = NoteTheme("dusty_rose", "Dusty Rose", "#F5DDE2")
    val NightSleep = NoteTheme("night_sleep", "Night Sleep", "#2E2E38", isDark = true)

    val Default = WarmCream

    val all = listOf(
        WarmCream, CozyPeach, FreshMint, DreamyLavender, BreezySky, DustyRose, NightSleep
    )

    /** Resolve a stored color hex back to a known theme, falling back to [Default]. */
    fun fromHex(hex: String?): NoteTheme =
        all.firstOrNull { it.backgroundHex.equals(hex, ignoreCase = true) } ?: Default
}
