package com.manidigit.yadin.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.ui.util.toPersianDigits
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.theme.YadinPalette

/**
 * صفحه تنظیمات اصلی برنامه یادین.
 *
 * شامل تنظیم پوسته ظاهری Gemini، حالت شب/روز،
 * آستانه پاسخ‌های متوالی تغییر سختی، جهت زبان یادگیری، پاک‌سازی سطل بازیافت و دسترسی به پشتیبان‌گیری.
 *
 * @param currentThemeId شناسه پوسته فعال
 * @param isDark آیا حالت شب فعال است
 * @param difficultyThreshold آستانه پاسخ متوالی جهت تغییر سطح سختی
 * @param languageDirection جهت زبان فعال مطالعه
 * @param uiLanguage زبان رابط کاربری
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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

            Text(
                text = "تنظیمات برنامه",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
        }

        // Section: Appearance & Theme Hub (Dropdown + Compact Dark Mode button)
        Text(
            text = "ظاهر و پوسته برنامه:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )

        YadinCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Gemini Theme Info
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "پوسته اختصاصی",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "تم Gemini (هوش مصنوعی)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Compact Dark/Light Mode Button
                IconButton(
                    onClick = { onToggleDarkMode(!isDark) },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceVariant)
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "تغییر حالت شب و روز",
                        tint = if (isDark) colors.warning else colors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Section Title: Learning & Review Settings
        Text(
            text = "تنظیمات یادگیری و آزمون:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.warning.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = colors.warning,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "پاسخ متوالی برای تغییر سطح سختی",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface
                        )
                    }
                }

                // Threshold Selector Buttons (1, 2, 3, 4, 5)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 2, 3, 4, 5).forEach { thresholdValue ->
                        val isSelected = (difficultyThreshold == thresholdValue)
                        val label = thresholdValue.toPersianDigits()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) colors.primary else colors.surfaceVariant
                                )
                                .clickable { onSetDifficultyThreshold(thresholdValue) }
                                .padding(vertical = 10.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colors.onPrimary else colors.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 13.sp
                            )
                        }
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "جهت زبان یادگیری برنامه در مرور، افزودن کلمات و آمار پیشرفت",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isNormal = (languageDirection == CardDirection.NORMAL)
                    LanguageDirectionPill(
                        title = "🇪🇸 اسپانیایی ← 🇮🇷 فارسی",
                        subtitle = "مبدأ: اسپانیایی / مقصد: فارسی",
                        isSelected = isNormal,
                        modifier = Modifier.weight(1f),
                        onClick = { onSetLanguageDirection(CardDirection.NORMAL) }
                    )
                    LanguageDirectionPill(
                        title = "🇮🇷 فارسی ← 🇪🇸 اسپانیایی",
                        subtitle = "مبدأ: فارسی / مقصد: اسپانیایی",
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                    Text("زبان رابط کاربری (UI Language)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.onSurface)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isFa = (uiLanguage == "fa")
                    LanguageDirectionPill(
                        title = "فارسی 🇮🇷",
                        subtitle = "راست‌به‌چپ (RTL)",
                        isSelected = isFa,
                        modifier = Modifier.weight(1f),
                        onClick = { onSetUiLanguage("fa") }
                    )
                    LanguageDirectionPill(
                        title = "English 🇬🇧",
                        subtitle = "Left-to-Right (LTR)",
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
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = colors.error, modifier = Modifier.size(22.dp))
                    Column {
                        Text("پاک‌سازی دائمی واژگان حذف‌شده", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.onSurface)
                        Text("تخلیه کامل سطل بازیافت و حذف قطعی از دیتابیس", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, fontSize = 11.sp)
                    }
                }
                androidx.compose.material3.OutlinedButton(
                    onClick = { showPurgeConfirmDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error)
                ) {
                    Text("تخلیه", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showPurgeConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showPurgeConfirmDialog = false },
                title = { Text("تأیید پاک‌سازی دائمی", fontWeight = FontWeight.Bold) },
                text = { Text("آیا مطمئن هستید؟ با این کار تمامی واژگانی که پیش‌تر حذف کرده‌اید برای همیشه از پایگاه داده پاک خواهند شد و قابل احیا نخواهند بود.") },
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
                        Text("بله، پاک شود", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPurgeConfirmDialog = false }) {
                        Text("انصراف")
                    }
                }
            )
        }

        if (showPurgeReportDialog) {
            AlertDialog(
                onDismissRequest = { showPurgeReportDialog = false },
                title = { Text("گزارش پاک‌سازی دیتابیس", fontWeight = FontWeight.Bold, color = colors.onSurface) },
                text = {
                    Text(
                        text = if (purgedCountResult > 0)
                            "عملیات با موفقیت انجام شد.\nتعداد $purgedCountResult واژه غیرفعال به‌طور کامل و دائمی از پایگاه داده حذف گردید."
                        else
                            "هیچ واژه غیرفعالی در سطل بازیافت جهت پاک‌سازی پیدا نشد.",
                        color = colors.onSurface
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showPurgeReportDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("تأیید و بستن", fontWeight = FontWeight.Bold)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.info.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = colors.info,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "پشتیبان‌گیری و بازیابی داده‌ها",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "صادرات و بازگردانی فایل پشتیبان واژگان و پیشرفت (JSON)",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }

        // About Us Button
        YadinCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface,
            onClick = onOpenAbout
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "درباره ما و مشخصات یادین",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "اطلاعات سازنده (maniDigit)، اهداف و نسخه نرم‌افزار",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }

        // Help & Guide Button
        YadinCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface,
            onClick = onOpenHelp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.info.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = null,
                        tint = colors.info,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "راهنما و آموزش قوانین یادین",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "آموزش گام‌به‌گام مراحل یادگیری، سختی واژه و آزمون",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
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
    val dimensions = LocalYadinDimensions.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.cornerSmall))
            .background(if (isSelected) colors.primary.copy(alpha = 0.15f) else colors.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(dimensions.cornerSmall)
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) colors.primary else colors.onSurface,
                fontSize = 12.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) colors.primary.copy(alpha = 0.8f) else colors.onSurfaceVariant,
                fontSize = 9.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
