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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.domain.model.AuditActionDetail
import com.manidigit.yadin.domain.model.AuditActionType
import com.manidigit.yadin.domain.model.Category
import com.manidigit.yadin.domain.model.LibraryAuditReport
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.WordDetail
import com.manidigit.yadin.ui.components.DifficultyBadge
import com.manidigit.yadin.ui.components.ExposedCategoryDropdown
import com.manidigit.yadin.ui.components.SpeakButton
import com.manidigit.yadin.ui.components.StageBadge
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions

@Composable
fun LibraryScreen(
    words: List<WordDetail>,
    searchQuery: String,
    categories: List<Category>,
    selectedCategory: String?,
    selectedStage: Stage?,
    selectedDifficulty: com.manidigit.yadin.domain.model.VocabularyDifficulty? = null,
    showOnlyInactive: Boolean = false,
    hasMoreResults: Boolean = false,
    isLoadingMore: Boolean = false,
    appLanguageDirection: com.manidigit.yadin.domain.model.CardDirection = com.manidigit.yadin.domain.model.CardDirection.NORMAL,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onStageFilterChange: (Stage?) -> Unit,
    onDifficultyFilterChange: (com.manidigit.yadin.domain.model.VocabularyDifficulty?) -> Unit = {},
    onInactiveFilterToggle: (Boolean) -> Unit = {},
    onLoadMore: () -> Unit = {},
    onReactivateWord: ((String) -> Unit)? = null,
    onAddNewCategory: ((String) -> Unit)? = null,
    onEditCategory: ((String, String) -> Unit)? = null,
    onDeleteCategory: ((String) -> Unit)? = null,
    onSelectWord: (String) -> Unit,
    onAddWord: () -> Unit,
    onBatchImport: () -> Unit = {},
    isAuditing: Boolean = false,
    auditProgress: Float = 0f,
    auditReport: LibraryAuditReport? = null,
    onRunAuditAndMerge: () -> Unit = {},
    onClearAuditReport: () -> Unit = {},
    onBack: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    var showAddOptionsDialog by remember { mutableStateOf(false) }
    var showAuditDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensions.screenPadding)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onBack,
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (showOnlyInactive) "سطل بازیافت (واژگان غیرفعال)" else "کتابخانه واژگان",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "${words.size} واژه بارگذاری‌شده (اسکرول نامحدود)",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }

                if (!showOnlyInactive) {
                    FilledTonalButton(
                        onClick = { showAuditDialog = true },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "پالایش و ادغام هوشمند",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "پالایش و ادغام",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("جستجو در واژه‌ها، ترجمه و یادداشت...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = colors.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "پاک کردن", tint = colors.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(dimensions.cornerMedium)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3-Combobox Compact Filter Toolbar (وضعیت، مرحله مرور، سطح سختی)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Combobox 1: وضعیت (فعال / سطل زباله)
                LibraryFilterDropdown(
                    label = "وضعیت",
                    selectedOption = if (showOnlyInactive) "سطل زباله" else "واژگان فعال",
                    options = listOf(
                        "واژگان فعال" to { onInactiveFilterToggle(false) },
                        "سطل زباله" to { onInactiveFilterToggle(true) }
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Combobox 2: مرحله مرور (لایتنر)
                val currentStageLabel = when (selectedStage) {
                    Stage.DAILY -> "روزانه"
                    Stage.WEEKLY -> "هفتگی"
                    Stage.MONTHLY -> "ماهانه"
                    Stage.LEARNED -> "تثبیت‌شده"
                    null -> "همه مراحل"
                }
                LibraryFilterDropdown(
                    label = "مرحله مرور",
                    selectedOption = currentStageLabel,
                    options = listOf(
                        "همه مراحل" to { onStageFilterChange(null) },
                        "روزانه" to { onStageFilterChange(Stage.DAILY) },
                        "هفتگی" to { onStageFilterChange(Stage.WEEKLY) },
                        "ماهانه" to { onStageFilterChange(Stage.MONTHLY) },
                        "تثبیت‌شده" to { onStageFilterChange(Stage.LEARNED) }
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Combobox 3: سطح سختی
                val currentDiffLabel = when (selectedDifficulty) {
                    com.manidigit.yadin.domain.model.VocabularyDifficulty.EASY -> "آسان"
                    com.manidigit.yadin.domain.model.VocabularyDifficulty.MEDIUM -> "متوسط"
                    com.manidigit.yadin.domain.model.VocabularyDifficulty.HARD -> "سخت"
                    com.manidigit.yadin.domain.model.VocabularyDifficulty.VERY_HARD -> "خیلی سخت"
                    null -> "همه سختی‌ها"
                }
                LibraryFilterDropdown(
                    label = "سطح سختی",
                    selectedOption = currentDiffLabel,
                    options = listOf(
                        "همه سختی‌ها" to { onDifficultyFilterChange(null) },
                        "آسان" to { onDifficultyFilterChange(com.manidigit.yadin.domain.model.VocabularyDifficulty.EASY) },
                        "متوسط" to { onDifficultyFilterChange(com.manidigit.yadin.domain.model.VocabularyDifficulty.MEDIUM) },
                        "سخت" to { onDifficultyFilterChange(com.manidigit.yadin.domain.model.VocabularyDifficulty.HARD) },
                        "خیلی سخت" to { onDifficultyFilterChange(com.manidigit.yadin.domain.model.VocabularyDifficulty.VERY_HARD) }
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dropdown Menu for Categories
            if (categories.isNotEmpty()) {
                ExposedCategoryDropdown(
                    categories = categories,
                    selectedCategoryId = selectedCategory,
                    onSelectCategory = { onCategoryFilterChange(it) },
                    label = "دسته‌بندی موضوعی کلمات",
                    onAddNewCategory = onAddNewCategory,
                    onEditCategory = onEditCategory,
                    onDeleteCategory = onDeleteCategory
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Word List
            if (words.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showOnlyInactive) "هیچ واژه غیرفعالی در سطل بازیافت وجود ندارد" else "هیچ واژه‌ای با این مشخصات یافت نشد",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(words, key = { it.concept.id }) { word ->
                        WordItemCard(
                            word = word,
                            direction = appLanguageDirection,
                            onClick = { onSelectWord(word.concept.id) },
                            onReactivate = if (!word.concept.active && onReactivateWord != null) {
                                { onReactivateWord(word.concept.id) }
                            } else null
                        )
                    }

                    if (hasMoreResults) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoadingMore) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = colors.primary,
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    androidx.compose.material3.OutlinedButton(
                                        onClick = onLoadMore,
                                        shape = RoundedCornerShape(dimensions.cornerMedium)
                                    ) {
                                        Text("بارگذاری موارد بیشتر...")
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(76.dp))
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddOptionsDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "افزودن واژه جدید")
        }

        // دیالوگ انتخاب شیوه افزودن واژه (تکی یا گروهی)
        if (showAddOptionsDialog) {
            AddWordChoiceDialog(
                onDismiss = { showAddOptionsDialog = false },
                onAddSingle = {
                    showAddOptionsDialog = false
                    onAddWord()
                },
                onAddBatch = {
                    showAddOptionsDialog = false
                    onBatchImport()
                }
            )
        }

        // دیالوگ بررسی، پالایش، ادغام واژگان تکراری و رفرش
        if (showAuditDialog) {
            LibraryAuditDialog(
                isAuditing = isAuditing,
                auditProgress = auditProgress,
                auditReport = auditReport,
                onDismiss = {
                    showAuditDialog = false
                    onClearAuditReport()
                },
                onRunAudit = onRunAuditAndMerge
            )
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    indicatorColor: Color? = null
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    val shape = RoundedCornerShape(dimensions.cornerSmall)

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (isSelected) colors.primary else colors.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = if (isSelected) colors.primary else colors.outline.copy(alpha = 0.25f),
                shape = shape
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (indicatorColor != null) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }
            Text(
                text = label,
                color = if (isSelected) colors.onPrimary else colors.onSurface,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

private fun getLanguageFlag(langCode: String?): String {
    return when (langCode?.lowercase()) {
        "es", "spa", "spanish" -> "🇪🇸"
        "fa", "fas", "per", "persian" -> "🇮🇷"
        "en", "eng", "english" -> "🇬🇧"
        "fr", "fra", "french" -> "🇫🇷"
        "de", "deu", "german" -> "🇩🇪"
        "it", "ita", "italian" -> "🇮🇹"
        "ar", "ara", "arabic" -> "🇸🇦"
        "ru", "rus", "russian" -> "🇷🇺"
        else -> "🇪🇸"
    }
}

@Composable
fun WordItemCard(
    word: WordDetail,
    direction: com.manidigit.yadin.domain.model.CardDirection = com.manidigit.yadin.domain.model.CardDirection.NORMAL,
    onClick: () -> Unit,
    onReactivate: (() -> Unit)? = null
) {
    val colors = LocalYadinColors.current

    val sourceFlag = getLanguageFlag(word.sourceContent.languageCode)
    val targetFlag = "🇮🇷"

    val activeLearning = if (direction == com.manidigit.yadin.domain.model.CardDirection.NORMAL) {
        word.normalLearning
    } else {
        word.reverseLearning
    }

    val activeDifficulty = if (direction == com.manidigit.yadin.domain.model.CardDirection.NORMAL) {
        word.normalDifficulty
    } else {
        word.reverseDifficulty
    }

    YadinCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        backgroundColor = colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Source Row with Flag & Word & Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        // Country flag before source word
                        Text(
                            text = sourceFlag,
                            fontSize = 16.sp
                        )
                        Text(
                            text = word.sourceContent.text,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!word.concept.active) {
                            Text(
                                text = "حذف‌شده",
                                color = colors.error,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.error.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        } else {
                            activeLearning?.stage?.let { StageBadge(stage = it) }
                            activeDifficulty?.current?.let { DifficultyBadge(difficulty = it) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Target Translation with Iranian Flag
                val trans = word.targetContents.joinToString("، ") { it.text }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = targetFlag,
                        fontSize = 14.sp
                    )
                    Text(
                        text = trans.ifEmpty { "بدون ترجمه" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!word.sourceContent.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = word.sourceContent.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!word.concept.active && onReactivate != null) {
                    androidx.compose.material3.TextButton(
                        onClick = onReactivate,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("احیا", color = colors.success, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                SpeakButton(text = word.sourceContent.text, languageCode = word.sourceContent.languageCode)
            }
        }
    }
}

@Composable
fun AddWordChoiceDialog(
    onDismiss: () -> Unit,
    onAddSingle: () -> Unit,
    onAddBatch: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = colors.primary
                )
                Text(
                    text = "افزودن واژه به کتابخانه",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "شیوه افزودن واژگان را انتخاب کنید:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )

                // گزینه ۱: افزودن تکی
                YadinCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimensions.cornerMedium))
                        .clickable { onAddSingle() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "افزودن تکی واژه",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.onSurface
                            )
                            Text(
                                text = "ورود دستی یک لغت با ترجمه‌ها، دسته‌بندی و یادداشت",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // گزینه ۲: افزودن گروهی
                YadinCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimensions.cornerMedium))
                        .clickable { onAddBatch() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.success.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = colors.success,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "افزودن گروهی واژگان",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.onSurface
                            )
                            Text(
                                text = "ورود دسته‌ای از متن کپی‌شده، فایل متنی، اکسل، یا فایل پشتیبان",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

@Composable
fun LibraryAuditDialog(
    isAuditing: Boolean,
    auditProgress: Float,
    auditReport: LibraryAuditReport?,
    onDismiss: () -> Unit,
    onRunAudit: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    AlertDialog(
        onDismissRequest = {
            if (!isAuditing) onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = colors.primary
                )
                Text(
                    text = "بررسی، پالایش و ادغام واژگان",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isAuditing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CircularProgressIndicator(
                            color = colors.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "در حال بررسی واژگان، ادغام تکراری‌ها و پالایش یادداشت‌ها...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (auditProgress > 0f) {
                            LinearProgressIndicator(
                                progress = { auditProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = colors.primary
                            )
                        }
                    }
                } else if (auditReport != null) {
                    // کارت آمار نتیجه
                    YadinCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "گزارش نتیجه پالایش و ادغام:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("کل واژگان بررسی‌شده:", fontSize = 12.sp, color = colors.onSurfaceVariant)
                                Text("${auditReport.totalScanned} واژه", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("واژگان تکراری ادغام‌شده:", fontSize = 12.sp, color = colors.onSurfaceVariant)
                                Text("${auditReport.mergedCount} مورد", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("واژگان پالایش‌شده (پرانتز و علائم):", fontSize = 12.sp, color = colors.onSurfaceVariant)
                                Text("${auditReport.cleanedCount} مورد", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.success)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("تعداد خالص نهایی در کتابخانه:", fontSize = 12.sp, color = colors.onSurfaceVariant)
                                Text("${auditReport.totalRemaining} واژه", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                            }
                        }
                    }

                    if (auditReport.actions.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.success.copy(alpha = 0.12f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.success)
                            Text(
                                text = "تمامی واژگان کتابخانه بررسی شدند و هیچ مورد تکراری یا نیازمند پالایش یافت نشد. کتابخانه کاملاً مرتب و استاندارد است.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface
                            )
                        }
                    } else {
                        Text(
                            text = "جزئیات اصلاحات و ادغام‌های انجام‌شده (${auditReport.actions.size} مورد):",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        auditReport.actions.take(30).forEach { act ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = when (act.actionType) {
                                            AuditActionType.MERGED -> "ادغام تکراری"
                                            AuditActionType.CLEANED -> "پالایش کاراکتر"
                                            AuditActionType.NOTE_EXTRACTED -> "انتقال به یادداشت"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (act.actionType) {
                                            AuditActionType.MERGED -> colors.primary
                                            else -> colors.success
                                        },
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colors.surface)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Text(
                                        text = "${act.originalText} ➔ ${act.cleanedText}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = act.description,
                                    fontSize = 11.sp,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // توضیحات اولیه قبل از اجرا
                    Text(
                        text = "این عملیات تمامی واژگان کتابخانه را به طور هوشمند بررسی و یکپارچه می‌کند:\n\n" +
                                "• شناسایی و ادغام (Merge) کلیه واژه‌های تکراری با حفظ بالاترین مرحله لایتنر و ترکیب تمام ترجمه‌ها\n" +
                                "• انتقال کاراکترهای اضافی، پرانتزها و توضیحات چسبیده به واژه به بخش یادداشت‌ها\n" +
                                "• جداسازی ترجمه‌های چسبیده با علامت‌های دوطرفه (:، -، =)\n" +
                                "• حذف شماره‌گذاری‌های آغازین، نشانه‌ها و گیومه‌های زائد\n" +
                                "• رفرش و به‌روزرسانی کامل پایگاه داده کتابخانه",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        },
        confirmButton = {
            if (!isAuditing) {
                if (auditReport == null) {
                    Button(
                        onClick = onRunAudit,
                        shape = RoundedCornerShape(dimensions.cornerMedium)
                    ) {
                        Text("شروع بررسی و ادغام هوشمند")
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(dimensions.cornerMedium)
                    ) {
                        Text("تأیید و بستن")
                    }
                }
            }
        },
        dismissButton = {
            if (!isAuditing) {
                if (auditReport != null) {
                    TextButton(onClick = onRunAudit) {
                        Text("بررسی مجدد")
                    }
                } else {
                    TextButton(onClick = onDismiss) {
                        Text("انصراف")
                    }
                }
            }
        }
    )
}

@Composable
private fun LibraryFilterDropdown(
    label: String,
    selectedOption: String,
    options: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = LocalYadinColors.current

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surface)
                .border(1.dp, colors.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    fontSize = 9.sp
                )
                Text(
                    text = selectedOption,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.surface)
        ) {
            options.forEach { (title, onSelect) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (title == selectedOption) FontWeight.Bold else FontWeight.Normal,
                            color = if (title == selectedOption) colors.primary else colors.onSurface
                        )
                    },
                    onClick = {
                        onSelect()
                        expanded = false
                    }
                )
            }
        }
    }
}
