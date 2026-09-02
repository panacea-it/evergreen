package com.example.app.ui.equipment

import android.widget.ImageView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
private val DarkText = Color(0xFF17204A)
private val Blue = Color(0xFF246BFF)
private val BlueLight = Color(0xFFF2F6FF)
private val Green = Color(0xFF159447)
private val GreenLight = Color(0xFFEDF9F1)
private val Red = Color(0xFFB42318)
private val RedLight = Color(0xFFFEECEC)
private val Border = Color(0xFFE7E9F1)
private val Purple = Color(0xFF635BFF)

data class EquipmentItem(
    val id: String,
    val name: String,
    val description: String,
    val modelNumber: String,
    val location: String,
    val serialNumber: String,
    val maintenanceFrequency: String,
    val imageUrl: String? = null,
    val imageRes: Int? = null,
    val isActive: Boolean = true
)

@Composable
fun EquipmentListScreen(
    equipments: List<EquipmentItem>,
    searchQuery: String = "",
    title: String = "Equipments List",
    onSearchQueryChange: (String) -> Unit = {},
    onEquipmentClick: (EquipmentItem) -> Unit = {},
    onEquipmentLongClick: (EquipmentItem) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onEquipmentTabClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onBackClick: (() -> Unit)? = null
) {
    EvergreenTheme {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EquipmentHeader(
                title = title,
                onMenuClick = onMenuClick,
                onScanClick = onScanClick,
                onNotificationClick = onNotificationClick,
                onBackClick = onBackClick
            )
            EquipmentSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearchClick = onSearchClick,
                onFilterClick = onFilterClick
            )
            if (equipments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No equipment found",
                        fontSize = 16.sp,
                        color = Color(0xFF8990A8)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    itemsIndexed(
                        items = equipments,
                        key = { index, item -> "${item.id}-$index" }
                    ) { _, equipment ->
                        EquipmentCard(
                            equipment = equipment,
                            onClick = { onEquipmentClick(equipment) },
                            onLongClick = { onEquipmentLongClick(equipment) }
                        )
                    }
                }
            }
            EquipmentBottomNavigation(
                onHomeClick = onHomeClick,
                onMessagesClick = onMessagesClick,
                onAddClick = onAddClick,
                onEquipmentClick = onEquipmentTabClick,
                onProfileClick = onProfileClick
            )
        }
        FloatingAddButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 82.dp),
            onClick = onAddClick
        )
    }
    }
}

@Composable
private fun EquipmentHeader(
    title: String,
    onMenuClick: () -> Unit,
    onScanClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onBackClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 20.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (onBackClick != null) Icons.Default.ArrowBack else Icons.Default.GridView,
            contentDescription = if (onBackClick != null) "Back" else "Menu",
            tint = DarkText,
            modifier = Modifier
                .size(27.dp)
                .clickable(onClick = onBackClick ?: onMenuClick)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = "Scan",
            tint = DarkText,
            modifier = Modifier
                .size(23.dp)
                .clickable(onClick = onScanClick)
        )
        Spacer(modifier = Modifier.width(18.dp))
        Icon(
            imageVector = Icons.Default.NotificationsNone,
            contentDescription = "Notifications",
            tint = DarkText,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onNotificationClick)
        )
    }
}

@Composable
private fun EquipmentSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(53.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(White)
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF78819D),
                modifier = Modifier
                    .size(27.dp)
                    .clickable(onClick = onSearchClick)
            )
            Spacer(modifier = Modifier.width(11.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                cursorBrush = SolidColor(DarkText),
                textStyle = TextStyle(fontSize = 16.sp, color = DarkText),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Search equipments by name, model or location...",
                            fontSize = 14.sp,
                            color = Color(0xFF8990A8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    inner()
                }
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(53.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(White)
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .clickable(onClick = onFilterClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = Purple,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EquipmentCard(
    equipment: EquipmentItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(17.dp))
            .background(White)
            .border(1.dp, Color(0xFFE9EBF2), RoundedCornerShape(17.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(12.dp)
    ) {
        EquipmentImage(equipment = equipment)
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            EquipmentTitle(equipment = equipment)
            Spacer(modifier = Modifier.height(11.dp))
            DetailsHeader()
            Spacer(modifier = Modifier.height(5.dp))
            EquipmentDetails(equipment = equipment)
        }
    }
}

@Composable
private fun EquipmentImage(equipment: EquipmentItem) {
    Box(
        modifier = Modifier
            .width(118.dp)
            .fillMaxHeight()
            .heightIn(min = 148.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFFE7E4DF)),
        contentAlignment = Alignment.Center
    ) {
        val imageUrl = MediaUrl.resolve(equipment.imageUrl)
        when {
            equipment.imageRes != null -> {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = equipment.imageRes),
                    contentDescription = equipment.name,
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFF687080),
                        modifier = Modifier.size(45.dp)
                    )
                    Spacer(modifier = Modifier.height(7.dp))
                    Text(
                        text = equipment.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF59606C),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun EquipmentTitle(equipment: EquipmentItem) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Blue)
        )
        Spacer(modifier = Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = equipment.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(5.dp))
                ActiveBadge(isActive = equipment.isActive)
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = equipment.description,
                fontSize = 14.sp,
                color = DarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = null,
            tint = Purple,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun ActiveBadge(isActive: Boolean) {
    val color = if (isActive) Green else Red
    val background = if (isActive) GreenLight else RedLight
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = if (isActive) "Active" else "Inactive",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun DetailsHeader() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFFF5F8FF))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Blue, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "Details", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Blue)
    }
}

@Composable
private fun EquipmentDetails(equipment: EquipmentItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(BlueLight)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        EquipmentDetailRow(Icons.Default.Settings, "Model Number", equipment.modelNumber)
        EquipmentDetailRow(Icons.Default.LocationOn, "Location", equipment.location)
        EquipmentDetailRow(Icons.Default.Tag, "Serial Number", equipment.serialNumber)
        EquipmentDetailRow(Icons.Default.CalendarMonth, "Maintenance Frequency", equipment.maintenanceFrequency)
    }
}

@Composable
private fun EquipmentDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Blue, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = DarkText,
            modifier = Modifier.width(112.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(text = ":", fontSize = 14.sp, color = DarkText)
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = value.ifBlank { "-" },
            fontSize = 14.sp,
            color = DarkText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FloatingAddButton(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(53.dp)
            .shadow(elevation = 8.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(Color(0xFF1768F5))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add equipment", tint = Color.White, modifier = Modifier.size(31.dp))
    }
}

@Composable
private fun EquipmentBottomNavigation(
    onHomeClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onAddClick: () -> Unit,
    onEquipmentClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(79.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
            .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
            .background(White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            EquipmentNavItem(Icons.Default.Home, "Home", false, onHomeClick)
            EquipmentNavItem(Icons.Default.Message, "Messages", false, onMessagesClick)
            Box(
                modifier = Modifier
                    .width(58.dp)
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF1768F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
            EquipmentNavItem(Icons.Default.Settings, "Equipments", true, onEquipmentClick)
            EquipmentNavItem(Icons.Default.Person, "Profile", false, onProfileClick)
        }
    }
}

@Composable
private fun EquipmentNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(62.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Blue else Color(0xFF858B9B),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Blue else Color(0xFF858B9B)
        )
    }
}
