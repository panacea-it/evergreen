package com.example.app.ui.home

import android.graphics.Paint
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import com.example.app.ui.theme.EvergreenTheme

// ------------------------------------------------------------
// COLORS
// ------------------------------------------------------------

private val ScreenBackground = Color(0xFFF9FAFC)
private val TextDark = Color(0xFF1D2939)
private val TextGray = Color(0xFF7C8798)
private val BorderColor = Color(0xFFE9EDF3)

private val Blue = Color(0xFF377FE0)
private val Green = Color(0xFF38A866)
private val Purple = Color(0xFF7650B3)
private val Orange = Color(0xFFEA911B)

private val BlueLight = Color(0xFFEFF6FF)
private val GreenLight = Color(0xFFF1FAF4)
private val PurpleLight = Color(0xFFF8F2FC)
private val OrangeLight = Color(0xFFFFF8EC)

// ------------------------------------------------------------
// DATA
// ------------------------------------------------------------

data class DashboardStat(
    val value: String,
    val title: String,
    val color: Color,
    val background: Color,
    val iconType: Int
)

val defaultAccessStats = listOf(
    DashboardStat("0", "POC's", Green, GreenLight, 0),
    DashboardStat("0", "Client's", Blue, BlueLight, 1),
    DashboardStat("0", "Manager's", Purple, PurpleLight, 2),
    DashboardStat("0", "Technician's", Orange, OrangeLight, 3)
)

val defaultStatusStats = listOf(
    DashboardStat("0", "Open", Green, GreenLight, 4),
    DashboardStat("0", "Hold", Orange, OrangeLight, 5),
    DashboardStat("0", "In Progress", Blue, BlueLight, 5),
    DashboardStat("0", "Closed", Purple, PurpleLight, 6)
)

val defaultChartLabels = emptyList<String>()

val defaultChartValues = emptyList<Float>()

data class HomeUiState(
    val greetingName: String = "Admin",
    val showAccessStats: Boolean = true,
    val accessStats: List<DashboardStat> = defaultAccessStats,
    val statusStats: List<DashboardStat> = defaultStatusStats,
    val chartTitle: String = "User counts by access level",
    val chartLabels: List<String> = defaultChartLabels,
    val chartValues: List<Float> = defaultChartValues
)

data class HomeActions(
    val onMenuClick: () -> Unit = {},
    val onScanClick: () -> Unit = {},
    val onNotificationsClick: () -> Unit = {},
    val onAccessStatClick: () -> Unit = {},
    val onStatusStatClick: () -> Unit = {},
    val onHomeClick: () -> Unit = {},
    val onMessagesClick: () -> Unit = {},
    val onAddClick: () -> Unit = {},
    val onTasksClick: () -> Unit = {},
    val onProfileClick: () -> Unit = {}
)

// ------------------------------------------------------------
// MAIN HOME SCREEN
// ------------------------------------------------------------

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeUiState = HomeUiState(),
    actions: HomeActions = HomeActions()
) {
    EvergreenTheme {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 17.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                HomeHeader(
                    greetingName = state.greetingName,
                    onMenuClick = actions.onMenuClick,
                    onScanClick = actions.onScanClick,
                    onNotificationsClick = actions.onNotificationsClick
                )

                Spacer(modifier = Modifier.height(14.dp))

                WelcomeBanner()

                if (state.showAccessStats) {
                    Spacer(modifier = Modifier.height(17.dp))
                    SectionHeader()
                    Spacer(modifier = Modifier.height(10.dp))
                    StatRows(state.accessStats, actions.onAccessStatClick)
                    UserCountChart(
                        title = state.chartTitle,
                        labels = state.chartLabels,
                        values = state.chartValues
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                } else {
                    Spacer(modifier = Modifier.height(17.dp))
                }

                StatRows(state.statusStats, actions.onStatusStatClick)
                Spacer(modifier = Modifier.height(15.dp))
            }

            BottomNavigation(actions)
        }
    }
    }
}

@Composable
private fun StatRows(
    stats: List<DashboardStat>,
    onClick: () -> Unit
) {
    stats.chunked(2).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            row.forEach { stat ->
                StatCard(
                    stat = stat,
                    modifier = Modifier.weight(1f),
                    onClick = onClick
                )
            }
            if (row.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

// ------------------------------------------------------------
// HEADER
// ------------------------------------------------------------

@Composable
private fun HomeHeader(
    greetingName: String,
    onMenuClick: () -> Unit,
    onScanClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(13.dp))
                .clip(RoundedCornerShape(13.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFF0F2F5), RoundedCornerShape(13.dp))
                .clickable(onClick = onMenuClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = "Menu",
                tint = TextDark,
                modifier = Modifier.size(25.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello, $greetingName! 👋",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Dashboard Overview",
                fontSize = 14.sp,
                color = TextGray
            )
        }

        SmallIconButton(
            icon = Icons.Default.QrCodeScanner,
            onClick = onScanClick
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box {
            SmallIconButton(
                icon = Icons.Default.NotificationsNone,
                onClick = onNotificationsClick
            )
            Box(
                modifier = Modifier
                    .offset(x = (-7).dp, y = 7.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF25B56D))
            )
        }
    }
}

