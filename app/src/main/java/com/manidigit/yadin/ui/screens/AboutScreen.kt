package com.manidigit.yadin.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.components.YadinScreenHeader
import com.manidigit.yadin.ui.components.YadinSectionHeader
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.theme.YadinIcons
import com.manidigit.yadin.ui.util.formatDigits

@Composable
fun AboutScreen(onBack: () -> Unit) {
    BackHandler { onBack() }

    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    val context = LocalContext.current

    val currentVersionName = com.manidigit.yadin.BuildConfig.VERSION_NAME
    val currentVersionCode = com.manidigit.yadin.BuildConfig.VERSION_CODE.toString()

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            YadinScreenHeader(
                title = stringResource(R.string.about_title),
                subtitle = stringResource(R.string.about_subtitle),
                onBack = onBack
            )

            // Hero Brand Banner Card
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Logo Box with Monogram and Sparkle Badge
                    Box(
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(dimensions.cornerMedium))
                                .background(colors.heroGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.app_monogram),
                                color = colors.onHero,
                                fontWeight = FontWeight.Black,
                                fontSize = 36.sp
                            )
                        }

                        // Sparkle badge overlay
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(colors.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = YadinIcons.brandSparkle,
                                    contentDescription = null,
                                    tint = colors.onPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.about_app_full_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )

                    // Version Status Chip
                    Surface(
                        shape = CircleShape,
                        color = colors.primary.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            colors.primary.copy(alpha = 0.25f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.success)
                            )
                            Text(
                                text = stringResource(
                                    R.string.about_version_build,
                                    currentVersionName,
                                    currentVersionCode.formatDigits()
                                ) + " • " + stringResource(R.string.about_stable_build),
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.about_summary_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            // Technical Specs Card
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    YadinSectionHeader(
                        title = stringResource(R.string.about_tech_specs_title),
                        icon = YadinIcons.aboutInfo,
                        iconTint = colors.primary
                    )

                    TechRow(
                        label = stringResource(R.string.about_developer_label),
                        value = stringResource(R.string.about_developer_name)
                    )
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.2f))

                    TechRow(
                        label = stringResource(R.string.about_version_label),
                        value = "$currentVersionName (${currentVersionCode.formatDigits()})"
                    )
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.2f))

                    TechRow(
                        label = stringResource(R.string.about_leitner_engine_label),
                        value = stringResource(R.string.about_tech_engine_val)
                    )
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.2f))

                    TechRow(
                        label = stringResource(R.string.about_difficulty_algo_label),
                        value = stringResource(R.string.about_tech_diff_val)
                    )
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.2f))

                    TechRow(
                        label = stringResource(R.string.about_tech_direction_label),
                        value = stringResource(R.string.about_tech_direction_val)
                    )
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.2f))

                    TechRow(
                        label = stringResource(R.string.about_local_db_label),
                        value = stringResource(R.string.about_tech_db_val)
                    )
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.2f))

                    TechRow(
                        label = stringResource(R.string.about_theme_system_label),
                        value = stringResource(R.string.about_theme_system_value)
                    )
                }
            }

            // External Links & Actions (GitHub, WhatsApp, Share)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // GitHub Button
                    ActionLinkCard(
                        modifier = Modifier.weight(1f),
                        icon = YadinIcons.developerCode,
                        title = stringResource(R.string.about_github_btn),
                        subtitle = stringResource(R.string.about_github_sub),
                        accentColor = colors.primary,
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/manidigit/Yadin")
                            )
                            context.startActivity(intent)
                        }
                    )

                    // WhatsApp Support Button
                    ActionLinkCard(
                        modifier = Modifier.weight(1f),
                        icon = YadinIcons.contactSupport,
                        title = stringResource(R.string.about_whatsapp_btn),
                        subtitle = stringResource(R.string.about_whatsapp_sub),
                        accentColor = colors.success,
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://wa.me/34685644444")
                            )
                            context.startActivity(intent)
                        }
                    )
                }

                // Share Button
                val shareSubject = stringResource(R.string.about_share_subject)
                val shareText = stringResource(R.string.about_share_text)
                ActionLinkCard(
                    modifier = Modifier.fillMaxWidth(),
                    icon = YadinIcons.share,
                    title = stringResource(R.string.about_share_btn),
                    subtitle = stringResource(R.string.about_share_sub),
                    accentColor = colors.info,
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, shareSubject))
                    }
                )
            }

            // Core Pillars & Features
            YadinSectionHeader(
                title = stringResource(R.string.about_features_header_title),
                icon = YadinIcons.brandSparkle,
                iconTint = colors.primary
            )

            // Scientific Repetition
            AboutFeatureCard(
                icon = YadinIcons.retentionBrain,
                title = stringResource(R.string.about_feature1_title),
                description = stringResource(R.string.about_feature1_desc),
                color = colors.info
            )

            // Scheduling Engines
            AboutFeatureCard(
                icon = YadinIcons.leitnerBox,
                title = stringResource(R.string.about_feature2_title),
                description = stringResource(R.string.about_feature2_desc),
                color = colors.warning
            )

            // Dynamic Difficulty & Smart Quiz
            AboutFeatureCard(
                icon = YadinIcons.difficultySpectrum,
                title = stringResource(R.string.about_feature3_title),
                description = stringResource(R.string.about_feature3_desc),
                color = colors.primary
            )

            // Offline Word Bank & Backup
            AboutFeatureCard(
                icon = YadinIcons.databaseStorage,
                title = stringResource(R.string.about_feature4_title),
                description = stringResource(R.string.about_feature4_desc),
                color = colors.success
            )

            // Footer & Copyright
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.about_footer_prefix),
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.about_copyright),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Back Button
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(dimensions.cornerMedium),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Text(stringResource(R.string.action_back), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TechRow(label: String, value: String) {
    val colors = LocalYadinColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = colors.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
    }
}

@Composable
private fun ActionLinkCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .clip(RoundedCornerShape(dimensions.cornerMedium))
            .background(colors.surface)
            .border(
                1.dp,
                accentColor.copy(alpha = 0.35f),
                RoundedCornerShape(dimensions.cornerMedium)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = colors.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AboutFeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    YadinCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(dimensions.cornerSmall))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    lineHeight = 21.sp
                )
            }
        }
    }
}
