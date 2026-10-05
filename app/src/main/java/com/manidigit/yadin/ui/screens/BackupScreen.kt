package com.manidigit.yadin.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.data.repository.BackupType
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import java.util.Locale

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
    val clipboardManager = LocalClipboardManager.current

    var selectedBackupType by remember { mutableStateOf(BackupType.FULL) }
    var loadedJsonContent by remember { mutableStateOf<String?>(null) }
    var loadedFileName by remember { mutableStateOf<String?>(null) }
    var loadedFileSize by remember { mutableStateOf<String?>(null) }
    var manualJsonText by remember { mutableStateOf("") }
    var isReplaceMode by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(0) } // 0 = Export, 1 = Restore

    val exportDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && onExportBackupToUri != null) {
            onExportBackupToUri(selectedBackupType, uri)
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
                        loadedJsonContent = text
                        loadedFileName = "فایل پشتیبان انتخاب‌شده"
                        val sizeKb = text.length / 1024
                        loadedFileSize = if (sizeKb > 1024) String.format(Locale.US, "%.1f مگابایت", sizeKb / 1024.0) else "$sizeKb کیلوبایت"
                        manualJsonText = ""
                        Toast.makeText(context, "فایل پشتیبان با موفقیت بارگذاری شد ($loadedFileSize)", Toast.LENGTH_SHORT).show()
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
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = colors.onSurface
                    )
                }

                Column {
                    Text(
                        text = "پشتیبان‌گیری و بازیابی داده‌ها",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "صادرات و بازگردانی امن بر اساس Schema v2",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            // Database Overview Card
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(dimensions.cornerSmall))
                            .background(colors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "بانک داده‌های محلی یادین",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            text = "$totalConcepts واژه فعال • $totalCategories دسته‌بندی موضوعی",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }

            // Tab Selector: ساخت پشتیبان / بازیابی
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
                            text = "تهیه پشتیبان (Export)",
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

            // Status / Progress indicator
            if (isProcessing) {
                YadinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = colors.primary,
                            trackColor = colors.primary.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            // Success / Error Message Banner
            if (!isProcessing && lastResult != null) {
                YadinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (isError) colors.error.copy(alpha = 0.12f) else colors.success.copy(alpha = 0.12f),
                    borderStroke = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isError) colors.error.copy(alpha = 0.4f) else colors.success.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isError) colors.error else colors.success,
                            modifier = Modifier.size(24.dp)
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
                // EXPORT TAB
                Text(
                    text = "نوع فایل پشتیبان",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BackupTypeOption(
                        type = BackupType.FULL,
                        title = "پشتیبان کامل (FULL)",
                        description = "شامل همه واژگان، ترجمه‌ها، مراحل لایتنر، زنجیره روزانه و تاریخچه مرورها",
                        isSelected = selectedBackupType == BackupType.FULL,
                        onClick = { selectedBackupType = BackupType.FULL }
                    )
                    BackupTypeOption(
                        type = BackupType.VOCABULARY,
                        title = "فقط بانک واژگان (VOCABULARY)",
                        description = "شامل لغات، معانی و دسته‌بندی‌ها بدون تاریخچه تمرین‌های کاربر",
                        isSelected = selectedBackupType == BackupType.VOCABULARY,
                        onClick = { selectedBackupType = BackupType.VOCABULARY }
                    )
                    BackupTypeOption(
                        type = BackupType.PROGRESS,
                        title = "فقط سوابق پیشرفت (PROGRESS)",
                        description = "شامل سطوح سختی، تاریخچه جلسات و مراحل تکرار فاصله‌دار",
                        isSelected = selectedBackupType == BackupType.PROGRESS,
                        onClick = { selectedBackupType = BackupType.PROGRESS }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 1. SAF Storage Picker Export (User selects destination folder and name)
                Button(
                    onClick = {
                        val fileName = "yadin_backup_${selectedBackupType.name.lowercase()}_${System.currentTimeMillis()}.json"
                        exportDocumentLauncher.launch(fileName)
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(dimensions.cornerMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.SaveAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("انتخاب پوشه و ذخیره فایل پشتیبان در دستگاه", fontWeight = FontWeight.Bold)
                }

                // 2. Fallback quick save to internal storage
                OutlinedButton(
                    onClick = { onExportBackup(selectedBackupType) },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(dimensions.cornerMedium)
                ) {
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ذخیره سریع در حافظه داخلی برنامه", fontSize = 13.sp)
                }
            } else {
                // RESTORE TAB
                Text(
                    text = "بازگردانی اطلاعات از فایل یا متن JSON",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )

                // 1. Prominent File Picker Button
                Button(
                    onClick = { importFileLauncher.launch("*/*") },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(dimensions.cornerMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.secondary,
                        contentColor = colors.onSecondary
                    )
                ) {
                    Icon(imageVector = Icons.Default.FileOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("انتخاب و بارگذاری فایل پشتیبان (JSON) از دستگاه", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (loadedJsonContent != null) {
                    // Loaded File Status Card (Optimized: No heavy TextField layout for multi-megabyte JSONs!)
                    YadinCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.success.copy(alpha = 0.12f),
                        borderStroke = BorderStroke(1.5.dp, colors.success)
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
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.success)
                                Column {
                                    Text(
                                        text = loadedFileName ?: "فایل پشتیبان آماده است",
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "حجم: ${loadedFileSize ?: ""} • آماده بازیابی کامل",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.success
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    loadedJsonContent = null
                                    loadedFileName = null
                                    loadedFileSize = null
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "حذف فایل بارگذاری‌شده",
                                    tint = colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // Manual JSON Text Input / Clipboard
                    Text(
                        text = "یا متن فایل JSON را مستقیماً در کادر زیر وارد کنید:",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = manualJsonText,
                        onValueChange = { manualJsonText = it },
                        label = { Text("محتوای فایل پشتیبان JSON") },
                        placeholder = { Text("{\"format\":\"yadin-backup\", ...}") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(dimensions.cornerSmall)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboardText = clipboardManager.getText()?.text ?: ""
                                if (clipboardText.isNotEmpty()) {
                                    if (clipboardText.length > 50000) {
                                        loadedJsonContent = clipboardText
                                        loadedFileName = "متن پشتیبان از کلیپ‌بورد"
                                        val sizeKb = clipboardText.length / 1024
                                        loadedFileSize = if (sizeKb > 1024) String.format(Locale.US, "%.1f مگابایت", sizeKb / 1024.0) else "$sizeKb کیلوبایت"
                                        manualJsonText = ""
                                    } else {
                                        manualJsonText = clipboardText
                                        loadedJsonContent = null
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.surfaceVariant,
                                contentColor = colors.onSurface
                            )
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("چسباندن از کلیپ‌بورد", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "روش ادغام اطلاعات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimensions.cornerSmall))
                        .background(colors.surface)
                        .border(1.dp, colors.outline.copy(alpha = 0.35f), RoundedCornerShape(dimensions.cornerSmall))
                        .clickable { isReplaceMode = false }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RadioButton(
                        selected = !isReplaceMode,
                        onClick = { isReplaceMode = false },
                        colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                    )
                    Column {
                        Text("ادغام هوشمند (Merge)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.onSurface)
                        Text("واژه‌های جدید اضافه می‌شوند و واژگان موجود حفظ خواهند شد", fontSize = 11.sp, color = colors.onSurfaceVariant)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimensions.cornerSmall))
                        .background(colors.surface)
                        .border(1.dp, colors.outline.copy(alpha = 0.35f), RoundedCornerShape(dimensions.cornerSmall))
                        .clickable { isReplaceMode = true }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RadioButton(
                        selected = isReplaceMode,
                        onClick = { isReplaceMode = true },
                        colors = RadioButtonDefaults.colors(selectedColor = colors.error)
                    )
                    Column {
                        Text("جایگزینی کامل (Replace)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.error)
                        Text("تمام داده‌های قبلی پاک شده و نسخه پشتیبان جایگزین می‌گردد", fontSize = 11.sp, color = colors.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val restoreTarget = loadedJsonContent ?: manualJsonText
                val canRestore = !isProcessing && restoreTarget.isNotBlank()

                Button(
                    onClick = {
                        if (canRestore) {
                            onRestoreBackup(restoreTarget, isReplaceMode)
                        }
                    },
                    enabled = canRestore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(dimensions.cornerMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReplaceMode) colors.error else colors.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isReplaceMode) "تأیید و جایگزینی کامل اطلاعات" else "شروع بازیابی و ادغام داده‌ها",
                        fontWeight = FontWeight.Bold
                    )
                }
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
                containerColor = colors.surfaceVariant,
                contentColor = colors.onSurface
            )
        ) {
            Text("بازگشت", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BackupTypeOption(
    type: BackupType,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensions.cornerSmall))
            .background(if (isSelected) colors.primary.copy(alpha = 0.12f) else colors.surface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(dimensions.cornerSmall)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) colors.primary else colors.onSurface,
                    fontSize = 13.sp
                )
                Text(
                    text = description,
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
