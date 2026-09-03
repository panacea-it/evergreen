package com.example.app.ui.upload

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.ui.theme.AppColors
import com.example.app.ui.theme.AppHeader
import com.example.app.ui.theme.AppIcons
import com.example.app.ui.theme.EvergreenTheme

@Composable
fun UploadExcelScreen(
    title: String,
    subtitle: String,
    companyName: String,
    showCompany: Boolean,
    companyLocked: Boolean,
    fileName: String?,
    templateLabel: String,
    onBackClick: () -> Unit,
    onCompanyClick: () -> Unit,
    onChooseFileClick: () -> Unit,
    onDownloadTemplateClick: () -> Unit,
    onUploadClick: () -> Unit
) {
    EvergreenTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            AppHeader(title = title, subtitle = subtitle, onLeadingClick = onBackClick)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppColors.surface)
                        .border(1.dp, AppColors.border, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    if (showCompany) {
                        FieldLabel("Company")
                        Spacer(modifier = Modifier.height(6.dp))
                        SelectRow(
                            value = companyName.ifBlank { "Select company" },
                            placeholder = companyName.isBlank(),
                            enabled = !companyLocked,
                            onClick = onCompanyClick
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    FieldLabel("Excel file")
                    Spacer(modifier = Modifier.height(6.dp))
                    SelectRow(
                        value = fileName ?: "Choose .xlsx file",
                        placeholder = fileName.isNullOrBlank(),
                        enabled = true,
                        onClick = onChooseFileClick
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    ActionButton(
                        label = templateLabel,
                        filled = false,
                        onClick = onDownloadTemplateClick
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ActionButton(
                        label = "Upload file",
                        filled = true,
                        onClick = onUploadClick
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
}

@Composable
private fun SelectRow(
    value: String,
    placeholder: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFDFDFF))
            .border(1.dp, AppColors.border, RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = AppColors.purple,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = if (placeholder) Color(0xFF8A91AC) else AppColors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionButton(label: String, filled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (filled) AppColors.blueDark else AppColors.blueLight)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (filled) Icons.Default.CloudUpload else AppIcons.scan,
                contentDescription = null,
                tint = if (filled) Color.White else AppColors.blue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (filled) Color.White else AppColors.blue
            )
        }
    }
}
