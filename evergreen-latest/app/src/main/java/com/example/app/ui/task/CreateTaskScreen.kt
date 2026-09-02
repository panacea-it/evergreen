package com.example.app.ui.task

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide

private val Background = Color(0xFFF9FAFD)
private val White = Color.White
private val DarkText = Color(0xFF202443)
private val SecondaryText = Color(0xFF858BA0)
private val Purple = Color(0xFF5C52F5)
private val PurpleLight = Color(0xFFF3F1FF)
private val Border = Color(0xFFE7E8F1)
private val RequiredRed = Color(0xFFE34251)

data class CreateTaskFormState(
    val companyName: String = "",
    val branchName: String = "",
    val location: String = "",
    val equipment: String = "",
    val equipmentSummary: String = "",
    val issueType: String = "",
    val subject: String = "",
    val description: String = "",
    val photoPreviewPath: String? = null,
    val photoCount: Int = 0,
    val companyLocked: Boolean = false
)

@Composable
fun CreateTaskScreen(
    state: CreateTaskFormState,
    onStateChange: (CreateTaskFormState) -> Unit,
    onBackClick: () -> Unit = {},
    onCreateTaskClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onCompanyClick: () -> Unit = {},
    onEquipmentClick: () -> Unit = {},
    onIssueTypeClick: () -> Unit = {},
    onPhotoClick: () -> Unit = {},
    onClearPhotoClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CreateTaskHeader(
                onBackClick = onBackClick,
                onMenuClick = onMenuClick,
                onNotificationClick = onNotificationClick
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                TaskInformationCard(
                    state = state,
                    onStateChange = onStateChange,
                    onCompanyClick = onCompanyClick,
                    onEquipmentClick = onEquipmentClick,
                    onIssueTypeClick = onIssueTypeClick,
                    onPhotoClick = onPhotoClick,
                    onClearPhotoClick = onClearPhotoClick,
                    onCreateTaskClick = onCreateTaskClick,
                    onCancelClick = onCancelClick
                )
                Spacer(modifier = Modifier.height(15.dp))
            }
            BottomNavigation(
                onHomeClick = onHomeClick,
                onMessagesClick = onMessagesClick,
                onAddClick = onAddClick,
                onTasksClick = onTasksClick,
                onProfileClick = onProfileClick
            )
        }
    }
}

@Composable
private fun CreateTaskHeader(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 13.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(1.dp, Border, RoundedCornerShape(10.dp))
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = DarkText,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Task Details",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Text(text = "Add new task", fontSize = 8.sp, color = SecondaryText)
        }
        Icon(
            imageVector = Icons.Default.GridView,
            contentDescription = "Menu",
            tint = DarkText,
            modifier = Modifier
                .size(21.dp)
                .clickable(onClick = onMenuClick)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Box(modifier = Modifier.clickable(onClick = onNotificationClick)) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notifications",
                tint = DarkText,
                modifier = Modifier.size(21.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Purple)
            )
        }
    }
}

