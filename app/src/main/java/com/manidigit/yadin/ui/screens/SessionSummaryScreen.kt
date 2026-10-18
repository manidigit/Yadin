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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manidigit.yadin.R
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.ui.components.VocabStatType
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.components.YadinSectionHeader
import com.manidigit.yadin.ui.components.YadinStatTile
import com.manidigit.yadin.ui.components.getReviewModeIcon
import com.manidigit.yadin.ui.components.getVocabStatIcon
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.util.formatDigits

@Composable
fun SessionSummaryScreen(
    correctCount: Int,
    wrongCount: Int,
    onBackHome: () -> Unit,
    onReviewAgain: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    val total = correctCount + wrongCount
    val accuracy = if (total > 0) ((correctCount.toFloat() / total) * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(dimensions.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(colors.heroGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = stringResource(R.string.summary_celebration_icon),
                    tint = colors.onHero,
                    modifier = Modifier.size(46.dp)
                )
            }

            Text(
                text = stringResource(R.string.summary_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )

            Text(
                text = if (accuracy >= 80) stringResource(R.string.summary_congrats_high) else stringResource(R.string.summary_congrats_low),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        YadinCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                YadinSectionHeader(
                    title = stringResource(R.string.summary_performance_title),
                    icon = getReviewModeIcon(ReviewMode.QUIZ),
                    iconTint = colors.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    YadinStatTile(
                        title = stringResource(R.string.summary_total_reviewed_label),
                        value = stringResource(R.string.summary_cards_count, total.formatDigits()),
                        icon = getVocabStatIcon(VocabStatType.PRACTICED),
                        accentColor = colors.primary,
                        modifier = Modifier.weight(1f)
                    )
                    YadinStatTile(
                        title = stringResource(R.string.summary_accuracy_label),
                        value = "${accuracy.formatDigits()}%",
                        icon = getReviewModeIcon(ReviewMode.QUIZ),
                        accentColor = colors.accentPurple,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    YadinStatTile(
                        title = stringResource(R.string.summary_correct_count_label),
                        value = correctCount.formatDigits(),
                        icon = Icons.Default.CheckCircle,
                        accentColor = colors.success,
                        modifier = Modifier.weight(1f)
                    )
                    YadinStatTile(
                        title = stringResource(R.string.summary_wrong_count_label),
                        value = wrongCount.formatDigits(),
                        icon = Icons.Default.Cancel,
                        accentColor = colors.error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onBackHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(dimensions.cornerMedium),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(stringResource(R.string.summary_back_home), fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onReviewAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(dimensions.cornerMedium)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(stringResource(R.string.summary_review_again), fontWeight = FontWeight.Bold)
            }
        }
    }
}
