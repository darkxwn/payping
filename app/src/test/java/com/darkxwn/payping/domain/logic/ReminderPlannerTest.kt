package com.darkxwn.payping.domain.logic

import com.darkxwn.payping.domain.model.PaymentReminder
import com.darkxwn.payping.domain.model.RecurrenceType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderPlannerTest {

    private val today = LocalDate.of(2026, 11, 25)

    private fun reminder(
        id: Long,
        title: String,
        amountMinor: Long,
        currency: String,
        dueDate: LocalDate,
        hour: Int = 10,
        minute: Int = 0
    ) = PaymentReminder(
        id = id,
        title = title,
        amountMinor = amountMinor,
        currencyCode = currency,
        dueDate = dueDate,
        notifyHour = hour,
        notifyMinute = minute,
        recurrence = RecurrenceType.MONTHLY,
        remindDaysBefore = listOf(0, 1, 3)
    )

    @Test
    fun groupReminders_partitionsAndSortsChronologically() {
        val items = listOf(
            reminder(1, "Later Rent", 50000_00, "RUB", LocalDate.of(2026, 12, 10)),
            reminder(2, "Upcoming Internet", 800_00, "RUB", LocalDate.of(2026, 11, 28)),
            reminder(3, "Today Late", 500_00, "RUB", today, hour = 18, minute = 0),
            reminder(4, "Today Early", 300_00, "RUB", today, hour = 9, minute = 0),
            reminder(5, "Overdue Sub", 199_00, "RUB", LocalDate.of(2026, 11, 23))
        )

        val grouped = ReminderPlanner.groupReminders(items, today)

        assertEquals(listOf(5L), grouped.overdue.map { it.id })
        assertEquals(listOf(4L, 3L), grouped.today.map { it.id })
        assertEquals(listOf(2L), grouped.upcoming.map { it.id })
        assertEquals(listOf(1L), grouped.later.map { it.id })
    }

    @Test
    fun calculateSummary_aggregates7DaysAndEndOfMonthPerCurrency() {
        val items = listOf(
            reminder(1, "Overdue", 1000_00, "RUB", LocalDate.of(2026, 11, 24)),
            reminder(2, "Today", 500_00, "RUB", LocalDate.of(2026, 11, 25)),
            reminder(3, "Day+4 USD", 20_00, "USD", LocalDate.of(2026, 11, 29)),
            reminder(4, "Day+6 (Dec 1)", 300_00, "RUB", LocalDate.of(2026, 12, 1)),
            reminder(5, "Day+8 (Dec 3)", 900_00, "RUB", LocalDate.of(2026, 12, 3))
        )

        val summary = ReminderPlanner.calculateSummary(items, today, defaultCurrencyCode = "RUB")

        // Next 7 days covers through Dec 1 (today..today+6) + overdue, excluding Dec 3.
        assertEquals(
            listOf(CurrencyTotal("RUB", 1800_00), CurrencyTotal("USD", 20_00)),
            summary.next7DaysTotals
        )
        // End of November covers Nov 24, Nov 25, Nov 29 (excluding Dec 1 and Dec 3).
        assertEquals(
            listOf(CurrencyTotal("RUB", 1500_00), CurrencyTotal("USD", 20_00)),
            summary.endOfMonthTotals
        )
        assertEquals(1, summary.overdueCount)
        assertEquals(listOf(CurrencyTotal("RUB", 1000_00)), summary.overdueTotals)
    }

    @Test
    fun computeFutureTriggers_skipsPastSlotsAndKeepsFutureOnes() {
        val zone = ZoneId.of("Europe/Moscow")
        val now = ZonedDateTime.of(2026, 11, 25, 11, 0, 0, 0, zone)
        val item = reminder(
            id = 42,
            title = "Phone",
            amountMinor = 650_00,
            currency = "RUB",
            dueDate = LocalDate.of(2026, 11, 26),
            hour = 10,
            minute = 0
        )

        val triggers = NotificationScheduleCalculator.computeFutureTriggers(item, now, zone)
        // daysBefore=3 -> Nov 23 10:00 (past)
        // daysBefore=1 -> Nov 25 10:00 (past vs 11:00)
        // daysBefore=0 -> Nov 26 10:00 (future)
        assertEquals(1, triggers.size)
        assertEquals(0, triggers.first().daysBefore)
    }
}
