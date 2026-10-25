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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import com.manidigit.yadin.ui.theme.YadinIcons
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.Category
import com.manidigit.yadin.domain.model.WordDetail
import com.manidigit.yadin.domain.text.TextUtilities
import com.manidigit.yadin.ui.components.YadinScreenHeader
import com.manidigit.yadin.ui.components.YadinSelectableChip
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions

@Composable
fun AddEditWordScreen(
    initialWord: WordDetail? = null,
    categories: List<Category>,
    languageDirection: CardDirection = CardDirection.NORMAL,
    onSave: (sourceText: String, translations: List<String>, categoryId: String?, note: String?, categoryIds: List<String>, onError: (String) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    val isNormal = (languageDirection == CardDirection.NORMAL)

    var sourceText by remember(initialWord, languageDirection) {
        mutableStateOf(
            if (isNormal) {
                initialWord?.sourceContent?.text ?: ""
            } else {
                initialWord?.targetContents?.joinToString("، ") { it.text } ?: ""
            }
        )
    }
    var translationsText by remember(initialWord, languageDirection) {
        mutableStateOf(
            if (isNormal) {
                initialWord?.targetContents?.joinToString("، ") { it.text } ?: ""
            } else {
                initialWord?.sourceContent?.text ?: ""
            }
        )
    }
    var note by remember(initialWord, languageDirection) {
        mutableStateOf(
            if (isNormal) {
                initialWord?.sourceContent?.note ?: ""
            } else {
                initialWord?.targetContents?.firstOrNull { !it.note.isNullOrBlank() }?.note
                    ?: initialWord?.sourceContent?.note ?: ""
            }
        )
    }
    var selectedCategoryIds by remember(initialWord) {
        mutableStateOf(
            if (initialWord != null && initialWord.categories.isNotEmpty()) {
                initialWord.categories.map { it.id }.toSet()
            } else if (initialWord?.concept?.categoryId != null) {
                setOf(initialWord.concept.categoryId)
            } else {
                emptySet()
            }
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            YadinScreenHeader(
                title = if (initialWord != null) stringResource(R.string.word_edit_title) else stringResource(R.string.word_add_title),
                onBack = onBack
            )

            val sourceLabel = if (isNormal) stringResource(R.string.word_source_label_es) else stringResource(R.string.word_source_label_fa)
            val sourcePlaceholder = if (isNormal) stringResource(R.string.word_source_placeholder_es) else stringResource(R.string.word_source_placeholder_fa)
            val targetLabel = if (isNormal) stringResource(R.string.word_target_label_fa) else stringResource(R.string.word_target_label_es)
            val targetPlaceholder = if (isNormal) stringResource(R.string.word_target_placeholder_fa) else stringResource(R.string.word_target_placeholder_es)

            // Source Input
            OutlinedTextField(
                value = sourceText,
                onValueChange = {
                    sourceText = it
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(textDirection = if (isNormal) TextDirection.Ltr else TextDirection.Rtl),
                label = { Text(sourceLabel) },
                placeholder = { Text(sourcePlaceholder) },
                singleLine = true,
                shape = RoundedCornerShape(dimensions.cornerMedium)
            )

            // Translations Input
            OutlinedTextField(
                value = translationsText,
                onValueChange = {
                    translationsText = it
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(textDirection = if (isNormal) TextDirection.Rtl else TextDirection.Ltr),
                label = { Text(targetLabel) },
                placeholder = { Text(targetPlaceholder) },
                shape = RoundedCornerShape(dimensions.cornerMedium)
            )

            // Multi-Category Selection Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.word_categories_label),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories, key = { it.id }) { cat ->
                        val isSelected = selectedCategoryIds.contains(cat.id)
                        YadinSelectableChip(
                            text = cat.name,
                            isSelected = isSelected,
                            accentColor = colors.primary,
                            preserveAccentWhenUnselected = true,
                            fontSize = 12.sp,
                            onClick = {
                                selectedCategoryIds = if (isSelected) {
                                    selectedCategoryIds - cat.id
                                } else {
                                    selectedCategoryIds + cat.id
                                }
                            }
                        )
                    }
                }
            }

            // Note Input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.word_note_label)) },
                placeholder = { Text(stringResource(R.string.word_note_placeholder)) },
                minLines = 2,
                shape = RoundedCornerShape(dimensions.cornerMedium)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = colors.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Save Button
        Button(
            onClick = {
                if (isSaving) return@Button
                val cleanSrc = sourceText.trim()
                val translationsList = if (isNormal) {
                    translationsText.split("،", ",", "\n")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                } else {
                    listOf(translationsText.trim()).filter { it.isNotEmpty() }
                }

                if (cleanSrc.isEmpty()) {
                    errorMessage = context.getString(R.string.word_error_empty_source)
                } else if (translationsList.isEmpty()) {
                    errorMessage = context.getString(R.string.word_error_empty_target)
                } else {
                    isSaving = true
                    errorMessage = null
                    val finalSrc = if (isNormal) TextUtilities.normalizeSpanishPunctuation(cleanSrc).first else cleanSrc
                    val finalTrans = translationsList.map { trans ->
                        if (!isNormal) TextUtilities.normalizeSpanishPunctuation(trans).first else trans
                    }
                    onSave(
                        finalSrc,
                        finalTrans,
                        selectedCategoryIds.firstOrNull(),
                        note.trim().ifEmpty { null },
                        selectedCategoryIds.toList()
                    ) { err ->
                        isSaving = false
                        errorMessage = err
                    }
                }
            },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(dimensions.cornerMedium),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Icon(YadinIcons.save, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.word_save_leitner), fontWeight = FontWeight.Bold)
        }
    }
}
