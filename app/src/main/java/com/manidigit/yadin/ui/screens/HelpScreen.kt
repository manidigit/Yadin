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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = colors.onSurface
                )
            }

            Text(
                text = stringResource(R.string.help_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
        }

        HelpStepCard(
            stepNumber = stringResource(R.string.help_step1_number),
            title = stringResource(R.string.help_step1_title),
            description = stringResource(R.string.help_step1_desc)
        )

        HelpStepCard(
            stepNumber = stringResource(R.string.help_step2_number),
            title = stringResource(R.string.help_step2_title),
            description = stringResource(R.string.help_step2_desc)
        )

        HelpStepCard(
            stepNumber = stringResource(R.string.help_step3_number),
            title = stringResource(R.string.help_step3_title),
            description = stringResource(R.string.help_step3_desc)
        )

        HelpStepCard(
            stepNumber = stringResource(R.string.help_step4_number),
            title = stringResource(R.string.help_step4_title),
            description = stringResource(R.string.help_step4_desc)
        )

        HelpStepCard(
            stepNumber = stringResource(R.string.help_step5_number),
            title = stringResource(R.string.help_step5_title),
            description = stringResource(R.string.help_step5_desc)
        )

        HelpStepCard(
            stepNumber = stringResource(R.string.help_step6_number),
            title = stringResource(R.string.help_step6_title),
            description = stringResource(R.string.help_step6_desc)
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
