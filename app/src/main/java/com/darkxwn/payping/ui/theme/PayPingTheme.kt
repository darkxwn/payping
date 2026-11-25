package com.darkxwn.payping.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.darkxwn.payping.R
import com.darkxwn.payping.domain.model.AppFont
import com.darkxwn.payping.domain.model.AppThemeMode
import com.darkxwn.payping.domain.model.BaseAccentColor

private val PayPingShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@OptIn(ExperimentalTextApi::class)
private fun variableFontFamily(resId: Int): FontFamily = FontFamily(
    Font(
        resId = resId,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resId = resId,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = resId,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        resId = resId,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    )
)

private val RobotoFontFamily = variableFontFamily(R.font.roboto_variable)
private val InterFontFamily = variableFontFamily(R.font.inter_variable)
private val ConsolasFontFamily = variableFontFamily(R.font.inconsolata_lgc_variable)

fun fontFamilyFor(appFont: AppFont): FontFamily = when (appFont) {
    AppFont.SYSTEM -> FontFamily.Default
    AppFont.ROBOTO -> RobotoFontFamily
    AppFont.INTER -> InterFontFamily
    AppFont.CONSOLAS -> ConsolasFontFamily
}

private fun buildTypography(fontFamily: FontFamily): Typography {
    if (fontFamily == FontFamily.Default) return Typography()
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = base.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = base.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = base.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = base.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = base.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = base.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = base.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = base.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = base.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = base.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = base.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = base.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = base.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = base.labelSmall.copy(fontFamily = fontFamily)
    )
}

