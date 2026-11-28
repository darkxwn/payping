package com.darkxwn.payping.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.darkxwn.payping.R
import com.darkxwn.payping.domain.model.AppLanguage
import com.darkxwn.payping.domain.model.CategoryCatalog
import com.darkxwn.payping.domain.model.PaymentCategory
import com.darkxwn.payping.domain.model.ServiceBadgeCatalog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerSheet(
    selectedCategoryUid: String,
    customCategories: List<PaymentCategory>,
    language: AppLanguage,
    onSelectCategory: (PaymentCategory) -> Unit,
    onSaveNewCategory: (PaymentCategory) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val langTag = remember(language) {
        language.resolvedLocaleTag(resolveSystemLanguageTag())
    }
    val builtIn = remember(langTag) { CategoryCatalog.builtInCategories(langTag) }
    var showCreateDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.field_category_label),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item(key = "hdr_builtin") {
                    Text(
                        text = stringResource(R.string.categories_builtin_section),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                    )
                }

                items(builtIn, key = { it.uid }) { category ->
                    CategorySheetRow(
                        category = category,
                        selected = category.uid == selectedCategoryUid,
                        onClick = {
                            onSelectCategory(category)
                            onDismiss()
                        }
                    )
                }

                item(key = "hdr_custom") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.categories_custom_section),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                    )
                }

                items(customCategories, key = { it.uid }) { category ->
                    CategorySheetRow(
                        category = category,
                        selected = category.uid == selectedCategoryUid,
                        onClick = {
                            onSelectCategory(category)
                            onDismiss()
                        }
                    )
                }

                item(key = "create_category_btn") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(R.string.action_create_category),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CategoryEditorDialog(
            existingCategory = null,
            onDismiss = { showCreateDialog = false },
            onSave = { created ->
                showCreateDialog = false
                onSaveNewCategory(created)
                onSelectCategory(created)
                onDismiss()
            }
        )
    }
}

