package com.example.app.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.ui.theme.AppColors
import com.example.app.ui.theme.AppEmptyState
import com.example.app.ui.theme.AppHeader
import com.example.app.ui.theme.EvergreenTheme
import kotlinx.coroutines.flow.distinctUntilChanged

data class NotificationUiItem(
    val id: Int,
    val title: String,
    val description: String,
    val time: String,
    val unread: Boolean,
    val canAccept: Boolean
)

@Composable
fun NotificationListScreen(
    items: List<NotificationUiItem>,
    loading: Boolean,
    loadingMore: Boolean,
    empty: Boolean,
    onBackClick: () -> Unit,
    onItemClick: (NotificationUiItem) -> Unit,
    onAcceptClick: (NotificationUiItem) -> Unit,
    onLoadMore: () -> Unit
) {
    EvergreenTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.background)
                .statusBarsPadding()
        ) {
            AppHeader(title = "Notifications", subtitle = "Latest updates", onLeadingClick = onBackClick)
            if (empty && !loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AppEmptyState(
                        title = "No notifications yet",
                        subtitle = "Updates about tasks and assignments will appear here."
                    )
                }
            } else {
                val listState = rememberLazyListState()
                LaunchedEffect(listState, items.size) {
                    snapshotFlow {
                        val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        last >= items.lastIndex - 2
                    }.distinctUntilChanged().collect { nearEnd ->
                        if (nearEnd) onLoadMore()
                    }
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        NotificationCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            onAcceptClick = { onAcceptClick(item) }
                        )
                    }
                    if (loadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = AppColors.purple,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationUiItem,
    onClick: () -> Unit,
    onAcceptClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(AppColors.surface)
            .border(1.dp, AppColors.border, RoundedCornerShape(13.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (item.unread) AppColors.purpleLight else Color(0xFFF3F5F7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = if (item.unread) AppColors.purple else AppColors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.description,
                    fontSize = 13.sp,
                    color = AppColors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = item.time, fontSize = 11.sp, color = AppColors.textSecondary)
            }
        }
        if (item.canAccept) {
            Box(
                modifier = Modifier
                    .padding(start = 58.dp, end = 12.dp, bottom = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.blue.copy(alpha = 0.1f))
                    .clickable(onClick = onAcceptClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.blue)
            }
        }
    }
}