private val DefaultLightColors = lightColorScheme(
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

private val DefaultDarkColors = darkColorScheme(
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
fun isPayPingDarkTheme(themeMode: AppThemeMode): Boolean = when (themeMode) {
    AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    AppThemeMode.LIGHT -> false
    AppThemeMode.DARK -> true
}

@Composable
fun PayPingTheme(
    themeMode: AppThemeMode,
    pureBlack: Boolean = false,
    useDynamicColor: Boolean = false,
    accentColorArgb: Long = BaseAccentColor.DEFAULT_ARGB,
    appFont: AppFont = AppFont.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDark = isPayPingDarkTheme(themeMode)
    val context = LocalContext.current

    val baseScheme = if (useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        remember(accentColorArgb, useDark) {
            buildAccentColorScheme(accentColorArgb = accentColorArgb, useDark = useDark)
        }
    }

    val colorScheme = if (useDark && pureBlack) {
        baseScheme.withPureBlackSurfaces()
    } else {
        baseScheme
    }

    val typography = remember(appFont) {
        buildTypography(fontFamilyFor(appFont))
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = PayPingShapes
    ) {
        CompositionLocalProvider(LocalContentColor provides colorScheme.onBackground) {
            content()
        }
    }
}

private fun ColorScheme.withPureBlackSurfaces(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceBright = Color(0xFF141414),
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0A0A0A),
    surfaceContainer = Color(0xFF121212),
    surfaceContainerHigh = Color(0xFF1A1A1A),
    surfaceContainerHighest = Color(0xFF222222),
    surfaceVariant = Color(0xFF1A1A1A),
    outlineVariant = Color(0xFF2A2A2A)
)

private fun buildAccentColorScheme(accentColorArgb: Long, useDark: Boolean): ColorScheme {
    val normalized = BaseAccentColor.normalizeOpaqueArgb(accentColorArgb)
    if (normalized == BaseAccentColor.DEFAULT_ARGB) {
        return if (useDark) DefaultDarkColors else DefaultLightColors
    }

    val hsl = FloatArray(3)
    ColorUtils.colorToHSL((normalized and 0xFFFFFFFFL).toInt(), hsl)
    val hue = hsl[0]
    val sat = hsl[1]
    val lit = hsl[2]
    val neutralSat = (sat * 0.11f).coerceIn(0.01f, 0.08f)
    val tertiaryHue = (hue + 38f) % 360f

    return if (useDark) {
        val primary = hslColor(
            hue = hue,
            sat = (if (sat < 0.08f) sat else sat.coerceAtLeast(0.42f)).coerceAtMost(0.85f),
            lit = 0.68f
        )
        val primaryContainer = hslColor(hue, (sat * 0.72f).coerceIn(0f, 0.80f), 0.22f)
        val secondary = hslColor(hue, (sat * 0.36f).coerceIn(0f, 0.48f), 0.72f)
        val secondaryContainer = hslColor(hue, (sat * 0.38f).coerceIn(0f, 0.50f), 0.24f)
        val tertiary = hslColor(tertiaryHue, (sat * 0.55f).coerceIn(0f, 0.70f), 0.74f)
        val tertiaryContainer = hslColor(tertiaryHue, (sat * 0.55f).coerceIn(0f, 0.65f), 0.24f)
        val surface = hslColor(hue, neutralSat, 0.075f)
        val onSurface = hslColor(hue, neutralSat * 0.6f, 0.89f)

        darkColorScheme(
            primary = primary,
            onPrimary = contrastingContentColor(primary, hslColor(hue, sat * 0.7f, 0.14f), Color.White),
            primaryContainer = primaryContainer,
            onPrimaryContainer = hslColor(hue, (sat * 0.85f).coerceIn(0f, 0.92f), 0.90f),
            secondary = secondary,
            onSecondary = contrastingContentColor(secondary, hslColor(hue, sat * 0.45f, 0.16f), Color.White),
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = hslColor(hue, (sat * 0.42f).coerceIn(0f, 0.52f), 0.88f),
            tertiary = tertiary,
            onTertiary = contrastingContentColor(tertiary, hslColor(tertiaryHue, sat * 0.55f, 0.16f), Color.White),
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = hslColor(tertiaryHue, (sat * 0.60f).coerceIn(0f, 0.75f), 0.90f),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),
            background = surface,
            onBackground = onSurface,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = hslColor(hue, neutralSat * 1.2f, 0.26f),
            onSurfaceVariant = hslColor(hue, neutralSat * 0.8f, 0.76f),
            surfaceContainerLow = hslColor(hue, neutralSat * 1.1f, 0.105f),
            surfaceContainer = hslColor(hue, neutralSat * 1.15f, 0.125f),
            surfaceContainerHigh = hslColor(hue, neutralSat * 1.2f, 0.16f),
            outline = hslColor(hue, neutralSat * 0.9f, 0.56f),
            outlineVariant = hslColor(hue, neutralSat * 1.1f, 0.26f)
        )
    } else {
        val primary = hslColor(
            hue = hue,
            sat = if (sat < 0.08f) sat else sat.coerceAtLeast(0.35f),
            lit = lit.coerceIn(0.28f, 0.43f)
        )
        val primaryContainer = hslColor(hue, (sat * 0.85f).coerceIn(0f, 0.90f), 0.90f)
        val secondary = hslColor(hue, (sat * 0.36f).coerceIn(0f, 0.42f), 0.36f)
        val secondaryContainer = hslColor(hue, (sat * 0.35f).coerceIn(0f, 0.45f), 0.88f)
        val tertiary = hslColor(tertiaryHue, (sat * 0.55f).coerceIn(0f, 0.65f), 0.38f)
        val tertiaryContainer = hslColor(tertiaryHue, (sat * 0.55f).coerceIn(0f, 0.70f), 0.89f)
        val surface = hslColor(hue, neutralSat, 0.975f)
        val onSurface = hslColor(hue, neutralSat * 0.8f, 0.11f)

        lightColorScheme(
            primary = primary,
            onPrimary = contrastingContentColor(primary, Color(0xFF111413), Color.White),
            primaryContainer = primaryContainer,
            onPrimaryContainer = hslColor(hue, (sat * 0.90f).coerceIn(0f, 0.90f), 0.14f),
            secondary = secondary,
            onSecondary = contrastingContentColor(secondary, Color(0xFF111413), Color.White),
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = hslColor(hue, (sat * 0.45f).coerceIn(0f, 0.55f), 0.15f),
            tertiary = tertiary,
            onTertiary = contrastingContentColor(tertiary, Color(0xFF111413), Color.White),
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = hslColor(tertiaryHue, (sat * 0.60f).coerceIn(0f, 0.75f), 0.15f),
            error = Color(0xFFBA1A1A),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF410002),
            background = surface,
            onBackground = onSurface,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = hslColor(hue, neutralSat * 1.4f, 0.89f),
            onSurfaceVariant = hslColor(hue, neutralSat, 0.28f),
            surfaceContainerLow = hslColor(hue, neutralSat * 1.1f, 0.955f),
            surfaceContainer = hslColor(hue, neutralSat * 1.2f, 0.935f),
            surfaceContainerHigh = hslColor(hue, neutralSat * 1.3f, 0.915f),
            outline = hslColor(hue, neutralSat, 0.46f),
            outlineVariant = hslColor(hue, neutralSat * 1.1f, 0.78f)
        )
    }
}

private fun hslColor(hue: Float, sat: Float, lit: Float): Color =
    Color(ColorUtils.HSLToColor(floatArrayOf(hue, sat.coerceIn(0f, 1f), lit.coerceIn(0f, 1f))))

private fun contrastingContentColor(
    background: Color,
    darkCandidate: Color,
    lightCandidate: Color
): Color {
    val bgArgb = background.toArgb()
    val darkContrast = ColorUtils.calculateContrast(darkCandidate.toArgb(), bgArgb)
    val lightContrast = ColorUtils.calculateContrast(lightCandidate.toArgb(), bgArgb)
    return if (darkContrast >= lightContrast) darkCandidate else lightCandidate
}
