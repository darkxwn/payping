package com.darkxwn.payping.domain.logic

import com.darkxwn.payping.domain.model.RecurrenceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class RecurrenceCalculatorTest {

    @Test
    fun once_returnsNull() {
        val date = LocalDate.of(2026, 3, 15)
        assertNull(RecurrenceCalculator.nextDueDate(date, RecurrenceType.ONCE))
    }

    @Test
    fun daily_addsOneDay() {
        val date = LocalDate.of(2026, 2, 28)
        assertEquals(
            LocalDate.of(2026, 3, 1),
            RecurrenceCalculator.nextDueDate(date, RecurrenceType.DAILY)
        )
    }

    @Test
    fun weekly_addsSevenDays() {
        val date = LocalDate.of(2026, 3, 28)
        assertEquals(
            LocalDate.of(2026, 4, 4),
            RecurrenceCalculator.nextDueDate(date, RecurrenceType.WEEKLY)
        )
    }

    @Test
    fun monthly_handlesEndOfMonthAndRestoresAnchorDay() {
        val jan31 = LocalDate.of(2026, 1, 31)
        val feb = RecurrenceCalculator.nextDueDate(jan31, RecurrenceType.MONTHLY, anchorDayOfMonth = 31)
        assertEquals(LocalDate.of(2026, 2, 28), feb)

        val mar = RecurrenceCalculator.nextDueDate(feb!!, RecurrenceType.MONTHLY, anchorDayOfMonth = 31)
        assertEquals(LocalDate.of(2026, 3, 31), mar)

        val apr = RecurrenceCalculator.nextDueDate(mar!!, RecurrenceType.MONTHLY, anchorDayOfMonth = 31)
        assertEquals(LocalDate.of(2026, 4, 30), apr)
    }

    @Test
    fun monthly_handlesLeapYearFebruary() {
        val jan30 = LocalDate.of(2028, 1, 30)
        val feb = RecurrenceCalculator.nextDueDate(jan30, RecurrenceType.MONTHLY, anchorDayOfMonth = 30)
        assertEquals(LocalDate.of(2028, 2, 29), feb)
    }

    @Test
    fun yearly_clampsLeapDayAndRestoresOnNextLeapYear() {
        val feb29 = LocalDate.of(2024, 2, 29)
        val y2025 = RecurrenceCalculator.nextDueDate(feb29, RecurrenceType.YEARLY, anchorDayOfMonth = 29)
        assertEquals(LocalDate.of(2025, 2, 28), y2025)

        val y2028 = RecurrenceCalculator.nextDueDate(LocalDate.of(2027, 2, 28), RecurrenceType.YEARLY, anchorDayOfMonth = 29)
        assertEquals(LocalDate.of(2028, 2, 29), y2028)
    }
}
