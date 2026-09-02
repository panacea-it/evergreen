package com.example.app.ui.profile

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
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFFFAFAFD)
private val White = Color.White
private val DarkText = Color(0xFF202443)
private val SecondaryText = Color(0xFF7D849B)
private val Purple = Color(0xFF654BFF)
private val PurpleLight = Color(0xFFF2EEFF)
private val Blue = Color(0xFF3185F5)
private val Orange = Color(0xFFFF8B32)
private val Green = Color(0xFF24B86B)
private val Border = Color(0xFFE9E9F1)

data class ProfileData(
    val name: String = "",
    val role: String = "",
    val location: String = "",
    val phone: String = "",
    val userId: String = "",
    val projects: String = "-",
    val tasks: String = "-",
    val documents: String = "-",
    val activityDays: String = "-",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val userRole: String = "",
    val joinedOn: String = "",
    val status: String = "",
    val address: String = "",
    val company: String = ""
)

@Composable
fun ProfileScreen(
    profile: ProfileData,
    notificationCount: Int = 0,
    showEdit: Boolean = false,
    onBackClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onOverviewClick: () -> Unit = {},
    onProjectsClick: () -> Unit = {},
    onActivityClick: () -> Unit = {},
    onDocumentsClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onUsersClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ProfileHeader(
                notificationCount = notificationCount,
                onBackClick = onBackClick,
                onNotificationClick = onNotificationClick,
                onMoreClick = onMoreClick
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp)
            ) {
                Spacer(modifier = Modifier.height(7.dp))
                ProfileSummaryCard(profile = profile)
                Spacer(modifier = Modifier.height(12.dp))
                StatisticsCard(profile = profile)
                Spacer(modifier = Modifier.height(12.dp))
                ProfileTabs(
                    onOverviewClick = onOverviewClick,
                    onProjectsClick = onProjectsClick,
                    onActivityClick = onActivityClick,
                    onDocumentsClick = onDocumentsClick
                )
                Spacer(modifier = Modifier.height(12.dp))
                InformationCard(
                    profile = profile,
                    showEdit = showEdit,
                    onEditClick = onEditClick
                )
                Spacer(modifier = Modifier.height(12.dp))
                AddressCard(profile = profile)
                Spacer(modifier = Modifier.height(12.dp))
            }
            ProfileBottomNavigation(
                onHomeClick = onHomeClick,
                onUsersClick = onUsersClick,
                onAddClick = onAddClick,
                onSearchClick = onSearchClick,
                onSettingsClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    notificationCount: Int,
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(White)
                .border(1.dp, Border, RoundedCornerShape(14.dp))
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = DarkText,
                modifier = Modifier.size(23.dp)
            )
        }
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Profile", fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "View and manage user details", fontSize = 10.sp, color = SecondaryText)
        }
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(White)
                .border(1.dp, Border, RoundedCornerShape(13.dp))
                .clickable(onClick = onNotificationClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notifications",
                tint = DarkText,
                modifier = Modifier.size(23.dp)
            )
            if (notificationCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 2.dp, end = 3.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Purple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = notificationCount.toString(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(White)
                .border(1.dp, Border, RoundedCornerShape(13.dp))
                .clickable(onClick = onMoreClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = DarkText,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@Composable
private fun ProfileSummaryCard(profile: ProfileData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(163.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFFBFAFF), Color(0xFFF6F2FF))))
            .border(1.dp, Color(0xFFECE8FA), RoundedCornerShape(18.dp))
            .padding(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0EAFE))
                    .border(5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier.size(54.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Green)
                        .border(3.dp, Color.White, CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(15.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.name.ifBlank { "-" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (profile.role.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PurpleLight)
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = profile.role,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium,
                                color = Purple
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(9.dp))
                ProfileInfoLine(icon = Icons.Default.LocationOn, text = profile.location.ifBlank { "-" })
                Spacer(modifier = Modifier.height(7.dp))
                ProfileInfoLine(icon = Icons.Default.Phone, text = profile.phone.ifBlank { "-" })
                Spacer(modifier = Modifier.height(9.dp))
                Text(
                    text = "User ID: ${profile.userId.ifBlank { "-" }}",
                    fontSize = 9.sp,
                    color = SecondaryText
                )
                if (profile.company.isNotBlank()) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = profile.company,
                        fontSize = 9.sp,
                        color = SecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = Purple, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = text,
            fontSize = 9.sp,
            color = SecondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatisticsCard(profile: ProfileData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(White)
            .border(1.dp, Border, RoundedCornerShape(17.dp))
            .padding(vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileStat(Modifier.weight(1f), Icons.Default.Group, profile.projects, "Projects", Purple)
        StatDivider()
        ProfileStat(Modifier.weight(1f), Icons.Default.TaskAlt, profile.tasks, "Tasks", Blue)
        StatDivider()
        ProfileStat(Modifier.weight(1f), Icons.Default.Description, profile.documents, "Documents", Orange)
        StatDivider()
        ProfileStat(Modifier.weight(1f), Icons.Default.CalendarMonth, profile.activityDays, "Activity Days", Green)
    }
}

@Composable
private fun ProfileStat(
    modifier: Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(21.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value.ifBlank { "-" }, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkText)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = label, fontSize = 8.sp, color = SecondaryText)
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .height(55.dp)
            .width(1.dp)
            .background(Border)
    )
}

@Composable
private fun ProfileTabs(
    onOverviewClick: () -> Unit,
    onProjectsClick: () -> Unit,
    onActivityClick: () -> Unit,
    onDocumentsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(White)
            .border(1.dp, Border, RoundedCornerShape(15.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileTab(Modifier.weight(1f), Icons.Default.Person, "Overview", true, onOverviewClick)
        ProfileTab(Modifier.weight(1f), Icons.Default.BusinessCenter, "Projects", false, onProjectsClick)
        ProfileTab(Modifier.weight(1f), Icons.Default.History, "Activity", false, onActivityClick)
        ProfileTab(Modifier.weight(1f), Icons.Default.Description, "Documents", false, onDocumentsClick)
    }
}

@Composable
private fun ProfileTab(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Purple else SecondaryText,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Purple else SecondaryText
            )
        }
        Spacer(modifier = Modifier.height(11.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(2.dp)
                .clip(CircleShape)
                .background(if (selected) Purple else Color.Transparent)
        )
    }
}

