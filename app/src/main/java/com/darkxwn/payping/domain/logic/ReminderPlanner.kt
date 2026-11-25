package com.darkxwn.payping.domain.logic

import com.darkxwn.payping.domain.model.PaymentReminder
import java.time.LocalDate
import java.time.YearMonth

enum class ReminderBucket {
    OVERDUE,
    TODAY,
    UPCOMING,
    LATER
}

data class CurrencyTotal(
    val currencyCode: String,
    val totalMinor: Long
)

data class ReminderSummary(
    val next7DaysTotals: List<CurrencyTotal>,
    val endOfMonthTotals: List<CurrencyTotal>,
    val overdueTotals: List<CurrencyTotal>,
    val overdueCount: Int
)

data class GroupedReminders(
    val overdue: List<PaymentReminder>,
    val today: List<PaymentReminder>,
    val upcoming: List<PaymentReminder>,
    val later: List<PaymentReminder>
) {
    val isEmpty: Boolean
        get() = overdue.isEmpty() && today.isEmpty() && upcoming.isEmpty() && later.isEmpty()
}

object ReminderPlanner {
    private val chronologicalComparator = compareBy<PaymentReminder>(
        { it.dueDate },
        { it.notifyHour },
        { it.notifyMinute },
        { it.title.lowercase() }
    )

    fun classify(dueDate: LocalDate, today: LocalDate): ReminderBucket = when {
        dueDate.isBefore(today) -> ReminderBucket.OVERDUE
        dueDate.isEqual(today) -> ReminderBucket.TODAY
        !dueDate.isAfter(today.plusDays(7)) -> ReminderBucket.UPCOMING
        else -> ReminderBucket.LATER
    }

    fun groupReminders(
        reminders: List<PaymentReminder>,
        today: LocalDate
    ): GroupedReminders {
        val sorted = reminders.sortedWith(chronologicalComparator)
        val overdue = mutableListOf<PaymentReminder>()
        val todayList = mutableListOf<PaymentReminder>()
        val upcoming = mutableListOf<PaymentReminder>()
        val later = mutableListOf<PaymentReminder>()

        for (reminder in sorted) {
            when (classify(reminder.dueDate, today)) {
                ReminderBucket.OVERDUE -> overdue += reminder
                ReminderBucket.TODAY -> todayList += reminder
                ReminderBucket.UPCOMING -> upcoming += reminder
                ReminderBucket.LATER -> later += reminder
            }
        }

        return GroupedReminders(
            overdue = overdue,
            today = todayList,
            upcoming = upcoming,
            later = later
        )
    }

    /**
     * Aggregates unpaid amounts across currencies.
     * Unpaid overdue bills are included in both the 7-day and end-of-month required totals
     * so users see the full amount needed to cover obligations.
     */
    fun calculateSummary(
        reminders: List<PaymentReminder>,
        today: LocalDate,
        defaultCurrencyCode: String
    ): ReminderSummary {
        val sevenDayHorizon = today.plusDays(6)
        val monthEnd = YearMonth.from(today).atEndOfMonth()

        val overdueItems = reminders.filter { it.dueDate.isBefore(today) }
        val next7DaysItems = reminders.filter { !it.dueDate.isAfter(sevenDayHorizon) }
        val endOfMonthItems = reminders.filter { !it.dueDate.isAfter(monthEnd) }

        return ReminderSummary(
            next7DaysTotals = sumByCurrency(next7DaysItems, defaultCurrencyCode),
            endOfMonthTotals = sumByCurrency(endOfMonthItems, defaultCurrencyCode),
            overdueTotals = sumByCurrency(overdueItems, defaultCurrencyCode),
            overdueCount = overdueItems.size
        )
    }

    private fun sumByCurrency(
        items: List<PaymentReminder>,
        defaultCurrencyCode: String
    ): List<CurrencyTotal> {
        if (items.isEmpty()) return emptyList()
        return items
            .groupBy { it.currencyCode }
            .map { (currency, list) ->
                CurrencyTotal(
                    currencyCode = currency,
                    totalMinor = list.sumOf { it.amountMinor }
                )
            }
            .sortedWith(
                compareBy<CurrencyTotal> { !it.currencyCode.equals(defaultCurrencyCode, ignoreCase = true) }
                    .thenBy { it.currencyCode }
            )
    }
}
