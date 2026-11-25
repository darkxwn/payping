package com.darkxwn.payping.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.darkxwn.payping.domain.model.AppThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF0E7C66),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFF6EA),
    onPrimaryContainer = Color(0xFF002119),
    secondary = Color(0xFF3E6359),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC1E8DC),
    onSecondaryContainer = Color(0xFF09201A),
    tertiary = Color(0xFF2B638B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCBE6FF),
    onTertiaryContainer = Color(0xFF001E31),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF6F9F7),
    onBackground = Color(0xFF171D1B),
    surface = Color(0xFFF6F9F7),
    onSurface = Color(0xFF171D1B),
    surfaceVariant = Color(0xFFDAE5E0),
    onSurfaceVariant = Color(0xFF3F4945),
    surfaceContainerLow = Color(0xFFF0F5F2),
    surfaceContainer = Color(0xFFEAF0ED),
    surfaceContainerHigh = Color(0xFFE4EAE7),
    outline = Color(0xFF6F7975),
    outlineVariant = Color(0xFFBFC9C4)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6AD9BC),
    onPrimary = Color(0xFF00382C),
    primaryContainer = Color(0xFF005141),
    onPrimaryContainer = Color(0xFFCFF6EA),
    secondary = Color(0xFFA5CCC0),
    onSecondary = Color(0xFF12372D),
    secondaryContainer = Color(0xFF274D43),
    onSecondaryContainer = Color(0xFFC1E8DC),
    tertiary = Color(0xFF99CCFA),
    onTertiary = Color(0xFF003352),
    tertiaryContainer = Color(0xFF0A4A72),
    onTertiaryContainer = Color(0xFFCBE6FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0F1513),
    onBackground = Color(0xFFDFE4E1),
    surface = Color(0xFF0F1513),
    onSurface = Color(0xFFDFE4E1),
    surfaceVariant = Color(0xFF3F4945),
    onSurfaceVariant = Color(0xFFBFC9C4),
    surfaceContainerLow = Color(0xFF171D1B),
    surfaceContainer = Color(0xFF1B211F),
    surfaceContainerHigh = Color(0xFF252B29),
    outline = Color(0xFF89938E),
    outlineVariant = Color(0xFF3F4945)
)

@Composable
fun PayPingTheme(
    themeMode: AppThemeMode,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (useDark) DarkColors else LightColors,
        content = content
    )
}
