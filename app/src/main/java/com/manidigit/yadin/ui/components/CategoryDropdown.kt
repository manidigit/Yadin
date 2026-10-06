package com.manidigit.yadin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    label: String = "دسته‌بندی موضوعی"
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    var expanded by remember { mutableStateOf(false) }

    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
    val displayText = selectedCategory?.name ?: "همه دسته‌ها"
    val icon = getCategoryIcon(selectedCategory?.name)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
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
                                contentDescription = "حذف انتخاب",
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
                        Text("همه دسته‌ها", fontWeight = if (selectedCategoryId == null) FontWeight.Bold else FontWeight.Normal)
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
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        onSelectCategory(cat.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
