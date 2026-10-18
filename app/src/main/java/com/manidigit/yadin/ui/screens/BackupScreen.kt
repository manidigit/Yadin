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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.data.repository.BackupExportFormat
import com.manidigit.yadin.data.repository.BackupOptions
import com.manidigit.yadin.data.repository.BackupType
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.components.YadinScreenHeader
import com.manidigit.yadin.ui.components.YadinSelectableChip
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.util.formatDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
    internalBackups: List<File> = emptyList(),
    onUpdateBackupOptions: (BackupOptions) -> Unit = {},
    onToggleFullBackup: (Boolean) -> Unit = {},
    onToggleOption: (String, Boolean) -> Unit = { _, _ -> },
    onExportCustomBackup: ((BackupExportFormat, BackupOptions) -> Unit)? = null,
    onExportCustomBackupToUri: ((BackupExportFormat, android.net.Uri, BackupOptions) -> Unit)? = null,
    onExportBackup: (BackupType) -> Unit = {},
    onExportBackupToUri: ((BackupType, android.net.Uri) -> Unit)? = null,
    onShareBackupFile: ((File) -> Unit)? = null,
    onDeleteBackupFile: ((File) -> Unit)? = null,
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
                            val kb = (text.length / 1024).coerceAtLeast(1)
                            loadedFileName = context.getString(R.string.backup_file_info_fmt, kb.formatDigits(), "")
                            Toast.makeText(context, context.getString(R.string.backup_file_loaded_toast), Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.backup_file_error_toast, e.message ?: ""), Toast.LENGTH_LONG).show()
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
            // Standardized Header
            YadinScreenHeader(
                title = stringResource(R.string.backup_title),
                subtitle = stringResource(R.string.backup_subtitle),
                onBack = onBack
            )

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
                                text = progressMessage.ifEmpty { stringResource(R.string.backup_progress_default) },
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

            // Result Banner (نمایش نتیجه عملیات پشتیبان‌گیری یا بازیابی)
            AnimatedVisibility(
                visible = !lastResult.isNullOrBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (!lastResult.isNullOrBlank()) {
                    val containerColor = if (isError) colors.error.copy(alpha = 0.12f) else colors.success.copy(alpha = 0.12f)
                    val contentColor = if (isError) colors.error else colors.success
                    val borderColor = if (isError) colors.error.copy(alpha = 0.35f) else colors.success.copy(alpha = 0.35f)
                    val icon = if (isError) Icons.Default.Warning else Icons.Default.CheckCircle

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(containerColor)
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = lastResult,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = contentColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Tabs Selector (Standardized M3 Segmented Chips)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    title = stringResource(R.string.backup_tab_backup),
                    icon = Icons.Default.CloudUpload,
                    isSelected = activeTab == 0,
                    modifier = Modifier.weight(1f)
                ) {
                    activeTab = 0
                }

                TabButton(
                    title = stringResource(R.string.backup_tab_restore),
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
                                text = stringResource(R.string.backup_db_status_title),
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
                                label = stringResource(R.string.backup_db_total_words),
                                value = totalConcepts.formatDigits(),
                                icon = Icons.Default.Translate,
                                modifier = Modifier.weight(1f)
                            )
                            StatBox(
                                label = stringResource(R.string.backup_db_categories),
                                value = totalCategories.formatDigits(),
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
                                        text = stringResource(R.string.backup_scope_title),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.backup_scope_subtitle),
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
                                .clickable {
                                    val nextState = !backupOptions.fullBackup
                                    onToggleFullBackup(nextState)
                                    onUpdateBackupOptions(backupOptions.copy(fullBackup = nextState))
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.backup_full_title),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                                Text(
                                    text = stringResource(R.string.backup_full_subtitle),
                                    fontSize = 11.sp,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = backupOptions.fullBackup,
                                onCheckedChange = {
                                    onToggleFullBackup(it)
                                    onUpdateBackupOptions(backupOptions.copy(fullBackup = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.primary,
                                    checkedTrackColor = colors.primary.copy(alpha = 0.4f)
                                )
                            )
                        }

                        // Granular Checkboxes
                        BackupOptionCheckboxRow(
                            title = stringResource(R.string.backup_vocab_title),
                            subtitle = stringResource(R.string.backup_vocab_sub),
                            icon = Icons.Default.Translate,
                            isChecked = backupOptions.includeVocabulary,
                            onCheckedChange = { onToggleOption("vocabulary", it) }
                        )

                        BackupOptionCheckboxRow(
                            title = stringResource(R.string.backup_cat_title),
                            subtitle = stringResource(R.string.backup_cat_sub),
                            icon = Icons.Default.Category,
                            isChecked = backupOptions.includeCategories,
                            onCheckedChange = { onToggleOption("categories", it) }
                        )

                        BackupOptionCheckboxRow(
                            title = stringResource(R.string.backup_diff_title),
                            subtitle = stringResource(R.string.backup_diff_sub),
                            icon = Icons.Default.Speed,
                            isChecked = backupOptions.includeDifficulty,
                            onCheckedChange = { onToggleOption("difficulty", it) }
                        )

                        BackupOptionCheckboxRow(
                            title = stringResource(R.string.backup_streak_title),
                            subtitle = stringResource(R.string.backup_streak_sub),
                            icon = Icons.Default.LocalFireDepartment,
                            isChecked = backupOptions.includeStreakAndProgress,
                            onCheckedChange = { onToggleOption("streak", it) }
                        )

                        BackupOptionCheckboxRow(
                            title = stringResource(R.string.backup_leitner_title),
                            subtitle = stringResource(R.string.backup_leitner_sub),
                            icon = Icons.Default.School,
                            isChecked = backupOptions.includeReviewStats,
                            onCheckedChange = { onToggleOption("reviewStats", it) }
                        )

                        BackupOptionCheckboxRow(
                            title = stringResource(R.string.backup_process_title),
                            subtitle = stringResource(R.string.backup_process_sub),
                            icon = Icons.Default.History,
                            isChecked = backupOptions.includeProcessHistory,
                            onCheckedChange = { onToggleOption("process", it) }
                        )

                        BackupOptionCheckboxRow(
                            title = stringResource(R.string.backup_settings_title),
                            subtitle = stringResource(R.string.backup_settings_sub),
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
                                text = stringResource(R.string.backup_format_title),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        }

                        FormatChoiceCard(
                            title = stringResource(R.string.backup_fmt_json_title),
                            subtitle = stringResource(R.string.backup_fmt_json_sub),
                            icon = Icons.Default.Description,
                            isSelected = selectedFormat == BackupExportFormat.JSON,
                            onClick = { selectedFormat = BackupExportFormat.JSON }
                        )

                        FormatChoiceCard(
                            title = stringResource(R.string.backup_fmt_csv_vocab_title),
                            subtitle = stringResource(R.string.backup_fmt_csv_vocab_sub),
                            icon = Icons.Default.TableChart,
                            isSelected = selectedFormat == BackupExportFormat.EXCEL_CSV_VOCABULARY,
                            onClick = { selectedFormat = BackupExportFormat.EXCEL_CSV_VOCABULARY }
                        )

                        FormatChoiceCard(
                            title = stringResource(R.string.backup_fmt_csv_streak_title),
                            subtitle = stringResource(R.string.backup_fmt_csv_streak_sub),
                            icon = Icons.Default.BarChart,
                            isSelected = selectedFormat == BackupExportFormat.EXCEL_CSV_PROGRESS,
                            onClick = { selectedFormat = BackupExportFormat.EXCEL_CSV_PROGRESS }
                        )

                        FormatChoiceCard(
                            title = stringResource(R.string.backup_fmt_csv_master_title),
                            subtitle = stringResource(R.string.backup_fmt_csv_master_sub),
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
                            try {
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
                            } catch (e: Exception) {
                                Toast.makeText(context, context.getString(R.string.backup_file_manager_error, e.message ?: ""), Toast.LENGTH_LONG).show()
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
                        Text(stringResource(R.string.backup_btn_save_to), fontSize = 13.sp)
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
                            tint = colors.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedFormat == BackupExportFormat.JSON) stringResource(R.string.backup_btn_create_json) else stringResource(R.string.backup_btn_export_excel),
                            color = colors.onPrimary,
                            fontSize = 13.sp
                        )
                    }
                }

                // Saved Backups and Reports in internal storage
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
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.backup_internal_files_title, internalBackups.size.formatDigits()),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        }

                        if (internalBackups.isEmpty()) {
                            Text(
                                text = stringResource(R.string.backup_internal_files_empty),
                                fontSize = 12.sp,
                                color = colors.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        } else {
                            internalBackups.forEach { file ->
                                SavedBackupItemRow(
                                    file = file,
                                    onShare = { onShareBackupFile?.invoke(file) },
                                    onDelete = { onDeleteBackupFile?.invoke(file) }
                                )
                            }
                        }
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
                                text = stringResource(R.string.backup_restore_card_title),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        }

                        Text(
                            text = stringResource(R.string.backup_restore_card_desc),
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        // File picker button
                        OutlinedButton(
                            onClick = {
                                try {
                                    importFileLauncher.launch("*/*")
                                } catch (e: Exception) {
                                    Toast.makeText(context, context.getString(R.string.import_err_file_picker, e.message ?: ""), Toast.LENGTH_LONG).show()
                                }
                            },
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
                                text = loadedFileName ?: stringResource(R.string.backup_restore_select_file),
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
                                text = stringResource(R.string.backup_restore_strategy_title),
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
                                        text = stringResource(R.string.backup_strategy_merge_title),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.backup_strategy_merge_desc),
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
                                        text = stringResource(R.string.backup_strategy_replace_title),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.error
                                    )
                                    Text(
                                        text = stringResource(R.string.backup_strategy_replace_desc),
                                        fontSize = 11.sp,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { showConfirmRestoreDialog = true },
                                enabled = !isProcessing,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isReplaceMode) colors.error else colors.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = if (isReplaceMode) colors.onError else colors.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isReplaceMode) stringResource(R.string.backup_btn_start_replace) else stringResource(R.string.backup_btn_start_merge),
                                    color = if (isReplaceMode) colors.onError else colors.onPrimary,
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
            onDismissRequest = { if (!isProcessing) showConfirmRestoreDialog = false },
            title = {
                Text(
                    text = if (isReplaceMode) stringResource(R.string.backup_dialog_replace_title) else stringResource(R.string.backup_dialog_merge_title),
                    fontWeight = FontWeight.Bold,
                    color = if (isReplaceMode) colors.error else colors.onSurface
                )
            },
            text = {
                Text(
                    text = if (isReplaceMode)
                        stringResource(R.string.backup_dialog_replace_msg)
                    else
                        stringResource(R.string.backup_dialog_merge_msg),
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isProcessing) {
                            showConfirmRestoreDialog = false
                            loadedFileContent?.let { content ->
                                onRestoreBackup(content, isReplaceMode)
                            }
                        }
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReplaceMode) colors.error else colors.primary
                    )
                ) {
                    Text(stringResource(R.string.backup_btn_yes_proceed), color = if (isReplaceMode) colors.onError else colors.onPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmRestoreDialog = false },
                    enabled = !isProcessing
                ) {
                    Text(stringResource(R.string.action_cancel), color = colors.onSurfaceVariant)
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
                checkmarkColor = colors.onPrimary
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
    YadinSelectableChip(
        text = title,
        icon = icon,
        isSelected = isSelected,
        accentColor = colors.primary,
        preserveAccentWhenUnselected = true,
        minHeight = 42.dp,
        fontSize = 13.sp,
        modifier = modifier,
        onClick = onClick
    )
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
            .background(colors.primary.copy(alpha = 0.08f))
            .border(1.dp, colors.primary.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
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
                tint = if (isSelected) colors.onPrimary else colors.onSurfaceVariant,
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

@Composable
private fun SavedBackupItemRow(
    file: File,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalYadinColors.current
    val isCsv = file.name.endsWith(".csv", ignoreCase = true)
    val sizeKb = (file.length() / 1024).coerceAtLeast(1)
    val dateStr = try {
        java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(file.lastModified()))
    } catch (_: Exception) {
        ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceVariant.copy(alpha = 0.6f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isCsv) colors.secondary.copy(alpha = 0.15f) else colors.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCsv) Icons.Default.TableChart else Icons.Default.Description,
                contentDescription = null,
                tint = if (isCsv) colors.secondary else colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 1
            )
            Text(
                text = stringResource(R.string.backup_file_info_fmt, sizeKb.formatDigits(), dateStr),
                fontSize = 10.sp,
                color = colors.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onShare,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = stringResource(R.string.action_share),
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.action_delete),
                tint = colors.error,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