@Composable
private fun InformationCard(
    profile: ProfileData,
    showEdit: Boolean,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(White)
            .border(1.dp, Border, RoundedCornerShape(17.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(PurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Purple, modifier = Modifier.size(19.dp))
            }
            Spacer(modifier = Modifier.width(9.dp))
            Text(
                text = "Information",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText,
                modifier = Modifier.weight(1f)
            )
            if (showEdit) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Border, RoundedCornerShape(8.dp))
                        .clickable(onClick = onEditClick)
                        .padding(horizontal = 11.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = DarkText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = "Edit", fontSize = 9.sp, color = DarkText)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        InformationRow(Icons.Default.Person, "Full Name", profile.fullName)
        InformationRow(Icons.Default.Mail, "Email Address", profile.email)
        InformationRow(Icons.Default.Phone, "Phone Number", profile.phoneNumber)
        InformationRow(Icons.Default.BusinessCenter, "Role", profile.userRole)
        if (profile.joinedOn.isNotBlank()) {
            InformationRow(Icons.Default.CalendarMonth, "Joined On", profile.joinedOn)
        }
        if (profile.status.isNotBlank()) {
            InformationStatusRow(status = profile.status)
        }
    }
}

@Composable
private fun InformationRow(icon: ImageVector, label: String, value: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF7B83A0), modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, fontSize = 9.sp, color = SecondaryText, modifier = Modifier.weight(1f))
            Text(
                text = value.ifBlank { "-" },
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = DarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFF0F0F5))
        )
    }
}

@Composable
private fun InformationStatusRow(status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF7B83A0),
            modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "Status", fontSize = 9.sp, color = SecondaryText, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEAFBF1))
                .border(1.dp, Color(0xFFB8EFD0), RoundedCornerShape(12.dp))
                .padding(horizontal = 11.dp, vertical = 5.dp)
        ) {
            Text(text = status, fontSize = 8.sp, fontWeight = FontWeight.Medium, color = Green)
        }
    }
}

@Composable
private fun AddressCard(profile: ProfileData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(White)
            .border(1.dp, Border, RoundedCornerShape(17.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(PurpleLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Purple, modifier = Modifier.size(19.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Address", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = profile.address.ifBlank { "-" },
                fontSize = 9.sp,
                lineHeight = 15.sp,
                color = SecondaryText
            )
        }
        Box(
            modifier = Modifier
                .width(108.dp)
                .height(92.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEAF2E9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(35.dp)
            )
        }
    }
}

@Composable
private fun ProfileBottomNavigation(
    onHomeClick: () -> Unit,
    onUsersClick: () -> Unit,
    onAddClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .navigationBarsPadding()
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
            ProfileBottomItem(Icons.Default.Home, "Home", false, onHomeClick)
            ProfileBottomItem(Icons.Default.Group, "Users", true, onUsersClick)
            Box(modifier = Modifier.width(58.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(51.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF6849F7))
                        .clickable(onClick = onAddClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(30.dp))
                }
            }
            ProfileBottomItem(Icons.Default.Search, "Search", false, onSearchClick)
            ProfileBottomItem(Icons.Default.Settings, "Settings", false, onSettingsClick)
        }
    }
}

@Composable
private fun ProfileBottomItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(58.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (selected) PurpleLight else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Purple else Color(0xFF727A91),
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Purple else Color(0xFF727A91)
        )
    }
}
