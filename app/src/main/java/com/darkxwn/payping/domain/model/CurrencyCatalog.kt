package com.darkxwn.payping.domain.model

data class CurrencySpec(
    val code: String,
    val symbol: String,
    val nameRu: String,
    val nameEn: String,
    val decimalDigits: Int = 2
) {
    fun localizedName(languageTag: String): String =
        if (languageTag.equals("ru", ignoreCase = true)) nameRu else nameEn

    fun matchesQuery(query: String): Boolean {
        val q = query.trim()
        if (q.isEmpty()) return true
        return code.contains(q, ignoreCase = true) ||
            symbol.contains(q, ignoreCase = true) ||
            nameRu.contains(q, ignoreCase = true) ||
            nameEn.contains(q, ignoreCase = true)
    }
}

object CurrencyCatalog {
    val all: List<CurrencySpec> = listOf(
        CurrencySpec("RUB", "₽", "Российский рубль", "Russian Ruble", 2),
        CurrencySpec("USD", "$", "Доллар США", "US Dollar", 2),
        CurrencySpec("EUR", "€", "Евро", "Euro", 2),
        CurrencySpec("TRY", "₺", "Турецкая лира", "Turkish Lira", 2),
        CurrencySpec("GEL", "₾", "Грузинский лари", "Georgian Lari", 2),
        CurrencySpec("KZT", "₸", "Казахстанский тенге", "Kazakhstani Tenge", 2),
        CurrencySpec("BYN", "Br", "Белорусский рубль", "Belarusian Ruble", 2),
        CurrencySpec("UAH", "₴", "Украинская гривна", "Ukrainian Hryvnia", 2),
        CurrencySpec("AED", "د.إ", "Дирхам ОАЭ", "UAE Dirham", 2),
        CurrencySpec("CNY", "¥", "Китайский юань", "Chinese Yuan", 2),
        CurrencySpec("GBP", "£", "Фунт стерлингов", "British Pound", 2),
        CurrencySpec("AMD", "֏", "Армянский драм", "Armenian Dram", 2),
        CurrencySpec("UZS", "сўм", "Узбекский сум", "Uzbekistani Som", 2),
        CurrencySpec("AZN", "₼", "Азербайджанский манат", "Azerbaijani Manat", 2),
        CurrencySpec("THB", "฿", "Тайский бат", "Thai Baht", 2),
        CurrencySpec("RSD", "дин.", "Сербский динар", "Serbian Dinar", 2),
        CurrencySpec("PLN", "zł", "Польский злотый", "Polish Zloty", 2),
        CurrencySpec("CHF", "Fr", "Швейцарский франк", "Swiss Franc", 2),
        CurrencySpec("CAD", "C$", "Канадский доллар", "Canadian Dollar", 2),
        CurrencySpec("AUD", "A$", "Австралийский доллар", "Australian Dollar", 2),
        CurrencySpec("JPY", "¥", "Японская иена", "Japanese Yen", 0),
        CurrencySpec("KRW", "₩", "Южнокорейская вона", "South Korean Won", 0),
        CurrencySpec("INR", "₹", "Индийская рупия", "Indian Rupee", 2),
        CurrencySpec("BRL", "R$", "Бразильский реал", "Brazilian Real", 2),
        CurrencySpec("CZK", "Kč", "Чешская крона", "Czech Koruna", 2),
        CurrencySpec("SEK", "kr", "Шведская крона", "Swedish Krona", 2),
        CurrencySpec("NOK", "kr", "Норвежская крона", "Norwegian Krone", 2),
        CurrencySpec("ILS", "₪", "Израильский шекель", "Israeli New Shekel", 2)
    )

    private val byCodeMap: Map<String, CurrencySpec> =
        all.associateBy { it.code.uppercase() }

    val DEFAULT: CurrencySpec = all.first()

    fun isSupported(code: String?): Boolean =
        !code.isNullOrBlank() && byCodeMap.containsKey(code.trim().uppercase())

    fun fromCode(code: String?): CurrencySpec {
        val clean = code?.trim()?.uppercase().orEmpty()
        return byCodeMap[clean] ?: DEFAULT
    }

    fun symbolFor(code: String): String =
        byCodeMap[code.trim().uppercase()]?.symbol ?: code
}

/**
 * Compatibility facade preserving CurrencyOption calls while backing them with CurrencyCatalog.
 */
object CurrencyOption {
    val RUB: CurrencySpec get() = CurrencyCatalog.DEFAULT
    val entries: List<CurrencySpec> get() = CurrencyCatalog.all

    fun fromCode(code: String?): CurrencySpec = CurrencyCatalog.fromCode(code)

    fun symbolFor(code: String): String = CurrencyCatalog.symbolFor(code)

    fun decimalDigitsFor(code: String): Int = CurrencyCatalog.fromCode(code).decimalDigits
}
