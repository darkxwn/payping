package com.darkxwn.payping.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.darkxwn.payping.domain.model.AppLanguage
import com.darkxwn.payping.domain.model.AppSettings
import com.darkxwn.payping.domain.model.AppThemeMode
import com.darkxwn.payping.domain.model.CurrencyOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "payping_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        val language = AppLanguage.fromTag(prefs[Keys.LANGUAGE])
        val themeMode = AppThemeMode.entries.firstOrNull {
            it.name.equals(prefs[Keys.THEME_MODE], ignoreCase = true)
        } ?: AppThemeMode.SYSTEM
        val currency = CurrencyOption.fromCode(prefs[Keys.DEFAULT_CURRENCY]).code
        AppSettings(
            language = language,
            themeMode = themeMode,
            defaultCurrencyCode = currency
        )
    }

    suspend fun getCurrentSettings(): AppSettings = settings.first()

    suspend fun setLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.tag
        }
    }

    suspend fun setThemeMode(themeMode: AppThemeMode) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun setDefaultCurrency(currencyCode: String) {
        val validCode = CurrencyOption.fromCode(currencyCode).code
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.DEFAULT_CURRENCY] = validCode
        }
    }

    suspend fun applyBackupSettings(imported: AppSettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = imported.language.tag
            prefs[Keys.THEME_MODE] = imported.themeMode.name
            prefs[Keys.DEFAULT_CURRENCY] = CurrencyOption.fromCode(imported.defaultCurrencyCode).code
        }
    }
}
