package com.darkxwn.payping.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.darkxwn.payping.R
import com.darkxwn.payping.domain.model.AppFont
import com.darkxwn.payping.domain.model.AppSettings
import com.darkxwn.payping.domain.model.AppThemeMode
import com.darkxwn.payping.domain.model.BaseAccentColor
import com.darkxwn.payping.domain.model.BottomBarStyle
import com.darkxwn.payping.domain.model.LiquidGlassLevel
import com.darkxwn.payping.ui.components.ColorPickerDialog
import com.darkxwn.payping.ui.components.PayPingSubmenuScreen
import com.darkxwn.payping.ui.theme.fontFamilyFor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeSettingsScreen(
    settings: AppSettings,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onPureBlackChange: (Boolean) -> Unit,
    onSelectAccentColor: (Long) -> Unit,
    onSelectDynamicColor: () -> Unit,
    onAddCustomAccentColor: (Long) -> Unit,
    onRemoveCustomAccentColor: (Long) -> Unit,
    onAppFontChange: (AppFont) -> Unit,
    onBottomBarStyleChange: (BottomBarStyle) -> Unit,
    onLiquidGlassLevelChange: (LiquidGlassLevel) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPicker by remember { mutableStateOf(false) }
    var fontMenuExpanded by remember { mutableStateOf(false) }

    PayPingSubmenuScreen(
        title = stringResource(R.string.settings_theme_screen_title),
        liquidGlassLevel = settings.liquidGlassLevel,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExternalSectionTitle(stringResource(R.string.theme_section_appearance))
            ThemeOptionCard {
                val options = listOf(
                    AppThemeMode.SYSTEM to R.string.theme_auto,
                    AppThemeMode.LIGHT to R.string.theme_light,
                    AppThemeMode.DARK to R.string.theme_dark
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, (mode, labelRes) ->
                        SegmentedButton(
                            selected = settings.themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                            icon = {}
                        ) {
                            Text(
                                text = stringResource(labelRes),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            ExternalSectionTitle(stringResource(R.string.theme_section_pure_black))
            ThemeOptionCard {
                val blackOptions = listOf(
                    false to R.string.theme_black_standard,
                    true to R.string.theme_black_pure
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    blackOptions.forEachIndexed { index, (pureBlack, labelRes) ->
                        SegmentedButton(
                            selected = settings.pureBlack == pureBlack,
                            onClick = { onPureBlackChange(pureBlack) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = blackOptions.size),
                            icon = {}
                        ) {
                            Text(
                                text = stringResource(labelRes),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            ExternalSectionTitle(stringResource(R.string.theme_section_color))
            ThemeOptionCard {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        DynamicColorSwatch(
                            selected = settings.useDynamicColor,
                            onClick = onSelectDynamicColor
                        )
                    }

                    BaseAccentColor.entries.forEach { preset ->
                        AccentSwatchCircle(
                            argb = preset.argb,
                            selected = !settings.useDynamicColor && settings.accentColorArgb == preset.argb,
                            removable = false,
                            onSelect = { onSelectAccentColor(preset.argb) },
                            onRemove = {}
                        )
                    }

                    settings.customAccentColors.forEach { customArgb ->
                        AccentSwatchCircle(
                            argb = customArgb,
                            selected = !settings.useDynamicColor && settings.accentColorArgb == customArgb,
                            removable = true,
                            onSelect = { onSelectAccentColor(customArgb) },
                            onRemove = { onRemoveCustomAccentColor(customArgb) }
                        )
                    }

                    AddAccentSwatchButton(onClick = { showColorPicker = true })
                }
            }

            ExternalSectionTitle(stringResource(R.string.theme_section_font))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                val activeFontLabel = when (settings.appFont) {
                    AppFont.SYSTEM -> stringResource(R.string.font_system)
                    AppFont.ROBOTO -> stringResource(R.string.font_roboto)
                    AppFont.INTER -> stringResource(R.string.font_inter)
                    AppFont.CONSOLAS -> stringResource(R.string.font_consolas)
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.DropdownList) {
                                fontMenuExpanded = true
                            }
                            .padding(horizontal = 16.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TextFields,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = activeFontLabel,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = fontMenuExpanded,
                        onDismissRequest = { fontMenuExpanded = false }
                    ) {
                        val fontOptions = listOf(
                            AppFont.SYSTEM to R.string.font_system,
                            AppFont.ROBOTO to R.string.font_roboto,
                            AppFont.INTER to R.string.font_inter,
                            AppFont.CONSOLAS to R.string.font_consolas
                        )
                        fontOptions.forEach { (option, labelRes) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(labelRes),
                                        fontFamily = fontFamilyFor(option)
                                    )
                                },
                                trailingIcon = if (settings.appFont == option) {
                                    {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    null
                                },
                                onClick = {
                                    fontMenuExpanded = false
                                    onAppFontChange(option)
                                }
                            )
                        }
                    }
                }
            }

            ExternalSectionTitle(stringResource(R.string.theme_section_bottom_bar))
            ThemeOptionCard {
                val barOptions = listOf(
                    BottomBarStyle.PINNED to R.string.theme_bar_pinned,
                    BottomBarStyle.FLOATING to R.string.theme_bar_floating
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    barOptions.forEachIndexed { index, (style, labelRes) ->
                        SegmentedButton(
                            selected = settings.bottomBarStyle == style,
                            onClick = { onBottomBarStyleChange(style) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = barOptions.size),
                            icon = {}
                        ) {
                            Text(
                                text = stringResource(labelRes),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            ExternalSectionTitle(stringResource(R.string.theme_section_liquid_glass))
            ThemeOptionCard {
                val glassOptions = listOf(
                    LiquidGlassLevel.NONE to R.string.theme_glass_none,
                    LiquidGlassLevel.STANDARD to R.string.theme_glass_standard,
                    LiquidGlassLevel.MAXIMUM to R.string.theme_glass_maximum
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    glassOptions.forEachIndexed { index, (level, labelRes) ->
                        SegmentedButton(
                            selected = settings.liquidGlassLevel == level,
                            onClick = { onLiquidGlassLevelChange(level) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = glassOptions.size),
                            icon = {}
                        ) {
                            Text(
                                text = stringResource(labelRes),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColorArgb = settings.accentColorArgb,
            onDismiss = { showColorPicker = false },
            onConfirm = { pickedArgb ->
                showColorPicker = false
                onAddCustomAccentColor(pickedArgb)
            }
        )
    }
}

@Composable
private fun ExternalSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ThemeOptionCard(
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun DynamicColorSwatch(
    selected: Boolean,
    onClick: () -> Unit
) {
    val ringColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .border(
                width = if (selected) 2.5.dp else 1.dp,
                color = ringColor,
                shape = CircleShape
            )
            .padding(3.dp)
            .clip(CircleShape)
            .background(
                Brush.sweepGradient(
                    listOf(
                        Color(0xFF0E7C66),
                        Color(0xFF1565C0),
                        Color(0xFF7E57C2),
                        Color(0xFFEC407A),
                        Color(0xFFFFA726),
                        Color(0xFF0E7C66)
                    )
                )
            )
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (selected) Icons.Rounded.Check else Icons.Rounded.AutoAwesome,
            contentDescription = stringResource(R.string.theme_color_material_you),
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun AccentSwatchCircle(
    argb: Long,
    selected: Boolean,
    removable: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit
) {
    val swatchColor = Color(argb)
    val checkTint = if (swatchColor.luminance() > 0.52f) Color(0xFF111413) else Color.White
    val ringColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(modifier = Modifier.size(44.dp)) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .border(
                    width = if (selected) 2.5.dp else 1.dp,
                    color = if (selected) ringColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                    shape = CircleShape
                )
                .padding(3.dp)
                .clip(CircleShape)
                .background(swatchColor)
                .clickable(role = Role.Button, onClick = onSelect),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = checkTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (removable) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .clickable(role = Role.Button, onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

@Composable
private fun AddAccentSwatchButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = stringResource(R.string.color_picker_title),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
