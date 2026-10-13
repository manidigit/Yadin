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
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.domain.model.ParsedEntry
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions

data class ReviewItem(
    val id: String,
    var sourceText: String,
    var translationText: String,
    val confidence: Int,
    val reason: String
)

@Composable
fun ImportReviewScreen(
    itemsToReview: List<ReviewItem>,
    onApproveItems: (List<ParsedEntry>) -> Unit,
    onBack: () -> Unit
) {
    androidx.activity.compose.BackHandler(onBack = onBack)

    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    val pendingList = remember { mutableStateListOf(*itemsToReview.toTypedArray()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
            Text(
                text = "بررسی و اصلاح ورودی‌ها",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.width(38.dp))
        }

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
                        text = "هیچ واژه نیاز به بررسی در صف وجود ندارد",
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
                Text("انصراف")
            }

            Button(
                onClick = {
                    val approvedEntries = pendingList.map {
                        ParsedEntry(
                            sourceText = it.sourceText,
                            translations = listOf(it.translationText),
                            note = null
                        )
                    }
                    onApproveItems(approvedEntries)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(dimensions.cornerMedium),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("تایید همه و ثبت")
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

    YadinCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surface,
        shape = RoundedCornerShape(dimensions.cornerMedium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = colors.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.reason,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.error
                    )
                }
                Text(
                    text = "اطمینان: ${item.confidence}%",
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
                label = { Text("واژه / عبارت مبدأ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = translation,
                onValueChange = {
                    translation = it
                    item.translationText = it
                },
                label = { Text("ترجمه مقصد") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onReject) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "رد",
                        tint = colors.error
                    )
                }
                IconButton(onClick = { onApprove(item) }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "تایید",
                        tint = colors.primary
                    )
                }
            }
        }
    }
}
