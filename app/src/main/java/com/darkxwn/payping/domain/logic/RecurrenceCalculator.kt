package com.darkxwn.payping.domain.logic

import com.darkxwn.payping.domain.model.RecurrenceType
import java.time.LocalDate
import java.time.YearMonth

object RecurrenceCalculator {
    /**
     * Computes the next scheduled due date after a cycle is marked done.
     * Uses [anchorDayOfMonth] for monthly/yearly recurrence so month-end dates
     * (for example, 31 Jan -> 28 Feb -> 31 Mar) return to the original day when available.
     */
    fun nextDueDate(
        currentDueDate: LocalDate,
        recurrence: RecurrenceType,
        anchorDayOfMonth: Int = currentDueDate.dayOfMonth
    ): LocalDate? {
        val safeAnchor = anchorDayOfMonth.coerceIn(1, 31)
        return when (recurrence) {
            RecurrenceType.ONCE -> null
            RecurrenceType.WEEKLY -> currentDueDate.plusWeeks(1)
            RecurrenceType.MONTHLY -> {
                val targetMonth = YearMonth.from(currentDueDate).plusMonths(1)
                targetMonth.atDay(safeAnchor.coerceAtMost(targetMonth.lengthOfMonth()))
            }
            RecurrenceType.YEARLY -> {
                val targetMonth = YearMonth.from(currentDueDate).plusYears(1)
                targetMonth.atDay(safeAnchor.coerceAtMost(targetMonth.lengthOfMonth()))
            }
        }
    }
}
