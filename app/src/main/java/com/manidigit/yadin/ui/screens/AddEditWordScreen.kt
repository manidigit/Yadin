package com.manidigit.yadin.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.manidigit.yadin.domain.model.WordDetail
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions

@Composable
fun AddEditWordScreen(
    initialWord: WordDetail? = null,
    categories: List<Category>,
    onSave: (sourceText: String, translations: List<String>, categoryId: String?, note: String?, pronunciation: String?) -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    var sourceText by remember { mutableStateOf(initialWord?.sourceContent?.text ?: "") }
    var translationsText by remember {
        mutableStateOf(initialWord?.targetContents?.joinToString("، ") { it.text } ?: "")
    }
    var pronunciation by remember { mutableStateOf(initialWord?.sourceContent?.pronunciation ?: "") }
    var note by remember { mutableStateOf(initialWord?.sourceContent?.note ?: "") }
    var selectedCategoryId by remember { mutableStateOf(initialWord?.concept?.categoryId) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = colors.onSurface
                    )
                }

                Text(
                    text = if (initialWord != null) "ویرایش واژه" else "افزودن واژه جدید",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }

            // Source Input
            OutlinedTextField(
                value = sourceText,
                onValueChange = {
                    sourceText = it
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("واژه به زبان مبدأ (اسپانیایی)") },
                placeholder = { Text("مثال: buenos días") },
                singleLine = true,
                shape = RoundedCornerShape(dimensions.cornerMedium)
            )

            // Translations Input
            OutlinedTextField(
                value = translationsText,
                onValueChange = {
                    translationsText = it
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("ترجمه‌های فارسی (با ویرگول جدا کنید)") },
                placeholder = { Text("مثال: صبح بخیر، روز خوش") },
                shape = RoundedCornerShape(dimensions.cornerMedium)
            )

            // Pronunciation Input
            OutlinedTextField(
                value = pronunciation,
                onValueChange = { pronunciation = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("تلفظ صوتی / فونتیک (اختیاری)") },
                placeholder = { Text("مثال: bwe-nos di-as") },
                singleLine = true,
                shape = RoundedCornerShape(dimensions.cornerMedium)
            )

            // Category Picker
            Text(
                text = "دسته‌بندی موضوعی:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    CategoryPill(
                        label = "بدون دسته",
                        isSelected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null }
                    )
                }
                items(categories) { cat ->
                    CategoryPill(
                        label = cat.name,
                        isSelected = selectedCategoryId == cat.id,
                        onClick = { selectedCategoryId = cat.id }
                    )
                }
            }

            // Note Input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("توضیحات و مثال کاربردی (اختیاری)") },
                placeholder = { Text("مثال: اصطلاح رایج در احوال‌پرسی روزانه") },
                minLines = 2,
                shape = RoundedCornerShape(dimensions.cornerMedium)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = colors.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Save Button
        Button(
            onClick = {
                val cleanSrc = sourceText.trim()
                val translationsList = translationsText.split("،", ",", "\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                if (cleanSrc.isEmpty()) {
                    errorMessage = "لطفاً واژه مبدأ را وارد کنید."
                } else if (translationsList.isEmpty()) {
                    errorMessage = "لطفاً حداقل یک ترجمه برای واژه وارد کنید."
                } else {
                    onSave(
                        cleanSrc,
                        translationsList,
                        selectedCategoryId,
                        note.trim().ifEmpty { null },
                        pronunciation.trim().ifEmpty { null }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(dimensions.cornerMedium),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("ذخیره در جعبه لایتنر", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CategoryPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(dimensions.cornerSmall))
            .background(if (isSelected) colors.primary else colors.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) colors.onPrimary else colors.onSurface,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
