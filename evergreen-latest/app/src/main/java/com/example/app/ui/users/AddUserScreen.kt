package com.example.app.ui.users

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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.ui.theme.AppColors
import com.example.app.ui.theme.EvergreenTheme

private val Background = AppColors.background
private val White = AppColors.surface
private val DarkText = AppColors.textPrimary
private val SecondaryText = AppColors.textSecondary
private val Purple = AppColors.purple
private val Border = AppColors.border
private val RequiredRed = AppColors.red

data class AddUserFormState(
    val title: String = "User Details",
    val subtitle: String = "Add new user",
    val saveLabel: String = "Save User",
    val name: String = "",
    val mobile: String = "",
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val passwordPlaceholder: String = "Enter password",
    val passwordRequired: Boolean = true,
    val accessType: String = "",
    val amc: String = "",
    val hideAmc: Boolean = false,
    val amcLocked: Boolean = false,
    val hideAccessType: Boolean = false
)

@Composable
fun AddUserScreen(
    state: AddUserFormState,
    onStateChange: (AddUserFormState) -> Unit,
    onBackClick: () -> Unit = {},
    onSaveUserClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
    onAccessTypeClick: () -> Unit = {},
    onAmcClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    EvergreenTheme {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        com.example.app.ui.theme.AppHeader(
            title = state.title,
            subtitle = state.subtitle,
            onLeadingClick = onBackClick
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 13.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            UserInformationCard(
                state = state,
                onStateChange = onStateChange,
                onAccessTypeClick = onAccessTypeClick,
                onAmcClick = onAmcClick,
                onSaveClick = onSaveUserClick,
                onCancelClick = onCancelClick
            )
            Spacer(modifier = Modifier.height(15.dp))
        }
    }
    }
}

@Composable
private fun UserDetailsHeader(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 17.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(49.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(White)
                .border(1.dp, Border, RoundedCornerShape(13.dp))
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = DarkText,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 14.sp, color = SecondaryText)
        }
        Icon(
            imageVector = Icons.Default.GridView,
            contentDescription = "Menu",
            tint = DarkText,
            modifier = Modifier
                .size(27.dp)
                .clickable(onClick = onMenuClick)
        )
        Spacer(modifier = Modifier.width(17.dp))
        Box {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notifications",
                tint = DarkText,
                modifier = Modifier
                    .size(27.dp)
                    .clickable(onClick = onNotificationClick)
            )
        }
    }
}

@Composable
private fun UserInformationCard(
    state: AddUserFormState,
    onStateChange: (AddUserFormState) -> Unit,
    onAccessTypeClick: () -> Unit,
    onAmcClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(White)
            .border(1.dp, Color(0xFFEDEEF5), RoundedCornerShape(22.dp))
            .padding(horizontal = 18.dp, vertical = 17.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.CenterHorizontally)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF6F65FF), Color(0xFF4138EA)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "User Information", modifier = Modifier.fillMaxWidth(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "Please provide basic details of user",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 12.sp,
            color = SecondaryText
        )
        Spacer(modifier = Modifier.height(16.dp))
        UserInputField(
            icon = Icons.Default.Person,
            label = "Name",
            placeholder = "Enter full name",
            value = state.name,
            onValueChange = { onStateChange(state.copy(name = it)) },
            required = true
        )
        Spacer(modifier = Modifier.height(23.dp))
        UserInputField(
            icon = Icons.Default.Phone,
            label = "Mobile Number",
            placeholder = "Enter mobile number",
            value = state.mobile,
            onValueChange = { onStateChange(state.copy(mobile = it)) },
            required = true
        )
        Spacer(modifier = Modifier.height(23.dp))
        UserInputField(
            icon = Icons.Default.Email,
            label = "Email",
            placeholder = "Enter email address",
            value = state.email,
            onValueChange = { onStateChange(state.copy(email = it)) },
            required = true
        )
        Spacer(modifier = Modifier.height(23.dp))
        PasswordInputField(
            value = state.password,
            placeholder = state.passwordPlaceholder,
            required = state.passwordRequired,
            onValueChange = { onStateChange(state.copy(password = it)) },
            visible = state.passwordVisible,
            onVisibilityClick = { onStateChange(state.copy(passwordVisible = !state.passwordVisible)) }
        )
        if (!state.hideAccessType) {
            Spacer(modifier = Modifier.height(23.dp))
            UserDropdownField(
                icon = Icons.Default.Badge,
                label = "Access Type",
                placeholder = "Select access type",
                value = state.accessType,
                onClick = onAccessTypeClick
            )
        }
        if (!state.hideAmc) {
            Spacer(modifier = Modifier.height(23.dp))
            UserDropdownField(
                icon = Icons.Default.Business,
                label = "Assign to AMC",
                placeholder = "Select AMC",
                value = state.amc,
                onClick = if (state.amcLocked) ({}) else onAmcClick
            )
        }
        Spacer(modifier = Modifier.height(31.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SaveUserButton(modifier = Modifier.weight(1f), label = state.saveLabel, onClick = onSaveClick)
            CancelUserButton(modifier = Modifier.weight(1f), onClick = onCancelClick)
        }
    }
}

