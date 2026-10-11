package com.manidigit.yadin.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import kotlinx.coroutines.launch

data class TutorialPage(
    val title: String,
    val description: String,
    val icon: ImageVector
)

val tutorialPages = listOf(
    TutorialPage(
        title = "به یادین خوش آمدید!",
        description = "سامانه هوشمند یادگیری و مرور واژگان بر پایه جعبه لایتنر و منحنی فراموشی ابینگهاوس.",
        icon = Icons.Default.School
    ),
    TutorialPage(
        title = "مرور علمی با فلش‌کارت",
        description = "کارت‌ها بر اساس درجه سختی و پاسخ‌های شما در ۵ مرحله لایتنر زمان‌بندی می‌شوند تا تثبیت شوند.",
        icon = Icons.Default.Style
    ),
    TutorialPage(
        title = "آزمون هوشمند ۴ گزینه‌ای",
        description = "بررسی میزان تسلط با گزینه‌های انحرافی هوشمند و بر اساس لول‌های آسان، متوسط و سخت.",
        icon = Icons.Default.Quiz
    ),
    TutorialPage(
        title = "تنظیم درجه سختی واژگان",
        description = "تعیین سختی واژگان (آسان، متوسط، سخت، خیلی سخت) برای شخصی‌سازی زمان‌بندی مرورها.",
        icon = Icons.Default.Psychology
    ),
    TutorialPage(
        title = "حفظ زنجیره روزهای مطالعه (Streak)",
        description = "با مطالعه مداوم روزانه زنجیره آتشین مطالعه خود را نگه دارید و مدال‌های افتخار کسب کنید.",
        icon = Icons.Default.LocalFireDepartment
    ),
    TutorialPage(
        title = "آماده شروع یادگیری هستید؟",
        description = "واژگان اولیه خود را وارد کنید یا از بانک اختصاصی یادین برای شروع مرور استفاده نمایید.",
        icon = Icons.Default.AutoAwesome
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialScreen(
    onFinishTutorial: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    val pagerState = rememberPagerState(pageCount = { tutorialPages.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Row Skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (pagerState.currentPage < tutorialPages.size - 1) {
                TextButton(onClick = onFinishTutorial) {
                    Text(
                        text = "رد کردن",
                        color = colors.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        // Pager Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { pageIndex ->
            val page = tutorialPages[pageIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(60.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bottom Controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Page Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(tutorialPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) colors.primary else colors.surfaceVariant)
                    )
                }
            }

            // Next or Finish Button
            Button(
                onClick = {
                    if (pagerState.currentPage < tutorialPages.size - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinishTutorial()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(dimensions.cornerMedium),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text(
                    text = if (pagerState.currentPage < tutorialPages.size - 1) "بعدی" else "شروع برنامه",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimary
                )
            }
        }
    }
}
