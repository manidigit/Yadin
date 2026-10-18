package com.manidigit.yadin.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.domain.algorithm.VocabularyParser
import com.manidigit.yadin.domain.model.ParsedEntry
import com.manidigit.yadin.domain.text.TextUtilities
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.components.YadinScreenHeader
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.util.formatDigits

data class ReviewItem(
    val id: String,
    var sourceText: String,
    var translationText: String,
    var noteText: String = "",
    val reason: String,
    val confidence: Int
)

@Composable
fun ImportReviewScreen(
    itemsToReview: List<ReviewItem>,
    onApproveItems: (List<ParsedEntry>) -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    val pendingList = remember { mutableStateListOf<ReviewItem>().apply { addAll(itemsToReview) } }
    val approvedList = remember { mutableStateListOf<ParsedEntry>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(dimensions.screenPadding)
    ) {
        // Standardized Header
        YadinScreenHeader(
            title = stringResource(R.string.import_review_title),
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (pendingList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (approvedList.isNotEmpty())
                            stringResource(R.string.import_review_all_done, approvedList.size.formatDigits())
                        else
                            stringResource(R.string.import_review_queue_empty),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingList, key = { it.id }) { item ->
                    ReviewItemCard(
                        item = item,
                        onApprove = { approved ->
                            approvedList.add(
                                ParsedEntry(
                                    sourceText = approved.sourceText,
                                    translations = TextUtilities.splitTranslations(approved.translationText),
                                    note = approved.noteText.ifBlank { null }
                                )
                            )
                            pendingList.remove(item)
                        },
                        onReject = {
                            pendingList.remove(item)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(dimensions.cornerMedium)
            ) {
                Text(stringResource(R.string.action_cancel))
            }

            Button(
                onClick = {
                    val remainingEntries = pendingList.map {
                        ParsedEntry(
                            sourceText = it.sourceText,
                            translations = TextUtilities.splitTranslations(it.translationText),
                            note = it.noteText.ifBlank { null }
                        )
                    }
                    val finalApproved = approvedList + remainingEntries
                    onApproveItems(finalApproved)
                },
                enabled = approvedList.isNotEmpty() || pendingList.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(dimensions.cornerMedium),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                val totalApprovedCount = approvedList.size + pendingList.size
                Text(
                    text = if (totalApprovedCount > 0)
                        stringResource(R.string.import_review_btn_confirm, totalApprovedCount.formatDigits())
                    else
                        stringResource(R.string.action_save)
                )
            }
        }
    }
}

@Composable
private fun ReviewItemCard(
    item: ReviewItem,
    onApprove: (ReviewItem) -> Unit,
    onReject: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    var source by remember { mutableStateOf(item.sourceText) }
    var translation by remember { mutableStateOf(item.translationText) }
    var note by remember { mutableStateOf(item.noteText) }

    YadinCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surface,
        shape = RoundedCornerShape(dimensions.cornerMedium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (item.confidence < 80) colors.error else colors.warning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.reason,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (item.confidence < 80) colors.error else colors.warning
                    )
                }
                Text(
                    text = stringResource(R.string.import_review_confidence, item.confidence.formatDigits()),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = source,
                onValueChange = {
                    source = it
                    item.sourceText = it
                },
                label = { Text(stringResource(R.string.import_review_source_field)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
                singleLine = true
            )

            OutlinedTextField(
                value = translation,
                onValueChange = {
                    translation = it
                    item.translationText = it
                },
                label = { Text(stringResource(R.string.import_review_target_field)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl),
                singleLine = true
            )

            OutlinedTextField(
                value = note,
                onValueChange = {
                    note = it
                    item.noteText = it
                },
                label = { Text(stringResource(R.string.import_review_note_label)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl),
                singleLine = false,
                maxLines = 3
            )

            // Quick button to move extra parts from source to note if needed
            if (VocabularyParser.hasPersian(source) || source.contains("\n") || source.contains("(") || source.contains(":")) {
                TextButton(
                    onClick = {
                        val pair = VocabularyParser.splitPair(source)
                        if (pair != null) {
                            source = pair.first.trim()
                            item.sourceText = source
                            val extra = pair.second.trim()
                            if (VocabularyParser.hasPersian(extra)) {
                                translation = if (translation.isBlank()) extra else "$translation، $extra"
                                item.translationText = translation
                            } else {
                                note = if (note.isBlank()) extra else "$note\n$extra"
                                item.noteText = note
                            }
                        } else {
                            val paren = Regex("""^(.*?)\s*[\(\[（]([^\)\]）]+)[\)\]）]\s*$""").find(source)
                            if (paren != null) {
                                val main = paren.groupValues[1].trim()
                                val inside = paren.groupValues[2].trim()
                                source = main
                                item.sourceText = source
                                note = if (note.isBlank()) inside else "$note\n$inside"
                                item.noteText = note
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.import_review_move_to_note), fontSize = 12.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onReject) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.import_review_desc_reject),
                        tint = colors.error
                    )
                }
                IconButton(onClick = { onApprove(item) }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.import_review_desc_approve),
                        tint = colors.primary
                    )
                }
            }
        }
    }
}
