package com.darkxwn.payping.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.darkxwn.payping.R
import com.darkxwn.payping.domain.model.AppLanguage
import com.darkxwn.payping.domain.model.CategoryCatalog
import com.darkxwn.payping.domain.model.LiquidGlassLevel
import com.darkxwn.payping.domain.model.PaymentCategory
import com.darkxwn.payping.ui.components.CategoryBadgeAvatar
import com.darkxwn.payping.ui.components.CategoryEditorDialog
import com.darkxwn.payping.ui.components.PayPingSubmenuScreen
import com.darkxwn.payping.ui.components.resolveSystemLanguageTag

private sealed interface CustomCategoryGridItem {
    data class Category(val value: PaymentCategory) : CustomCategoryGridItem
    data object CreateTile : CustomCategoryGridItem
}

@Composable
fun CategoriesScreen(
    customCategories: List<PaymentCategory>,
    language: AppLanguage,
    liquidGlassLevel: LiquidGlassLevel,
    onSaveCategory: (PaymentCategory) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val langTag = remember(language) {
        language.resolvedLocaleTag(resolveSystemLanguageTag())
    }
    val builtInCategories = remember(langTag) {
        CategoryCatalog.builtInCategories(langTag)
    }

    var dialogCategory by remember { mutableStateOf<PaymentCategory?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    PayPingSubmenuScreen(
        title = stringResource(R.string.settings_categories),
        liquidGlassLevel = liquidGlassLevel,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = stringResource(R.string.categories_builtin_section),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                builtInCategories.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { category ->
                            BuiltInCategoryTile(
                                category = category,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.categories_custom_section),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            val customGridItems = remember(customCategories) {
                customCategories.map { CustomCategoryGridItem.Category(it) } + CustomCategoryGridItem.CreateTile
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                customGridItems.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { cell ->
                            when (cell) {
                                is CustomCategoryGridItem.Category -> CustomCategoryTile(
                                    category = cell.value,
                                    onClick = {
                                        dialogCategory = cell.value
                                        showDialog = true
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                                CustomCategoryGridItem.CreateTile -> CreateCategoryTile(
                                    onClick = {
                                        dialogCategory = null
                                        showDialog = true
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CategoryEditorDialog(
            existingCategory = dialogCategory,
            onDismiss = { showDialog = false },
            onSave = { saved ->
                showDialog = false
                onSaveCategory(saved)
            },
            onDelete = { uid ->
                showDialog = false
                onDeleteCategory(uid)
            }
        )
    }
}

@Composable
private fun BuiltInCategoryTile(
    category: PaymentCategory,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryBadgeAvatar(
                iconKey = category.iconKey,
                colorArgb = category.colorArgb,
                size = 40.dp
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CustomCategoryTile(
    category: PaymentCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryBadgeAvatar(
                iconKey = category.iconKey,
                colorArgb = category.colorArgb,
                size = 40.dp
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CreateCategoryTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.65f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.action_create_category),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
