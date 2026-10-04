package com.manidigit.yadin.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions

@Composable
fun HelpScreen(onBack: () -> Unit) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(dimensions.screenPadding)
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

            Text(
                text = "راهنما و آموزش یادین",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
        }

        HelpStepCard(
            stepNumber = "۱",
            title = "به یادین خوش آمدید",
            description = "یادین یک سامانه مرور فاصله‌دار هوشمند (جعبه لایتنر مدرن) است که به شما کمک می‌کند واژگان زبان جدید را به حافظه بلندمدت بسپارید. واژه‌ها بر اساس برنامه علمی تکرار، سررسید می‌شوند."
        )

        HelpStepCard(
            stepNumber = "۲",
            title = "مرحله‌های یادگیری",
            description = "هر واژه از ۴ مرحله عبور می‌کند:\n• روزانه (Daily): شروع یادگیری، مرور هر روز\n• هفتگی (Weekly): با یک پاسخ درست، مرور بعد از ۷ روز\n• ماهانه (Monthly): با پاسخ درست بعدی، مرور بعد از ۳۰ روز\n• یادگرفته‌شده (Learned): تثبیت کامل در حافظه دائمی\n* هر زمان به واژه‌ای پاسخ غلط بدهید، بلافاصله به مرحله روزانه بازمی‌گردد."
        )

        HelpStepCard(
            stepNumber = "۳",
            title = "مرور هر روز و سررسید",
            description = "سیستم هر روز کارت‌هایی که تاریخ سررسید آن‌ها فرارسیده را در صف مرور قرار می‌دهد. مرور منظم و روزانه باعث افزایش رگبار (Streak) شما می‌شود."
        )

        HelpStepCard(
            stepNumber = "۴",
            title = "محاسبه سختی کلمه",
            description = "واژه‌ها دارای ۴ درجه سختی (ساده، متوسط، سخت، خیلی‌سخت) هستند. با ۳ پاسخ درست متوالی، سختی یک پله کمتر می‌شود و با ۳ پاسخ غلط متوالی، سختی یک پله افزایش می‌یابد."
        )

        HelpStepCard(
            stepNumber = "۵",
            title = "آزمون چهارگزینه‌ای",
            description = "می‌توانید علاوه بر فلش‌کارت، با آزمون‌های ۴ گزینه‌ای هوشمند خود را محک بزنید. گزینه‌های انحرافی آزمون با الگوریتم مشابهت انتخاب می‌شوند تا چالش واقعی ایجاد کنند."
        )

        HelpStepCard(
            stepNumber = "۶",
            title = "کتابخانه و ورود متنی",
            description = "در بخش کتابخانه به تمام ۶۰۰۰ واژه دسترسی دارید و می‌توانید آن‌ها را بر اساس دسته و مرحله فیلتر کنید. همچنین می‌توانید واژگان جدید را به صورت دستی یا ورود متنی با پارسر اضافه نمایید."
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun HelpStepCard(
    stepNumber: String,
    title: String,
    description: String
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    YadinCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    color = colors.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
