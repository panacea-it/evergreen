package com.example.app.ui.company

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
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
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

private val PageBackground = Color(0xFFF8F9FC)
private val DarkText = Color(0xFF20243D)
private val GrayText = Color(0xFF7B8195)
private val Purple = Color(0xFF635BFF)
private val LightPurple = Color(0xFFF3F0FF)
private val CardBorder = Color(0xFFECECF3)
private val Green = Color(0xFF22B573)

data class Company(
    val id: Int? = null,
    val name: String,
    val location: String,
    val pocName: String,
    val email: String,
    val mobile: String,
    val startDate: String,
    val endDate: String,
    val imageUrl: String? = null,
    val imageRes: Int? = null,
    val isActive: Boolean = true
)

@Composable
fun CompanyListScreen(
    companies: List<Company>,
    greetingName: String = "Admin",
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onCompanyClick: (Company) -> Unit = {},
    onCompanyLongClick: (Company) -> Unit = {},
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CompanyHeader(
                greetingName = greetingName,
                onMenuClick = onMenuClick,
                onScanClick = onScanClick,
                onNotificationClick = onNotificationClick
            )

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
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = 95.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = companies,
                    key = { company -> company.id ?: "${company.name}-${company.email}" }
                ) { company ->
                    CompanyCard(
                        company = company,
                        onClick = { onCompanyClick(company) },
                        onLongClick = { onCompanyLongClick(company) }
                    )
                }
            }

            BottomNavigation(
                onHomeClick = onHomeClick,
                onMessagesClick = onMessagesClick,
                onAddClick = onAddClick,
                onTasksClick = onTasksClick,
                onProfileClick = onProfileClick
            )
        }

        FloatingAddButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 78.dp),
            onClick = onAddClick
        )
    }
}

@Composable
private fun CompanyHeader(
    greetingName: String,
    onMenuClick: () -> Unit,
    onScanClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 15.dp, end = 15.dp, top = 8.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFF0F1F5), RoundedCornerShape(12.dp))
                .clickable(onClick = onMenuClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = "Menu",
                tint = DarkText,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(11.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello, $greetingName! 👋",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )
            Text(
                text = "Dashboard Overview",
                fontSize = 9.sp,
                color = GrayText
            )
        }

        HeaderIcon(icon = Icons.Default.QrCodeScanner, onClick = onScanClick)
        Spacer(modifier = Modifier.width(7.dp))
        Box {
            HeaderIcon(icon = Icons.Default.NotificationsNone, onClick = onNotificationClick)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 7.dp, end = 7.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Green)
            )
        }
    }
}

@Composable
private fun HeaderIcon(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(39.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF0F1F5), RoundedCornerShape(11.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF667085),
            modifier = Modifier.size(20.dp)
        )
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
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp)
            .height(39.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE8E8F1), RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color(0xFF55607A),
            modifier = Modifier
                .padding(start = 12.dp)
                .size(20.dp)
                .clickable(onClick = onSearchClick)
        )

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            cursorBrush = SolidColor(DarkText),
            textStyle = TextStyle(
                fontSize = 12.sp,
                color = DarkText
            ),
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 8.dp),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = "Search companies...",
                        fontSize = 9.sp,
                        color = Color(0xFFA1A6B5)
                    )
                }
                inner()
            }
        )

        Box(
            modifier = Modifier
                .padding(end = 6.dp)
                .size(29.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(LightPurple)
                .clickable(onClick = onFilterClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = Purple,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompanyCard(
    company: Company,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(10.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                CompanyImage(company = company)
                Spacer(modifier = Modifier.width(13.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(31.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Purple)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = company.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                ActiveBadge(isActive = company.isActive)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Purple,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = company.location,
                                    fontSize = 8.sp,
                                    color = GrayText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color(0xFFC1C4CE),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    POCDetails(company = company)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            DateRow(startDate = company.startDate, endDate = company.endDate)
        }
    }
}

@Composable
private fun CompanyImage(company: Company) {
    Box(
        modifier = Modifier
            .width(84.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFD9D1C3)),
        contentAlignment = Alignment.Center
    ) {
        val logoUrl = MediaUrl.resolve(company.imageUrl)
        when {
            company.imageRes != null -> {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = company.imageRes),
                    contentDescription = company.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            logoUrl.isNotBlank() -> {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        ImageView(context).apply {
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    },
                    update = { imageView ->
                        Glide.with(imageView)
                            .load(logoUrl)
                            .placeholder(R.drawable.place_holder1)
                            .error(R.drawable.place_holder1)
                            .into(imageView)
                    }
                )
            }
            else -> {
                val initial = company.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "C"
                Text(
                    text = initial,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF244341)
                )
            }
        }
    }
}

@Composable
private fun ActiveBadge(isActive: Boolean) {
    val label = if (isActive) "Active" else "Inactive"
    val color = if (isActive) Green else Color(0xFFB42318)
    val background = if (isActive) Color(0xFFF1FBF6) else Color(0xFFFEECEC)

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
        Text(
            text = label,
            fontSize = 7.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun POCDetails(company: Company) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFFFBFAFF))
            .border(1.dp, Color(0xFFEDEBFA), RoundedCornerShape(9.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "POC Details",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = Purple
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        CompanyInfoRow(Icons.Default.Person, "Name", company.pocName)
        CompanyInfoRow(Icons.Default.Email, "Email ID", company.email)
        CompanyInfoRow(Icons.Default.Call, "Mobile", company.mobile)
    }
}

@Composable
private fun CompanyInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Purple,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 7.sp,
            color = Color(0xFF72798C),
            modifier = Modifier.width(39.dp)
        )
        Text(text = ":", fontSize = 7.sp, color = Color(0xFF72798C))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value.ifBlank { "-" },
            fontSize = 7.sp,
            color = Color(0xFF41485A),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DateRow(
    startDate: String,
    endDate: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8F6FF))
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = Purple,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = "Start Date", fontSize = 6.sp, color = GrayText)
            Text(text = startDate, fontSize = 7.sp, color = DarkText)
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(18.dp)
                .background(Color(0xFFE0DDF0))
        )
        Spacer(modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "End Date", fontSize = 6.sp, color = GrayText)
            Text(text = endDate, fontSize = 7.sp, color = DarkText)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = Purple,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun FloatingAddButton(
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(37.dp)
            .shadow(elevation = 7.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(Color(0xFF573BDF))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add company",
            tint = Color.White,
            modifier = Modifier.size(23.dp)
        )
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(76.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(topStart = 23.dp, topEnd = 23.dp)
            )
            .clip(RoundedCornerShape(topStart = 23.dp, topEnd = 23.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BottomNavItem(Icons.Default.GridView, "Home", false, onHomeClick)
            BottomNavItem(Icons.Default.Message, "Messages", false, onMessagesClick)
            Box(
                modifier = Modifier.width(55.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(39.dp)
                        .shadow(elevation = 7.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF2872EE))
                        .clickable(onClick = onAddClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
            BottomNavItem(Icons.Default.TaskAlt, "Tasks", false, onTasksClick)
            BottomNavItem(Icons.Default.Person, "Profile", false, onProfileClick)
        }
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
            .width(52.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color(0xFF2872EE) else Color(0xFF8C93A1),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color(0xFF2872EE) else Color(0xFF8C93A1)
        )
    }
}
