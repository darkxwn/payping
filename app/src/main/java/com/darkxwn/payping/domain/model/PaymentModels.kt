package com.darkxwn.payping.domain.model

import java.time.LocalDate
import java.util.Locale
import java.util.UUID

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

enum class AppFont {
    SYSTEM,
    ROBOTO,
    INTER,
    CONSOLAS
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

object ReminderConstants {
    val SUPPORTED_REMIND_DAYS = listOf(0, 1, 2, 3, 5, 7, 14)
    val DEFAULT_REMIND_DAYS = listOf(0, 1, 3)
    const val DEFAULT_NOTIFY_HOUR = 10
    const val DEFAULT_NOTIFY_MINUTE = 0
}

data class PaymentReminder(
    val id: Long = 0L,
    val uid: String = UUID.randomUUID().toString(),
    val title: String,
    val amountMinor: Long,
    val currencyCode: String,
    val dueDate: LocalDate,
    val notifyHour: Int,
    val notifyMinute: Int,
    val recurrence: RecurrenceType,
    val remindDaysBefore: List<Int>,
    val note: String = "",
    val paymentType: PaymentType = PaymentType.MANUAL,
    val categoryUid: String = BuiltInCategory.OTHER.uid,
    val badgeIconKey: String? = null,
    val badgeText: String? = null,
    val badgeColorArgb: Long = BaseAccentColor.DEFAULT_ARGB,
    val isArchived: Boolean = false,
    val isVariableAmount: Boolean = false,
    val serviceLink: String = "",
    val installmentPaid: Int? = null,
    val installmentTotal: Int? = null,
    val anchorDayOfMonth: Int = dueDate.dayOfMonth,
    val createdAtEpochMillis: Long = 0L,
    val updatedAtEpochMillis: Long = 0L
)

data class PaymentHistory(
    val id: Long = 0L,
    val uid: String = UUID.randomUUID().toString(),
    val reminderId: Long?,
    val reminderUid: String? = null,
    val title: String,
    val amountMinor: Long,
    val currencyCode: String,
    val dueDate: LocalDate,
    val paidAtEpochMillis: Long,
    val note: String = "",
    val recurrence: RecurrenceType = RecurrenceType.ONCE,
    val paymentType: PaymentType = PaymentType.MANUAL,
    val categoryUid: String = BuiltInCategory.OTHER.uid,
    val badgeIconKey: String? = null,
    val badgeText: String? = null,
    val badgeColorArgb: Long = BaseAccentColor.DEFAULT_ARGB,
    val previousDueDate: LocalDate? = null,
    val nextDueDateAfterPayment: LocalDate? = null,
    val rateToUsd: Double? = null
)

object FontScaleRange {
    const val MIN_PERCENT = 80
    const val MAX_PERCENT = 120
    const val STEP_PERCENT = 5

    fun clamp(percent: Int): Int = percent.coerceIn(MIN_PERCENT, MAX_PERCENT)
}

data class AppSettings(
    val language: AppLanguage = AppLanguage.SYSTEM,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val pureBlack: Boolean = false,
    val useDynamicColor: Boolean = false,
    val accentColorArgb: Long = BaseAccentColor.DEFAULT_ARGB,
    val customAccentColors: List<Long> = emptyList(),
    val appFont: AppFont = AppFont.SYSTEM,
    val useSystemFontScale: Boolean = true,
    val fontScalePercent: Int = 100,
    val bottomBarStyle: BottomBarStyle = BottomBarStyle.PINNED,
    val liquidGlassLevel: LiquidGlassLevel = LiquidGlassLevel.NONE,
    val defaultCurrencyCode: String = CurrencyOption.RUB.code,
    val recentCurrencies: List<String> = emptyList()
)
