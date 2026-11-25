package com.darkxwn.payping.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.darkxwn.payping.R
import com.darkxwn.payping.domain.model.AppLanguage
import com.darkxwn.payping.domain.model.RecurrenceType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun recurrenceLabel(recurrence: RecurrenceType): String = when (recurrence) {
    RecurrenceType.ONCE -> stringResource(R.string.recurrence_once)
    RecurrenceType.WEEKLY -> stringResource(R.string.recurrence_weekly)
    RecurrenceType.MONTHLY -> stringResource(R.string.recurrence_monthly)
    RecurrenceType.YEARLY -> stringResource(R.string.recurrence_yearly)
}

@Composable
fun remindDayChipLabel(daysBefore: Int): String = if (daysBefore == 0) {
    stringResource(R.string.remind_chip_0)
} else {
    stringResource(R.string.remind_chip_n, daysBefore)
}

@Composable
fun remindDayFullLabel(daysBefore: Int): String = if (daysBefore == 0) {
    stringResource(R.string.remind_day_0)
} else {
    stringResource(R.string.remind_day_n, daysBefore)
}

fun formatLocalDate(date: LocalDate, language: AppLanguage): String {
    val locale = Locale(language.tag)
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    return date.format(formatter)
}

fun formatDateTimeMillis(epochMillis: Long, language: AppLanguage): String {
    val locale = Locale(language.tag)
    val formatter = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(locale)
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}
