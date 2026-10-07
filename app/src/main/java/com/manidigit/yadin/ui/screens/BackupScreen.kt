package com.manidigit.yadin.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.data.repository.BackupType
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions

enum class SimpleBackupFormat {
    EXCEL,
    JSON
}

@Composable
fun BackupScreen(
    totalConcepts: Int,
    totalCategories: Int,
    isProcessing: Boolean,
    progress: Float,
    progressMessage: String,
    lastResult: String?,
    isError: Boolean,
    onExportBackup: (BackupType) -> Unit,
    onExportBackupToUri: ((BackupType, android.net.Uri) -> Unit)? = null,
    onRestoreBackup: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    var selectedFormat by remember { mutableStateOf(SimpleBackupFormat.EXCEL) }
    var activeTab by remember { mutableStateOf(0) } // 0 = Export, 1 = Restore

    // Restore state
    var loadedFileContent by remember { mutableStateOf<String?>(null) }
    var loadedFileName by remember { mutableStateOf<String?>(null) }
    var isReplaceMode by remember { mutableStateOf(false) }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && onExportBackupToUri != null) {
            onExportBackupToUri(BackupType.FULL, uri)
        }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null && onExportBackupToUri != null) {
            onExportBackupToUri(BackupType.VOCABULARY_EXCEL, uri)
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val text = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    if (text.isNotBlank()) {
                        loadedFileContent = text
                        loadedFileName = "فایل انتخاب‌شده (${text.length / 1024} کیلوبایت)"
                        Toast.makeText(context, "فایل با موفقیت بارگذاری شد", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "خطا در خواندن فایل: ${e.message}", Toast.LENGTH_LONG).show()
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
                        text = "پشتیبان‌گیری و بازیابی",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "صادرات آسان در قالب اکسل و JSON",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            // Overview Card
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(dimensions.cornerSmall))
                            .background(colors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "بانک اطلاعات واژگان",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            text = "$totalConcepts واژه • $totalCategories دسته‌بندی موضوعی",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }

            // Tab Switcher: خروجی گرفتن / بازیابی
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensions.cornerMedium))
                    .background(colors.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(dimensions.cornerSmall))
                        .background(if (activeTab == 0) colors.primary else Color.Transparent)
                        .clickable { activeTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = if (activeTab == 0) colors.onPrimary else colors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "تهیه خروجی (Export)",
                            color = if (activeTab == 0) colors.onPrimary else colors.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(dimensions.cornerSmall))
                        .background(if (activeTab == 1) colors.primary else Color.Transparent)
                        .clickable { activeTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = if (activeTab == 1) colors.onPrimary else colors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "بازیابی داده‌ها (Restore)",
                            color = if (activeTab == 1) colors.onPrimary else colors.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Progress Banner
            if (isProcessing) {
                YadinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = progressMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = colors.primary,
                            trackColor = colors.surfaceVariant
                        )
                    }
                }
            }

            // Result Alert
            if (!isProcessing && lastResult != null) {
                YadinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (isError) colors.error.copy(alpha = 0.12f) else colors.success.copy(alpha = 0.12f),
                    borderStroke = BorderStroke(
                        1.dp,
                        if (isError) colors.error.copy(alpha = 0.4f) else colors.success.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isError) colors.error else colors.success,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = lastResult,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isError) colors.error else colors.success
                        )
                    }
                }
            }

            if (activeTab == 0) {
                // EXPORT TAB - CLEAR & SIMPLE
                Text(
                    text = "انتخاب قالب فایل خروجی:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )

                // 2 Clear Format Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FormatChoiceCard(
                        title = "فایل اکسل (Excel)",
                        subtitle = "مناسب برای باز کردن در اکسل و Google Sheets با تفکیک ستون‌ها",
                        icon = Icons.Default.TableChart,
                        isSelected = selectedFormat == SimpleBackupFormat.EXCEL,
                        accentColor = colors.success,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedFormat = SimpleBackupFormat.EXCEL }
                    )

                    FormatChoiceCard(
                        title = "فایل JSON",
                        subtitle = "پشتیبان کامل دیتابیس یادین برای انتقال به دستگاه دیگر یا بازیابی",
                        icon = Icons.Default.Description,
                        isSelected = selectedFormat == SimpleBackupFormat.JSON,
                        accentColor = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedFormat = SimpleBackupFormat.JSON }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Primary Export Button (Downloads/Saves with Document Picker)
                Button(
                    onClick = {
                        val timestamp = System.currentTimeMillis()
                        if (selectedFormat == SimpleBackupFormat.EXCEL) {
                            exportCsvLauncher.launch("yadin_vocabulary_$timestamp.csv")
                        } else {
                            exportJsonLauncher.launch("yadin_backup_$timestamp.json")
                        }
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(dimensions.cornerMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedFormat == SimpleBackupFormat.EXCEL) colors.success else colors.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedFormat == SimpleBackupFormat.EXCEL) "دانلود و ذخیره فایل اکسل (Excel)" else "دانلود و ذخیره فایل پشتیبان JSON",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Secondary Quick Save
                OutlinedButton(
                    onClick = {
                        val type = if (selectedFormat == SimpleBackupFormat.EXCEL) BackupType.VOCABULARY_EXCEL else BackupType.FULL
                        onExportBackup(type)
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(dimensions.cornerMedium)
                ) {
                    Text("ذخیره سریع در حافظه دستگاه", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            } else {
                // RESTORE TAB - SIMPLE & CLEAR
                Text(
                    text = "بازگردانی فایل پشتیبان:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )

                YadinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "فایل پشتیبان را از حافظه دستگاه خود انتخاب کنید:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface
                        )

                        // File Picker Button
                        Button(
                            onClick = { importFileLauncher.launch("*/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(dimensions.cornerMedium),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.secondary,
                                contentColor = colors.onSecondary
                            )
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(loadedFileName ?: "انتخاب فایل پشتیبان (JSON یا Excel/CSV)", fontWeight = FontWeight.Bold)
                        }

                        // Mode switch: Replace vs Merge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surfaceVariant.copy(alpha = 0.5f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isReplaceMode) "جایگزینی کامل دیتابیس" else "ادغام با داده‌های فعلی",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                                Text(
                                    text = if (isReplaceMode) "اطلاعات قبلی حذف و با فایل جایگزین می‌شود" else "واژگان جدید اضافه و موارد قبلی حفظ می‌شوند",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = isReplaceMode,
                                onCheckedChange = { isReplaceMode = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.primary,
                                    checkedTrackColor = colors.primary.copy(alpha = 0.3f)
                                )
                            )
                        }

                        // Confirm Restore Button
                        Button(
                            onClick = {
                                loadedFileContent?.let {
                                    onRestoreBackup(it, isReplaceMode)
                                }
                            },
                            enabled = !isProcessing && loadedFileContent != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(dimensions.cornerMedium),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("شروع بازگردانی اطلاعات", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatChoiceCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.cornerMedium))
            .background(if (isSelected) accentColor.copy(alpha = 0.12f) else colors.surface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else colors.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(dimensions.cornerMedium)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) accentColor else colors.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}
