package com.pillow.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * A user-selectable accent. [Dynamic] keeps the Material You wallpaper colors on
 * Android 12+; the others override the primary color with a fixed seed so the
 * choice is honored on every device.
 */
data class AccentPalette(
    val key: String,
    val label: String,
    val lightPrimary: Color,
    val darkPrimary: Color
) {
    val swatch: Color get() = lightPrimary
}

object AccentPalettes {
    val Dynamic = AccentPalette("dynamic", "System", PrimaryLight, PrimaryDark)
    val Purple = AccentPalette("purple", "Purple", Color(0xFF6B6B9D), Color(0xFFD0BCFF))
    val Blue = AccentPalette("blue", "Blue", Color(0xFF3F6CCB), Color(0xFFADC6FF))
    val Teal = AccentPalette("teal", "Teal", Color(0xFF00696E), Color(0xFF4DD8E0))
    val Rose = AccentPalette("rose", "Rose", Color(0xFFB03A6E), Color(0xFFFFB0CC))
    val Amber = AccentPalette("amber", "Amber", Color(0xFF8B5000), Color(0xFFFFB868))

    val all = listOf(Dynamic, Purple, Blue, Teal, Rose, Amber)

    fun fromKey(key: String?): AccentPalette =
        all.firstOrNull { it.key == key } ?: Dynamic
}

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

@Composable
fun PillowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentKey: String = AccentPalettes.Dynamic.key,
    content: @Composable () -> Unit
) {
    // Only use Material You wallpaper colors when the user keeps the "System" accent.
    val useDynamic = accentKey == AccentPalettes.Dynamic.key &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        useDynamic -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            val accent = AccentPalettes.fromKey(accentKey)
            DarkColorScheme.copy(
                primary = accent.darkPrimary,
                primaryContainer = accent.darkPrimary
            )
        }
        else -> {
            val accent = AccentPalettes.fromKey(accentKey)
            LightColorScheme.copy(
                primary = accent.lightPrimary,
                primaryContainer = accent.lightPrimary.copy(alpha = 0.18f)
            )
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)?.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
