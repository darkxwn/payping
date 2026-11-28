package com.darkxwn.payping.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.LocalParking
import androidx.compose.material.icons.rounded.LocalPharmacy
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkxwn.payping.R
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
    "food" -> Icons.Rounded.Restaurant
    "cafe" -> Icons.Rounded.LocalCafe
    "groceries" -> Icons.Rounded.LocalGroceryStore
    "music" -> Icons.Rounded.MusicNote
    "video" -> Icons.Rounded.LiveTv
    "cloud" -> Icons.Rounded.Cloud
    "software" -> Icons.Rounded.Terminal
    "books" -> Icons.Rounded.MenuBook
    "water" -> Icons.Rounded.WaterDrop
    "electricity" -> Icons.Rounded.Bolt
    "repair" -> Icons.Rounded.Build
    "fuel" -> Icons.Rounded.LocalGasStation
    "pharmacy" -> Icons.Rounded.LocalPharmacy
    "kids" -> Icons.Rounded.ChildCare
    "beauty" -> Icons.Rounded.Spa
    "clothes" -> Icons.Rounded.Checkroom
    "gift" -> Icons.Rounded.CardGiftcard
    "charity" -> Icons.Rounded.Favorite
    "savings" -> Icons.Rounded.Savings
    "insurance" -> Icons.Rounded.Security
    "investments" -> Icons.Rounded.TrendingUp
    "parking" -> Icons.Rounded.LocalParking
    "delivery" -> Icons.Rounded.LocalShipping
    "photo" -> Icons.Rounded.PhotoCamera
    else -> Icons.Rounded.Category
}

@DrawableRes
fun serviceDrawableResOrNull(iconKey: String): Int? = when (iconKey) {
    "yandex_plus" -> R.drawable.ic_svc_yandex_plus
    "sber" -> R.drawable.ic_svc_sber
    "tbank" -> R.drawable.ic_svc_tbank
    "alfa" -> R.drawable.ic_svc_alfa
    "vtb" -> R.drawable.ic_svc_vtb
    "gazprombank" -> R.drawable.ic_svc_gazprombank
    "raiffeisen" -> R.drawable.ic_svc_raiffeisen
    "yoomoney" -> R.drawable.ic_svc_yoomoney
    "telegram" -> R.drawable.ic_svc_telegram
    "vk" -> R.drawable.ic_svc_vk
    "ozon" -> R.drawable.ic_svc_ozon
    "wb" -> R.drawable.ic_svc_wb
    "mts" -> R.drawable.ic_svc_mts
    "megafon" -> R.drawable.ic_svc_megafon
    "beeline" -> R.drawable.ic_svc_beeline
    "tele2" -> R.drawable.ic_svc_tele2
    "rostelecom" -> R.drawable.ic_svc_rostelecom
    "kinopoisk" -> R.drawable.ic_svc_kinopoisk
    "kion" -> R.drawable.ic_svc_kion
    "okko" -> R.drawable.ic_svc_okko
    "wink" -> R.drawable.ic_svc_wink
    "ivi" -> R.drawable.ic_svc_ivi
    "litres" -> R.drawable.ic_svc_litres
    "spotify" -> R.drawable.ic_svc_spotify
    "youtube" -> R.drawable.ic_svc_youtube
    "apple" -> R.drawable.ic_svc_apple
    "icloud" -> R.drawable.ic_svc_icloud
    "google" -> R.drawable.ic_svc_google
    "google_one" -> R.drawable.ic_svc_google_one
    "gemini" -> R.drawable.ic_svc_gemini
    "chatgpt" -> R.drawable.ic_svc_chatgpt
    "claude" -> R.drawable.ic_svc_claude
    "steam" -> R.drawable.ic_svc_steam
    "netflix" -> R.drawable.ic_svc_netflix
    "discord" -> R.drawable.ic_svc_discord
    "twitch" -> R.drawable.ic_svc_twitch
    "playstation" -> R.drawable.ic_svc_playstation
    "xbox" -> R.drawable.ic_svc_xbox
    "boosty" -> R.drawable.ic_svc_boosty
    "patreon" -> R.drawable.ic_svc_patreon
    "cloud_mail" -> R.drawable.ic_svc_cloud_mail
    "dropbox" -> R.drawable.ic_svc_dropbox
    "onedrive" -> R.drawable.ic_svc_onedrive
    "notion" -> R.drawable.ic_svc_notion
    "duolingo" -> R.drawable.ic_svc_duolingo
    else -> null
}

fun serviceFallbackIconFor(iconKey: String): ImageVector = when (iconKey) {
    "cloud" -> Icons.Rounded.Cloud
    "bank_card" -> Icons.Rounded.CreditCard
    else -> Icons.Rounded.Autorenew
}

/**
 * Circular badge rendered for a specific payment (Lawnicons service drawable or custom/auto monogram text).
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
            val drawableRes = serviceDrawableResOrNull(preset.key)
            if (drawableRes != null) {
                Icon(
                    painter = painterResource(drawableRes),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(size * 0.62f)
                )
            } else {
                Icon(
                    imageVector = serviceFallbackIconFor(preset.key),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(size * 0.52f)
                )
            }
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
