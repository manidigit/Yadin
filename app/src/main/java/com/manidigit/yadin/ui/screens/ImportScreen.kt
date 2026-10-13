package com.manidigit.yadin.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileUpload
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.domain.model.Category
import com.manidigit.yadin.domain.model.DuplicatePolicy
import com.manidigit.yadin.domain.model.ParseResult
import com.manidigit.yadin.domain.model.ParsedEntry
import com.manidigit.yadin.ui.components.ExposedCategoryDropdown
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions

import androidx.compose.ui.platform.LocalClipboardManager
import com.manidigit.yadin.domain.model.ImportSummary
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileOpen
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.provider.OpenableColumns
import android.net.Uri
import com.manidigit.yadin.domain.algorithm.FileReaders
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun ImportScreen(
    parseResult: ParseResult?,
    categories: List<Category> = emptyList(),
    isImporting: Boolean,
    importProgress: Float,
    importSummary: ImportSummary? = null,
    errorMessage: String? = null,
    onParseText: (String) -> Unit,
    onParseResult: ((ParseResult) -> Unit)? = null,
    onResetParse: (() -> Unit)? = null,
    onAddNewCategory: ((String, (String) -> Unit) -> Unit)? = null,
    onConfirmImport: (DuplicatePolicy, String?) -> Unit,
    onNavigateToReview: (() -> Unit)? = null,
    onGoToLibrary: () -> Unit = {},
    onBack: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var fileLoadError by remember { mutableStateOf<String?>(null) }

    var inputText by remember { mutableStateOf("") }
    var isShowingSample by remember { mutableStateOf(false) }
    var selectedPolicy by remember { mutableStateOf(DuplicatePolicy.MERGE) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

    val sampleText = """
        amigo: دوست (مذکر)
        casa: خانه، منزل
        libro: کتاب
        feliz: خوشحال، شاد
    """.trimIndent()

    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
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
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
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
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        fileLoadError = "خطا در خواندن فایل: ${e.message}"
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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = {
                    if (importSummary != null) {
                        onResetParse?.invoke()
                    } else if (parseResult != null && onResetParse != null) {
                        onResetParse()
                    } else {
                        onBack()
                    }
                },
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
                text = "ورود متنی واژگان (پارسر هوشمند)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
        }

        if (importSummary != null) {
            // SUCCESS REPORT CARD inside scrollable container
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
                            text = "گزارش ورود گروهی واژگان",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )

                        Text(
                            text = "عملیات ورود با موفقیت به پایان رسید و در پایگاه داده ثبت شد.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                                Text("کل ردیف‌های پردازش‌شده:", color = colors.onSurfaceVariant)
                                Text("${importSummary.totalProcessed} واژه", fontWeight = FontWeight.Bold, color = colors.onSurface)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("واژگان جدید افزوده شده:", color = colors.onSurfaceVariant)
                                Text("${importSummary.addedCount} واژه", fontWeight = FontWeight.Bold, color = colors.success)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("واژگان به‌روزشده یا ادغام‌شده:", color = colors.onSurfaceVariant)
                                Text("${importSummary.updatedCount} واژه", fontWeight = FontWeight.Bold, color = colors.info)
                            }
                            if (importSummary.skippedCount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("واژگان تکراری ردشده:", color = colors.onSurfaceVariant)
                                    Text("${importSummary.skippedCount} واژه", fontWeight = FontWeight.Bold, color = colors.warning)
                                }
                            }
                            if (!importSummary.categoryName.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("دسته‌بندی موضوعی:", color = colors.onSurfaceVariant)
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
                                Text("ورود دسته جدید", fontWeight = FontWeight.Bold)
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
                                Text("مشاهده در کتابخانه", fontWeight = FontWeight.Bold)
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
                    text = "متن واژگان را در کادر زیر وارد کنید (پشتیبانی از فرمت دو نقطه : یا خط تیره - و جداکننده کاما):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )

                // Quick Action Bar: Clear & Paste & File Picker & Sample buttons
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
                                fileLoadError = "خطا در خواندن کلیپ‌بورد: ${e.message}"
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
                                fileLoadError = "عدم امکان باز کردن انتخابگر فایل روی این دستگاه: ${e.localizedMessage ?: "برنامه مناسبی یافت نشد"}"
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
                        Text("انتخاب فایل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            Text("پاک", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            Text("نمونه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    label = "دسته‌بندی موضوعی برای این واژگان (اختیاری)"
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
                        Text(
                            "متن واژگان را اینجا کپی یا بنویسید...\nمثال:\namigo: دوست\ncasa: خانه\nlibro: کتاب"
                        )
                    },
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            IconButton(onClick = {
                                inputText = ""
                                isShowingSample = false
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "پاک کردن")
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
                    Text("پردازش و استخراج واژگان", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Preview parsed entries: Top Header & Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "پیش‌نمایش استخراج (${parseResult.entries.size} مدخل)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                if (onResetParse != null) {
                    OutlinedButton(
                        onClick = onResetParse,
                        shape = RoundedCornerShape(dimensions.cornerSmall)
                    ) {
                        Text("ویرایش مجدد متن", fontSize = 11.sp)
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
                label = "دسته‌بندی موضوعی واژگان واردشده (اختیاری)"
            )

            // Duplicate policy selection
            Text(
                text = "سیاست برخورد با واژگان تکراری:",
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
                        label = "ادغام (MERGE)",
                        isSelected = selectedPolicy == DuplicatePolicy.MERGE,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedPolicy = DuplicatePolicy.MERGE }
                    )
                    PolicyChip(
                        label = "مدخل جدید (SEPARATE)",
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
                        label = "صرف‌نظر (SKIP)",
                        isSelected = selectedPolicy == DuplicatePolicy.SKIP,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedPolicy = DuplicatePolicy.SKIP }
                    )
                    PolicyChip(
                        label = "جایگزینی (REPLACE)",
                        isSelected = selectedPolicy == DuplicatePolicy.REPLACE,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedPolicy = DuplicatePolicy.REPLACE }
                    )
                }
            }

            // Warning Card for discarded lines / parse warnings
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
                                text = "هشدار و خطوط کنارگذاشته‌شده (${parseResult.warnings.size} مورد):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.warning
                            )
                        }
                        parseResult.warnings.take(5).forEach { warn ->
                            Text(
                                text = "• $warn",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface
                            )
                        }
                        if (parseResult.warnings.size > 5) {
                            Text(
                                text = "... و ${parseResult.warnings.size - 5} مورد دیگر",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
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
                        text = "در حال ذخیره‌سازی... ${(importProgress * 100).toInt()}%",
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
                            text = "بازبینی و ویرایش لغات قبل از ذخیره (${parseResult.entries.size} مورد)",
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
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تأیید و ذخیره در پایگاه داده", fontWeight = FontWeight.Bold)
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
    val dimensions = LocalYadinDimensions.current

    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(dimensions.cornerSmall))
            .background(if (isSelected) colors.primary.copy(alpha = 0.15f) else colors.surface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(dimensions.cornerSmall)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) colors.primary else colors.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
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

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${(entry.confidence * 100).toInt()}% اطمینان",
                    fontSize = 10.sp,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}
