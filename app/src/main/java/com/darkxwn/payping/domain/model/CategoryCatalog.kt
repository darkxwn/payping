package com.darkxwn.payping.domain.model

import java.util.UUID

enum class PaymentType {
    MANUAL,
    AUTO_DEBIT,
    TRIAL,
    INSTALLMENT;

    companion object {
        val STAGE_1_SELECTABLE: List<PaymentType> = listOf(MANUAL, AUTO_DEBIT, TRIAL)

        fun fromRaw(raw: String?): PaymentType =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: MANUAL
    }
}

data class PaymentCategory(
    val uid: String,
    val name: String,
    val iconKey: String,
    val colorArgb: Long,
    val isBuiltIn: Boolean = false,
    val sortOrder: Int = 0,
    val createdAtEpochMillis: Long = 0L
)

enum class BuiltInCategory(
    val uid: String,
    val iconKey: String,
    val colorArgb: Long,
    val nameRu: String,
    val nameEn: String
) {
    SUBSCRIPTIONS("cat_subscriptions", "subscriptions", 0xFF0E7C66L, "Подписки и медиа", "Subscriptions & Media"),
    HOUSING("cat_housing", "home", 0xFF1565C0L, "ЖКХ и дом", "Utilities & Home"),
    TAXES("cat_taxes", "taxes", 0xFFB45309L, "Налоги", "Taxes"),
    TELECOM("cat_telecom", "telecom", 0xFF00838FL, "Связь и интернет", "Telecom & Internet"),
    CREDITS("cat_credits", "credits", 0xFFC62828L, "Кредиты", "Loans & Credits"),
    RENT("cat_rent", "rent", 0xFF4F46E5L, "Аренда", "Rent"),
    TRANSPORT("cat_transport", "transport", 0xFF2E7D32L, "Транспорт", "Transport"),
    EDUCATION("cat_education", "education", 0xFF6A1B9AL, "Образование", "Education"),
    OTHER("cat_other", "other", 0xFF546E7AL, "Другое", "Other");

    fun toCategory(languageTag: String, order: Int = ordinal): PaymentCategory = PaymentCategory(
        uid = uid,
        name = if (languageTag.equals("ru", ignoreCase = true)) nameRu else nameEn,
        iconKey = iconKey,
        colorArgb = colorArgb,
        isBuiltIn = true,
        sortOrder = order
    )
}

object CategoryCatalog {
    val CATEGORY_ICON_KEYS: List<String> = listOf(
        "subscriptions",
        "home",
        "taxes",
        "telecom",
        "credits",
        "rent",
        "transport",
        "education",
        "shopping",
        "health",
        "gaming",
        "travel",
        "fitness",
        "pets",
        "work",
        "food",
        "cafe",
        "groceries",
        "music",
        "video",
        "cloud",
        "software",
        "books",
        "water",
        "electricity",
        "repair",
        "fuel",
        "pharmacy",
        "kids",
        "beauty",
        "clothes",
        "gift",
        "charity",
        "savings",
        "insurance",
        "investments",
        "parking",
        "delivery",
        "photo",
        "other"
    )

    val CATEGORY_COLORS: List<Long> = listOf(
        0xFF0E7C66L,
        0xFF1565C0L,
        0xFF4F46E5L,
        0xFF6A1B9AL,
        0xFFBE185DL,
        0xFFC62828L,
        0xFFB45309L,
        0xFF2E7D32L,
        0xFF546E7AL
    )

    fun builtInCategories(languageTag: String): List<PaymentCategory> =
        BuiltInCategory.entries.mapIndexed { index, entry ->
            entry.toCategory(languageTag = languageTag, order = index)
        }

    fun resolve(
        uid: String?,
        customCategories: List<PaymentCategory>,
        languageTag: String
    ): PaymentCategory {
        val clean = uid?.trim().orEmpty()
        customCategories.firstOrNull { it.uid == clean }?.let { return it }
        val builtIn = BuiltInCategory.entries.firstOrNull { it.uid == clean } ?: BuiltInCategory.OTHER
        return builtIn.toCategory(languageTag)
    }

    fun newCustomUid(): String = "cat_custom_${UUID.randomUUID()}"
}

data class ServicePresetSpec(
    val key: String,
    val labelRu: String,
    val labelEn: String,
    val defaultColorArgb: Long
) {
    fun localizedLabel(languageTag: String): String =
        if (languageTag.equals("ru", ignoreCase = true)) labelRu else labelEn

    fun matchesQuery(query: String): Boolean {
        val q = query.trim()
        if (q.isEmpty()) return true
        return key.contains(q, ignoreCase = true) ||
            labelRu.contains(q, ignoreCase = true) ||
            labelEn.contains(q, ignoreCase = true)
    }
}

