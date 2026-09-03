package com.example.app.ui.equipment

import android.widget.ImageView
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.example.app.ui.theme.AppColors
import com.example.app.ui.theme.AppEmptyState
import com.example.app.ui.theme.AppHeader
import com.example.app.ui.theme.AppHeaderAction
import com.example.app.ui.theme.AppIcons
import com.example.app.ui.theme.EvergreenTheme
import com.prod.evergreen.helper.MediaUrl

data class EquipmentHistoryItem(
    val id: String,
    val date: String,
    val technician: String,
    val title: String,
    val status: String,
    val canSelfAssign: Boolean
)

data class EquipmentDetailsData(
    val name: String = "",
    val companyName: String = "",
    val make: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val egSerial: String = "",
    val year: String = "",
    val location: String = "",
    val frequency: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val history: List<EquipmentHistoryItem> = emptyList()
)

@Composable
fun EquipmentDetailsScreen(
    equipment: EquipmentDetailsData,
    errorMessage: String? = null,
    showEdit: Boolean,
    showCreateTask: Boolean,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onCreateTaskClick: () -> Unit = {},
    onDownloadQrClick: () -> Unit = {},
    onHistoryClick: (EquipmentHistoryItem) -> Unit = {},
    onSelfAssignClick: (EquipmentHistoryItem) -> Unit = {}
) {
    EvergreenTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            AppHeader(
                title = equipment.name.ifBlank { "Equipment" },
                subtitle = equipment.companyName.ifBlank { "Equipment details" },
                onLeadingClick = onBackClick,
                actions = buildList {
                    if (showEdit) add(AppHeaderAction(Icons.Default.Edit, "Edit", onEditClick))
                    if (showCreateTask) add(AppHeaderAction(AppIcons.add, "Create task", onCreateTaskClick))
                    add(AppHeaderAction(AppIcons.scan, "Download QR", onDownloadQrClick))
                }
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                if (!errorMessage.isNullOrBlank() && equipment.name.isBlank()) {
                    AppEmptyState(
                        title = "Equipment not found",
                        subtitle = errorMessage
                    )
                } else {
                    EquipmentImage(equipment.imageUrl, equipment.name)
                    Spacer(modifier = Modifier.height(12.dp))
                    SpecCard(equipment)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Repair history", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (equipment.history.isEmpty()) {
                        AppEmptyState(
                            title = "No tasks yet",
                            subtitle = "Tasks for this equipment will show here."
                        )
                    } else {
                        equipment.history.forEach { item ->
                            HistoryRow(
                                item = item,
                                onClick = { onHistoryClick(item) },
                                onSelfAssignClick = { onSelfAssignClick(item) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EquipmentImage(imageUrl: String?, name: String) {
    val resolved = MediaUrl.resolve(imageUrl)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE6EEF7)),
        contentAlignment = Alignment.Center
    ) {
        if (resolved.isNotBlank()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
                },
                update = { imageView ->
                    Glide.with(imageView).load(resolved).into(imageView)
                }
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF7890A8), modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text(name.ifBlank { "Equipment" }, color = Color(0xFF718097), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SpecCard(equipment: EquipmentDetailsData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface)
            .border(1.dp, AppColors.border, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        SpecRow("Make", equipment.make)
        SpecRow("Model", equipment.model)
        SpecRow("Serial number", equipment.serialNumber)
        SpecRow("EG serial", equipment.egSerial)
        SpecRow("Year", equipment.year)
        SpecRow("Location", equipment.location)
        SpecRow("PM frequency", equipment.frequency)
        SpecRow("Description", equipment.description, last = true)
    }
}

@Composable
private fun SpecRow(label: String, value: String, last: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = AppColors.textSecondary, modifier = Modifier.width(120.dp))
        Text(
            text = value.ifBlank { "-" },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.textPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
    if (!last) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AppColors.border)
        )
    }
}

@Composable
private fun HistoryRow(
    item: EquipmentHistoryItem,
    onClick: () -> Unit,
    onSelfAssignClick: () -> Unit
) {
    val statusColor = when (item.status.lowercase()) {
        "open" -> AppColors.green
        "hold" -> AppColors.orange
        "in_progress", "in progress" -> AppColors.blue
        "closed" -> AppColors.purple
        else -> AppColors.textSecondary
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface)
            .border(1.dp, AppColors.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title.ifBlank { "Task" }, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(3.dp))
                Text(item.date, fontSize = 12.sp, color = AppColors.textSecondary)
                Text(item.technician.ifBlank { "Unassigned" }, fontSize = 12.sp, color = AppColors.textSecondary)
            }
            Text(item.status.replace('_', ' '), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
        }
        if (item.canSelfAssign) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.blueLight)
                    .clickable(onClick = onSelfAssignClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = AppColors.blue, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Assign to me", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.blue)
            }
        }
    }
}
