package com.example.app.ui.task

import android.widget.ImageView
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.prod.evergreen.R
import com.prod.evergreen.helper.MediaUrl
import com.example.app.ui.theme.EvergreenTheme

private val Background = Color(0xFFF9FAFD)
private val White = Color.White
private val DarkText = Color(0xFF202443)
private val GrayText = Color(0xFF858BA0)
private val Purple = Color(0xFF6258F5)
private val Blue = Color(0xFF1685E8)
private val Green = Color(0xFF1EAF60)
private val Orange = Color(0xFFFF921E)
private val Red = Color(0xFFF0445D)
private val Border = Color(0xFFE9EAF2)

data class TaskItem(
    val id: Int = 0,
    val title: String,
    val type: String,
    val status: String,
    val statusKey: String = "",
    val equipmentName: String,
    val serialNumber: String,
    val company: String,
    val createdAt: String,
    val imageUrl: String? = null,
    val imageRes: Int? = null
)

data class TaskStatusCount(
    val value: String,
    val label: String,
    val color: Color,
    val icon: ImageVector,
    val statusKey: String
)

val taskTabs = listOf("Not Started", "Hold", "In Progress", "Done")
val taskStatusKeys = listOf("open", "hold", "in_progress", "closed")

@Composable
fun TaskListScreen(
    tasks: List<TaskItem>,
    statusCounts: List<TaskStatusCount>,
    selectedTab: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onTabSelected: (Int) -> Unit,
    onTaskClick: (TaskItem) -> Unit = {},
    onStatusClick: (Int) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    EvergreenTheme {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TaskHeader(
                onMenuClick = onMenuClick,
                onScanClick = onScanClick,
                onNotificationClick = onNotificationClick
            )
            TaskStatusRow(statusCounts = statusCounts, onStatusClick = onStatusClick)
            TaskTabs(selectedTab = selectedTab, onTabSelected = onTabSelected)
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearchClick = onSearchClick,
                onFilterClick = onFilterClick
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 3.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = tasks,
                    key = { "${it.id}-${it.title}-${it.serialNumber}-${it.createdAt}" }
                ) { task ->
                    TaskCard(task = task, onClick = { onTaskClick(task) })
                }
            }
            BottomNavigation(
                onHomeClick = onHomeClick,
                onMessagesClick = onMessagesClick,
                onTasksClick = onTasksClick,
                onProfileClick = onProfileClick,
                onAddClick = onAddClick
            )
        }
        FloatingAddButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 76.dp),
            onClick = onAddClick
        )
    }
    }
}

@Composable
private fun TaskHeader(
    onMenuClick: () -> Unit,
    onScanClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.GridView,
            contentDescription = "Menu",
            tint = DarkText,
            modifier = Modifier
                .size(21.dp)
                .clickable(onClick = onMenuClick)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Tasks List",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = "Scan",
            tint = DarkText,
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onScanClick)
        )
        Spacer(modifier = Modifier.width(15.dp))
        Icon(
            imageVector = Icons.Default.NotificationsNone,
            contentDescription = "Notifications",
            tint = DarkText,
            modifier = Modifier
                .size(21.dp)
                .clickable(onClick = onNotificationClick)
        )
    }
}

@Composable
private fun TaskStatusRow(
    statusCounts: List<TaskStatusCount>,
    onStatusClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statusCounts.forEachIndexed { index, status ->
            TaskStatusCard(
                status = status,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onStatusClick(index) }
            )
        }
    }
}