@Composable
private fun CategorySheetRow(
    category: PaymentCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    val rowBg = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
        Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(rowBg)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryBadgeAvatar(
            iconKey = category.iconKey,
            colorArgb = category.colorArgb,
            size = 36.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorDialog(
    existingCategory: PaymentCategory?,
    onDismiss: () -> Unit,
    onSave: (PaymentCategory) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    var name by remember(existingCategory) { mutableStateOf(existingCategory?.name ?: "") }
    var iconKey by remember(existingCategory) { mutableStateOf(existingCategory?.iconKey ?: "other") }
    var colorArgb by remember(existingCategory) {
        mutableLongStateOf(existingCategory?.colorArgb ?: CategoryCatalog.CATEGORY_COLORS.first())
    }
    var showNameError by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showCustomColorPicker by remember { mutableStateOf(false) }

    val colorChoices = remember(colorArgb) {
        (CategoryCatalog.CATEGORY_COLORS + colorArgb).distinct()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        if (existingCategory == null) R.string.dialog_category_create_title
                        else R.string.dialog_category_edit_title
                    ),
                    fontWeight = FontWeight.SemiBold
                )
                if (existingCategory != null && onDelete != null) {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryBadgeAvatar(
                        iconKey = iconKey,
                        colorArgb = colorArgb,
                        size = 46.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (showNameError && it.isNotBlank()) showNameError = false
                        },
                        label = { Text(stringResource(R.string.category_name_label)) },
                        singleLine = true,
                        isError = showNameError,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = stringResource(R.string.category_icon_label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryCatalog.CATEGORY_ICON_KEYS.forEach { key ->
                        val isSelected = key == iconKey
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(colorArgb).copy(alpha = 0.22f)
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(colorArgb) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .clickable(role = Role.Button) { iconKey = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryIconFor(key),
                                contentDescription = null,
                                tint = if (isSelected) legibleCategoryAccentColor(colorArgb) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.category_color_label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorChoices.forEach { argb ->
                        ColorCircleOption(
                            argb = argb,
                            selected = colorArgb == argb,
                            onClick = { colorArgb = argb }
                        )
                    }
                    AddCustomColorOption(onClick = { showCustomColorPicker = true })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isEmpty()) {
                        showNameError = true
                    } else {
                        val saved = (existingCategory ?: PaymentCategory(
                            uid = CategoryCatalog.newCustomUid(),
                            name = trimmed,
                            iconKey = iconKey,
                            colorArgb = colorArgb,
                            isBuiltIn = false
                        )).copy(
                            name = trimmed,
                            iconKey = iconKey,
                            colorArgb = colorArgb,
                            isBuiltIn = false
                        )
                        onSave(saved)
                    }
                }
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )

    if (showCustomColorPicker) {
        ColorPickerDialog(
            initialColorArgb = colorArgb,
            onDismiss = { showCustomColorPicker = false },
            onConfirm = { picked ->
                colorArgb = picked
                showCustomColorPicker = false
            },
            confirmTextRes = R.string.action_confirm
        )
    }

    if (showDeleteConfirm && existingCategory != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.dialog_delete_category_title)) },
            text = { Text(stringResource(R.string.dialog_delete_category_body, existingCategory.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(existingCategory.uid)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ServiceBadgePickerSheet(
    titleForPreview: String,
    initialIconKey: String?,
    initialText: String?,
    initialColorArgb: Long,
    language: AppLanguage,
    onApply: (badgeIconKey: String?, badgeText: String?, badgeColorArgb: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val langTag = remember(language) {
        language.resolvedLocaleTag(resolveSystemLanguageTag())
    }
    var useServiceIcon by remember { mutableStateOf(!initialIconKey.isNullOrBlank()) }
    var selectedIconKey by remember { mutableStateOf(initialIconKey ?: ServiceBadgeCatalog.presets.first().key) }
    var customText by remember { mutableStateOf(initialText ?: "") }
    var selectedColorArgb by remember { mutableLongStateOf(initialColorArgb) }
    var serviceSearch by remember { mutableStateOf("") }
    var showCustomColorPicker by remember { mutableStateOf(false) }

    val filteredPresets = remember(serviceSearch) {
        ServiceBadgeCatalog.presets.filter { it.matchesQuery(serviceSearch) }
    }

    val badgeColorChoices = remember(selectedColorArgb) {
        (CategoryCatalog.CATEGORY_COLORS + listOf(
            0xFF21A038L,
            0xFFFFDD2DL,
            0xFF229ED9L,
            0xFFFF5500L,
            0xFF1F2229L,
            selectedColorArgb
        )).distinct()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PaymentBadgeAvatar(
                    title = titleForPreview.ifBlank { "PayPing" },
                    badgeIconKey = if (useServiceIcon) selectedIconKey else null,
                    badgeText = if (useServiceIcon) null else customText,
                    badgeColorArgb = selectedColorArgb,
                    size = 52.dp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = stringResource(R.string.badge_picker_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val modes = listOf(
                    false to R.string.badge_mode_monogram,
                    true to R.string.badge_mode_service
                )
                modes.forEachIndexed { index, (isServiceMode, labelRes) ->
                    SegmentedButton(
                        selected = useServiceIcon == isServiceMode,
                        onClick = { useServiceIcon = isServiceMode },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                        icon = {}
                    ) {
                        Text(
                            text = stringResource(labelRes),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (!useServiceIcon) {
                OutlinedTextField(
                    value = customText,
                    onValueChange = { raw ->
                        customText = ServiceBadgeCatalog.takeCodePoints(raw, 2)
                    },
                    label = { Text(stringResource(R.string.badge_monogram_label)) },
                    placeholder = {
                        Text(
                            ServiceBadgeCatalog.deriveMonogram(
                                titleForPreview.ifBlank { "PP" },
                                null
                            )
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = serviceSearch,
                    onValueChange = { serviceSearch = it },
                    placeholder = { Text(stringResource(R.string.badge_service_search)) },
                    leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = if (serviceSearch.isNotEmpty()) {
                        {
                            IconButton(onClick = { serviceSearch = "" }) {
                                Icon(imageVector = Icons.Rounded.Close, contentDescription = null)
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPresets, key = { it.key }) { spec ->
                        val isSelected = selectedIconKey == spec.key
                        Surface(
                            onClick = {
                                selectedIconKey = spec.key
                                selectedColorArgb = spec.defaultColorArgb
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            border = if (isSelected) {
                                BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                null
                            }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                PaymentBadgeAvatar(
                                    title = spec.localizedLabel(langTag),
                                    badgeIconKey = spec.key,
                                    badgeText = null,
                                    badgeColorArgb = spec.defaultColorArgb,
                                    size = 36.dp
                                )
                                Text(
                                    text = spec.localizedLabel(langTag),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.badge_color_label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    badgeColorChoices.forEach { argb ->
                        ColorCircleOption(
                            argb = argb,
                            selected = selectedColorArgb == argb,
                            onClick = { selectedColorArgb = argb }
                        )
                    }
                    AddCustomColorOption(onClick = { showCustomColorPicker = true })
                }
            }

            Button(
                onClick = {
                    if (useServiceIcon) {
                        onApply(selectedIconKey, null, selectedColorArgb)
                    } else {
                        onApply(null, customText.trim().takeIf { it.isNotEmpty() }, selectedColorArgb)
                    }
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.action_confirm),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showCustomColorPicker) {
        ColorPickerDialog(
            initialColorArgb = selectedColorArgb,
            onDismiss = { showCustomColorPicker = false },
            onConfirm = { picked ->
                selectedColorArgb = picked
                showCustomColorPicker = false
            },
            confirmTextRes = R.string.action_confirm
        )
    }
}

@Composable
private fun AddCustomColorOption(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = stringResource(R.string.color_picker_title),
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ColorCircleOption(
    argb: Long,
    selected: Boolean,
    onClick: () -> Unit
) {
    val swatchColor = Color(argb)
    val checkTint = if (swatchColor.luminance() > 0.55f) Color(0xFF111413) else Color.White
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .border(
                width = if (selected) 2.5.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .padding(3.dp)
            .clip(CircleShape)
            .background(swatchColor)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = checkTint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
