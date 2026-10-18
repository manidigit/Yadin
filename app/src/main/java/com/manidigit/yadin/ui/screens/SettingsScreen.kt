package com.manidigit.yadin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.components.YadinScreenHeader
import com.manidigit.yadin.ui.components.YadinSectionHeader
import com.manidigit.yadin.ui.components.YadinSelectableChip
import com.manidigit.yadin.ui.components.getDifficultyIcon
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.theme.YadinIcons
import com.manidigit.yadin.ui.util.formatDigits

/**
 * صفحه تنظیمات اصلی برنامه یادین با طراحی یکپارچه کارت‌ها، هدرها و چیپ‌های M3.
 */
@Composable
fun SettingsScreen(
    currentThemeId: String,
    isDark: Boolean,
    difficultyThreshold: Int = 3,
    languageDirection: CardDirection = CardDirection.NORMAL,
    uiLanguage: String = "fa",
    onSelectTheme: (String) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSetDifficultyThreshold: (Int) -> Unit = {},
    onSetLanguageDirection: (CardDirection) -> Unit = {},
    onSetUiLanguage: (String) -> Unit = {},
    onPurgeInactiveWords: (onComplete: (Int) -> Unit) -> Unit = {},
    onOpenBackup: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenAbout: () -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    var showPurgeConfirmDialog by remember { mutableStateOf(false) }
    var showPurgeReportDialog by remember { mutableStateOf(false) }
    var purgedCountResult by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(dimensions.screenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Standardized Screen Header
        YadinScreenHeader(
            title = stringResource(R.string.settings_title),
            onBack = onBack
        )

        // Section: Appearance & Theme Hub
        YadinCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                YadinSectionHeader(
                    title = stringResource(R.string.settings_appearance_section),
                    icon = YadinIcons.themePalette,
                    iconTint = colors.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceVariant)
                            .clickable { onSelectTheme(currentThemeId.ifBlank { "gemini" }) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_theme_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                            Text(
                                text = stringResource(R.string.settings_theme_gemini),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = { onToggleDarkMode(!isDark) },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (isDark) YadinIcons.lightMode else YadinIcons.darkMode,
                            contentDescription = stringResource(R.string.settings_toggle_dark_mode),
                            tint = if (isDark) colors.warning else colors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Difficulty Transition Threshold Card
        YadinCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                YadinSectionHeader(
                    title = stringResource(R.string.settings_diff_threshold_title),
                    icon = getDifficultyIcon(VocabularyDifficulty.MEDIUM),
                    iconTint = colors.warning
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 2, 3, 4, 5).forEach { thresholdValue ->
                        val isSelected = (difficultyThreshold == thresholdValue)
                        val label = thresholdValue.formatDigits()

                        YadinSelectableChip(
                            text = label,
                            isSelected = isSelected,
                            accentColor = colors.warning,
                            preserveAccentWhenUnselected = true,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f),
                            onClick = { onSetDifficultyThreshold(thresholdValue) }
                        )
                    }
                }
            }
        }

        // Language Direction Selector Card
        YadinCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                YadinSectionHeader(
                    title = stringResource(R.string.settings_language_direction_title),
                    icon = YadinIcons.languageDirection,
                    iconTint = colors.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isNormal = (languageDirection == CardDirection.NORMAL)
                    LanguageDirectionPill(
                        title = stringResource(R.string.settings_lang_normal),
                        subtitle = "es → fa",
                        isSelected = isNormal,
                        modifier = Modifier.weight(1f),
                        onClick = { onSetLanguageDirection(CardDirection.NORMAL) }
                    )
                    LanguageDirectionPill(
                        title = stringResource(R.string.settings_lang_reverse),
                        subtitle = "fa → es",
                        isSelected = !isNormal,
                        modifier = Modifier.weight(1f),
                        onClick = { onSetLanguageDirection(CardDirection.REVERSE) }
                    )
                }
            }
        }

        // Section: UI Language
        YadinCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                YadinSectionHeader(
                    title = stringResource(R.string.settings_ui_lang_title),
                    icon = YadinIcons.uiLanguage,
                    iconTint = colors.accentPurple
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isFa = (uiLanguage == "fa")
                    LanguageDirectionPill(
                        title = stringResource(R.string.settings_ui_lang_fa),
                        subtitle = stringResource(R.string.settings_ui_lang_fa_sub),
                        isSelected = isFa,
                        modifier = Modifier.weight(1f),
                        onClick = { onSetUiLanguage("fa") }
                    )
                    LanguageDirectionPill(
                        title = stringResource(R.string.settings_ui_lang_en),
                        subtitle = stringResource(R.string.settings_ui_lang_en_sub),
                        isSelected = !isFa,
                        modifier = Modifier.weight(1f),
                        onClick = { onSetUiLanguage("en") }
                    )
                }
            }
        }

        // Section: Purge Inactive Words
        YadinCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                YadinSectionHeader(
                    title = stringResource(R.string.settings_purge_title),
                    subtitle = stringResource(R.string.settings_purge_desc),
                    icon = YadinIcons.purgeTrash,
                    iconTint = colors.error,
                    modifier = Modifier.weight(1f),
                    trailingContent = {
                        androidx.compose.material3.OutlinedButton(
                            onClick = { showPurgeConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error)
                        ) {
                            Text(stringResource(R.string.settings_purge_button), fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }

        if (showPurgeConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showPurgeConfirmDialog = false },
                title = { Text(stringResource(R.string.settings_purge_confirm_title), fontWeight = FontWeight.Bold) },
                text = { Text(stringResource(R.string.settings_purge_confirm_msg)) },
                confirmButton = {
                    Button(
                        onClick = {
                            showPurgeConfirmDialog = false
                            onPurgeInactiveWords { count ->
                                purgedCountResult = count
                                showPurgeReportDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.error)
                    ) {
                        Text(stringResource(R.string.action_yes_purge), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPurgeConfirmDialog = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }

        if (showPurgeReportDialog) {
            AlertDialog(
                onDismissRequest = { showPurgeReportDialog = false },
                title = { Text(stringResource(R.string.settings_purge_success_title), fontWeight = FontWeight.Bold, color = colors.onSurface) },
                text = {
                    Text(
                        text = if (purgedCountResult > 0)
                            stringResource(R.string.settings_purge_success_msg, purgedCountResult.formatDigits())
                        else
                            stringResource(R.string.settings_purge_desc),
                        color = colors.onSurface
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showPurgeReportDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text(stringResource(R.string.action_confirm), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Backup & Restore Button
        YadinCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface,
            onClick = onOpenBackup
        ) {
            YadinSectionHeader(
                title = stringResource(R.string.settings_backup_title),
                subtitle = stringResource(R.string.settings_backup_desc),
                icon = YadinIcons.backupRestore,
                iconTint = colors.info,
                modifier = Modifier.padding(14.dp)
            )
        }

        // About Us Button
        YadinCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface,
            onClick = onOpenAbout
        ) {
            YadinSectionHeader(
                title = stringResource(R.string.settings_about_title),
                subtitle = stringResource(R.string.settings_about_desc),
                icon = YadinIcons.aboutInfo,
                iconTint = colors.primary,
                modifier = Modifier.padding(14.dp)
            )
        }

        // Help & Guide Button
        YadinCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface,
            onClick = onOpenHelp
        ) {
            YadinSectionHeader(
                title = stringResource(R.string.settings_help_title),
                subtitle = stringResource(R.string.settings_help_desc),
                icon = YadinIcons.helpGuide,
                iconTint = colors.accentPurple,
                modifier = Modifier.padding(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun LanguageDirectionPill(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    YadinSelectableChip(
        text = title,
        subtitle = subtitle,
        isSelected = isSelected,
        accentColor = colors.primary,
        preserveAccentWhenUnselected = true,
        verticalLayout = true,
        minHeight = 46.dp,
        fontSize = 12.sp,
        modifier = modifier,
        onClick = onClick
    )
}
