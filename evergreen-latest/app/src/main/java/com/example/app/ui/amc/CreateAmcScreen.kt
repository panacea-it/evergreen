package com.example.app.ui.amc

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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide

private val Background = Color(0xFFF9FAFE)
private val DarkText = Color(0xFF202344)
private val SecondaryText = Color(0xFF858BA1)
private val Purple = Color(0xFF6258F5)
private val PurpleLight = Color(0xFFF3F1FF)
private val Border = Color(0xFFE9E8F2)
private val InputBackground = Color(0xFFFCFCFF)
private val RequiredRed = Color(0xFFE34D59)

data class CreateAmcFormState(
    val siteName: String = "",
    val branchName: String = "",
    val companyEmail: String = "",
    val siteLocation: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val clientName: String = "",
    val mobileNumber: String = "",
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val logoPreviewPath: String? = null
)

@Composable
fun CreateAmcScreen(
    state: CreateAmcFormState,
    onStateChange: (CreateAmcFormState) -> Unit,
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onStartDateClick: () -> Unit = {},
    onEndDateClick: () -> Unit = {},
    onLogoClick: () -> Unit = {},
    onClearLogoClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CreateAmcHeader(
                onBackClick = onBackClick,
                onMenuClick = onMenuClick,
                onNotificationClick = onNotificationClick
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 13.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                CompanyInformationSection(
                    state = state,
                    onStateChange = onStateChange,
                    onStartDateClick = onStartDateClick,
                    onEndDateClick = onEndDateClick,
                    onLogoClick = onLogoClick,
                    onClearLogoClick = onClearLogoClick
                )
                Spacer(modifier = Modifier.height(12.dp))
                ClientDetailsSection(
                    state = state,
                    onStateChange = onStateChange
                )
                Spacer(modifier = Modifier.height(15.dp))
            }

            SaveContinueButton(onClick = onSaveClick)
            BottomNavigation(
                onHomeClick = onHomeClick,
                onMessagesClick = onMessagesClick,
                onTasksClick = onTasksClick,
                onProfileClick = onProfileClick,
                onAddClick = onAddClick
            )
        }
    }
}

@Composable
private fun CreateAmcHeader(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 13.dp, end = 13.dp, top = 8.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(37.dp)
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
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Create AMC",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Text(
                text = "Add new AMC company",
                fontSize = 8.sp,
                color = SecondaryText
            )
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
        Box(
            modifier = Modifier.clickable(onClick = onNotificationClick)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notifications",
                tint = DarkText,
                modifier = Modifier.size(21.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Purple)
            )
        }
    }
}

