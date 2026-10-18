package com.manidigit.yadin.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.domain.algorithm.FileReaders
import com.manidigit.yadin.domain.model.Category
import com.manidigit.yadin.domain.model.DuplicatePolicy
import com.manidigit.yadin.domain.model.ImportSummary
import com.manidigit.yadin.domain.model.ParseResult
import com.manidigit.yadin.domain.model.ParsedEntry
import com.manidigit.yadin.ui.components.ExposedCategoryDropdown
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.components.YadinScreenHeader
import com.manidigit.yadin.ui.components.YadinSelectableChip
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.util.formatDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImportScreen(
    parseResult: ParseResult?,
    categories: List<Category>,
    isImporting: Boolean = false,
    importProgress: Float = 0f,
    errorMessage: String? = null,
    importSummary: ImportSummary? = null,
    onParseText: (String) -> Unit,
    onParseResult: ((ParseResult) -> Unit)? = null,
    onConfirmImport: (policy: DuplicatePolicy, categoryId: String?) -> Unit,
    onResetParse: (() -> Unit)? = null,
    onGoToLibrary: () -> Unit = {},
    onAddNewCategory: ((String, (String) -> Unit) -> Unit)? = null,
    onNavigateToReview: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    var fileLoadError by remember { mutableStateOf<String?>(null) }

    var inputText by remember { mutableStateOf("") }
    var isShowingSample by remember { mutableStateOf(false) }
    var selectedPolicy by remember { mutableStateOf(DuplicatePolicy.MERGE) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

    val sampleText = listOf(
        stringResource(R.string.import_sample_amigo),
        stringResource(R.string.import_sample_casa),
        stringResource(R.string.import_sample_libro),
        stringResource(R.string.import_sample_feliz)
    ).joinToString("\n")

    val coroutineScope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    withContext(Dispatchers.Main) {
                        fileLoadError = null
                    }
                    val contentResolver = context.contentResolver
                    val inputStream = contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        var displayName = ""
                        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIndex != -1 && cursor.moveToFirst()) {
                                displayName = cursor.getString(nameIndex) ?: ""
                            }
                        }
                        val lowerName = displayName.lowercase()
                        val result = when {
                            lowerName.endsWith(".csv") || lowerName.endsWith(".tsv") -> FileReaders.readCsv(inputStream)
                            lowerName.endsWith(".json") -> FileReaders.readJson(inputStream)
                            lowerName.endsWith(".xlsx") -> FileReaders.readXlsx(inputStream)
                            else -> FileReaders.readText(inputStream)
                        }
                        withContext(Dispatchers.Main) {
                            if (onParseResult != null) {
                                onParseResult(result)
                            } else {
                                inputText = result.entries.joinToString("\n") { e ->
                                    "${e.sourceText}: ${e.translations.joinToString("، ")}"
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        fileLoadError = context.getString(R.string.import_err_read_file, e.message ?: "")
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Standardized Header
        YadinScreenHeader(
            title = stringResource(R.string.import_title),
            onBack = {
                if (parseResult != null && onResetParse != null) {
                    onResetParse()
                } else {
                    onBack()
                }
            }
        )

        if (importSummary != null) {
            // SUCCESS REPORT CARD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                YadinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(colors.success.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colors.success,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = stringResource(R.string.import_summary_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )

                        Text(
                            text = stringResource(R.string.import_summary_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        // Stats Summary Grid
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(dimensions.cornerMedium))
                                .background(colors.surfaceVariant)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.import_total_rows), color = colors.onSurfaceVariant)
                                Text(stringResource(R.string.library_total_count, importSummary.totalProcessed.formatDigits()), fontWeight = FontWeight.Bold, color = colors.onSurface)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.import_new_added), color = colors.onSurfaceVariant)
                                Text(stringResource(R.string.library_total_count, importSummary.addedCount.formatDigits()), fontWeight = FontWeight.Bold, color = colors.success)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.import_updated_merged), color = colors.onSurfaceVariant)
                                Text(stringResource(R.string.library_total_count, importSummary.updatedCount.formatDigits()), fontWeight = FontWeight.Bold, color = colors.info)
                            }
                            if (importSummary.skippedCount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stringResource(R.string.import_skipped_duplicates), color = colors.onSurfaceVariant)
                                    Text(stringResource(R.string.library_total_count, importSummary.skippedCount.formatDigits()), fontWeight = FontWeight.Bold, color = colors.warning)
                                }
                            }
                            if (!importSummary.categoryName.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stringResource(R.string.import_category_label), color = colors.onSurfaceVariant)
                                    Text(importSummary.categoryName, fontWeight = FontWeight.Bold, color = colors.primary)
                                }
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    inputText = ""
                                    isShowingSample = false
                                    onResetParse?.invoke()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(dimensions.cornerMedium)
                            ) {
                                Text(stringResource(R.string.import_btn_new_batch), fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onGoToLibrary,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(dimensions.cornerMedium),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary,
                                    contentColor = colors.onPrimary
                                )
                            ) {
                                Text(stringResource(R.string.import_btn_view_library), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else if (parseResult == null) {
            // Input Form Area with Scroll
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.import_instructions),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )

                // Quick Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            try {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    inputText = clip
                                    isShowingSample = false
                                }
                            } catch (e: Exception) {
                                fileLoadError = context.getString(R.string.import_err_clipboard, e.message ?: "")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(dimensions.cornerSmall),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                filePickerLauncher.launch(arrayOf("*/*"))
                            } catch (e: Exception) {
                                fileLoadError = context.getString(R.string.import_err_file_picker, e.localizedMessage ?: "")
                            }
                        },
                        modifier = Modifier
                            .weight(1.1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(dimensions.cornerSmall),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.import_btn_select_file), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (inputText.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                inputText = ""
                                isShowingSample = false
                            },
                            modifier = Modifier
                                .weight(0.9f)
                                .height(42.dp),
                            shape = RoundedCornerShape(dimensions.cornerSmall),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(stringResource(R.string.import_btn_clear), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                inputText = sampleText
                                isShowingSample = true
                            },
                            modifier = Modifier
                                .weight(0.9f)
                                .height(42.dp),
                            shape = RoundedCornerShape(dimensions.cornerSmall),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(stringResource(R.string.import_btn_sample), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (fileLoadError != null) {
                    Text(
                        text = fileLoadError!!,
                        color = colors.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(dimensions.cornerSmall))
                            .background(colors.error.copy(alpha = 0.1f))
                            .padding(8.dp)
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = colors.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(dimensions.cornerSmall))
                            .background(colors.error.copy(alpha = 0.1f))
                            .padding(8.dp)
                    )
                }

                // Category selection BEFORE parsing
                ExposedCategoryDropdown(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onSelectCategory = { selectedCategoryId = it },
                    onAddNewCategory = { newName ->
                        onAddNewCategory?.invoke(newName) { newId ->
                            selectedCategoryId = newId
                        }
                    },
                    label = stringResource(R.string.import_default_category_label)
                )

                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        isShowingSample = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 280.dp),
                    placeholder = {
                        Text(stringResource(R.string.import_text_placeholder))
                    },
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            IconButton(onClick = {
                                inputText = ""
                                isShowingSample = false
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.action_clear))
                            }
                        }
                    },
                    shape = RoundedCornerShape(dimensions.cornerMedium)
                )

                Button(
                    onClick = { onParseText(inputText) },
                    enabled = inputText.trim().isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(dimensions.cornerMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.import_btn_parse), fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Preview parsed entries
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.import_preview_title, parseResult.entries.size.formatDigits()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                if (onResetParse != null) {
                    OutlinedButton(
                        onClick = onResetParse,
                        shape = RoundedCornerShape(dimensions.cornerSmall)
                    ) {
                        Text(stringResource(R.string.import_btn_edit_text), fontSize = 11.sp)
                    }
                }
            }

            // Category selection for imported words
            ExposedCategoryDropdown(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onSelectCategory = { selectedCategoryId = it },
                onAddNewCategory = { newName ->
                    onAddNewCategory?.invoke(newName) { newId ->
                        selectedCategoryId = newId
                    }
                },
                label = stringResource(R.string.import_default_category_label)
            )

            // Duplicate policy selection
            Text(
                text = stringResource(R.string.import_duplicate_policy_label),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PolicyChip(
                        label = stringResource(R.string.import_policy_merge),
                        isSelected = selectedPolicy == DuplicatePolicy.MERGE,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedPolicy = DuplicatePolicy.MERGE }
                    )
                    PolicyChip(
                        label = stringResource(R.string.import_policy_separate),
                        isSelected = selectedPolicy == DuplicatePolicy.KEEP_SEPARATE,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedPolicy = DuplicatePolicy.KEEP_SEPARATE }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PolicyChip(
                        label = stringResource(R.string.import_policy_skip),
                        isSelected = selectedPolicy == DuplicatePolicy.SKIP,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedPolicy = DuplicatePolicy.SKIP }
                    )
                    PolicyChip(
                        label = stringResource(R.string.import_policy_replace),
                        isSelected = selectedPolicy == DuplicatePolicy.REPLACE,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedPolicy = DuplicatePolicy.REPLACE }
                    )
                }
            }

            // Warning Card
            if (parseResult.warnings.isNotEmpty()) {
                YadinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.warning.copy(alpha = 0.12f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = colors.warning,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.import_warnings_title, parseResult.warnings.size.formatDigits()),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.warning
                            )
                        }
                        for (warn in parseResult.warnings.take(5)) {
                            Text(
                                text = "• $warn",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface
                            )
                        }
                        if (parseResult.warnings.size > 5) {
                            Text(
                                text = stringResource(R.string.import_more_warnings, (parseResult.warnings.size - 5).formatDigits()),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Warning Banner for Items Needing Manual Review
            val itemsNeedingReview = parseResult.entries.count { it.confidence < 0.98 || it.possibleCorrection != null }
            if (itemsNeedingReview > 0 && onNavigateToReview != null) {
                YadinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.warning.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = colors.warning, modifier = Modifier.size(20.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.import_screen_review_warning_title, itemsNeedingReview.formatDigits()),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = colors.warning
                                )
                                Text(
                                    text = stringResource(R.string.import_screen_review_warning_desc),
                                    fontSize = 11.sp,
                                    color = colors.onSurface
                                )
                            }
                        }
                        Button(
                            onClick = onNavigateToReview,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.warning, contentColor = colors.surface),
                            shape = RoundedCornerShape(dimensions.cornerSmall)
                        ) {
                            Text(stringResource(R.string.import_screen_btn_manual_review), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Parsed entries LazyColumn
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = parseResult.entries,
                    key = { index, entry -> "${index}_${entry.lineNumber}_${entry.sourceText.hashCode()}" }
                ) { _, entry ->
                    ParsedEntryCard(
                        entry = entry,
                        categoryName = selectedCategory?.name
                    )
                }
            }

            if (isImporting) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { importProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = colors.primary,
                        trackColor = colors.surfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.import_saving_progress, (importProgress * 100).toInt().formatDigits()),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.primary
                    )
                }
            } else {
                if (onNavigateToReview != null && parseResult.entries.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onNavigateToReview,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(dimensions.cornerMedium)
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.import_review_before_save, parseResult.entries.size.formatDigits()),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = { onConfirmImport(selectedPolicy, selectedCategoryId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(dimensions.cornerMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.success,
                        contentColor = colors.onSuccess
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.import_btn_confirm_save), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PolicyChip(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    YadinSelectableChip(
        text = label,
        isSelected = isSelected,
        accentColor = colors.primary,
        preserveAccentWhenUnselected = true,
        minHeight = 40.dp,
        fontSize = 12.sp,
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
fun ParsedEntryCard(
    entry: ParsedEntry,
    categoryName: String? = null
) {
    val colors = LocalYadinColors.current

    YadinCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = entry.sourceText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    if (!categoryName.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = categoryName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primary
                            )
                        }
                    }
                }
                Text(
                    text = entry.translations.joinToString("، "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.primary
                )
                if (!entry.note.isNullOrBlank()) {
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            val confPercent = (entry.confidence * 100).toInt()
            val confColor = when {
                confPercent >= 95 -> colors.success
                confPercent >= 80 -> colors.primary
                else -> colors.warning
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(confColor.copy(alpha = 0.12f))
                    .border(1.dp, confColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.import_review_confidence, confPercent.formatDigits()),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = confColor
                )
            }
        }
    }
}
