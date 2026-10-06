package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.ThemePreference

private val AmoledDarkColorScheme = darkColorScheme(
    primary = ChromeDarkBlue,
    onPrimary = Color.Black,
    primaryContainer = AmoledSurfaceVariant,
    onPrimaryContainer = Color(0xFFE8EAED),
    secondary = ChromeDarkTextSecondary,
    onSecondary = Color.Black,
    secondaryContainer = AmoledSurfaceVariant,
    onSecondaryContainer = Color(0xFFE8EAED),
    background = AmoledBg,
    onBackground = Color.White,
    surface = AmoledSurface,
    onSurface = Color.White,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = Color(0xFF9AA0A6),
    outline = AmoledBorder,
    outlineVariant = Color(0xFF1E1E1E)
)

private val PremiumDarkColorScheme = darkColorScheme(
    primary = ChromeDarkBlue,
    onPrimary = ChromeDarkBg,
    primaryContainer = ChromeDarkSurfaceVariant,
    onPrimaryContainer = ChromeDarkTextPrimary,
    secondary = ChromeDarkTextSecondary,
    onSecondary = ChromeDarkBg,
    secondaryContainer = ChromeDarkSurfaceVariant,
    onSecondaryContainer = ChromeDarkTextPrimary,
    tertiary = Color(0xFFCBD5E1),
    onTertiary = ChromeDarkBg,
    background = ChromeDarkBg,
    onBackground = ChromeDarkTextPrimary,
    surface = ChromeDarkSurface,
    onSurface = ChromeDarkTextPrimary,
    surfaceVariant = ChromeDarkSurfaceVariant,
    onSurfaceVariant = ChromeDarkTextSecondary,
    outline = ChromeDarkBorder,
    outlineVariant = Color(0xFF35393E)
)

private val PremiumLightColorScheme = lightColorScheme(
    primary = ChromeBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F0FE),
    onPrimaryContainer = Color(0xFF174EA6),
    secondary = ChromeLightTextSecondary,
    onSecondary = Color.White,
    secondaryContainer = ChromeLightSurfaceVariant,
    onSecondaryContainer = ChromeLightTextPrimary,
    tertiary = Color(0xFF3C4043),
    onTertiary = Color.White,
    background = ChromeLightBg,
    onBackground = ChromeLightTextPrimary,
    surface = ChromeLightSurface,
    onSurface = ChromeLightTextPrimary,
    surfaceVariant = ChromeLightSurfaceVariant,
    onSurfaceVariant = ChromeLightTextSecondary,
    outline = ChromeLightBorder,
    outlineVariant = Color(0xFFE8EAED)
)

@Composable
fun OberonBrowserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isIncognito: Boolean = false,
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themePreference) {
        ThemePreference.ALABASTER_LIGHT -> false
        ThemePreference.TITANIUM_DARK -> true
        ThemePreference.AMOLED_BLACK -> true
        ThemePreference.SYSTEM -> darkTheme
    }

    val colorScheme = when {
        isIncognito -> darkColorScheme(
            primary = IncognitoAccent,
            onPrimary = IncognitoBg,
            primaryContainer = IncognitoSurfaceVariant,
            onPrimaryContainer = IncognitoTextPrimary,
            secondary = IncognitoAccent,
            onSecondary = IncognitoBg,
            background = IncognitoBg,
            onBackground = IncognitoTextPrimary,
            surface = IncognitoSurface,
            onSurface = IncognitoTextPrimary,
            surfaceVariant = IncognitoSurfaceVariant,
            onSurfaceVariant = IncognitoTextSecondary,
            outline = IncognitoBorder,
            outlineVariant = IncognitoBorder
        )
        themePreference == ThemePreference.AMOLED_BLACK -> AmoledDarkColorScheme
        themePreference == ThemePreference.ALABASTER_LIGHT -> PremiumLightColorScheme
        themePreference == ThemePreference.TITANIUM_DARK -> PremiumDarkColorScheme
        isDark -> PremiumDarkColorScheme
        else -> PremiumLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
