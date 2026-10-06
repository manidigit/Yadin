package com.manidigit.yadin.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    val context = LocalContext.current

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
                        text = "درباره یادین",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "اطلاعات برنامه، نسخه، سازنده و مشخصات فنی",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            // Hero Brand Banner
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(dimensions.cornerMedium))
                            .background(colors.heroGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "یا",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        )
                    }

                    Text(
                        text = "نرم‌افزار یادین (Yadin)",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )

                    Text(
                        text = "نسخه ۱.۱ (Build 682) • پلتفرم FlashLearn",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "سامانه مدرن یادگیری واژگان زبان اسپانیایی و فارسی بر پایه موتور مقتدر تکرار فاصله‌دار، آزمون‌های ۴ گزینه‌ای تطبیقی، ساختار آفلاین Room و سیستم پوسته چندگانه.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            // Technical Specs Card
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "مشخصات فنی و استانداردهای طراحی",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )

                    TechRow("سازنده / صاحب پروژه", "maniDigit")
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.25f))
                    TechRow("نسخه برنامه (Version Code)", "682 (v1.1-Production)")
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.25f))
                    TechRow("موتور تکرار فاصله‌دار", "Daily → Weekly → Monthly → Learned")
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.25f))
                    TechRow("الگوریتم سختی واژگان", "EASY / MEDIUM / HARD / VERY_HARD (آستانه ۳)")
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.25f))
                    TechRow("فرمول درصد پیشرفت", "مجموع ضرایب (۳۵، ۶۰، ۸۰، ۱۰۰) بر طبق بند ۶.۱۸")
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.25f))
                    TechRow("پایگاه داده محلی", "Android Room SQLite (Schema v2)")
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.25f))
                    TechRow("سیستم طراحی پوسته", "ThemeDesign v3.3-ADAPTIVE (GTP & Gemini)")
                }
            }

            // External Links: GitHub & WhatsApp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // GitHub Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(dimensions.cornerSmall))
                        .background(colors.surface)
                        .border(1.dp, colors.outline.copy(alpha = 0.4f), RoundedCornerShape(dimensions.cornerSmall))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/manidigit/FlashLearn"))
                            context.startActivity(intent)
                        }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "مخزن گیت‌هاب",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.onSurface
                        )
                    }
                }

                // WhatsApp Support Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(dimensions.cornerSmall))
                        .background(colors.surface)
                        .border(1.dp, colors.success.copy(alpha = 0.4f), RoundedCornerShape(dimensions.cornerSmall))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/34685644444"))
                            context.startActivity(intent)
                        }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = colors.success,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "پشتیبانی واتساپ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.success
                        )
                    }
                }
            }

            // Scientific Principles
            AboutFeatureCard(
                icon = Icons.Default.Psychology,
                title = "پشتوانه علمی و الگوریتم تکرار فاصله‌دار",
                description = "یادین بر پایه مدل علمی منحنی فراموشی ابینگهاوس (Ebbinghaus) طراحی شده است. فواصل زمانی مرورها (روزانه، هفتگی و ماهانه) به‌گونه‌ای زمان‌بندی می‌شوند که واژه‌ها در آستانه فراموشی تکرار شوند تا به حافظه بلندمدت منتقل گردند.",
                color = colors.info
            )

            // Difficulty & State Engine
            AboutFeatureCard(
                icon = Icons.Default.School,
                title = "موتور محاسبه پویای سختی واژگان",
                description = "هر واژه دارای ۴ سطح سختی (آسان، متوسط، سخت، خیلی سخت) است. سیستم با بررسی تاریخچه پاسخ‌ها و آستانه ۳ پاسخ متوالی، درجه سختی کلمات را به‌طور خودکار تنظیم و گزینه‌های آزمون را بر اساس این شاخص هوشمندانه انتخاب می‌نماید.",
                color = colors.warning
            )

            // Database & Offline First
            AboutFeatureCard(
                icon = Icons.Default.Storage,
                title = "بانک واژگان آفلاین و مستقل",
                description = "بیش از ۶,۰۰۰ واژه غنی و تخصصی زبان اسپانیایی همراه با ترجمه‌های دقیق فارسی، صوت هوشمند سیستم و یادداشت‌های کاربردی به‌صورت ۱۰۰٪ آفلاین و محلی بر روی دیتابیس Room ذخیره و پردازش می‌شوند.",
                color = colors.success
            )

            // Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "یادین • طراحی‌شده با ",
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = " برای شیفتگان زبان",
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant
                )
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
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Text("بازگشت", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TechRow(label: String, value: String) {
    val colors = LocalYadinColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = colors.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
    }
}

@Composable
fun AboutFeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
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
                    .size(44.dp)
                    .clip(RoundedCornerShape(dimensions.cornerSmall))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
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
                    lineHeight = 21.sp
                )
            }
        }
    }
}