@Composable
private fun SmallIconButton(
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(43.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF0F2F5), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF667085),
            modifier = Modifier.size(21.dp)
        )
    }
}

// ------------------------------------------------------------
// WELCOME BANNER
// ------------------------------------------------------------

@Composable
private fun WelcomeBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF303FC1),
                        Color(0xFF2943C8),
                        Color(0xFF3159DD)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .padding(start = 17.dp, top = 18.dp)
                .fillMaxWidth(0.57f)
        ) {
            Text(
                text = "Welcome back",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Everything looks\ngood today! 🎉",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Here's what's happening with your\norganization.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color.White.copy(alpha = 0.82f)
            )
        }

        DashboardIllustration(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp)
        )
    }
}

@Composable
private fun DashboardIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(165.dp)
            .height(108.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .width(108.dp)
                .height(68.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .border(3.dp, Color(0xFF9DBEFF), RoundedCornerShape(7.dp))
        ) {
            Box(
                modifier = Modifier
                    .padding(9.dp)
                    .width(43.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF7694F5))
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 9.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .width(9.dp)
                        .height(17.dp)
                        .background(Color(0xFFEF5965))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .width(9.dp)
                        .height(27.dp)
                        .background(Color(0xFF4B7DE7))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .width(9.dp)
                        .height(13.dp)
                        .background(Color(0xFF59B96C))
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = 32.dp)
                .width(70.dp)
                .height(23.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFF7DA6FA))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .width(73.dp)
                .height(47.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color.White.copy(alpha = 0.92f))
                .border(2.dp, Color(0xFFC5D7FF), RoundedCornerShape(7.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path()
                path.moveTo(4f, size.height * .75f)
                path.cubicTo(
                    size.width * .2f, size.height * .7f,
                    size.width * .22f, size.height * .35f,
                    size.width * .38f, size.height * .48f
                )
                path.cubicTo(
                    size.width * .53f, size.height * .62f,
                    size.width * .55f, size.height * .15f,
                    size.width * .7f, size.height * .3f
                )
                path.cubicTo(
                    size.width * .82f, size.height * .42f,
                    size.width * .9f, size.height * .12f,
                    size.width - 4f, size.height * .1f
                )
                drawPath(
                    path = path,
                    color = Color(0xFF4B7FE9),
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "At a Glance",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )
    }
}

