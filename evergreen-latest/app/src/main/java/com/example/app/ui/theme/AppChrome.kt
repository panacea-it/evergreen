package com.example.app.ui.theme

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class AppTab {
    HOME,
    EQUIPMENT,
    TASKS,
    PROFILE
}

data class AppHeaderAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)

@Composable
fun AppHeader(
    title: String,
    subtitle: String? = null,
    leadingIcon: ImageVector = AppIcons.back,
    leadingDescription: String = "Back",
    onLeadingClick: () -> Unit,
    actions: List<AppHeaderAction> = emptyList()
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(AppColors.surface)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppHeaderButton(
            icon = leadingIcon,
            contentDescription = leadingDescription,
            onClick = onLeadingClick
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = title,
                style = AppType.screenTitle,
                color = AppColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (subtitle.isNullOrBlank()) " " else subtitle,
                style = AppType.screenSubtitle,
                color = if (subtitle.isNullOrBlank()) Color.Transparent else AppColors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        actions.forEachIndexed { index, action ->
            if (index > 0) {
                Spacer(modifier = Modifier.width(8.dp))
            }
            AppHeaderButton(
                icon = action.icon,
                contentDescription = action.contentDescription,
                onClick = action.onClick
            )
        }
    }
}

@Composable
fun AppHeaderButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface)
            .border(1.dp, AppColors.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = AppColors.textPrimary,
            modifier = Modifier.size(AppIcons.header)
        )
    }
}

@Composable
fun AppBottomBar(
    selected: AppTab,
    onHomeClick: () -> Unit,
    onEquipmentClick: () -> Unit,
    onAddClick: () -> Unit,
    onTasksClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(72.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
            )
            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .background(AppColors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppNavItem(AppIcons.home, "Home", selected == AppTab.HOME, onHomeClick)
            AppNavItem(AppIcons.equipment, "Equipment", selected == AppTab.EQUIPMENT, onEquipmentClick)
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(elevation = 4.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(AppColors.blueDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppIcons.add,
                        contentDescription = "Create AMC",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            AppNavItem(AppIcons.tasks, "Tasks", selected == AppTab.TASKS, onTasksClick)
            AppNavItem(AppIcons.profile, "Profile", selected == AppTab.PROFILE, onProfileClick)
        }
    }
}

@Composable
private fun AppNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) AppColors.blue else Color(0xFF8C95A3)
    Column(
        modifier = Modifier
            .width(68.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(AppIcons.nav)
        )
        Spacer(modifier = Modifier.height(3.dp))
        // label below
        Text(
            text = label,
            style = AppType.nav.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AppFloatingAdd(
    modifier: Modifier = Modifier,
    contentDescription: String = "Add",
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(AppIcons.addButton)
            .shadow(elevation = 8.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(AppColors.blueDark)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = AppIcons.add,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(AppIcons.addIcon)
        )
    }
}

@Composable
fun AppEmptyState(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = AppType.cardTitle,
            color = AppColors.textPrimary
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = AppType.body,
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
        if (!actionLabel.isNullOrBlank() && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.blue.copy(alpha = 0.12f))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = AppType.label,
                    color = AppColors.blue
                )
            }
        }
    }
}
