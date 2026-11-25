package com.darkxwn.payping.domain.model

import java.time.LocalDate
import java.util.Locale

enum class RecurrenceType {
    ONCE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class BottomBarStyle {
    PINNED,
    FLOATING
}

enum class LiquidGlassLevel {
    NONE,
    STANDARD,
    MAXIMUM
}

enum class BaseAccentColor(val argb: Long) {
    EMERALD(0xFF0E7C66L),
    OCEAN(0xFF1565C0L),
    INDIGO(0xFF4F46E5L),
    AMBER(0xFFB45309L),
    ROSE(0xFFBE185DL),
    GRAPHITE(0xFF455A64L);

    companion object {
        const val DEFAULT_ARGB = 0xFF0E7C66L

        fun normalizeOpaqueArgb(argb: Long): Long =
            0xFF000000L or (argb and 0x00FFFFFFL)

        fun formatHex(argb: Long): String =
            "#%06X".format(Locale.US, (argb and 0x00FFFFFFL))

        fun parseHex(raw: String): Long? {
            val clean = raw.trim().removePrefix("#")
            if (clean.length != 6) return null
            val rgb = clean.toLongOrNull(16) ?: return null
            return 0xFF000000L or rgb
        }
    }
}

enum class AppLanguage(val tag: String?) {
    SYSTEM(null),
    RU("ru"),
    EN("en");

    fun resolvedLocaleTag(systemLanguageTag: String = Locale.getDefault().language): String {
        if (tag != null) return tag
        return if (systemLanguageTag.equals("ru", ignoreCase = true)) "ru" else "en"
    }

    companion object {
        fun fromTag(tag: String?): AppLanguage = when {
            tag.isNullOrBlank() || tag.equals("system", ignoreCase = true) -> SYSTEM
            tag.equals("ru", ignoreCase = true) -> RU
            tag.equals("en", ignoreCase = true) -> EN
            else -> SYSTEM
        }
    }
}

enum class CurrencyOption(val code: String, val symbol: String) {
    RUB("RUB", "₽"),
    USD("USD", "$"),
    EUR("EUR", "€"),
    GBP("GBP", "£"),
    KZT("KZT", "₸"),
    BYN("BYN", "Br"),
    CNY("CNY", "¥");

    companion object {
        fun fromCode(code: String?): CurrencyOption =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: RUB

        fun symbolFor(code: String): String =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }?.symbol ?: code
    }
}

object ReminderConstants {
    val SUPPORTED_REMIND_DAYS = listOf(0, 1, 2, 3, 5, 7, 14)
    val DEFAULT_REMIND_DAYS = listOf(0, 1, 3)
    const val DEFAULT_NOTIFY_HOUR = 10
    const val DEFAULT_NOTIFY_MINUTE = 0
}

data class PaymentReminder(
    val id: Long = 0L,
    val title: String,
    val amountMinor: Long,
    val currencyCode: String,
    val dueDate: LocalDate,
    val notifyHour: Int,
    val notifyMinute: Int,
    val recurrence: RecurrenceType,
    val remindDaysBefore: List<Int>,
    val note: String = "",
    val anchorDayOfMonth: Int = dueDate.dayOfMonth,
    val createdAtEpochMillis: Long = 0L,
    val updatedAtEpochMillis: Long = 0L
)

data class PaymentHistory(
    val id: Long = 0L,
    val reminderId: Long?,
    val title: String,
    val amountMinor: Long,
    val currencyCode: String,
    val dueDate: LocalDate,
    val paidAtEpochMillis: Long,
    val note: String = "",
    val recurrence: RecurrenceType = RecurrenceType.ONCE
)

data class AppSettings(
    val language: AppLanguage = AppLanguage.SYSTEM,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val pureBlack: Boolean = false,
    val useDynamicColor: Boolean = false,
    val accentColorArgb: Long = BaseAccentColor.DEFAULT_ARGB,
    val customAccentColors: List<Long> = emptyList(),
    val bottomBarStyle: BottomBarStyle = BottomBarStyle.PINNED,
    val liquidGlassLevel: LiquidGlassLevel = LiquidGlassLevel.NONE,
    val defaultCurrencyCode: String = CurrencyOption.RUB.code
)
