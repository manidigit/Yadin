package com.manidigit.yadin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.domain.model.Category
import com.manidigit.yadin.domain.model.DuplicatePolicy
import com.manidigit.yadin.domain.model.ParseResult
import com.manidigit.yadin.domain.model.ParsedEntry
import com.manidigit.yadin.ui.components.ExposedCategoryDropdown
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions

@Composable
fun ImportScreen(
    parseResult: ParseResult?,
    categories: List<Category> = emptyList(),
    isImporting: Boolean,
    importProgress: Float,
    onParseText: (String) -> Unit,
    onResetParse: (() -> Unit)? = null,
    onAddNewCategory: ((String, (String) -> Unit) -> Unit)? = null,
    onConfirmImport: (DuplicatePolicy, String?) -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    var inputText by remember {
        mutableStateOf(
            """
            # نمونه واژگان برای ورود
            amigo: دوست (مذکر)
            casa: خانه، منزل
            libro: کتاب
            feliz: خوشحال، شاد
            """.trimIndent()
        )
    }
    var selectedPolicy by remember { mutableStateOf(DuplicatePolicy.MERGE) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = {
                        if (parseResult != null && onResetParse != null) {
                            onResetParse()
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = colors.onSurface
                    )
                }

                Text(
                    text = "ورود متنی واژگان (پارسر هوشمند)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }

            if (parseResult == null) {
                // Input area
                Text(
                    text = "متن واژگان را در کادر زیر وارد کنید (پشتیبانی از قالب‌های دو نقطه، خط تیره، تب و چند ترجمه‌ای):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )

                // Category selection BEFORE parsing
                ExposedCategoryDropdown(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onSelectCategory = { selectedCategoryId = it },
                    onAddNewCategory = { newName ->
                        onAddNewCategory?.invoke(newName) { newId ->
                            selectedCategoryId = newId
                        }
                    },
                    label = "دسته‌بندی موضوعی برای این واژگان (اختیاری)"
                )

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = { Text("کلمه: ترجمه1، ترجمه2") },
                    shape = RoundedCornerShape(dimensions.cornerMedium)
                )

                Button(
                    onClick = { onParseText(inputText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(dimensions.cornerMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("پردازش و استخراج واژگان", fontWeight = FontWeight.Bold)
                }
            } else {
                // Preview parsed entries
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "پیش‌نمایش استخراج (${parseResult.entries.size} مدخل)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    if (onResetParse != null) {
                        OutlinedButton(
                            onClick = onResetParse,
                            shape = RoundedCornerShape(dimensions.cornerSmall)
                        ) {
                            Text("ویرایش مجدد متن", fontSize = 11.sp)
                        }
                    }
                }

                // Category selection for imported words
                ExposedCategoryDropdown(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onSelectCategory = { selectedCategoryId = it },
                    onAddNewCategory = { newName ->
                        onAddNewCategory?.invoke(newName) { newId ->
                            selectedCategoryId = newId
                        }
                    },
                    label = "دسته‌بندی موضوعی واژگان واردشده (اختیاری)"
                )

                // Duplicate policy selection
                Text(
                    text = "سیاست برخورد با واژگان تکراری:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PolicyChip(
                        label = "ادغام ترجمه‌ها (MERGE)",
                        isSelected = selectedPolicy == DuplicatePolicy.MERGE,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedPolicy = DuplicatePolicy.MERGE }
                    )
                    PolicyChip(
                        label = "صرف‌نظر (SKIP)",
                        isSelected = selectedPolicy == DuplicatePolicy.SKIP,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedPolicy = DuplicatePolicy.SKIP }
                    )
                    PolicyChip(
                        label = "جایگزینی (REPLACE)",
                        isSelected = selectedPolicy == DuplicatePolicy.REPLACE,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedPolicy = DuplicatePolicy.REPLACE }
                    )
                }

                // Parsed entries list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(parseResult.entries) { entry ->
                        ParsedEntryCard(
                            entry = entry,
                            categoryName = selectedCategory?.name
                        )
                    }
                }

                if (isImporting) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(
                            progress = { importProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = colors.primary,
                            trackColor = colors.surfaceVariant
                        )
                        Text(
                            text = "در حال ذخیره‌سازی... ${(importProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.primary
                        )
                    }
                } else {
                    Button(
                        onClick = { onConfirmImport(selectedPolicy, selectedCategoryId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(dimensions.cornerMedium),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.success,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تأیید و ذخیره در پایگاه داده", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PolicyChip(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.cornerSmall))
            .background(if (isSelected) colors.primary.copy(alpha = 0.15f) else colors.surface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(dimensions.cornerSmall)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) colors.primary else colors.onSurface
        )
    }
}

@Composable
fun ParsedEntryCard(
    entry: ParsedEntry,
    categoryName: String? = null
) {
    val colors = LocalYadinColors.current

    YadinCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = entry.sourceText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    if (!categoryName.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = categoryName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primary
                            )
                        }
                    }
                }
                Text(
                    text = entry.translations.joinToString("، "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.primary
                )
                if (!entry.note.isNullOrBlank()) {
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${(entry.confidence * 100).toInt()}% اطمینان",
                    fontSize = 10.sp,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}
