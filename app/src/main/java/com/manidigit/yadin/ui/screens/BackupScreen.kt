package com.manidigit.yadin.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.data.repository.BackupExportFormat
import com.manidigit.yadin.data.repository.BackupOptions
import com.manidigit.yadin.data.repository.BackupType
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions

@Composable
fun BackupScreen(
    totalConcepts: Int,
    totalCategories: Int,
    isProcessing: Boolean,
    progress: Float,
    progressMessage: String,
    lastResult: String?,
    isError: Boolean,
    backupOptions: BackupOptions = BackupOptions(),
    onUpdateBackupOptions: (BackupOptions) -> Unit = {},
    onToggleFullBackup: (Boolean) -> Unit = {},
    onToggleOption: (String, Boolean) -> Unit = { _, _ -> },
    onExportCustomBackup: ((BackupExportFormat, BackupOptions) -> Unit)? = null,
    onExportCustomBackupToUri: ((BackupExportFormat, android.net.Uri, BackupOptions) -> Unit)? = null,
    onExportBackup: (BackupType) -> Unit = {},
    onExportBackupToUri: ((BackupType, android.net.Uri) -> Unit)? = null,
    onRestoreBackup: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    val coroutineScope = rememberCoroutineScope()

    var selectedFormat by remember { mutableStateOf(BackupExportFormat.JSON) }
    var activeTab by remember { mutableStateOf(0) } // 0 = Export, 1 = Restore

    // Restore state
    var loadedFileContent by remember { mutableStateOf<String?>(null) }
    var loadedFileName by remember { mutableStateOf<String?>(null) }
    var isReplaceMode by remember { mutableStateOf(false) }
    var showConfirmRestoreDialog by remember { mutableStateOf(false) }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            if (onExportCustomBackupToUri != null) {
                onExportCustomBackupToUri(BackupExportFormat.JSON, uri, backupOptions)
            } else if (onExportBackupToUri != null) {
                onExportBackupToUri(BackupType.FULL, uri)
            }
        }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            if (onExportCustomBackupToUri != null) {
                onExportCustomBackupToUri(selectedFormat, uri, backupOptions)
            } else if (onExportBackupToUri != null) {
                val legacyType = when (selectedFormat) {
                    BackupExportFormat.EXCEL_CSV_PROGRESS -> BackupType.PROGRESS_EXCEL
                    else -> BackupType.VOCABULARY_EXCEL
                }
                onExportBackupToUri(legacyType, uri)
            }
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val text = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    }
                    withContext(Dispatchers.Main) {
                        if (!text.isNullOrBlank()) {
                            loadedFileContent = text
                            loadedFileName = "فایل انتخاب‌شده (${text.length / 1024} کیلوبایت)"
                            Toast.makeText(context, "فایل با موفقیت بارگذاری شد", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "خطا در خواندن فایل: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
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
                        text = "پشتیبان‌گیری، اکسل و بازیابی",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "مدیریت جامع پایگاه داده، استخراج گزارش‌ها و بازیابی",
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            // Progress Banner
            AnimatedVisibility(
                visible = isProcessing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                YadinCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = progressMessage.ifEmpty { "در حال پردازش عملیات..." },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurface
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = colors.primary,
                            trackColor = colors.surfaceVariant,
                        )
                    }
                }
            }

            // Result Banner
            AnimatedVisibility(
                visible = !lastResult.isNullOrBlank() && !isProcessing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val bannerColor = if (isError) colors.error else colors.success
                val bannerIcon = if (isError) Icons.Default.Error else Icons.Default.CheckCircle

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(bannerColor.copy(alpha = 0.12f))
                        .border(1.dp, bannerColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = bannerIcon,
                            contentDescription = null,
                            tint = bannerColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = lastResult ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Tabs Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TabButton(
                    title = "پشتیبان‌گیری و اکسل",
                    icon = Icons.Default.CloudUpload,
                    isSelected = activeTab == 0,
                    modifier = Modifier.weight(1f)
                ) {
                    activeTab = 0
                }

                TabButton(
                    title = "بازیابی اطلاعات",
                    icon = Icons.Default.CloudDownload,
                    isSelected = activeTab == 1,
                    modifier = Modifier.weight(1f)
                ) {
                    activeTab = 1
                }
            }

            if (activeTab == 0) {
                // EXPORT TAB CONTENT

                // 1. Stats Card
                YadinCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "وضعیت پایگاه داده فعلی",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatBox(
                                label = "کل واژگان",
                                value = "$totalConcepts",
                                icon = Icons.Default.Translate,
                                modifier = Modifier.weight(1f)
                            )
                            StatBox(
                                label = "دسته‌بندی‌ها",
                                value = "$totalCategories",
                                icon = Icons.Default.Category,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 2. Comprehensive Backup Scope & Checkboxes
                YadinCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "انتخاب اجزا و قلمرو خروجی",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface
                                    )
                                    Text(
                                        text = "تعیین بخش‌های مورد نظر برای پشتیبان‌گیری یا خروجی",
                                        fontSize = 11.sp,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = colors.outline.copy(alpha = 0.5f)
                        )

                        // Master Full Backup Switch/Checkbox
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.primary.copy(alpha = 0.08f))
                                .border(1.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .clickable { onToggleFullBackup(!backupOptions.fullBackup) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "فول بکاپ کامل (Full Backup)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                                Text(
                                    text = "پشتیبان‌گیری جامع از تمام واژگان، تنظیمات، لایتنر و پروسه",
                                    fontSize = 11.sp,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = backupOptions.fullBackup,
                                onCheckedChange = { onToggleFullBackup(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.primary,
                                    checkedTrackColor = colors.primary.copy(alpha = 0.4f)
                                )
                            )
                        }

                        // Granular Checkboxes
                        BackupOptionCheckboxRow(
                            title = "پایگاه واژگان (Vocabulary Database)",
                            subtitle = "تمام کلمات، ترجمه‌های فارسی، دسته‌بندی‌ها و یادداشت‌ها",
                            icon = Icons.Default.Translate,
                            isChecked = backupOptions.includeVocabulary,
                            onCheckedChange = { onToggleOption("vocabulary", it) }
                        )

                        BackupOptionCheckboxRow(
                            title = "دسته‌بندی‌ها (Categories)",
                            subtitle = "ساختار موضوعی و گروه‌بندی کلمات",
                            icon = Icons.Default.Category,
                            isChecked = backupOptions.includeCategories,
                            onCheckedChange = { onToggleOption("categories", it) }
                        )

                        BackupOptionCheckboxRow(
                            title = "درجه سختی کلمات (Difficulty Levels)",
                            subtitle = "سطح دشواری (آسان تا خیلی سخت) و ترجیحات آستانه",
                            icon = Icons.Default.Speed,
                            isChecked = backupOptions.includeDifficulty,
                            onCheckedChange = { onToggleOption("difficulty", it) }
                        )

                        BackupOptionCheckboxRow(
                            title = "آمار رگبار متوالی و تقویم (Streak & Daily Progress)",
                            subtitle = "سابقه زنجیره مطالعه متوالی روزانه بدون وقفه",
                            icon = Icons.Default.LocalFireDepartment,
                            isChecked = backupOptions.includeStreakAndProgress,
                            onCheckedChange = { onToggleOption("streak", it) }
                        )

                        BackupOptionCheckboxRow(
                            title = "آمار مرور و جعبه لایتنر (Word Review Stats)",
                            subtitle = "مراحل ۴گانه جعبه لایتنر، فواصل زمانی و زمان مرور بعدی",
                            icon = Icons.Default.School,
                            isChecked = backupOptions.includeReviewStats,
                            onCheckedChange = { onToggleOption("reviewStats", it) }
                        )

                        BackupOptionCheckboxRow(
                            title = "پروسه پیشرفت و جلسات (Process Progress)",
                            subtitle = "ریز سوابق آزمون‌ها، فلش‌کارت‌ها، دقت و زمان جلسات",
                            icon = Icons.Default.History,
                            isChecked = backupOptions.includeProcessHistory,
                            onCheckedChange = { onToggleOption("process", it) }
                        )

                        BackupOptionCheckboxRow(
                            title = "تنظیمات و دستاوردها (Settings & Achievements)",
                            subtitle = "پیکربندی‌ها، تم رنگی، تنظیمات صوت و نشان‌های کسب‌شده",
                            icon = Icons.Default.Settings,
                            isChecked = backupOptions.includeSettings,
                            onCheckedChange = { onToggleOption("settings", it) }
                        )
                    }
                }

                // 3. Export Format Choice
                YadinCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "انتخاب فرمت خروجی",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        }

                        FormatChoiceCard(
                            title = "فایل پشتیبان یادین (.json)",
                            subtitle = "قالب استاندارد JSON برای بازیابی کامل و اتمیک با تنظیمات انتخابی",
                            icon = Icons.Default.Description,
                            isSelected = selectedFormat == BackupExportFormat.JSON,
                            onClick = { selectedFormat = BackupExportFormat.JSON }
                        )

                        FormatChoiceCard(
                            title = "خروجی اکسل واژگان و دسته‌بندی‌ها (.csv)",
                            subtitle = "جدول اکسل با UTF-8 BOM شامل کلمات، معانی فارسی، دسته‌ها و سختی",
                            icon = Icons.Default.TableChart,
                            isSelected = selectedFormat == BackupExportFormat.EXCEL_CSV_VOCABULARY,
                            onClick = { selectedFormat = BackupExportFormat.EXCEL_CSV_VOCABULARY }
                        )

                        FormatChoiceCard(
                            title = "خروجی اکسل گزارش پیشرفت و رگبار (.csv)",
                            subtitle = "گزارش تحلیلی جامع با جدول شاخص‌ها، حجم مرور روزانه و رگبار متوالی",
                            icon = Icons.Default.BarChart,
                            isSelected = selectedFormat == BackupExportFormat.EXCEL_CSV_PROGRESS,
                            onClick = { selectedFormat = BackupExportFormat.EXCEL_CSV_PROGRESS }
                        )

                        FormatChoiceCard(
                            title = "خروجی اکسل جامع و یکپارچه (.csv - Master)",
                            subtitle = "ترکیب تمام جداول واژگان، آمار، لایتنر و تنظیمات در قالب فایل اکسل",
                            icon = Icons.Default.Storage,
                            isSelected = selectedFormat == BackupExportFormat.EXCEL_CSV_COMPLETE,
                            onClick = { selectedFormat = BackupExportFormat.EXCEL_CSV_COMPLETE }
                        )
                    }
                }

                // Action Buttons for Export
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (selectedFormat == BackupExportFormat.JSON) {
                                val fname = if (backupOptions.fullBackup) "yadin-full-backup.json" else "yadin-custom-backup.json"
                                exportJsonLauncher.launch(fname)
                            } else {
                                val fname = when (selectedFormat) {
                                    BackupExportFormat.EXCEL_CSV_PROGRESS -> "yadin-progress-report.csv"
                                    BackupExportFormat.EXCEL_CSV_COMPLETE -> "yadin-master-export.csv"
                                    else -> "yadin-vocabulary.csv"
                                }
                                exportCsvLauncher.launch(fname)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                        border = BorderStroke(1.dp, colors.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ذخیره در...", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if (onExportCustomBackup != null) {
                                onExportCustomBackup(selectedFormat, backupOptions)
                            } else {
                                val legacyType = when (selectedFormat) {
                                    BackupExportFormat.EXCEL_CSV_VOCABULARY -> BackupType.VOCABULARY_EXCEL
                                    BackupExportFormat.EXCEL_CSV_PROGRESS -> BackupType.PROGRESS_EXCEL
                                    else -> if (backupOptions.fullBackup) BackupType.FULL else BackupType.VOCABULARY
                                }
                                onExportBackup(legacyType)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedFormat == BackupExportFormat.JSON) "ایجاد فایل پشتیبان" else "دریافت فایل اکسل",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }

            } else {
                // RESTORE TAB CONTENT
                YadinCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "بازیابی پایگاه داده از فایل",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        }

                        Text(
                            text = "فایل پشتیبان با پسوند .json را انتخاب کنید تا داده‌های واژگان، مراحل یادگیری و پیشرفت بازیابی شوند.",
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        // File picker button
                        OutlinedButton(
                            onClick = { importFileLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (loadedFileContent != null) colors.success else colors.primary)
                        ) {
                            Icon(
                                imageVector = if (loadedFileContent != null) Icons.Default.CheckCircle else Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = if (loadedFileContent != null) colors.success else colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = loadedFileName ?: "انتخاب فایل پشتیبان از حافظه...",
                                color = if (loadedFileContent != null) colors.success else colors.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (loadedFileContent != null) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = colors.outline.copy(alpha = 0.5f)
                            )

                            Text(
                                text = "استراتژی بازیابی اطلاعات:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )

                            // Merge option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (!isReplaceMode) colors.primary.copy(alpha = 0.08f) else Color.Transparent)
                                    .clickable { isReplaceMode = false }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = !isReplaceMode,
                                    onClick = { isReplaceMode = false },
                                    colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "ادغام هوشمند (Merge)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface
                                    )
                                    Text(
                                        text = "کلمات و سوابق جدید اضافه می‌شوند و اطلاعات فعلی حفظ خواهند شد.",
                                        fontSize = 11.sp,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }

                            // Replace option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isReplaceMode) colors.error.copy(alpha = 0.08f) else Color.Transparent)
                                    .clickable { isReplaceMode = true }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isReplaceMode,
                                    onClick = { isReplaceMode = true },
                                    colors = RadioButtonDefaults.colors(selectedColor = colors.error)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "جایگزینی کامل (Clean Replace)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.error
                                    )
                                    Text(
                                        text = "داده‌های فعلی پاکسازی شده و محتوای فایل پشتیبان نشانده می‌شود.",
                                        fontSize = 11.sp,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { showConfirmRestoreDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isReplaceMode) colors.error else colors.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isReplaceMode) "شروع جایگزینی کامل اطلاعات" else "شروع ادغام و بازیابی",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Confirmation Dialog for Restore
    if (showConfirmRestoreDialog && loadedFileContent != null) {
        AlertDialog(
            onDismissRequest = { showConfirmRestoreDialog = false },
            title = {
                Text(
                    text = if (isReplaceMode) "تأیید جایگزینی کامل اطلاعات" else "تأیید بازیابی و ادغام",
                    fontWeight = FontWeight.Bold,
                    color = if (isReplaceMode) colors.error else colors.onSurface
                )
            },
            text = {
                Text(
                    text = if (isReplaceMode)
                        "هشدار: تمام واژگان و سوابق فعلی حذف خواهند شد و با داده‌های فایل پشتیبان جایگزین می‌شوند. آیا ادامه می‌دهید؟"
                    else
                        "داده‌های فایل پشتیبان با بانک اطلاعاتی فعلی ادغام خواهند شد. آیا ادامه می‌دهید؟",
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmRestoreDialog = false
                        loadedFileContent?.let { content ->
                            onRestoreBackup(content, isReplaceMode)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReplaceMode) colors.error else colors.primary
                    )
                ) {
                    Text("بله، انجام شود", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRestoreDialog = false }) {
                    Text("انصراف", color = colors.onSurfaceVariant)
                }
            },
            containerColor = colors.surfaceVariant
        )
    }
}

@Composable
private fun BackupOptionCheckboxRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalYadinColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = colors.primary,
                checkmarkColor = Color.White
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isChecked) colors.primary else colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) colors.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else colors.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = LocalYadinColors.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceVariant)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FormatChoiceCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current

    val borderColor = if (isSelected) colors.primary else colors.outline.copy(alpha = 0.4f)
    val bgColor = if (isSelected) colors.primary.copy(alpha = 0.08f) else colors.surfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isSelected) colors.primary else colors.background),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) colors.primary else colors.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = colors.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }

        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
        )
    }
}
