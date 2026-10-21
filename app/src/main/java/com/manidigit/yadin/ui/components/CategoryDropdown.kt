package com.manidigit.yadin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.domain.model.Category
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.YadinIcons

fun getCategoryIcon(categoryName: String?): ImageVector = YadinIcons.forCategoryName(categoryName)

@Composable
fun ExposedCategoryDropdown(
    categories: List<Category>,
    selectedCategoryId: String?,
    onSelectCategory: (String?) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    onAddNewCategory: ((String) -> Unit)? = null,
    onEditCategory: ((String, String) -> Unit)? = null,
    onDeleteCategory: ((String) -> Unit)? = null
) {
    val colors = LocalYadinColors.current
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newCategoryText by remember { mutableStateOf("") }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var editCategoryText by remember { mutableStateOf("") }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    val resolvedLabel = label ?: stringResource(R.string.category_label)
    val defaultAllText = stringResource(R.string.category_all_general)
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
    val displayText = selectedCategory?.name ?: defaultAllText
    val icon = getCategoryIcon(selectedCategory?.name)
    val isFilterActive = selectedCategoryId != null

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.category_create_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.category_create_hint_prompt),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newCategoryText,
                        onValueChange = { newCategoryText = it },
                        label = { Text(stringResource(R.string.category_title_field)) },
                        placeholder = { Text(stringResource(R.string.category_title_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newCategoryText.trim()
                        if (name.isNotBlank()) {
                            onAddNewCategory?.invoke(name)
                            showAddDialog = false
                            newCategoryText = ""
                        }
                    },
                    enabled = newCategoryText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.category_submit_select), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (categoryToEdit != null) {
        AlertDialog(
            onDismissRequest = { categoryToEdit = null },
            title = { Text(stringResource(R.string.category_edit_title), fontWeight = FontWeight.Bold, color = colors.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editCategoryText,
                        onValueChange = { editCategoryText = it },
                        label = { Text(stringResource(R.string.category_edit_new_title)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = editCategoryText.trim()
                        categoryToEdit?.let { cat ->
                            if (name.isNotBlank()) {
                                onEditCategory?.invoke(cat.id, name)
                            }
                        }
                        categoryToEdit = null
                    },
                    enabled = editCategoryText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { categoryToEdit = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text(stringResource(R.string.category_delete_title), fontWeight = FontWeight.Bold, color = colors.onSurface) },
            text = {
                Text(stringResource(R.string.category_delete_confirm_msg, categoryToDelete?.name ?: ""))
            },
            confirmButton = {
                Button(
                    onClick = {
                        categoryToDelete?.let { cat ->
                            onDeleteCategory?.invoke(cat.id)
                        }
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error)
                ) {
                    Text(stringResource(R.string.category_btn_delete), fontWeight = FontWeight.Bold, color = colors.onError)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { categoryToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    val shape = RoundedCornerShape(12.dp)
    val bgColor = if (isFilterActive) colors.primary.copy(alpha = 0.10f) else colors.surface
    val borderColor = if (isFilterActive) colors.primary.copy(alpha = 0.55f) else colors.outline.copy(alpha = 0.25f)
    val accentColor = if (isFilterActive) colors.primary else colors.onSurfaceVariant

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(shape)
                .background(bgColor)
                .border(if (isFilterActive) 1.2.dp else 1.dp, borderColor, shape)
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resolvedLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isFilterActive) colors.primary else colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.5.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (isFilterActive) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSelectCategory(null) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = YadinIcons.clear,
                            contentDescription = stringResource(R.string.category_clear_selection),
                            tint = colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Icon(
                    imageVector = YadinIcons.dropdown,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.surface)
        ) {
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = YadinIcons.categoryDefault,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = defaultAllText,
                            fontWeight = if (selectedCategoryId == null) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedCategoryId == null) colors.primary else colors.onSurface
                        )
                    }
                },
                onClick = {
                    onSelectCategory(null)
                    expanded = false
                }
            )

            categories.forEach { cat ->
                val catIcon = getCategoryIcon(cat.name)
                val isSelected = cat.id == selectedCategoryId
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = catIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) colors.primary else colors.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = cat.name,
                                    color = if (isSelected) colors.primary else colors.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (!cat.isDefault && onEditCategory != null) {
                                    IconButton(
                                        onClick = {
                                            categoryToEdit = cat
                                            editCategoryText = cat.name
                                            expanded = false
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = YadinIcons.edit,
                                            contentDescription = stringResource(R.string.category_btn_edit),
                                            tint = colors.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                if (!cat.isDefault && onDeleteCategory != null) {
                                    IconButton(
                                        onClick = {
                                            categoryToDelete = cat
                                            expanded = false
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = YadinIcons.delete,
                                            contentDescription = stringResource(R.string.category_btn_delete),
                                            tint = colors.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = YadinIcons.check,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    },
                    onClick = {
                        onSelectCategory(cat.id)
                        expanded = false
                    }
                )
            }

            if (onAddNewCategory != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = YadinIcons.add,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.category_add_new_action),
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        showAddDialog = true
                    }
                )
            }
        }
    }
}

