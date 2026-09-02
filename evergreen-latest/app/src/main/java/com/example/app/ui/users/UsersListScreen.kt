package com.example.app.ui.users

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.ui.theme.EvergreenTheme

private val Background = Color(0xFFFAFBFE)
private val White = Color.White
private val DarkText = Color(0xFF192047)
private val SecondaryText = Color(0xFF7B839C)
private val Purple = Color(0xFF624BFF)
private val PurpleLight = Color(0xFFF1EEFF)
private val Blue = Color(0xFF3185F5)
private val BlueLight = Color(0xFFEEF6FF)
private val Red = Color(0xFFFF343A)
private val RedLight = Color(0xFFFFF0F1)
private val Green = Color(0xFF16A957)
private val GreenLight = Color(0xFFECFAF2)
private val Orange = Color(0xFFFF861D)
private val OrangeLight = Color(0xFFFFF5E9)
private val Border = Color(0xFFE9EAF2)

data class UserListItem(
    val id: String,
    val name: String,
    val email: String,
    val mobile: String,
    val role: UserRole,
    val status: String = "",
    val imageRes: Int? = null
)

enum class UserRole {
    CLIENT,
    POC,
    TECHNICIAN,
    MANAGER
}

fun UserRole.accessLevel(): String {
    return when (this) {
        UserRole.CLIENT -> "client"
        UserRole.POC -> "client_admin"
        UserRole.TECHNICIAN -> "technician"
        UserRole.MANAGER -> "eg_admin"
    }
}

fun userRoleFromAccessLevel(accessLevel: String?): UserRole? {
    return when (accessLevel?.lowercase()) {
        "client" -> UserRole.CLIENT
        "client_admin" -> UserRole.POC
        "technician" -> UserRole.TECHNICIAN
        "eg_admin" -> UserRole.MANAGER
        else -> null
    }
}

fun roleLabel(role: UserRole): String {
    return when (role) {
        UserRole.CLIENT -> "Client"
        UserRole.POC -> "POC"
        UserRole.TECHNICIAN -> "Technician"
        UserRole.MANAGER -> "Manager"
    }
}

@Composable
fun UsersListScreen(
    users: List<UserListItem>,
    clientCount: Int,
    pocCount: Int,
    technicianCount: Int,
    managerCount: Int,
    selectedRole: UserRole = UserRole.CLIENT,
    onRoleSelected: (UserRole) -> Unit = {},
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    showAddUser: Boolean = true,
    onUserClick: (UserListItem) -> Unit = {},
    onAddUserClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    EvergreenTheme {
    val needle = searchQuery.trim()
    val filteredUsers = users.filter { user ->
        user.role == selectedRole && (
            needle.isEmpty() ||
                user.name.contains(needle, ignoreCase = true) ||
                user.mobile.contains(needle, ignoreCase = true) ||
                user.email.contains(needle, ignoreCase = true)
            )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            UsersHeader(
                onMenuClick = onMenuClick,
                onScanClick = onScanClick,
                onNotificationClick = onNotificationClick
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 5.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    UserCountCards(
                        clientCount = clientCount,
                        pocCount = pocCount,
                        technicianCount = technicianCount,
                        managerCount = managerCount
                    )
                }
                item {
                    UserRoleTabs(
                        selectedRole = selectedRole,
                        onRoleSelected = onRoleSelected
                    )
                }
                item {
                    UserSearchBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onSearchClick = onSearchClick,
                        onFilterClick = onFilterClick,
                        selectedRole = selectedRole
                    )
                }
                if (filteredUsers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No users found",
                                fontSize = 16.sp,
                                color = SecondaryText
                            )
                        }
                    }
                } else {
                    itemsIndexed(
                        items = filteredUsers,
                        key = { index, user -> "${user.id}-$index" }
                    ) { _, user ->
                        UserCard(user = user, onClick = { onUserClick(user) })
                    }
                }
                if (showAddUser) {
                    item {
                        AddNewUserButton(onClick = onAddUserClick)
                    }
                }
            }
            UsersBottomNavigation(
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
private fun UsersHeader(
    onMenuClick: () -> Unit,
    onScanClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = "Menu",
            tint = DarkText,
            modifier = Modifier
                .size(29.dp)
                .clickable(onClick = onMenuClick)
        )
        Spacer(modifier = Modifier.width(15.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Users List", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Manage and view all your users", fontSize = 14.sp, color = SecondaryText)
        }
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = "Scan",
            tint = DarkText,
            modifier = Modifier
                .size(25.dp)
                .clickable(onClick = onScanClick)
        )
        Spacer(modifier = Modifier.width(18.dp))
        Icon(
            imageVector = Icons.Default.NotificationsNone,
            contentDescription = "Notifications",
            tint = DarkText,
            modifier = Modifier
                .size(26.dp)
                .clickable(onClick = onNotificationClick)
        )
    }
}

@Composable
private fun UserCountCards(
    clientCount: Int,
    pocCount: Int,
    technicianCount: Int,
    managerCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        UserCountCard(Modifier.weight(1f), Icons.Default.Description, clientCount, "Clients", Red, RedLight)
        UserCountCard(Modifier.weight(1f), Icons.Default.Business, pocCount, "Pocs", Blue, BlueLight)
        UserCountCard(Modifier.weight(1f), Icons.Default.Build, technicianCount, "Technicians", Purple, PurpleLight)
        UserCountCard(Modifier.weight(1f), Icons.Default.WorkspacePremium, managerCount, "Managers", Orange, OrangeLight)
    }
}

