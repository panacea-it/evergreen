package com.example.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.ui.theme.AppBottomBar
import com.example.app.ui.theme.AppColors
import com.example.app.ui.theme.AppIcons
import com.example.app.ui.theme.AppTab
import com.example.app.ui.theme.EvergreenTheme

private val Background = AppColors.background
private val White = AppColors.surface
private val DarkText = AppColors.textPrimary
private val SecondaryText = AppColors.textSecondary
private val Purple = AppColors.purple
private val PurpleLight = AppColors.purpleLight
private val Border = AppColors.border

data class ProfileData(
    val name: String = "",
    val role: String = "",
    val location: String = "",
    val phone: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val userRole: String = "",
    val joinedOn: String = "",
    val address: String = "",
    val company: String = ""
)

@Composable
fun ProfileScreen(
    profile: ProfileData,
    showBack: Boolean = false,
    showEdit: Boolean = false,
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onEquipmentClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    showLogout: Boolean = false,
    onLogoutClick: () -> Unit = {}
) {
    EvergreenTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .statusBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                com.example.app.ui.theme.AppHeader(
                    title = "Profile",
                    subtitle = "Account details",
                    leadingIcon = if (showBack) AppIcons.back else AppIcons.menu,
                    leadingDescription = if (showBack) "Back" else "Menu",
                    onLeadingClick = if (showBack) onBackClick else onMenuClick,
                    actions = listOf(
                        com.example.app.ui.theme.AppHeaderAction(AppIcons.scan, "Scan", onScanClick),
                        com.example.app.ui.theme.AppHeaderAction(AppIcons.more, "More", onMoreClick)
                    )
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    ProfileSummaryCard(profile = profile)
                    Spacer(modifier = Modifier.height(10.dp))
                    InformationCard(
                        profile = profile,
                        showEdit = showEdit,
                        onEditClick = onEditClick
                    )
                    if (profile.address.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        AddressCard(address = profile.address)
                    }
                    if (showLogout) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.redLight)
                                .clickable(onClick = onLogoutClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Logout", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.red)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                AppBottomBar(
                    selected = AppTab.PROFILE,
                    onHomeClick = onHomeClick,
                    onEquipmentClick = onEquipmentClick,
                    onAddClick = onAddClick,
                    onTasksClick = onTasksClick,
                    onProfileClick = onProfileClick
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    showBack: Boolean,
    onLeadingClick: () -> Unit,
    onScanClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderIcon(
            icon = if (showBack) Icons.Default.ArrowBack else AppIcons.menu,
            onClick = onLeadingClick
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Profile", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
            Text(text = "Account details", fontSize = 12.sp, color = SecondaryText)
        }
        HeaderIcon(icon = AppIcons.scan, onClick = onScanClick)
        Spacer(modifier = Modifier.width(8.dp))
        HeaderIcon(icon = AppIcons.more, onClick = onMoreClick)
    }
}

@Composable
private fun HeaderIcon(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DarkText,
            modifier = Modifier.size(AppIcons.header)
        )
    }
}

@Composable
private fun ProfileSummaryCard(profile: ProfileData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFFBFAFF), Color(0xFFF6F2FF))))
            .border(1.dp, Color(0xFFECE8FA), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0EAFE))
                .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = AppIcons.person,
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = profile.name.ifBlank { "-" },
                    fontSize = 16.sp,
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
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(text = profile.role, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Purple)
                    }
                }
            }
            if (profile.phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                ProfileInfoLine(icon = AppIcons.phone, text = profile.phone)
            }
            if (profile.company.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                ProfileInfoLine(icon = AppIcons.company, text = profile.company)
            }
        }
    }
}

@Composable
private fun ProfileInfoLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = Purple, modifier = Modifier.size(AppIcons.row))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = SecondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
            .clip(RoundedCornerShape(14.dp))
            .background(White)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = AppIcons.person, contentDescription = null, tint = Purple, modifier = Modifier.size(AppIcons.header))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Information",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText,
                modifier = Modifier.weight(1f)
            )
            if (showEdit) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Border, RoundedCornerShape(8.dp))
                        .clickable(onClick = onEditClick)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = DarkText, modifier = Modifier.size(AppIcons.row))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Edit", fontSize = 12.sp, color = DarkText)
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        InformationRow(AppIcons.person, "Full Name", profile.fullName)
        InformationRow(AppIcons.email, "Email", profile.email)
        InformationRow(AppIcons.phone, "Phone", profile.phoneNumber)
        InformationRow(AppIcons.company, "Role", profile.userRole)
        if (profile.joinedOn.isNotBlank()) {
            InformationRow(AppIcons.calendar, "Joined On", profile.joinedOn)
        }
    }
}

@Composable
private fun InformationRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF7B83A0), modifier = Modifier.size(AppIcons.row))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 13.sp, color = SecondaryText, modifier = Modifier.weight(1f))
        Text(
            text = value.ifBlank { "-" },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = DarkText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AddressCard(address: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(White)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(imageVector = AppIcons.location, contentDescription = null, tint = Purple, modifier = Modifier.size(AppIcons.header))
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Address", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = address, fontSize = 13.sp, lineHeight = 18.sp, color = SecondaryText)
        }
    }
}
