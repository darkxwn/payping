package com.darkxwn.payping.ui.components

import android.content.res.Resources
import java.util.Locale

/**
 * Reads the underlying OS language tag instead of the per-app overridden locale.
 */
fun resolveSystemLanguageTag(): String {
    val locales = Resources.getSystem().configuration.locales
    val systemLocale = if (!locales.isEmpty) locales[0] else null
    return systemLocale?.language?.takeIf { it.isNotBlank() }
        ?: Locale.getDefault().language.ifBlank { "en" }
}
