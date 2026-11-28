package com.darkxwn.payping.domain.logic

import com.darkxwn.payping.domain.backup.BackupSerializer
import com.darkxwn.payping.domain.model.AppFont
import com.darkxwn.payping.domain.model.AppLanguage
import com.darkxwn.payping.domain.model.AppSettings
import com.darkxwn.payping.domain.model.AppThemeMode
import com.darkxwn.payping.domain.model.BaseAccentColor
import com.darkxwn.payping.domain.model.BottomBarStyle
import com.darkxwn.payping.domain.model.BuiltInCategory
import com.darkxwn.payping.domain.model.CategoryCatalog
import com.darkxwn.payping.domain.model.CurrencyCatalog
import com.darkxwn.payping.domain.model.LiquidGlassLevel
import com.darkxwn.payping.domain.model.PaymentCategory
import com.darkxwn.payping.domain.model.PaymentHistory
import com.darkxwn.payping.domain.model.PaymentReminder
import com.darkxwn.payping.domain.model.PaymentType
import com.darkxwn.payping.domain.model.RecurrenceType
import com.darkxwn.payping.domain.model.ServiceBadgeCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

        // Zero-decimal currencies (JPY / KRW)
        assertEquals(1500L, MoneyFormat.parseAmountToMinor("1 500", "JPY"))
        assertNull(MoneyFormat.parseAmountToMinor("15.5", "JPY"))
        assertEquals("1 500 ¥", MoneyFormat.formatMinorAmount(1500L, "JPY"))
        assertEquals("1500", MoneyFormat.minorToEditableInput(1500L, "JPY"))
        assertEquals("₺", CurrencyCatalog.fromCode("TRY").symbol)
        assertEquals("₾", CurrencyCatalog.fromCode("GEL").symbol)
    }

    @Test
    fun backupSerializer_roundTripPreservesData() {
        val customCategory = PaymentCategory(
            uid = "cat_custom_1",
            name = "Gaming",
            iconKey = "gaming",
            colorArgb = 0xFF6A1B9AL,
            isBuiltIn = false,
            sortOrder = 1,
            createdAtEpochMillis = 1795000000000L
        )
        val reminders = listOf(
            PaymentReminder(
                id = 7L,
                uid = "rem-rent-1",
                title = "Rent",
                amountMinor = 45000_00L,
                currencyCode = "RUB",
                dueDate = LocalDate.of(2026, 12, 1),
                notifyHour = 9,
                notifyMinute = 30,
                recurrence = RecurrenceType.MONTHLY,
                remindDaysBefore = listOf(0, 1, 3),
                note = "Sber *4321",
                paymentType = PaymentType.AUTO_DEBIT,
                categoryUid = customCategory.uid,
                badgeIconKey = "sber",
                badgeText = null,
                badgeColorArgb = 0xFF21A038L,
                anchorDayOfMonth = 1
            )
        )
        val history = listOf(
            PaymentHistory(
                id = 3L,
                uid = "hist-rent-1",
                reminderId = 7L,
                reminderUid = "rem-rent-1",
                title = "Rent",
                amountMinor = 45000_00L,
                currencyCode = "RUB",
                dueDate = LocalDate.of(2026, 11, 1),
                paidAtEpochMillis = 1793520000000L,
                note = "Sber *4321",
                recurrence = RecurrenceType.MONTHLY,
                paymentType = PaymentType.AUTO_DEBIT,
                categoryUid = customCategory.uid,
                badgeIconKey = "sber",
                badgeText = null,
                badgeColorArgb = 0xFF21A038L,
                previousDueDate = LocalDate.of(2026, 11, 1),
                nextDueDateAfterPayment = LocalDate.of(2026, 12, 1)
            )
        )
        val settings = AppSettings(
            language = AppLanguage.SYSTEM,
            themeMode = AppThemeMode.DARK,
            pureBlack = true,
            useDynamicColor = false,
            accentColorArgb = 0xFF4F46E5L,
            customAccentColors = listOf(0xFF4F46E5L, 0xFF00BCD4L),
            appFont = AppFont.CONSOLAS,
            bottomBarStyle = BottomBarStyle.FLOATING,
            liquidGlassLevel = LiquidGlassLevel.MAXIMUM,
            defaultCurrencyCode = "EUR",
            recentCurrencies = listOf("TRY", "USD", "RUB")
        )

        val encoded = BackupSerializer.encode(
            reminders = reminders,
            history = history,
            settings = settings,
            categories = listOf(customCategory),
            exportedAtEpochMillis = 1795600000000L
        )
        val decoded = BackupSerializer.decode(encoded).getOrThrow()

        assertEquals(settings, decoded.settings)
        assertEquals(1, decoded.categories.size)
        assertEquals(customCategory, decoded.categories[0])
        assertEquals(1, decoded.reminders.size)
        assertEquals("rem-rent-1", decoded.reminders[0].uid)
        assertEquals(PaymentType.AUTO_DEBIT, decoded.reminders[0].paymentType)
        assertEquals(customCategory.uid, decoded.reminders[0].categoryUid)
        assertEquals("sber", decoded.reminders[0].badgeIconKey)
        assertEquals(1, decoded.history.size)
        assertEquals("rem-rent-1", decoded.history[0].reminderUid)
        assertEquals(LocalDate.of(2026, 12, 1), decoded.history[0].nextDueDateAfterPayment)
    }

    @Test
    fun badgesAndCategories_handleMonogramsEmojiAndFallback() {
        assertEquals("ЯП", ServiceBadgeCatalog.deriveMonogram("Яндекс Плюс", null))
        assertEquals("TE", ServiceBadgeCatalog.deriveMonogram("Telegram", null))
        assertEquals("🎬", ServiceBadgeCatalog.deriveMonogram("Cinema", "🎬🎵"))
        assertEquals(BuiltInCategory.SUBSCRIPTIONS.uid, CategoryCatalog.resolve("cat_subscriptions", emptyList(), "ru").uid)
        assertEquals(BuiltInCategory.OTHER.uid, CategoryCatalog.resolve("non_existent", emptyList(), "ru").uid)
    }

    @Test
    fun backupSerializer_decodesLegacySettingsDefaults() {
        val legacyJson = """
            {
              "schemaVersion": 1,
              "exportedAtEpochMillis": 1795600000000,
              "settings": {
                "language": "RU",
                "themeMode": "DARK",
                "defaultCurrencyCode": "USD"
              },
              "reminders": [],
              "history": []
            }
        """.trimIndent()

        val decoded = BackupSerializer.decode(legacyJson).getOrThrow()
        val settings = requireNotNull(decoded.settings)
        assertEquals(AppLanguage.RU, settings.language)
        assertEquals(AppThemeMode.DARK, settings.themeMode)
        assertEquals("USD", settings.defaultCurrencyCode)
        assertFalse(settings.pureBlack)
        assertFalse(settings.useDynamicColor)
        assertEquals(BaseAccentColor.DEFAULT_ARGB, settings.accentColorArgb)
        assertTrue(settings.customAccentColors.isEmpty())
        assertEquals(AppFont.SYSTEM, settings.appFont)
        assertEquals(BottomBarStyle.PINNED, settings.bottomBarStyle)
        assertEquals(LiquidGlassLevel.NONE, settings.liquidGlassLevel)
    }

    @Test
    fun appLanguage_and_accentColorHelpers_workCorrectly() {
        assertEquals("ru", AppLanguage.SYSTEM.resolvedLocaleTag("ru"))
        assertEquals("en", AppLanguage.SYSTEM.resolvedLocaleTag("de"))
        assertEquals("ru", AppLanguage.RU.resolvedLocaleTag("en"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTag(null))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTag("system"))
        assertEquals(AppLanguage.EN, AppLanguage.fromTag("en"))

        assertEquals(0xFF4F46E5L, BaseAccentColor.parseHex("#4f46e5"))
        assertEquals(0xFF4F46E5L, BaseAccentColor.parseHex("4F46E5"))
        assertNull(BaseAccentColor.parseHex("bad"))
        assertEquals("#4F46E5", BaseAccentColor.formatHex(0x124F46E5L))
        assertEquals(AppFont.INTER, AppFont.valueOf("INTER"))
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