@Composable
fun ExposedEntryTypeDropdown(
    selectedEntryType: com.manidigit.yadin.domain.model.EntryType?,
    onSelectEntryType: (com.manidigit.yadin.domain.model.EntryType?) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    val colors = LocalYadinColors.current
    var expanded by remember { mutableStateOf(false) }

    val resolvedLabel = label ?: stringResource(R.string.review_setup_entry_type_label)
    val defaultAllText = stringResource(R.string.entry_type_all)

    val entryOptions = listOf(
        Triple(
            com.manidigit.yadin.domain.model.EntryType.WORD,
            stringResource(R.string.entry_type_word),
            YadinIcons.categoryBook
        ),
        Triple(
            com.manidigit.yadin.domain.model.EntryType.PHRASE,
            stringResource(R.string.entry_type_phrase),
            YadinIcons.hintLightbulb
        ),
        Triple(
            com.manidigit.yadin.domain.model.EntryType.SENTENCE,
            stringResource(R.string.entry_type_sentence),
            YadinIcons.contactSupport
        )
    )

    val selectedOption = entryOptions.firstOrNull { it.first == selectedEntryType }
    val displayText = selectedOption?.second ?: defaultAllText
    val icon = selectedOption?.third ?: YadinIcons.vocabTotal
    val isFilterActive = selectedEntryType != null

    val shape = RoundedCornerShape(12.dp)
    val bgColor = if (isFilterActive) colors.primary.copy(alpha = 0.10f) else colors.surface
    val borderColor = if (isFilterActive) colors.primary.copy(alpha = 0.55f) else colors.outline.copy(alpha = 0.25f)
    val accentColor = if (isFilterActive) colors.primary else colors.onSurfaceVariant

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(shape)
                .background(bgColor)
                .border(if (isFilterActive) 1.2.dp else 1.dp, borderColor, shape)
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resolvedLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isFilterActive) colors.primary else colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.5.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (isFilterActive) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSelectEntryType(null) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = YadinIcons.clear,
                            contentDescription = stringResource(R.string.category_clear_selection),
                            tint = colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Icon(
                    imageVector = YadinIcons.dropdown,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.surface)
        ) {
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = YadinIcons.vocabTotal,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = defaultAllText,
                            fontWeight = if (selectedEntryType == null) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedEntryType == null) colors.primary else colors.onSurface
                        )
                    }
                },
                onClick = {
                    onSelectEntryType(null)
                    expanded = false
                }
            )

            entryOptions.forEach { (type, typeLabel, typeIcon) ->
                val isSelected = type == selectedEntryType
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = typeIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) colors.primary else colors.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = typeLabel,
                                    color = if (isSelected) colors.primary else colors.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = YadinIcons.check,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        onSelectEntryType(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

