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

enum class AppLanguage(val tag: String) {
    RU("ru"),
    EN("en");

    companion object {
        fun fromTag(tag: String?): AppLanguage = when {
            tag.equals("ru", ignoreCase = true) -> RU
            tag.equals("en", ignoreCase = true) -> EN
            else -> fromSystem()
        }

        fun fromSystem(): AppLanguage {
            val lang = Locale.getDefault().language
            return if (lang.equals("ru", ignoreCase = true)) RU else EN
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
    val language: AppLanguage = AppLanguage.fromSystem(),
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val defaultCurrencyCode: String = CurrencyOption.RUB.code
)
