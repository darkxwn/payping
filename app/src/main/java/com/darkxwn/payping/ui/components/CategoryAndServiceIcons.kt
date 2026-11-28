package com.darkxwn.payping.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SimCard
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkxwn.payping.domain.model.ServiceBadgeCatalog

fun categoryIconFor(iconKey: String): ImageVector = when (iconKey) {
    "subscriptions" -> Icons.Rounded.Subscriptions
    "home" -> Icons.Rounded.Home
    "taxes" -> Icons.Rounded.AccountBalance
    "telecom" -> Icons.Rounded.Wifi
    "credits" -> Icons.Rounded.CreditCard
    "rent" -> Icons.Rounded.Apartment
    "transport" -> Icons.Rounded.DirectionsCar
    "education" -> Icons.Rounded.School
    "shopping" -> Icons.Rounded.ShoppingBag
    "health" -> Icons.Rounded.MedicalServices
    "gaming" -> Icons.Rounded.SportsEsports
    "travel" -> Icons.Rounded.Flight
    "fitness" -> Icons.Rounded.FitnessCenter
    "pets" -> Icons.Rounded.Pets
    "work" -> Icons.Rounded.Work
    else -> Icons.Rounded.Category
}

fun serviceIconFor(iconKey: String): ImageVector = when (iconKey) {
    "yandex_plus" -> Icons.Rounded.AutoAwesome
    "sber" -> Icons.Rounded.CheckCircleOutline
    "tbank" -> Icons.Rounded.Shield
    "alfa" -> Icons.Rounded.AccountBalance
    "vtb" -> Icons.Rounded.AccountBalance
    "telegram" -> Icons.AutoMirrored.Rounded.Send
    "vk" -> Icons.Rounded.Language
    "ozon" -> Icons.Rounded.ShoppingCart
    "wb" -> Icons.Rounded.ShoppingBag
    "mts" -> Icons.Rounded.SimCard
    "megafon" -> Icons.Rounded.PhoneAndroid
    "beeline" -> Icons.Rounded.SimCard
    "tele2" -> Icons.Rounded.PhoneAndroid
    "rostelecom" -> Icons.Rounded.Router
    "kinopoisk" -> Icons.Rounded.Movie
    "spotify" -> Icons.Rounded.MusicNote
    "youtube" -> Icons.Rounded.PlayCircle
    "apple" -> Icons.Rounded.Cloud
    "google" -> Icons.Rounded.Cloud
    "chatgpt" -> Icons.Rounded.AutoAwesome
    "steam" -> Icons.Rounded.SportsEsports
    "netflix" -> Icons.Rounded.Movie
    "avito" -> Icons.Rounded.Storefront
    "gosuslugi" -> Icons.Rounded.ReceiptLong
    "cloud" -> Icons.Rounded.Cloud
    "bank_card" -> Icons.Rounded.CreditCard
    else -> Icons.Rounded.Autorenew
}

/**
 * Circular badge rendered for a specific payment (service icon or custom/auto monogram text).
 */
@Composable
fun PaymentBadgeAvatar(
    title: String,
    badgeIconKey: String?,
    badgeText: String?,
    badgeColorArgb: Long,
    size: Dp = 42.dp,
    modifier: Modifier = Modifier
) {
    val bgColor = Color(badgeColorArgb)
    val contentColor = if (bgColor.luminance() > 0.55f) Color(0xFF111413) else Color.White
    val preset = ServiceBadgeCatalog.findByKey(badgeIconKey)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (preset != null) {
            Icon(
                imageVector = serviceIconFor(preset.key),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(size * 0.52f)
            )
        } else {
            val monogram = ServiceBadgeCatalog.deriveMonogram(title, badgeText)
            val fontSize = when {
                monogram.codePointCount(0, monogram.length) <= 1 -> (size.value * 0.42f).sp
                else -> (size.value * 0.34f).sp
            }
            Text(
                text = monogram,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                maxLines = 1
            )
        }
    }
}

/**
 * Circular badge rendered for a category (subtle tinted backdrop + semantic category icon).
 */
@Composable
fun CategoryBadgeAvatar(
    iconKey: String,
    colorArgb: Long,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    val rawColor = Color(colorArgb)
    val isDarkSurface = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val iconTint = if (isDarkSurface && rawColor.luminance() < 0.16f) {
        Color.White.copy(alpha = 0.90f)
    } else {
        rawColor
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(rawColor.copy(alpha = if (isDarkSurface) 0.24f else 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = categoryIconFor(iconKey),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(size * 0.54f)
        )
    }
}

@Composable
fun legibleCategoryAccentColor(colorArgb: Long): Color {
    val rawColor = Color(colorArgb)
    val isDarkSurface = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return if (isDarkSurface && rawColor.luminance() < 0.22f) {
        Color(
            red = (rawColor.red + 0.38f).coerceAtMost(1f),
            green = (rawColor.green + 0.38f).coerceAtMost(1f),
            blue = (rawColor.blue + 0.38f).coerceAtMost(1f),
            alpha = 1f
        )
    } else {
        rawColor
    }
}
