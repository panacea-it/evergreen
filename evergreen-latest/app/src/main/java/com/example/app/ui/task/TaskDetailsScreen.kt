package com.example.app.ui.task

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.prod.evergreen.helper.MediaUrl
import com.example.app.ui.theme.AppColors
import com.example.app.ui.theme.AppHeader
import com.example.app.ui.theme.AppIcons
import com.example.app.ui.theme.EvergreenTheme

private val Background = AppColors.background
private val White = AppColors.surface
private val DarkText = AppColors.textPrimary
private val SecondaryText = AppColors.textSecondary
private val Purple = AppColors.purple
private val PurpleLight = AppColors.purpleLight
private val Blue = AppColors.blue
private val Green = AppColors.green
private val GreenLight = AppColors.greenLight
private val Red = AppColors.red
private val RedLight = AppColors.redLight
private val Orange = AppColors.orange
private val OrangeLight = AppColors.orangeLight

data class TaskDetailsData(
    val companyName: String = "",
    val location: String = "",
    val clientAdminName: String = "",
    val clientAdminPhone: String = "",
    val title: String = "",
    val description: String = "",
    val technician: String = "",
    val technicianPhone: String = "",
    val created: String = "",
    val lastUpdate: String = "",
    val status: String = "",
    val statusKey: String = "",
    val imageRes: Int? = null,
    val imageUrl: String? = null
)

@Composable
fun TaskDetailsScreen(
    task: TaskDetailsData,
    showActions: Boolean = false,
    showAssign: Boolean = false,
    showReport: Boolean = false,
    assignLabel: String = "Assign Technician",
    onCloseClick: () -> Unit = {},
    onEditTaskClick: () -> Unit = {},
    onDeleteTaskClick: () -> Unit = {},
    onAssignClick: () -> Unit = {},
    onReportClick: () -> Unit = {}
) {
    EvergreenTheme {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AppHeader(
            title = "Task Details",
            subtitle = "Overview of this ticket",
            leadingIcon = AppIcons.close,
            leadingDescription = "Close",
            onLeadingClick = onCloseClick
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
        TaskImage(task = task)
        Spacer(modifier = Modifier.height(17.dp))
        CompanyDetailsCard(task = task)
        Spacer(modifier = Modifier.height(17.dp))
        ClientAdminCard(task = task)
        Spacer(modifier = Modifier.height(17.dp))
        TaskInformationCard(task = task)
        if (showAssign || showReport) {
            Spacer(modifier = Modifier.height(14.dp))
            if (showAssign) {
                ActionButton(
                    label = assignLabel,
                    background = Brush.horizontalGradient(listOf(Color(0xFF7166FF), Color(0xFF4A42ED))),
                    textColor = Color.White,
                    onClick = onAssignClick
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            if (showReport) {
                ActionButton(
                    label = "Service Report",
                    background = Brush.horizontalGradient(listOf(Color(0xFF226DFF), Color(0xFF2057A6))),
                    textColor = Color.White,
                    onClick = onReportClick
                )
            }
        }
        if (showActions) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                EditTaskButton(modifier = Modifier.weight(1f), onClick = onEditTaskClick)
                DeleteTaskButton(modifier = Modifier.weight(1f), onClick = onDeleteTaskClick)
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
        }
    }
    }
}

@Composable
private fun TaskDetailsHeader(onCloseClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PurpleLight)
                .border(1.dp, Color(0xFFE1DCFF), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.TaskAlt,
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Task Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Text(text = "Overview of task information", fontSize = 13.sp, color = SecondaryText)
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(15.dp))
                .clip(RoundedCornerShape(15.dp))
                .background(White)
                .clickable(onClick = onCloseClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = DarkText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TaskImage(task: TaskDetailsData) {
    val imageUrl = MediaUrl.resolve(task.imageUrl)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE6EEF7))
    ) {
        when {
            task.imageRes != null -> {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = task.imageRes),
                    contentDescription = "Task image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            imageUrl.isNotBlank() -> {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
                    },
                    update = { imageView ->
                        Glide.with(imageView).load(imageUrl).into(imageView)
                    }
                )
            }
            else -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = Color(0xFF7890A8),
                        modifier = Modifier.size(55.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Task Image", fontSize = 16.sp, color = Color(0xFF718097))
                }
            }
        }
    }
}

@Composable
private fun CompanyDetailsCard(task: TaskDetailsData) {
    DetailsCard(
        background = Brush.linearGradient(listOf(Color(0xFFFDFBFF), Color(0xFFF7F5FF)))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LargeSectionIcon(icon = Icons.Default.Business, tint = Purple)
            Spacer(modifier = Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Company Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Spacer(modifier = Modifier.height(15.dp))
                DetailsRow(Icons.Default.Business, "Company Name", task.companyName, Purple)
                Spacer(modifier = Modifier.height(11.dp))
                DetailsRow(Icons.Default.LocationOn, "Location", task.location)
            }
        }
    }
}