@Composable
private fun UserInputField(
    icon: ImageVector,
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    required: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserFieldIcon(icon = icon)
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            UserFieldLabel(label = label, required = required)
            Spacer(modifier = Modifier.height(7.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFDFDFF))
                    .border(1.dp, Border, RoundedCornerShape(10.dp))
            ) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(
                            text = placeholder,
                            fontSize = 16.sp,
                            color = Color(0xFF8A91AC),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp, color = DarkText),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun PasswordInputField(
    value: String,
    placeholder: String,
    required: Boolean,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onVisibilityClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserFieldIcon(icon = Icons.Default.Lock)
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            UserFieldLabel(label = "Password", required = required)
            Spacer(modifier = Modifier.height(7.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFDFDFF))
                    .border(1.dp, Border, RoundedCornerShape(10.dp))
            ) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(
                            text = placeholder,
                            fontSize = 16.sp,
                            color = Color(0xFF8A91AC),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    singleLine = true,
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onVisibilityClick) {
                            Icon(
                                imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (visible) "Hide password" else "Show password",
                                tint = Purple
                            )
                        }
                    },
                    textStyle = TextStyle(fontSize = 16.sp, color = DarkText),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun UserDropdownField(
    icon: ImageVector,
    label: String,
    placeholder: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserFieldIcon(icon = icon)
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            UserFieldLabel(label = label, required = true)
            Spacer(modifier = Modifier.height(7.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFDFDFF))
                    .border(1.dp, Border, RoundedCornerShape(10.dp))
                    .clickable(onClick = onClick)
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value.ifEmpty { placeholder },
                    fontSize = 16.sp,
                    color = if (value.isEmpty()) Color(0xFF8A91AC) else DarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select",
                    tint = DarkText,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    }
}

@Composable
private fun UserFieldIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(57.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFFFBFAFF))
            .border(1.dp, Color(0xFFE5E2FF), RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Purple, modifier = Modifier.size(27.dp))
    }
}

@Composable
private fun UserFieldLabel(label: String, required: Boolean) {
    Row {
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
        if (required) {
            Text(text = " *", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RequiredRed)
        }
    }
}

@Composable
private fun SaveUserButton(modifier: Modifier, label: String, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(59.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF6256FF), Color(0xFF453BEA))))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.TaskAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(23.dp))
            Spacer(modifier = Modifier.width(9.dp))
            Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }
    }
}

@Composable
private fun CancelUserButton(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(59.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(White)
            .border(1.5.dp, Purple, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = com.example.app.ui.theme.AppIcons.close,
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(com.example.app.ui.theme.AppIcons.header)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cancel", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Purple)
        }
    }
}