@Composable
private fun TaskInformationCard(
    state: CreateTaskFormState,
    onStateChange: (CreateTaskFormState) -> Unit,
    onCompanyClick: () -> Unit,
    onEquipmentClick: () -> Unit,
    onIssueTypeClick: () -> Unit,
    onPhotoClick: () -> Unit,
    onClearPhotoClick: () -> Unit,
    onCreateTaskClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .border(1.dp, Color(0xFFF0EFF6), RoundedCornerShape(16.dp))
            .padding(horizontal = 11.dp, vertical = 11.dp)
    ) {
        Box(
            modifier = Modifier
                .size(53.dp)
                .align(Alignment.CenterHorizontally)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF8278FF), Color(0xFF4F45F2)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(27.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Task Information",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )
        Text(
            text = "Provide details of the task",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 3.dp),
            textAlign = TextAlign.Center,
            fontSize = 9.sp,
            color = SecondaryText
        )
        Spacer(modifier = Modifier.height(17.dp))
        SelectField(
            icon = Icons.Default.Business,
            label = "Company Name",
            required = true,
            value = state.companyName,
            placeholder = "Select company name",
            enabled = !state.companyLocked,
            onClick = onCompanyClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        SelectField(
            icon = Icons.Default.Business,
            label = "Branch Name",
            required = false,
            value = state.branchName,
            placeholder = "Select branch name",
            enabled = false,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(12.dp))
        InputField(
            icon = Icons.Default.LocationOn,
            label = "Location",
            required = false,
            value = state.location,
            placeholder = "Enter location",
            onValueChange = { onStateChange(state.copy(location = it)) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        SelectField(
            icon = Icons.Default.TaskAlt,
            label = "Equipments List",
            required = true,
            value = state.equipment,
            placeholder = "Select equipment",
            onClick = onEquipmentClick
        )
        if (state.equipmentSummary.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = state.equipmentSummary,
                fontSize = 8.sp,
                color = SecondaryText,
                modifier = Modifier.padding(start = 53.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        SelectField(
            icon = Icons.Default.HeadsetMic,
            label = "Issue Type",
            required = true,
            value = state.issueType,
            placeholder = "Select issue type",
            onClick = onIssueTypeClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        InputField(
            icon = Icons.Default.Edit,
            label = "Subject",
            required = true,
            value = state.subject,
            placeholder = "Enter subject",
            onValueChange = { onStateChange(state.copy(subject = it)) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        DescriptionField(
            icon = Icons.Default.Description,
            label = "Description",
            required = false,
            value = state.description,
            placeholder = "Enter detailed description of the issue",
            onValueChange = { onStateChange(state.copy(description = it)) }
        )
        Spacer(modifier = Modifier.height(14.dp))
        UploadPhotoSection(
            previewPath = state.photoPreviewPath,
            photoCount = state.photoCount,
            onClick = onPhotoClick,
            onClearClick = onClearPhotoClick
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CreateTaskButton(modifier = Modifier.weight(1f), onClick = onCreateTaskClick)
            CancelButton(modifier = Modifier.weight(1f), onClick = onCancelClick)
        }
    }
}

@Composable
private fun InputField(
    icon: ImageVector,
    label: String,
    required: Boolean,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        FieldIcon(icon)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            FieldLabel(label = label, required = required)
            Spacer(modifier = Modifier.height(5.dp))
            TextFieldContainer(value = value, placeholder = placeholder, onValueChange = onValueChange)
        }
    }
}

@Composable
private fun SelectField(
    icon: ImageVector,
    label: String,
    required: Boolean,
    value: String,
    placeholder: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        FieldIcon(icon)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            FieldLabel(label = label, required = required)
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFCFCFF))
                    .border(1.dp, Border, RoundedCornerShape(8.dp))
                    .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
                    .padding(horizontal = 11.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = value.ifEmpty { placeholder },
                        fontSize = 10.sp,
                        color = if (value.isEmpty()) Color(0xFF8E95A8) else DarkText,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(0xFF515A8A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DescriptionField(
    icon: ImageVector,
    label: String,
    required: Boolean,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        FieldIcon(icon)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            FieldLabel(label = label, required = required)
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(91.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Border, RoundedCornerShape(8.dp))
            ) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(text = placeholder, fontSize = 9.sp, color = Color(0xFF969CAE))
                    },
                    textStyle = TextStyle(fontSize = 9.sp, color = DarkText),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun FieldIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(43.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(PurpleLight)
            .border(1.dp, Color(0xFFE8E5FF), RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Purple, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun FieldLabel(label: String, required: Boolean) {
    Row {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
        if (required) {
            Text(text = " *", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RequiredRed)
        }
    }
}

@Composable
private fun TextFieldContainer(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFCFCFF))
            .border(1.dp, Border, RoundedCornerShape(8.dp))
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = placeholder, fontSize = 9.sp, color = Color(0xFF969CAE))
            },
            singleLine = true,
            textStyle = TextStyle(fontSize = 9.sp, color = DarkText),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun UploadPhotoSection(
    previewPath: String?,
    photoCount: Int,
    onClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Column {
        Text(text = "Add Photo", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(123.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFFFBFAFF))
                .border(1.dp, Color(0xFFA7A0FF), RoundedCornerShape(9.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (!previewPath.isNullOrBlank()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
                    },
                    update = { imageView ->
                        Glide.with(imageView).load(previewPath).into(imageView)
                    }
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(onClick = onClearClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove photo",
                        tint = DarkText,
                        modifier = Modifier.size(14.dp)
                    )
                }
                if (photoCount > 1) {
                    Text(
                        text = "$photoCount photos",
                        fontSize = 8.sp,
                        color = DarkText,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PurpleLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload photo",
                            tint = Purple,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(7.dp))
                    Text(
                        text = "Drag & drop your image here",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row {
                        Text(text = "or ", fontSize = 8.sp, color = SecondaryText)
                        Text(text = "browse", fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = Purple)
                        Text(text = " from gallery", fontSize = 8.sp, color = SecondaryText)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "PNG, JPG up to 5MB", fontSize = 7.sp, color = Color(0xFF9CA1B1))
                }
            }
        }
    }
}

@Composable
private fun CreateTaskButton(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF8178FF), Color(0xFF4842F1))))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.TaskAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(7.dp))
            Text(text = "Create Task", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }
    }
}

@Composable
private fun CancelButton(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Purple, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = Purple, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(7.dp))
            Text(text = "Cancel", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Purple)
        }
    }
}

@Composable
private fun BottomNavigation(
    onHomeClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onAddClick: () -> Unit,
    onTasksClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(70.dp)
            .background(Color.White)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BottomNavItem(Icons.Default.Home, "Home", false, onHomeClick)
        BottomNavItem(Icons.Default.Message, "Messages", false, onMessagesClick)
        Box(
            modifier = Modifier
                .width(52.dp)
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(43.dp)
                    .shadow(elevation = 7.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF2872EE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(26.dp))
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
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 7.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color(0xFF2872EE) else Color(0xFF9298A6)
        )
    }
}