@Composable
private fun ClientAdminCard(task: TaskDetailsData) {
    DetailsCard(
        background = Brush.linearGradient(listOf(Color(0xFFF8FFFC), Color(0xFFF1FAF6)))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LargeSectionIcon(icon = Icons.Default.Person, tint = Green)
            Spacer(modifier = Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Client Admin Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Spacer(modifier = Modifier.height(15.dp))
                DetailsRow(Icons.Default.Person, "Name", task.clientAdminName, Purple)
                Spacer(modifier = Modifier.height(11.dp))
                DetailsRow(Icons.Default.Phone, "Phone", task.clientAdminPhone, Purple)
            }
        }
    }
}

@Composable
private fun TaskInformationCard(task: TaskDetailsData) {
    DetailsCard(
        background = Brush.linearGradient(listOf(Color(0xFFF8FBFF), Color(0xFFF1F6FF)))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0xFFE6F0FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.TaskAlt, contentDescription = null, tint = Blue, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Task Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
        }
        Spacer(modifier = Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                TaskDetailLine("Title", task.title)
                TaskDetailLine("Description", task.description)
                TaskDetailLine("Technician", task.technician)
                TaskDetailLine("Technician Phone", task.technicianPhone)
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(125.dp)
                    .background(Color(0xFFD7E1F1))
            )
            Spacer(modifier = Modifier.width(17.dp))
            Column(modifier = Modifier.weight(0.9f)) {
                TaskDateLine("Created", task.created)
                TaskDateLine("Last Update", task.lastUpdate)
            }
        }
        Spacer(modifier = Modifier.height(13.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFDCE4F0))
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Status", fontSize = 16.sp, color = DarkText, modifier = Modifier.width(145.dp))
            Text(text = ":", fontSize = 16.sp, color = DarkText)
            Spacer(modifier = Modifier.width(17.dp))
            StatusBadge(status = task.status, statusKey = task.statusKey)
        }
    }
}

@Composable
private fun DetailsCard(
    background: Brush,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(background)
            .border(1.dp, Color(0xFFE7EAF3), RoundedCornerShape(13.dp))
            .padding(12.dp),
        content = content
    )
}

@Composable
private fun LargeSectionIcon(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .shadow(elevation = 2.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFEDEDF5), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun DetailsRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = DarkText
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Purple, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(9.dp))
        Text(text = label, fontSize = 14.sp, color = DarkText, modifier = Modifier.width(118.dp))
        Text(text = ":", fontSize = 14.sp, color = DarkText)
        Spacer(modifier = Modifier.width(15.dp))
        Text(
            text = value.ifEmpty { "-" },
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TaskDetailLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(Blue)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, fontSize = 14.sp, color = DarkText, modifier = Modifier.width(120.dp))
        Text(text = ":", fontSize = 14.sp, color = DarkText)
        Spacer(modifier = Modifier.width(11.dp))
        Text(
            text = value.ifEmpty { "-" },
            fontSize = 14.sp,
            color = DarkText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TaskDateLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(29.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8F2FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = Blue, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(9.dp))
        Text(text = label, fontSize = 14.sp, color = DarkText, modifier = Modifier.width(83.dp))
        Text(text = ":", fontSize = 14.sp, color = DarkText)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value.ifEmpty { "-" },
            fontSize = 14.sp,
            color = DarkText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusBadge(status: String, statusKey: String) {
    val key = statusKey.ifBlank { status }.lowercase()
    val (background, color) = when (key) {
        "closed", "done" -> GreenLight to Green
        "in_progress", "in progress" -> Color(0xFFEAF3FF) to Blue
        "hold" -> OrangeLight to Orange
        else -> RedLight to Red
    }
    val label = status.ifEmpty {
        when (key) {
            "open" -> "Not Started"
            "in_progress" -> "In Progress"
            "closed" -> "Closed"
            "hold" -> "Hold"
            else -> "Not Started"
        }
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .padding(horizontal = 17.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(9.dp))
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
private fun ActionButton(
    label: String,
    background: Brush,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@Composable
private fun EditTaskButton(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(57.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(17.dp))
            .clip(RoundedCornerShape(17.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF7166FF), Color(0xFF4A42ED))))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(23.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Edit Task", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
private fun DeleteTaskButton(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(57.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(Color.White)
            .border(1.5.dp, Color(0xFFFF6D70), RoundedCornerShape(17.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Red, modifier = Modifier.size(23.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Delete Task", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Red)
        }
    }
}