object ServiceBadgeCatalog {
    val presets: List<ServicePresetSpec> = listOf(
        ServicePresetSpec("yandex_plus", "Яндекс", "Yandex", 0xFFFC3F1DL),
        ServicePresetSpec("sber", "Сбер", "Sber", 0xFF21A038L),
        ServicePresetSpec("tbank", "Т-Банк", "T-Bank", 0xFFFFDD2DL),
        ServicePresetSpec("alfa", "Альфа-Банк", "Alfa-Bank", 0xFFEF3124L),
        ServicePresetSpec("vtb", "ВТБ", "VTB", 0xFF0A2896L),
        ServicePresetSpec("gazprombank", "Газпромбанк", "Gazprombank", 0xFF1A4B8CL),
        ServicePresetSpec("raiffeisen", "Райффайзен", "Raiffeisen", 0xFFFEE600L),
        ServicePresetSpec("yoomoney", "ЮMoney", "YooMoney", 0xFF8B3FFDL),
        ServicePresetSpec("telegram", "Telegram", "Telegram", 0xFF229ED9L),
        ServicePresetSpec("vk", "VK", "VK", 0xFF0077FFL),
        ServicePresetSpec("ozon", "Ozon", "Ozon", 0xFF005BFFL),
        ServicePresetSpec("wb", "Wildberries", "Wildberries", 0xFF8E24AAL),
        ServicePresetSpec("mts", "МТС", "MTS", 0xFFE30611L),
        ServicePresetSpec("megafon", "МегаФон", "MegaFon", 0xFF00B956L),
        ServicePresetSpec("beeline", "Билайн", "Beeline", 0xFFFFCC00L),
        ServicePresetSpec("tele2", "t2 / Tele2", "t2 / Tele2", 0xFF1F2229L),
        ServicePresetSpec("rostelecom", "Ростелеком", "Rostelecom", 0xFF6A1B9AL),
        ServicePresetSpec("kinopoisk", "Кинопоиск", "Kinopoisk", 0xFFFF5500L),
        ServicePresetSpec("kion", "KION", "KION", 0xFFC62828L),
        ServicePresetSpec("okko", "Okko", "Okko", 0xFF512DA8L),
        ServicePresetSpec("wink", "Wink", "Wink", 0xFFFF6D00L),
        ServicePresetSpec("ivi", "Иви", "Ivi", 0xFFEA003DL),
        ServicePresetSpec("litres", "Литрес", "Litres", 0xFFFF6D00L),
        ServicePresetSpec("spotify", "Spotify", "Spotify", 0xFF1DB954L),
        ServicePresetSpec("youtube", "YouTube", "YouTube", 0xFFE50914L),
        ServicePresetSpec("apple", "Apple / Music", "Apple / Music", 0xFF263238L),
        ServicePresetSpec("icloud", "iCloud", "iCloud", 0xFF0288D1L),
        ServicePresetSpec("google", "Google", "Google", 0xFF1A73E8L),
        ServicePresetSpec("google_one", "Google One", "Google One", 0xFF1A73E8L),
        ServicePresetSpec("gemini", "Gemini", "Gemini", 0xFF4F46E5L),
        ServicePresetSpec("chatgpt", "ChatGPT", "ChatGPT", 0xFF10A37FL),
        ServicePresetSpec("claude", "Claude", "Claude", 0xFFD97706L),
        ServicePresetSpec("steam", "Steam", "Steam", 0xFF1B2838L),
        ServicePresetSpec("netflix", "Netflix", "Netflix", 0xFFD32F2FL),
        ServicePresetSpec("discord", "Discord", "Discord", 0xFF5865F2L),
        ServicePresetSpec("twitch", "Twitch", "Twitch", 0xFF9146FFL),
        ServicePresetSpec("playstation", "PlayStation", "PlayStation", 0xFF003791L),
        ServicePresetSpec("xbox", "Xbox", "Xbox", 0xFF107C10L),
        ServicePresetSpec("boosty", "Boosty", "Boosty", 0xFFF15F2CL),
        ServicePresetSpec("patreon", "Patreon", "Patreon", 0xFFFF424DL),
        ServicePresetSpec("cloud_mail", "Облако Mail", "Mail Cloud", 0xFF005FF9L),
        ServicePresetSpec("dropbox", "Dropbox", "Dropbox", 0xFF0061FFL),
        ServicePresetSpec("onedrive", "OneDrive", "OneDrive", 0xFF0078D4L),
        ServicePresetSpec("notion", "Notion", "Notion", 0xFF2F3437L),
        ServicePresetSpec("duolingo", "Duolingo", "Duolingo", 0xFF58CC02L),
        ServicePresetSpec("bank_card", "Банк / Карта", "Bank / Card", 0xFF37474FL),
        ServicePresetSpec("cloud", "Облако", "Cloud", 0xFF0288D1L)
    )

    fun findByKey(key: String?): ServicePresetSpec? =
        key?.takeIf { it.isNotBlank() }?.let { clean ->
            presets.firstOrNull { it.key == clean }
        }

    fun deriveMonogram(title: String, explicitText: String?): String {
        val custom = explicitText?.trim().orEmpty()
        if (custom.isNotEmpty()) {
            return takeCodePoints(custom, 2).uppercase()
        }
        val cleaned = title.trim()
        if (cleaned.isEmpty()) return "?"
        val words = cleaned.split(Regex("[\\s\\p{Punct}]+")).filter { it.isNotEmpty() }
        if (words.size >= 2) {
            val first = takeCodePoints(words[0], 1)
            val second = takeCodePoints(words[1], 1)
            return "$first$second".uppercase()
        }
        return takeCodePoints(cleaned, 2).uppercase()
    }

    fun takeCodePoints(raw: String, maxCodePoints: Int): String {
        if (raw.isEmpty() || maxCodePoints <= 0) return ""
        val count = raw.codePointCount(0, raw.length)
        if (count <= maxCodePoints) return raw
        val endIndex = raw.offsetByCodePoints(0, maxCodePoints)
        return raw.substring(0, endIndex)
    }
}
