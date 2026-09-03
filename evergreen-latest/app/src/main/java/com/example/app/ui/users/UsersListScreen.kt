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
import com.example.app.ui.theme.AppBottomBar
import com.example.app.ui.theme.AppColors
import com.example.app.ui.theme.AppTab
import com.example.app.ui.theme.EvergreenTheme

private val Background = AppColors.background
private val White = AppColors.surface
private val DarkText = AppColors.textPrimary
private val SecondaryText = AppColors.textSecondary
private val Purple = AppColors.purple
private val PurpleLight = AppColors.purpleLight
private val Blue = AppColors.blue
private val BlueLight = AppColors.blueLight
private val Red = AppColors.red
private val RedLight = AppColors.redLight
private val Green = AppColors.green
private val GreenLight = AppColors.greenLight
private val Orange = AppColors.orange
private val OrangeLight = AppColors.orangeLight
private val Border = AppColors.border

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
        UserRole.POC -> "Client Admin"
        UserRole.TECHNICIAN -> "Technician"
        UserRole.MANAGER -> "Evergreen Manager"
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
            com.example.app.ui.theme.AppHeader(
                title = "Users",
                subtitle = "People in your organization",
                leadingIcon = com.example.app.ui.theme.AppIcons.menu,
                leadingDescription = "Menu",
                onLeadingClick = onMenuClick,
                actions = buildList {
                    if (showAddUser) {
                        add(com.example.app.ui.theme.AppHeaderAction(com.example.app.ui.theme.AppIcons.add, "Add user", onAddUserClick))
                    }
                    add(com.example.app.ui.theme.AppHeaderAction(com.example.app.ui.theme.AppIcons.scan, "Scan", onScanClick))
                    add(com.example.app.ui.theme.AppHeaderAction(com.example.app.ui.theme.AppIcons.notifications, "Notifications", onNotificationClick))
                }
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    UserCountCards(
                        selectedRole = selectedRole,
                        clientCount = clientCount,
                        pocCount = pocCount,
                        technicianCount = technicianCount,
                        managerCount = managerCount,
                        onRoleSelected = onRoleSelected
                    )
                }
                item {
                    UserSearchBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange
                    )
                }
                if (filteredUsers.isEmpty()) {
                    item {
                        com.example.app.ui.theme.AppEmptyState(
                            title = if (searchQuery.isBlank()) "No ${roleLabel(selectedRole).lowercase()}s yet" else "No matching users",
                            subtitle = if (searchQuery.isBlank()) {
                                "Add someone with this role to see them here."
                            } else {
                                "Try a different name, email, or phone number."
                            },
                            actionLabel = if (showAddUser && searchQuery.isBlank()) "Add user" else null,
                            onAction = if (showAddUser && searchQuery.isBlank()) onAddUserClick else null
                        )
                    }
                } else {
                    itemsIndexed(
                        items = filteredUsers,
                        key = { index, user -> "${user.id}-$index" }
                    ) { _, user ->
                        UserCard(user = user, onClick = { onUserClick(user) })
                    }
                }
            }
            AppBottomBar(
                selected = AppTab.HOME,
                onHomeClick = onHomeClick,
                onEquipmentClick = onMessagesClick,
                onAddClick = onAddClick,
                onTasksClick = onTasksClick,
                onProfileClick = onProfileClick
            )
        }
    }
    }
}

@Composable
private fun UserCountCards(
    selectedRole: UserRole,
    clientCount: Int,
    pocCount: Int,
    technicianCount: Int,
    managerCount: Int,
    onRoleSelected: (UserRole) -> Unit
) {
    val items = listOf(
        UserRoleCount(UserRole.MANAGER, managerCount, "Managers", Purple, PurpleLight, Icons.Default.WorkspacePremium),
        UserRoleCount(UserRole.POC, pocCount, "Admins", Green, GreenLight, Icons.Default.Business),
        UserRoleCount(UserRole.CLIENT, clientCount, "Clients", Blue, BlueLight, Icons.Default.Person),
        UserRoleCount(UserRole.TECHNICIAN, technicianCount, "Techs", Orange, OrangeLight, Icons.Default.Build)
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            UserCountCard(
                modifier = Modifier.weight(1f),
                item = item,
                selected = item.role == selectedRole,
                onClick = { onRoleSelected(item.role) }
            )
        }
    }
}

private data class UserRoleCount(
    val role: UserRole,
    val count: Int,
    val label: String,
    val color: Color,
    val background: Color,
    val icon: ImageVector
)

@Composable
private fun UserCountCard(
    modifier: Modifier,
    item: UserRoleCount,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(57.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(item.background)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) item.color else Color.White,
                shape = RoundedCornerShape(9.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(27.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(item.color.copy(alpha = 0.09f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = item.color,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(5.dp))
            Column {
                Text(
                    text = item.count.toString(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = item.color,
                    maxLines = 1
                )
                Text(
                    text = item.label,
                    fontSize = 11.sp,
                    color = DarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun UserSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = SecondaryText,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            cursorBrush = SolidColor(DarkText),
            textStyle = TextStyle(fontSize = 15.sp, color = DarkText),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = "Search by name, email, or phone",
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
}

@Composable
private fun UserCard(
    user: UserListItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PurpleLight),
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
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.name.ifBlank { "-" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (user.status.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    ActiveUserBadge(status = user.status)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = roleLabel(user.role),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Purple
            )
            if (user.email.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email,
                    fontSize = 13.sp,
                    color = SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (user.mobile.isNotBlank()) {
                Text(
                    text = user.mobile,
                    fontSize = 13.sp,
                    color = SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = null,
            tint = Color(0xFFB0B6C3),
            modifier = Modifier.size(12.dp)
        )
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
