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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
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
import com.prod.evergreen.helper.MediaUrl
import com.example.app.ui.theme.EvergreenTheme

private val Background = Color(0xFFF9FAFE)
private val White = Color.White
private val DarkText = Color(0xFF192047)
private val SecondaryText = Color(0xFF8187A0)
private val Purple = Color(0xFF5D54F5)
private val PurpleLight = Color(0xFFF3F1FF)
private val Border = Color(0xFFE7E8F1)
private val RequiredRed = Color(0xFFE54855)
private val Blue = Color(0xFF2670F5)

data class AddEquipmentFormState(
    val title: String = "Equipments Details",
    val subtitle: String = "Add new Equipment",
    val saveLabel: String = "Save Equipment",
    val equipmentName: String = "",
    val make: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val manufacturerYear: String = "",
    val location: String = "",
    val pmFrequency: String = "",
    val description: String = "",
    val companyName: String = "",
    val hideCompany: Boolean = false,
    val companyLocked: Boolean = false,
    val photoPreviewPath: String? = null,
    val photoRemoteUrl: String? = null
)

@Composable
fun AddEquipmentScreen(
    state: AddEquipmentFormState,
    onStateChange: (AddEquipmentFormState) -> Unit,
    onBackClick: () -> Unit = {},
    onSaveEquipmentClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
    onPhotoClick: () -> Unit = {},
    onClearPhotoClick: () -> Unit = {},
    onCompanyClick: () -> Unit = {},
    onYearClick: () -> Unit = {},
    onPmFrequencyClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    EvergreenTheme {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EquipmentDetailsHeader(
                title = state.title,
                subtitle = state.subtitle,
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
                Spacer(modifier = Modifier.height(5.dp))
                EquipmentInformationCard(
                    state = state,
                    onStateChange = onStateChange,
                    onPhotoClick = onPhotoClick,
                    onClearPhotoClick = onClearPhotoClick,
                    onCompanyClick = onCompanyClick,
                    onYearClick = onYearClick,
                    onPmFrequencyClick = onPmFrequencyClick,
                    onSaveClick = onSaveEquipmentClick,
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
}

@Composable
private fun EquipmentDetailsHeader(
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
            .padding(horizontal = 13.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(39.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color.White)
                .border(1.dp, Border, RoundedCornerShape(11.dp))
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
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Text(text = subtitle, fontSize = 14.sp, color = SecondaryText)
        }
        Icon(
            imageVector = Icons.Default.GridView,
            contentDescription = "Menu",
            tint = DarkText,
            modifier = Modifier
                .size(22.dp)
                .clickable(onClick = onMenuClick)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Icon(
            imageVector = Icons.Default.NotificationsNone,
            contentDescription = "Notifications",
            tint = DarkText,
            modifier = Modifier
                .size(22.dp)
                .clickable(onClick = onNotificationClick)
        )
    }
}

@Composable
private fun EquipmentInformationCard(
    state: AddEquipmentFormState,
    onStateChange: (AddEquipmentFormState) -> Unit,
    onPhotoClick: () -> Unit,
    onClearPhotoClick: () -> Unit,
    onCompanyClick: () -> Unit,
    onYearClick: () -> Unit,
    onPmFrequencyClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .border(1.dp, Color(0xFFF0EFF6), RoundedCornerShape(16.dp))
            .padding(horizontal = 11.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(53.dp)
                .align(Alignment.CenterHorizontally)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF8077FF), Color(0xFF4C43EF)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(27.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Equipment Information",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )
        Text(
            text = "Provide basic details about the Equipment",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 3.dp),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = SecondaryText
        )
        Spacer(modifier = Modifier.height(18.dp))
        if (!state.hideCompany) {
            InputField(
                icon = Icons.Default.Business,
                label = "Company",
                required = true,
                value = state.companyName,
                placeholder = "Select company",
                onValueChange = {},
                dropdown = true,
                readOnly = true,
                onClick = if (state.companyLocked) null else onCompanyClick
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        InputField(
            icon = Icons.Default.Settings,
            label = "Equipment Name",
            required = true,
            value = state.equipmentName,
            placeholder = "Enter equipment name",
            onValueChange = { onStateChange(state.copy(equipmentName = it)) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        InputField(
            icon = Icons.Default.Business,
            label = "Make",
            required = true,
            value = state.make,
            placeholder = "Enter make",
            onValueChange = { onStateChange(state.copy(make = it)) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        InputField(
            icon = Icons.Default.Tag,
            label = "Model",
            required = true,
            value = state.model,
            placeholder = "Enter model",
            onValueChange = { onStateChange(state.copy(model = it)) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        InputField(
            icon = Icons.Default.Tag,
            label = "Serial Number",
            required = true,
            value = state.serialNumber,
            placeholder = "Enter serial number",
            onValueChange = { onStateChange(state.copy(serialNumber = it)) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        InputField(
            icon = Icons.Default.CalendarMonth,
            label = "Manufacturer Year",
            required = true,
            value = state.manufacturerYear,
            placeholder = "Select manufacturer year",
            onValueChange = {},
            dropdown = true,
            readOnly = true,
            onClick = onYearClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        InputField(
            icon = Icons.Default.LocationOn,
            label = "Location",
            required = true,
            value = state.location,
            placeholder = "Enter location",
            onValueChange = { onStateChange(state.copy(location = it)) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        InputField(
            icon = Icons.Default.Refresh,
            label = "PM Frequency",
            required = true,
            value = state.pmFrequency,
            placeholder = "Select PM frequency",
            onValueChange = {},
            dropdown = true,
            readOnly = true,
            onClick = onPmFrequencyClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        DescriptionField(
            icon = Icons.Default.Description,
            label = "Description",
            required = false,
            value = state.description,
            placeholder = "Enter detailed description of the equipment",
            onValueChange = { onStateChange(state.copy(description = it)) }
        )
        Spacer(modifier = Modifier.height(15.dp))
        UploadPhotoSection(
            previewPath = state.photoPreviewPath,
            remoteUrl = state.photoRemoteUrl,
            onClick = onPhotoClick,
            onClearClick = onClearPhotoClick
        )
        Spacer(modifier = Modifier.height(13.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            SaveEquipmentButton(
                modifier = Modifier.weight(1f),
                label = state.saveLabel,
                onClick = onSaveClick
            )
            CancelButton(
                modifier = Modifier.weight(1f),
                onClick = onCancelClick
            )
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
    onValueChange: (String) -> Unit,
    dropdown: Boolean = false,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                            fontSize = 14.sp,
                            color = if (value.isBlank()) Color(0xFF969CAE) else DarkText,
                            modifier = Modifier.weight(1f)
                        )
                        if (dropdown) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF414B82),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    TextField(
                        value = value,
                        onValueChange = onValueChange,
                        placeholder = {
                            Text(text = placeholder, fontSize = 14.sp, color = Color(0xFF969CAE))
                        },
                        singleLine = true,
                        trailingIcon = if (dropdown) {
                            {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color(0xFF414B82),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            null
                        },
                        textStyle = TextStyle(fontSize = 14.sp, color = DarkText),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxSize()
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        FieldIcon(icon)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            FieldLabel(label = label, required = required)
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFCFCFF))
                    .border(1.dp, Border, RoundedCornerShape(8.dp))
            ) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(text = placeholder, fontSize = 14.sp, color = Color(0xFF969CAE))
                    },
                    textStyle = TextStyle(fontSize = 14.sp, color = DarkText),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
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
            .border(1.dp, Color(0xFFE7E4FF), RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Purple, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun FieldLabel(label: String, required: Boolean) {
    Row {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
        if (required) {
            Text(text = " *", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RequiredRed)
        }
    }
}

@Composable
private fun UploadPhotoSection(
    previewPath: String?,
    remoteUrl: String?,
    onClick: () -> Unit,
    onClearClick: () -> Unit
) {
    val preview = previewPath?.takeIf { it.isNotBlank() }
        ?: MediaUrl.resolve(remoteUrl).takeIf { it.isNotBlank() }
    Column {
        Row {
            Text(text = "Add Photo", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
            Text(text = " *", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RequiredRed)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(114.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFFFBFAFF))
                .border(1.dp, Color(0xFFA9A2FF), RoundedCornerShape(9.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (preview != null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
                    },
                    update = { imageView ->
                        Glide.with(imageView).load(preview).into(imageView)
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
                            .size(37.dp)
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row {
                        Text(text = "or ", fontSize = 14.sp, color = SecondaryText)
                        Text(text = "browse", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Purple)
                        Text(text = " from gallery", fontSize = 14.sp, color = SecondaryText)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "PNG, JPG up to 5MB", fontSize = 12.sp, color = Color(0xFF9CA1B0))
                }
            }
        }
    }
}

@Composable
private fun SaveEquipmentButton(
    modifier: Modifier,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF8178FF), Color(0xFF4C43F3))))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.TaskAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }
    }
}

@Composable
private fun CancelButton(
    modifier: Modifier,
    onClick: () -> Unit
) {
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
            Text(text = "⊗", fontSize = 15.sp, color = Purple)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Cancel", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Purple)
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
            .height(70.dp)
            .navigationBarsPadding()
            .background(Color.White)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BottomNavItem(Icons.Default.Home, "Home", true, onHomeClick)
        BottomNavItem(Icons.Default.Message, "Messages", false, onMessagesClick)
        Box(modifier = Modifier.width(52.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(43.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF2672F5))
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(27.dp)
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
            tint = if (selected) Blue else Color(0xFF9298A7),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Blue else Color(0xFF9298A7)
        )
    }
}