@Composable
private fun UserCountCard(
    modifier: Modifier,
    icon: ImageVector,
    count: Int,
    label: String,
    iconColor: Color,
    background: Color
) {
    Column(
        modifier = modifier
            .height(139.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(background)
            .padding(horizontal = 9.dp, vertical = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.65f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(9.dp))
        Text(text = count.toString(), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = iconColor)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 14.sp, color = DarkText)
    }
}

@Composable
private fun UserRoleTabs(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(White)
            .border(1.dp, Border, RoundedCornerShape(15.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserRoleTab(Modifier.weight(1f), Icons.Default.Group, "Clients", UserRole.CLIENT, selectedRole, onRoleSelected)
        UserRoleTab(Modifier.weight(1f), Icons.Default.Business, "Pocs", UserRole.POC, selectedRole, onRoleSelected)
        UserRoleTab(Modifier.weight(1f), Icons.Default.Build, "Technicians", UserRole.TECHNICIAN, selectedRole, onRoleSelected)
        UserRoleTab(Modifier.weight(1f), Icons.Default.WorkspacePremium, "Managers", UserRole.MANAGER, selectedRole, onRoleSelected)
    }
}

@Composable
private fun UserRoleTab(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    role: UserRole,
    selectedRole: UserRole,
    onSelected: (UserRole) -> Unit
) {
    val selected = role == selectedRole
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onSelected(role) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Purple else SecondaryText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Purple else SecondaryText
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(3.dp)
                .clip(CircleShape)
                .background(if (selected) Purple else Color.Transparent)
        )
    }
}

@Composable
private fun UserSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    selectedRole: UserRole
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(57.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(White)
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .padding(horizontal = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = SecondaryText,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onSearchClick)
            )
            Spacer(modifier = Modifier.width(10.dp))
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
                            text = "Search ${roleLabel(selectedRole).lowercase()}s by name or number...",
                            fontSize = 14.sp,
                            color = Color(0xFF8C93AA),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    inner()
                }
            )
        }
        Spacer(modifier = Modifier.width(9.dp))
        Box(
            modifier = Modifier
                .size(57.dp)
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
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

@Composable
private fun UserCard(
    user: UserListItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(White)
            .border(1.dp, Border, RoundedCornerShape(17.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserImage(user = user)
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(Purple)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = user.name.ifBlank { "-" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (user.status.isNotBlank()) {
                    ActiveUserBadge(status = user.status)
                }
            }
            Spacer(modifier = Modifier.height(9.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PurpleLight)
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Purple, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = roleLabel(user.role) + " Details",
                    fontSize = 14.sp,
                    color = Purple,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(9.dp))
            UserContactDetails(user = user)
        }
        Spacer(modifier = Modifier.width(5.dp))
        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = null,
            tint = Purple,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun UserImage(user: UserListItem) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(158.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFFECE9E2)),
        contentAlignment = Alignment.Center
    ) {
        if (user.imageRes != null) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(user.imageRes),
                contentDescription = user.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(55.dp)
            )
        }
    }
}

@Composable
private fun ActiveUserBadge(status: String) {
    val active = status.equals("Active", ignoreCase = true)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .background(if (active) GreenLight else RedLight)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (active) Green else Red)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = status,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (active) Green else Red
        )
    }
}

@Composable
private fun UserContactDetails(user: UserListItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFFFBFCFF))
            .border(1.dp, Color(0xFFEDEEF4), RoundedCornerShape(11.dp))
            .padding(horizontal = 9.dp, vertical = 7.dp)
    ) {
        ContactRow(Icons.Default.MailOutline, "Email ID", user.email)
        Spacer(modifier = Modifier.height(7.dp))
        ContactRow(Icons.Default.Phone, "Mobile", user.mobile)
    }
}

@Composable
private fun ContactRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Purple, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(9.dp))
        Text(text = label, fontSize = 14.sp, color = SecondaryText, modifier = Modifier.width(80.dp))
        Text(text = ":", fontSize = 14.sp, color = SecondaryText)
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = value.ifBlank { "-" },
            fontSize = 14.sp,
            color = SecondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AddNewUserButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 2.dp)
            .height(51.dp)
            .shadow(elevation = 7.dp, shape = RoundedCornerShape(25.dp))
            .clip(RoundedCornerShape(25.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF654BFF), Color(0xFF4936E9))))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(29.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Purple, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(11.dp))
            Text(text = "Add new user", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }
    }
}

@Composable
private fun UsersBottomNavigation(
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
            .height(78.dp)
            .shadow(elevation = 7.dp, shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
            .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
            .background(White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            UserNavItem(Icons.Default.Home, "Home", false, onHomeClick)
            UserNavItem(Icons.Default.Message, "Messages", false, onMessagesClick)
            Box(modifier = Modifier.width(57.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF216EF5))
                        .clickable(onClick = onAddClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(31.dp))
                }
            }
            UserNavItem(Icons.Default.Description, "Tasks", false, onTasksClick)
            UserNavItem(Icons.Default.Person, "Profile", false, onProfileClick)
        }
    }
}

@Composable
private fun UserNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(55.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(39.dp)
                .clip(CircleShape)
                .background(if (selected) PurpleLight else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Purple else SecondaryText,
                modifier = Modifier.size(21.dp)
            )
        }
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Purple else SecondaryText
        )
    }
}
