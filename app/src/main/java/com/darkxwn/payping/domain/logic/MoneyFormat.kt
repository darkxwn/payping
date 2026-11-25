package com.darkxwn.payping.domain.logic

import com.darkxwn.payping.domain.model.CurrencyOption
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object MoneyFormat {
    private const val MAX_MINOR_AMOUNT = 999_999_999_99L

    fun parseAmountToMinor(raw: String): Long? {
        val cleaned = raw
            .replace("\u00A0", "")
            .replace(" ", "")
            .trim()
            .replace(',', '.')

        if (cleaned.isEmpty()) return null
        if (!Regex("^\\d+(\\.\\d{1,2})?$").matches(cleaned)) return null

        return try {
            val minor = BigDecimal(cleaned).movePointRight(2).longValueExact()
            if (minor in 1..MAX_MINOR_AMOUNT) minor else null
        } catch (_: ArithmeticException) {
            null
        } catch (_: NumberFormatException) {
            null
        }
    }

    fun formatMinorAmount(amountMinor: Long, currencyCode: String): String {
        val absMinor = kotlin.math.abs(amountMinor)
        val whole = absMinor / 100
        val fraction = (absMinor % 100).toInt()
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ' '
            decimalSeparator = '.'
        }
        val integerFormatter = DecimalFormat("#,##0", symbols)
        val integerPart = integerFormatter.format(whole)
        val sign = if (amountMinor < 0) "-" else ""
        val numberPart = if (fraction == 0) {
            "$sign$integerPart"
        } else {
            val fracStr = fraction.toString().padStart(2, '0')
            "$sign$integerPart.$fracStr"
        }
        val symbol = CurrencyOption.symbolFor(currencyCode)
        return "$numberPart $symbol"
    }

    fun minorToEditableInput(amountMinor: Long): String {
        if (amountMinor <= 0) return ""
        val whole = amountMinor / 100
        val fraction = (amountMinor % 100).toInt()
        return if (fraction == 0) {
            whole.toString()
        } else {
            "$whole.${fraction.toString().padStart(2, '0')}"
        }
    }

    fun formatTime(hour: Int, minute: Int): String =
        "${hour.coerceIn(0, 23).toString().padStart(2, '0')}:${minute.coerceIn(0, 59).toString().padStart(2, '0')}"
}
