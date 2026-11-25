package com.darkxwn.payping.domain.logic

import com.darkxwn.payping.domain.backup.BackupSerializer
import com.darkxwn.payping.domain.model.AppLanguage
import com.darkxwn.payping.domain.model.AppSettings
import com.darkxwn.payping.domain.model.AppThemeMode
import com.darkxwn.payping.domain.model.PaymentHistory
import com.darkxwn.payping.domain.model.PaymentReminder
import com.darkxwn.payping.domain.model.RecurrenceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MoneyAndBackupTest {

    @Test
    fun moneyFormat_parsesAndFormatsCorrectly() {
        assertEquals(1500_00L, MoneyFormat.parseAmountToMinor("1 500"))
        assertEquals(1299_50L, MoneyFormat.parseAmountToMinor("1299,5"))
        assertEquals(99_05L, MoneyFormat.parseAmountToMinor("99.05"))
        assertNull(MoneyFormat.parseAmountToMinor("0"))
        assertNull(MoneyFormat.parseAmountToMinor("-10"))
        assertNull(MoneyFormat.parseAmountToMinor("12.345"))

        assertEquals("1 500 ₽", MoneyFormat.formatMinorAmount(1500_00L, "RUB"))
        assertEquals("1 299.50 $", MoneyFormat.formatMinorAmount(1299_50L, "USD"))
        assertEquals("1299.50", MoneyFormat.minorToEditableInput(1299_50L))
        assertEquals("1500", MoneyFormat.minorToEditableInput(1500_00L))
    }

    @Test
    fun backupSerializer_roundTripPreservesData() {
        val reminders = listOf(
            PaymentReminder(
                id = 7L,
                title = "Rent",
                amountMinor = 45000_00L,
                currencyCode = "RUB",
                dueDate = LocalDate.of(2026, 12, 1),
                notifyHour = 9,
                notifyMinute = 30,
                recurrence = RecurrenceType.MONTHLY,
                remindDaysBefore = listOf(0, 1, 3),
                note = "Sber *4321",
                anchorDayOfMonth = 1
            )
        )
        val history = listOf(
            PaymentHistory(
                id = 3L,
                reminderId = 7L,
                title = "Rent",
                amountMinor = 45000_00L,
                currencyCode = "RUB",
                dueDate = LocalDate.of(2026, 11, 1),
                paidAtEpochMillis = 1793520000000L,
                note = "Sber *4321",
                recurrence = RecurrenceType.MONTHLY
            )
        )
        val settings = AppSettings(
            language = AppLanguage.RU,
            themeMode = AppThemeMode.DARK,
            defaultCurrencyCode = "EUR"
        )

        val encoded = BackupSerializer.encode(reminders, history, settings, exportedAtEpochMillis = 1795600000000L)
        val decoded = BackupSerializer.decode(encoded).getOrThrow()

        assertEquals(settings, decoded.settings)
        assertEquals(1, decoded.reminders.size)
        assertEquals("Rent", decoded.reminders[0].title)
        assertEquals(45000_00L, decoded.reminders[0].amountMinor)
        assertEquals(listOf(0, 1, 3), decoded.reminders[0].remindDaysBefore)
        assertEquals(1, decoded.history.size)
        assertEquals(LocalDate.of(2026, 11, 1), decoded.history[0].dueDate)
    }

    @Test
    fun backupSerializer_rejectsInvalidSchemaOrBlankTitle() {
        val badJson = """{"schemaVersion":99,"exportedAtEpochMillis":1,"reminders":[],"history":[]}"""
        assertTrue(BackupSerializer.decode(badJson).isFailure)

        val blankTitleJson = """
            {"schemaVersion":1,"exportedAtEpochMillis":1,"reminders":[
              {"title":"  ","amountMinor":100,"currencyCode":"RUB","dueDate":"2026-12-01","notifyHour":10,"notifyMinute":0,"recurrence":"ONCE"}
            ],"history":[]}
        """.trimIndent()
        assertTrue(BackupSerializer.decode(blankTitleJson).isFailure)
    }
}
