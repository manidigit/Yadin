package com.manidigit.yadin.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.domain.model.Category
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
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onStageFilterChange: (Stage?) -> Unit,
    onSelectWord: (String) -> Unit,
    onAddWord: () -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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

                Column {
                    Text(
                        text = "کتابخانه واژگان",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "${words.size} واژه و عبارت با پرچم زبان و ترجمه",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
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

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips (Stage)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        label = "همه مراحل",
                        isSelected = selectedStage == null,
                        onClick = { onStageFilterChange(null) }
                    )
                }
                Stage.values().forEach { st ->
                    val name = when (st) {
                        Stage.DAILY -> "روزانه"
                        Stage.WEEKLY -> "هفتگی"
                        Stage.MONTHLY -> "ماهانه"
                        Stage.LEARNED -> "یادگرفته"
                    }
                    item {
                        FilterChip(
                            label = name,
                            isSelected = selectedStage == st,
                            onClick = { onStageFilterChange(if (selectedStage == st) null else st) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dropdown Menu for Categories
            if (categories.isNotEmpty()) {
                ExposedCategoryDropdown(
                    categories = categories,
                    selectedCategoryId = selectedCategory,
                    onSelectCategory = { onCategoryFilterChange(it) },
                    label = "دسته‌بندی موضوعی کلمات"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Word List
            if (words.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ واژه‌ای با این مشخصات یافت نشد",
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
                            onClick = { onSelectWord(word.concept.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(76.dp))
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddWord,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "افزودن واژه جدید")
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.primary else colors.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) colors.onPrimary else colors.onSurface,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
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
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current

    val sourceFlag = getLanguageFlag(word.sourceContent.languageCode)
    val targetFlag = "🇮🇷"

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
                        word.normalLearning?.stage?.let { StageBadge(stage = it) }
                        word.normalDifficulty?.current?.let { DifficultyBadge(difficulty = it) }
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                SpeakButton(text = word.sourceContent.text, languageCode = word.sourceContent.languageCode)
            }
        }
    }
}
