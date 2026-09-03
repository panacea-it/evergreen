package com.example.app.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.ui.theme.AppColors
import com.example.app.ui.theme.AppHeader
import com.example.app.ui.theme.AppHeaderAction
import com.example.app.ui.theme.AppIcons
import com.example.app.ui.theme.EvergreenTheme

data class ServiceReportItem(
    val id: Int,
    val number: String,
    val company: String,
    val taskId: String,
    val technician: String,
    val equipment: String,
    val status: String,
    val cost: String,
    val createdAt: String
)

@Composable
fun ServiceReportListScreen(
    reports: List<ServiceReportItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    onReportClick: (ServiceReportItem) -> Unit = {},
    onMoreClick: (ServiceReportItem) -> Unit = {}
) {
    EvergreenTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.background)
                .statusBarsPadding()
        ) {
            AppHeader(
                title = "Service Reports",
                subtitle = "Standalone and task-linked reports",
                leadingIcon = AppIcons.menu,
                leadingDescription = "Menu",
                onLeadingClick = onMenuClick,
                actions = listOf(
                    AppHeaderAction(AppIcons.add, "Create Service Report", onCreateClick),
                    AppHeaderAction(AppIcons.scan, "Scan", onScanClick),
                    AppHeaderAction(AppIcons.notifications, "Notifications", onNotificationClick)
                )
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.surface)
                    .border(1.dp, AppColors.border, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.textSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(color = AppColors.textPrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(AppColors.blue),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (searchQuery.isBlank()) {
                                Text("Search reports, company, technician...", color = AppColors.textSecondary, fontSize = 14.sp)
                            }
                            inner()
                        }
                    )
                }
            }
            if (reports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    com.example.app.ui.theme.AppEmptyState(
                        title = if (searchQuery.isBlank()) "No service reports yet" else "No matching reports",
                        subtitle = if (searchQuery.isBlank()) {
                            "Create a standalone report or generate one from a task."
                        } else {
                            "Try a different search."
                        },
                        actionLabel = if (searchQuery.isBlank()) "Create Service Report" else null,
                        onAction = if (searchQuery.isBlank()) onCreateClick else null
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reports, key = { it.id }) { report ->
                        ReportCard(
                            report = report,
                            onClick = { onReportClick(report) },
                            onMoreClick = { onMoreClick(report) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(
    report: ServiceReportItem,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.surface)
            .border(1.dp, AppColors.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.blueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = AppColors.blue, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.number.ifBlank { "Service Report" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
                Text(
                    text = report.company.ifBlank { "No company" },
                    fontSize = 13.sp,
                    color = AppColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            StatusChip(report.status)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Actions",
                tint = AppColors.textSecondary,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(onClick = onMoreClick)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        InfoRow("Task ID", report.taskId.ifBlank { "-" })
        InfoRow("Technician", report.technician.ifBlank { "-" })
        InfoRow("Equipment", report.equipment.ifBlank { "-" })
        InfoRow("Cost", report.cost.ifBlank { "-" })
        InfoRow("Created", report.createdAt.ifBlank { "-" })
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(text = "$label:", fontSize = 12.sp, color = AppColors.textSecondary, modifier = Modifier.width(86.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            color = AppColors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val background = when (status.lowercase()) {
        "completed", "closed" -> AppColors.greenLight
        "in progress" -> AppColors.blueLight
        else -> AppColors.orangeLight
    }
    val color = when (status.lowercase()) {
        "completed", "closed" -> AppColors.green
        "in progress" -> AppColors.blue
        else -> AppColors.orange
    }
    Text(
        text = status.ifBlank { "Not Started" },
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