@Composable
private fun CompanyInformationSection(
    state: CreateAmcFormState,
    onStateChange: (CreateAmcFormState) -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onLogoClick: () -> Unit,
    onClearLogoClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF0EFF6), RoundedCornerShape(15.dp))
            .padding(horizontal = 10.dp, vertical = 11.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.CenterHorizontally)
                .shadow(elevation = 5.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF8278FF), Color(0xFF574BEA)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(modifier = Modifier.height(9.dp))
        Text(
            text = "Company Information",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )
        Text(
            text = "Provide basic details about the company",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            textAlign = TextAlign.Center,
            fontSize = 8.sp,
            color = SecondaryText
        )
        Spacer(modifier = Modifier.height(13.dp))
        FormField(
            icon = Icons.Default.GridView,
            label = "Site name",
            required = true,
            value = state.siteName,
            placeholder = "Enter site name",
            onValueChange = { onStateChange(state.copy(siteName = it)) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        FormField(
            icon = Icons.Default.GridView,
            label = "Branch name",
            required = true,
            value = state.branchName,
            placeholder = "Enter branch name",
            onValueChange = { onStateChange(state.copy(branchName = it)) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        FormField(
            icon = Icons.Default.Email,
            label = "Company Email ID",
            required = false,
            value = state.companyEmail,
            placeholder = "Enter email address",
            onValueChange = { onStateChange(state.copy(companyEmail = it)) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        FormField(
            icon = Icons.Default.CalendarMonth,
            label = "Start Date",
            required = true,
            value = state.startDate,
            placeholder = "Select start date",
            trailingIcon = Icons.Default.CalendarMonth,
            readOnly = true,
            onClick = onStartDateClick,
            onValueChange = {}
        )
        Spacer(modifier = Modifier.height(10.dp))
        FormField(
            icon = Icons.Default.CalendarMonth,
            label = "End Date",
            required = true,
            value = state.endDate,
            placeholder = "Select end date",
            trailingIcon = Icons.Default.CalendarMonth,
            readOnly = true,
            onClick = onEndDateClick,
            onValueChange = {}
        )
        Spacer(modifier = Modifier.height(10.dp))
        FormField(
            icon = Icons.Default.LocationOn,
            label = "Site Location",
            required = false,
            value = state.siteLocation,
            placeholder = "Please enter site location",
            onValueChange = { onStateChange(state.copy(siteLocation = it)) }
        )
        Spacer(modifier = Modifier.height(13.dp))
        UploadLogoBox(
            previewPath = state.logoPreviewPath,
            onClick = onLogoClick,
            onClearClick = onClearLogoClick
        )
    }
}

@Composable
private fun FormField(
    icon: ImageVector,
    label: String,
    required: Boolean,
    value: String,
    placeholder: String,
    trailingIcon: ImageVector? = null,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PurpleLight)
                .border(1.dp, Color(0xFFE9E5FF), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(
                    text = label,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )
                if (required) {
                    Text(
                        text = " *",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = RequiredRed
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(InputBackground)
                    .border(1.dp, Border, RoundedCornerShape(7.dp))
                    .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            ) {
                if (readOnly) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = value.ifBlank { placeholder },
                            fontSize = 10.sp,
                            color = if (value.isBlank()) Color(0xFF9DA3B3) else DarkText,
                            modifier = Modifier.weight(1f)
                        )
                        if (trailingIcon != null) {
                            Icon(
                                imageVector = trailingIcon,
                                contentDescription = null,
                                tint = Purple,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                } else {
                    TextField(
                        value = value,
                        onValueChange = onValueChange,
                        placeholder = {
                            Text(text = placeholder, fontSize = 10.sp, color = Color(0xFF9DA3B3))
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(fontSize = 10.sp, color = DarkText),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun UploadLogoBox(
    previewPath: String?,
    onClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Column {
        Row {
            Text(
                text = "Add Company Photo / Logo",
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )
            Text(
                text = " *",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = RequiredRed
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .clip(RoundedCornerShape(9.dp))
                .border(1.dp, Color(0xFFAAA3FF), RoundedCornerShape(9.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0xFFFCFBFF), Color(0xFFF8F6FF)))
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (!previewPath.isNullOrBlank()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        ImageView(context).apply {
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
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
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape)
                            .background(PurpleLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload",
                            tint = Purple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Drag & drop your image here",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row {
                        Text(text = "or ", fontSize = 8.sp, color = SecondaryText)
                        Text(
                            text = "browse",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Purple
                        )
                        Text(text = " from gallery", fontSize = 8.sp, color = SecondaryText)
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "PNG, JPG up to 5MB",
                        fontSize = 7.sp,
                        color = Color(0xFF9CA2B2)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClientDetailsSection(
    state: CreateAmcFormState,
    onStateChange: (CreateAmcFormState) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF0EFF6), RoundedCornerShape(15.dp))
            .padding(horizontal = 11.dp, vertical = 11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Purple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(9.dp))
            Column {
                Text(
                    text = "Client Details",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = "Optional — fill all or leave empty",
                    fontSize = 7.sp,
                    color = SecondaryText
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        FormField(
            icon = Icons.Default.Person,
            label = "Name",
            required = false,
            value = state.clientName,
            placeholder = "Enter full name",
            onValueChange = { onStateChange(state.copy(clientName = it)) }
        )
        Spacer(modifier = Modifier.height(9.dp))
        FormField(
            icon = Icons.Default.Phone,
            label = "Mobile Number",
            required = false,
            value = state.mobileNumber,
            placeholder = "Enter mobile number",
            onValueChange = { onStateChange(state.copy(mobileNumber = it)) }
        )
        Spacer(modifier = Modifier.height(9.dp))
        FormField(
            icon = Icons.Default.Email,
            label = "Email",
            required = false,
            value = state.email,
            placeholder = "Enter email address",
            onValueChange = { onStateChange(state.copy(email = it)) }
        )
        Spacer(modifier = Modifier.height(9.dp))
        PasswordField(
            value = state.password,
            visible = state.passwordVisible,
            onValueChange = { onStateChange(state.copy(password = it)) },
            onVisibilityClick = {
                onStateChange(state.copy(passwordVisible = !state.passwordVisible))
            }
        )
    }
}

@Composable
private fun PasswordField(
    value: String,
    visible: Boolean,
    onValueChange: (String) -> Unit,
    onVisibilityClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PurpleLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Password",
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .border(1.dp, Border, RoundedCornerShape(7.dp))
            ) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(text = "Enter password", fontSize = 10.sp, color = Color(0xFF9DA3B3))
                    },
                    singleLine = true,
                    visualTransformation = if (visible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password",
                            tint = Color(0xFF8C91A3),
                            modifier = Modifier
                                .size(15.dp)
                                .clickable(onClick = onVisibilityClick)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = TextStyle(fontSize = 10.sp, color = DarkText),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun SaveContinueButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 13.dp, vertical = 8.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF8178FF), Color(0xFF4E48F5))))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Save & Continue",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(7.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
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
            .height(62.dp)
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BottomNavItem(Icons.Default.Home, "Home", false, onHomeClick)
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
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        BottomNavItem(Icons.Default.TaskAlt, "Tasks", false, onTasksClick)
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
            fontSize = 7.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color(0xFF2872EE) else Color(0xFF9298A6)
        )
    }
}