@Composable
private fun StatCard(
    stat: DashboardStat,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(stat.background)
            .border(1.dp, Color.White, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 11.dp, top = 11.dp, end = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconCircle(color = stat.color, iconType = stat.iconType)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = stat.value,
                    fontSize = 24.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = stat.color
                )
                Text(
                    text = stat.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4B5563)
                )
            }
        }
        MiniWave(
            color = stat.color,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun IconCircle(
    color: Color,
    iconType: Int
) {
    Box(
        modifier = Modifier
            .size(43.dp)
            .shadow(elevation = 2.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center
    ) {
        val icon = when (iconType) {
            0 -> Icons.Default.TaskAlt
            1 -> Icons.Default.People
            2 -> Icons.Default.Person
            3 -> Icons.Default.Person
            4 -> Icons.Default.TaskAlt
            5 -> Icons.Default.Schedule
            else -> Icons.Default.CheckCircle
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun MiniWave(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(39.dp)
    ) {
        val path = Path()
        path.moveTo(0f, size.height)
        path.cubicTo(
            size.width * .12f, size.height * .8f,
            size.width * .18f, size.height * .55f,
            size.width * .28f, size.height * .48f
        )
        path.cubicTo(
            size.width * .4f, size.height * .35f,
            size.width * .45f, size.height * .72f,
            size.width * .55f, size.height * .65f
        )
        path.cubicTo(
            size.width * .67f, size.height * .55f,
            size.width * .7f, size.height * .3f,
            size.width * .78f, size.height * .42f
        )
        path.cubicTo(
            size.width * .88f, size.height * .58f,
            size.width * .93f, size.height * .48f,
            size.width, size.height * .35f
        )
        path.lineTo(size.width, size.height)
        path.close()
        drawPath(path = path, color = color.copy(alpha = 0.18f))

        val line = Path()
        line.moveTo(0f, size.height)
        line.cubicTo(
            size.width * .12f, size.height * .8f,
            size.width * .18f, size.height * .55f,
            size.width * .28f, size.height * .48f
        )
        line.cubicTo(
            size.width * .4f, size.height * .35f,
            size.width * .45f, size.height * .72f,
            size.width * .55f, size.height * .65f
        )
        line.cubicTo(
            size.width * .67f, size.height * .55f,
            size.width * .7f, size.height * .3f,
            size.width * .78f, size.height * .42f
        )
        line.cubicTo(
            size.width * .88f, size.height * .58f,
            size.width * .93f, size.height * .48f,
            size.width, size.height * .35f
        )
        drawPath(
            path = line,
            color = color.copy(alpha = 0.45f),
            style = Stroke(width = 1.2f)
        )
    }
}

@Composable
private fun UserCountChart(
    title: String,
    labels: List<String>,
    values: List<Float>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEDF0F4), RoundedCornerShape(15.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(7.dp))
        UserChartCanvas(labels = labels, values = values)
    }
}

@Composable
private fun UserChartCanvas(
    labels: List<String>,
    values: List<Float>
) {
    val axisLabels = labels
    val points = values
    if (axisLabels.isEmpty() || points.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No chart data yet", fontSize = 14.sp, color = TextGray)
        }
        return
    }
    val maxY = max(7f, points.maxOrNull() ?: 7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(181.dp)
    ) {
        Column(
            modifier = Modifier
                .width(28.dp)
                .height(148.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("7", "6", "5", "4", "3", "2", "1", "0").forEach {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = Color(0xFF687385)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val chartWidth = size.width
                val chartHeight = size.height

                for (i in 0..7) {
                    val y = chartHeight - (chartHeight / 7f * i)
                    drawLine(
                        color = Color(0xFFE9EDF2),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f
                    )
                }

                val lastIndex = max(1, points.lastIndex)
                for (i in 0..lastIndex) {
                    val x = chartWidth / lastIndex * i
                    drawLine(
                        color = Color(0xFFF0F2F5),
                        start = Offset(x, 0f),
                        end = Offset(x, chartHeight),
                        strokeWidth = 1f
                    )
                }

                val mapped = points.mapIndexed { index, value ->
                    val x = if (points.size == 1) chartWidth / 2f else chartWidth * index / lastIndex
                    val y = chartHeight - (value / maxY * chartHeight)
                    Offset(x, y)
                }

                val area = Path()
                area.moveTo(mapped.first().x, chartHeight)
                mapped.forEach { area.lineTo(it.x, it.y) }
                area.lineTo(mapped.last().x, chartHeight)
                area.close()
                drawPath(path = area, color = Color(0xFFDDF1D9))

                val line = Path()
                line.moveTo(mapped.first().x, mapped.first().y)
                for (i in 1 until mapped.size) {
                    val previous = mapped[i - 1]
                    val current = mapped[i]
                    val midX = (previous.x + current.x) / 2
                    line.cubicTo(midX, previous.y, midX, current.y, current.x, current.y)
                }
                drawPath(
                    path = line,
                    color = Color(0xFF55A64D),
                    style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                )

                mapped.forEach {
                    drawCircle(color = Color.White, radius = 3.5f, center = it)
                    drawCircle(color = Color(0xFF55A64D), radius = 2.2f, center = it)
                }

                val labelPaint = Paint().apply {
                    textSize = 22f
                    color = android.graphics.Color.DKGRAY
                    isAntiAlias = true
                }
                mapped.forEachIndexed { index, offset ->
                    val value = points[index]
                    if (value > 0f) {
                        drawContext.canvas.nativeCanvas.drawText(
                            String.format("%.2f", value),
                            offset.x - 8f,
                            offset.y - 5f,
                            labelPaint
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                axisLabels.forEach {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = Color(0xFF687385)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavigation(actions: HomeActions) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(78.dp)
            .shadow(
                elevation = 7.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BottomNavItem(
                icon = Icons.Default.GridView,
                label = "Home",
                selected = true,
                onClick = actions.onHomeClick
            )
            BottomNavItem(
                icon = Icons.Default.NotificationsNone,
                label = "Messages",
                selected = false,
                onClick = actions.onMessagesClick
            )
            Box(
                modifier = Modifier
                    .width(58.dp)
                    .clickable(onClick = actions.onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = (-12).dp)
                        .size(50.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF2869E8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(29.dp)
                    )
                }
            }
            BottomNavItem(
                icon = Icons.Default.TaskAlt,
                label = "Tasks",
                selected = false,
                onClick = actions.onTasksClick
            )
            BottomNavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                selected = false,
                onClick = actions.onProfileClick
            )
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
            .width(55.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Blue else Color(0xFF8C95A3),
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Blue else Color(0xFF8C95A3)
        )
    }
}
