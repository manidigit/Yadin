package com.manidigit.yadin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.domain.model.CategoryMasteryItem
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.YadinIcons
import com.manidigit.yadin.ui.util.formatDigits

/**
 * کارت گزارش درصد تسلط و کلمات یادگرفته‌شده به تفکیک موضوعات و دسته‌بندی‌ها
 */
@Composable
fun CategoryMasteryCard(
    categoryItems: List<CategoryMasteryItem>,
    modifier: Modifier = Modifier
) {
    val colors = LocalYadinColors.current

    if (categoryItems.isEmpty()) return

    YadinCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            YadinSectionHeader(
                title = stringResource(R.string.category_mastery_title),
                subtitle = stringResource(R.string.category_mastery_subtitle),
                icon = YadinIcons.categoryMastery,
                iconTint = colors.info
            )

            // سطرهای دسته‌بندی‌ها
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                categoryItems.forEach { cat ->
                    val color = when {
                        cat.masteryPercent >= 75 -> colors.success
                        cat.masteryPercent >= 40 -> colors.primary
                        else -> colors.warning
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cat.categoryName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface,
                                fontSize = 12.sp
                            )

                            Text(
                                text = stringResource(
                                    R.string.category_mastery_fmt,
                                    cat.learnedWords.formatDigits(),
                                    cat.totalWords.formatDigits()
                                ) + " (${cat.masteryPercent.formatDigits()}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = color,
                                fontSize = 11.sp
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (cat.masteryPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = color,
                            trackColor = colors.surfaceVariant.copy(alpha = 0.78f)
                        )
                    }
                }
            }
        }
    }
}
