package com.manidigit.yadin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manidigit.yadin.R
import com.manidigit.yadin.domain.model.Category
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions

fun getCategoryIcon(categoryName: String?): ImageVector {
    if (categoryName == null) return Icons.Default.Category
    val lower = categoryName.lowercase()
    return when {
        lower.contains("سفر") || lower.contains("گردش") || lower.contains("flight") || lower.contains("travel") -> Icons.Default.Flight
        lower.contains("غذا") || lower.contains("خوراک") || lower.contains("نوشیدنی") || lower.contains("food") -> Icons.Default.Restaurant
        lower.contains("مکالمه") || lower.contains("احوال") || lower.contains("گفتگو") || lower.contains("chat") -> Icons.AutoMirrored.Filled.Chat
        lower.contains("اصطلاح") || lower.contains("ضرب") || lower.contains("idiom") -> Icons.Default.Lightbulb
        lower.contains("گرامر") || lower.contains("قواعد") || lower.contains("دستور") || lower.contains("grammar") -> Icons.Default.Description
        lower.contains("فعل") || lower.contains("افعال") || lower.contains("verb") -> Icons.Default.Bolt
        lower.contains("کار") || lower.contains("شغل") || lower.contains("کسب") || lower.contains("business") -> Icons.Default.Work
        lower.contains("طبیعت") || lower.contains("آب") || lower.contains("محیط") || lower.contains("nature") -> Icons.Default.Eco
        lower.contains("خانه") || lower.contains("منزل") || lower.contains("خانواده") || lower.contains("home") -> Icons.Default.Home
        lower.contains("خرید") || lower.contains("پوشاک") || lower.contains("لباس") || lower.contains("shop") -> Icons.Default.ShoppingBag
        lower.contains("هنر") || lower.contains("فرهنگ") || lower.contains("art") -> Icons.Default.Palette
        else -> Icons.Default.Category
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val dimensions = LocalYadinDimensions.current
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

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(resolvedLabel) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedCategoryId != null) {
                        IconButton(onClick = { onSelectCategory(null) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.category_clear_selection),
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            shape = RoundedCornerShape(dimensions.cornerMedium),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
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
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(defaultAllText, fontWeight = if (selectedCategoryId == null) FontWeight.Bold else FontWeight.Normal)
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
                                            imageVector = Icons.Default.Edit,
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
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.category_btn_delete),
                                            tint = colors.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
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
                                imageVector = Icons.Default.Add,
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
