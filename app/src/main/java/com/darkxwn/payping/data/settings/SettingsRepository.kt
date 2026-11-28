package com.darkxwn.payping.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.darkxwn.payping.domain.model.AppFont
import com.darkxwn.payping.domain.model.AppLanguage
import com.darkxwn.payping.domain.model.AppSettings
import com.darkxwn.payping.domain.model.AppThemeMode
import com.darkxwn.payping.domain.model.BaseAccentColor
import com.darkxwn.payping.domain.model.BottomBarStyle
import com.darkxwn.payping.domain.model.CurrencyCatalog
import com.darkxwn.payping.domain.model.CurrencyOption
import com.darkxwn.payping.domain.model.FontScaleRange
import com.darkxwn.payping.domain.model.LiquidGlassLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "payping_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PURE_BLACK = booleanPreferencesKey("pure_black")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val ACCENT_COLOR_ARGB = longPreferencesKey("accent_color_argb")
        val CUSTOM_ACCENT_COLORS = stringPreferencesKey("custom_accent_colors")
        val APP_FONT = stringPreferencesKey("app_font")
        val USE_SYSTEM_FONT_SCALE = booleanPreferencesKey("use_system_font_scale")
        val FONT_SCALE_PERCENT = intPreferencesKey("font_scale_percent")
        val BOTTOM_BAR_STYLE = stringPreferencesKey("bottom_bar_style")
        val LIQUID_GLASS_LEVEL = stringPreferencesKey("liquid_glass_level")
        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        val RECENT_CURRENCIES = stringPreferencesKey("recent_currencies")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        val language = AppLanguage.fromTag(prefs[Keys.LANGUAGE])
        val themeMode = enumOrDefault(prefs[Keys.THEME_MODE], AppThemeMode.SYSTEM)
        val pureBlack = prefs[Keys.PURE_BLACK] ?: false
        val useDynamicColor = prefs[Keys.USE_DYNAMIC_COLOR] ?: false
        val accentColorArgb = prefs[Keys.ACCENT_COLOR_ARGB]?.let(BaseAccentColor::normalizeOpaqueArgb)
            ?: BaseAccentColor.DEFAULT_ARGB
        val customColors = decodeCustomColors(prefs[Keys.CUSTOM_ACCENT_COLORS])
        val appFont = enumOrDefault(prefs[Keys.APP_FONT], AppFont.SYSTEM)
        val useSystemFontScale = prefs[Keys.USE_SYSTEM_FONT_SCALE] ?: true
        val fontScalePercent = (prefs[Keys.FONT_SCALE_PERCENT] ?: 100).coerceIn(FontScaleRange.MIN_PERCENT, FontScaleRange.MAX_PERCENT)
        val bottomBarStyle = enumOrDefault(prefs[Keys.BOTTOM_BAR_STYLE], BottomBarStyle.PINNED)
        val liquidGlassLevel = enumOrDefault(prefs[Keys.LIQUID_GLASS_LEVEL], LiquidGlassLevel.NONE)
        val currency = CurrencyOption.fromCode(prefs[Keys.DEFAULT_CURRENCY]).code
        val recentCurrencies = decodeRecentCurrencies(prefs[Keys.RECENT_CURRENCIES])

        AppSettings(
            language = language,
            themeMode = themeMode,
            pureBlack = pureBlack,
            useDynamicColor = useDynamicColor,
            accentColorArgb = accentColorArgb,
            customAccentColors = customColors,
            appFont = appFont,
            useSystemFontScale = useSystemFontScale,
            fontScalePercent = fontScalePercent,
            bottomBarStyle = bottomBarStyle,
            liquidGlassLevel = liquidGlassLevel,
            defaultCurrencyCode = currency,
            recentCurrencies = recentCurrencies
        )
    }

    suspend fun getCurrentSettings(): AppSettings = settings.first()

    suspend fun setLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.tag ?: "system"
        }
    }

    suspend fun setThemeMode(themeMode: AppThemeMode) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun setPureBlack(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.PURE_BLACK] = enabled
        }
    }

    suspend fun selectAccentColor(argb: Long) {
        val normalized = BaseAccentColor.normalizeOpaqueArgb(argb)
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.USE_DYNAMIC_COLOR] = false
            prefs[Keys.ACCENT_COLOR_ARGB] = normalized
        }
    }

    suspend fun selectDynamicColor() {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.USE_DYNAMIC_COLOR] = true
        }
    }

    suspend fun addCustomAccentColor(argb: Long) {
        val normalized = BaseAccentColor.normalizeOpaqueArgb(argb)
        context.settingsDataStore.edit { prefs ->
            val current = decodeCustomColors(prefs[Keys.CUSTOM_ACCENT_COLORS])
            val isBaseColor = BaseAccentColor.entries.any { it.argb == normalized }
            val updated = if (isBaseColor || normalized in current) current else current + normalized
            prefs[Keys.CUSTOM_ACCENT_COLORS] = encodeCustomColors(updated)
            prefs[Keys.USE_DYNAMIC_COLOR] = false
            prefs[Keys.ACCENT_COLOR_ARGB] = normalized
        }
    }

    suspend fun removeCustomAccentColor(argb: Long) {
        val normalized = BaseAccentColor.normalizeOpaqueArgb(argb)
        context.settingsDataStore.edit { prefs ->
            val current = decodeCustomColors(prefs[Keys.CUSTOM_ACCENT_COLORS])
            val updated = current.filterNot { it == normalized }
            prefs[Keys.CUSTOM_ACCENT_COLORS] = encodeCustomColors(updated)
            if (prefs[Keys.ACCENT_COLOR_ARGB] == normalized) {
                prefs[Keys.ACCENT_COLOR_ARGB] = BaseAccentColor.DEFAULT_ARGB
            }
        }
    }

    suspend fun setAppFont(font: AppFont) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.APP_FONT] = font.name
        }
    }

    suspend fun setUseSystemFontScale(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.USE_SYSTEM_FONT_SCALE] = enabled
        }
    }

    suspend fun setFontScalePercent(percent: Int) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.FONT_SCALE_PERCENT] = percent.coerceIn(FontScaleRange.MIN_PERCENT, FontScaleRange.MAX_PERCENT)
        }
    }

    suspend fun setBottomBarStyle(style: BottomBarStyle) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.BOTTOM_BAR_STYLE] = style.name
        }
    }

    suspend fun setLiquidGlassLevel(level: LiquidGlassLevel) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.LIQUID_GLASS_LEVEL] = level.name
        }
    }

    suspend fun setDefaultCurrency(currencyCode: String) {
        val validCode = CurrencyOption.fromCode(currencyCode).code
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.DEFAULT_CURRENCY] = validCode
        }
    }

    suspend fun recordRecentCurrency(currencyCode: String) {
        val validCode = CurrencyOption.fromCode(currencyCode).code
        context.settingsDataStore.edit { prefs ->
            val current = decodeRecentCurrencies(prefs[Keys.RECENT_CURRENCIES])
            val updated = (listOf(validCode) + current.filterNot { it.equals(validCode, ignoreCase = true) })
                .take(3)
            prefs[Keys.RECENT_CURRENCIES] = updated.joinToString(",")
        }
    }

    suspend fun applyBackupSettings(imported: AppSettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = imported.language.tag ?: "system"
            prefs[Keys.THEME_MODE] = imported.themeMode.name
            prefs[Keys.PURE_BLACK] = imported.pureBlack
            prefs[Keys.USE_DYNAMIC_COLOR] = imported.useDynamicColor
            prefs[Keys.ACCENT_COLOR_ARGB] = BaseAccentColor.normalizeOpaqueArgb(imported.accentColorArgb)
            prefs[Keys.CUSTOM_ACCENT_COLORS] = encodeCustomColors(
                imported.customAccentColors.map(BaseAccentColor::normalizeOpaqueArgb).distinct()
            )
            prefs[Keys.APP_FONT] = imported.appFont.name
            prefs[Keys.USE_SYSTEM_FONT_SCALE] = imported.useSystemFontScale
            prefs[Keys.FONT_SCALE_PERCENT] = imported.fontScalePercent.coerceIn(FontScaleRange.MIN_PERCENT, FontScaleRange.MAX_PERCENT)
            prefs[Keys.BOTTOM_BAR_STYLE] = imported.bottomBarStyle.name
            prefs[Keys.LIQUID_GLASS_LEVEL] = imported.liquidGlassLevel.name
            prefs[Keys.DEFAULT_CURRENCY] = CurrencyOption.fromCode(imported.defaultCurrencyCode).code
            prefs[Keys.RECENT_CURRENCIES] = imported.recentCurrencies
                .filter(CurrencyCatalog::isSupported)
                .map { CurrencyOption.fromCode(it).code }
                .distinct()
                .take(3)
                .joinToString(",")
        }
    }

    private fun decodeRecentCurrencies(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(',')
            .map { it.trim().uppercase() }
            .filter(CurrencyCatalog::isSupported)
            .distinct()
            .take(3)
    }

    private fun decodeCustomColors(raw: String?): List<Long> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(',')
            .mapNotNull { token -> token.trim().toLongOrNull(16) }
            .map(BaseAccentColor::normalizeOpaqueArgb)
            .distinct()
    }

    private fun encodeCustomColors(colors: List<Long>): String =
        colors.joinToString(",") { (it and 0x00FFFFFFL).toString(16).padStart(6, '0') }

    private inline fun <reified T : Enum<T>> enumOrDefault(raw: String?, default: T): T {
        if (raw.isNullOrBlank()) return default
        return enumValues<T>().firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) } ?: default
    }
}