@Composable
private fun TaskStatusCard(
    status: TaskStatusCount,
    modifier: Modifier = Modifier
) {
    val background = when (status.color) {
        Red -> Color(0xFFFFF2F3)
        Blue -> Color(0xFFF0F8FF)
        Orange -> Color(0xFFFFF7ED)
        else -> Color(0xFFF0FAF4)
    }
    Box(
        modifier = modifier
            .height(57.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(background)
            .border(1.dp, Color.White, RoundedCornerShape(9.dp))
            .padding(horizontal = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(27.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(status.color.copy(alpha = 0.09f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = status.icon,
                    contentDescription = null,
                    tint = status.color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = status.value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = status.color
                )
                Text(
                    text = status.label,
                    fontSize = 12.sp,
                    color = DarkText,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TaskTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        taskTabs.forEachIndexed { index, tab ->
            Column(
                modifier = Modifier
                    .width(58.dp)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tab,
                    fontSize = 14.sp,
                    fontWeight = if (index == selectedTab) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (index == selectedTab) DarkText else Color(0xFF8C91A5)
                )
                Spacer(modifier = Modifier.height(7.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (index == selectedTab) 2.dp else 1.dp)
                        .clip(CircleShape)
                        .background(if (index == selectedTab) Purple else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Border, RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color(0xFF67718A),
            modifier = Modifier
                .padding(start = 10.dp)
                .size(18.dp)
                .clickable(onClick = onSearchClick)
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            cursorBrush = SolidColor(DarkText),
            textStyle = TextStyle(fontSize = 16.sp, color = DarkText),
            modifier = Modifier
                .weight(1f)
                .padding(start = 7.dp, end = 8.dp),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(text = "Search...", fontSize = 14.sp, color = Color(0xFFA1A6B5))
                }
                inner()
            }
        )
        Box(
            modifier = Modifier
                .padding(end = 6.dp)
                .size(27.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFFF3F0FF))
                .clickable(onClick = onFilterClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = Purple,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskItem,
    onClick: () -> Unit
) {
    val accent = statusColor(task.statusKey)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEDEEF4), RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(9.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TaskImage(task = task)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(48.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(accent)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = task.type,
                            fontSize = 14.sp,
                            color = DarkText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    StatusBadge(status = task.status, statusKey = task.statusKey)
                }
                Spacer(modifier = Modifier.height(7.dp))
                DetailsBox(task = task)
            }
        }
    }
}

@Composable
private fun TaskImage(task: TaskItem) {
    Box(
        modifier = Modifier
            .width(84.dp)
            .height(156.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFD9D2C4)),
        contentAlignment = Alignment.Center
    ) {
        val imageUrl = MediaUrl.resolve(task.imageUrl)
        when {
            task.imageRes != null -> {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = task.imageRes),
                    contentDescription = task.title,
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
                        Glide.with(imageView)
                            .load(imageUrl)
                            .placeholder(R.drawable.place_holder1)
                            .error(R.drawable.place_holder1)
                            .into(imageView)
                    }
                )
            }
            else -> {
                Text(
                    text = task.title.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "T",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF23413F)
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String, statusKey: String) {
    val color = statusColor(statusKey)
    val background = color.copy(alpha = 0.12f)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = status, fontSize = 12.sp, color = color)
    }
}

@Composable
private fun DetailsBox(task: TaskItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFCFCFF))
            .border(1.dp, Color(0xFFF0EFF7), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Details",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Purple
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        TaskDetailRow(Icons.Default.Person, "Equipment name", task.equipmentName)
        TaskDetailRow(Icons.Default.Tag, "S.no", task.serialNumber)
        TaskDetailRow(Icons.Default.Business, "Company", task.company)
        TaskDetailRow(Icons.Default.CalendarMonth, "Created at", task.createdAt)
    }
}

@Composable
private fun TaskDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Purple, modifier = Modifier.size(10.dp))
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF697185),
            modifier = Modifier.width(120.dp),
            maxLines = 1
        )
        Text(text = ":", fontSize = 12.sp, color = Color(0xFF697185))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value.ifBlank { "-" },
            fontSize = 12.sp,
            color = Color(0xFF3F4658),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FloatingAddButton(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(37.dp)
            .shadow(elevation = 7.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(Color(0xFF6045E9))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Task",
            tint = Color.White,
            modifier = Modifier.size(23.dp)
        )
    }
}

@Composable
private fun BottomNavigation(
    onHomeClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onTasksClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(70.dp)
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BottomNavItem(Icons.Default.GridView, "Home", false, onHomeClick)
        BottomNavItem(Icons.Default.Message, "Messages", false, onMessagesClick)
        Box(
            modifier = Modifier
                .width(50.dp)
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(elevation = 7.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF2872EE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
        BottomNavItem(Icons.Default.TaskAlt, "Tasks", true, onTasksClick)
        BottomNavItem(Icons.Default.Person, "Profile", false, onProfileClick)
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(50.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color(0xFF2872EE) else Color(0xFF9298A6),
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color(0xFF2872EE) else Color(0xFF9298A6)
        )
    }
}

private fun statusColor(statusKey: String): Color {
    return when (statusKey) {
        "open" -> Red
        "hold" -> Blue
        "in_progress" -> Orange
        "closed" -> Green
        else -> GrayText
    }
}

fun defaultStatusCounts(
    open: Int = 0,
    hold: Int = 0,
    inProgress: Int = 0,
    closed: Int = 0
): List<TaskStatusCount> {
    return listOf(
        TaskStatusCount(open.toString(), "Open", Red, Icons.Default.TaskAlt, "open"),
        TaskStatusCount(hold.toString(), "Hold", Blue, Icons.Default.Schedule, "hold"),
        TaskStatusCount(inProgress.toString(), "In Progress", Orange, Icons.Default.Schedule, "in_progress"),
        TaskStatusCount(closed.toString(), "Closed", Green, Icons.Default.CheckCircle, "closed")
    )
}
