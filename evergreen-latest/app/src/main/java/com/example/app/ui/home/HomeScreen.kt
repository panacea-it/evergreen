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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import com.example.app.ui.theme.AppBottomBar
import com.example.app.ui.theme.AppColors
import com.example.app.ui.theme.AppTab
import com.example.app.ui.theme.EvergreenTheme

private val ScreenBackground = AppColors.background
private val TextDark = AppColors.textPrimary
private val TextGray = AppColors.textSecondary
private val BorderColor = AppColors.border
private val Blue = AppColors.blue
private val Green = AppColors.green
private val Purple = AppColors.purple
private val Orange = AppColors.orange
private val BlueLight = AppColors.blueLight
private val GreenLight = AppColors.greenLight
private val PurpleLight = AppColors.purpleLight
private val OrangeLight = AppColors.orangeLight

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
    DashboardStat("0", "Client Admins", Green, GreenLight, 0),
    DashboardStat("0", "Clients", Blue, BlueLight, 1),
    DashboardStat("0", "Evergreen Managers", Purple, PurpleLight, 2),
    DashboardStat("0", "Technicians", Orange, OrangeLight, 3)
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
    val bannerTitle: String = "Everything looks good today",
    val bannerSubtitle: String = "Here's what's happening with your organization.",
    val showAccessStats: Boolean = true,
    val accessStats: List<DashboardStat> = defaultAccessStats,
    val statusStats: List<DashboardStat> = defaultStatusStats,
    val chartTitle: String = "Companies onboarded",
    val chartLabels: List<String> = defaultChartLabels,
    val chartValues: List<Float> = defaultChartValues
)

data class HomeActions(
    val onMenuClick: () -> Unit = {},
    val onScanClick: () -> Unit = {},
    val onNotificationsClick: () -> Unit = {},
    val onAccessStatClick: (Int) -> Unit = {},
    val onStatusStatClick: (Int) -> Unit = {},
    val onHomeClick: () -> Unit = {},
    val onEquipmentClick: () -> Unit = {},
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
            com.example.app.ui.theme.AppHeader(
                title = "Hello, ${state.greetingName}",
                subtitle = "Dashboard Overview",
                leadingIcon = Icons.Default.Menu,
                leadingDescription = "Menu",
                onLeadingClick = actions.onMenuClick,
                actions = listOf(
                    com.example.app.ui.theme.AppHeaderAction(
                        icon = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan",
                        onClick = actions.onScanClick
                    ),
                    com.example.app.ui.theme.AppHeaderAction(
                        icon = Icons.Default.NotificationsNone,
                        contentDescription = "Notifications",
                        onClick = actions.onNotificationsClick
                    )
                )
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 17.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                WelcomeBanner(
                    title = state.bannerTitle,
                    subtitle = state.bannerSubtitle
                )

                if (state.showAccessStats) {
                    Spacer(modifier = Modifier.height(17.dp))
                    SectionHeader("People")
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

                SectionHeader("Tasks")
                Spacer(modifier = Modifier.height(10.dp))
                StatRows(state.statusStats, actions.onStatusStatClick)
                Spacer(modifier = Modifier.height(15.dp))
            }

            AppBottomBar(
                selected = AppTab.HOME,
                onHomeClick = actions.onHomeClick,
                onEquipmentClick = actions.onEquipmentClick,
                onAddClick = actions.onAddClick,
                onTasksClick = actions.onTasksClick,
                onProfileClick = actions.onProfileClick
            )
        }
    }
    }
}

@Composable
private fun StatRows(
    stats: List<DashboardStat>,
    onClick: (Int) -> Unit
) {
    stats.chunked(2).forEachIndexed { rowIndex, row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            row.forEachIndexed { colIndex, stat ->
                StatCard(
                    stat = stat,
                    modifier = Modifier.weight(1f),
                    onClick = { onClick(rowIndex * 2 + colIndex) }
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
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .clickable(onClick = onMenuClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = TextDark,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello, $greetingName",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Dashboard Overview",
                fontSize = 13.sp,
                color = TextGray,
                maxLines = 1
            )
        }

        SmallIconButton(
            icon = Icons.Default.QrCodeScanner,
            onClick = onScanClick
        )

        Spacer(modifier = Modifier.width(8.dp))

        SmallIconButton(
            icon = Icons.Default.NotificationsNone,
            onClick = onNotificationsClick
        )
    }
}

@Composable
private fun SmallIconButton(
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
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
private fun WelcomeBanner(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(AppColors.blueDark, AppColors.blue)
                )
            )
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Welcome back",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Color.White.copy(alpha = 0.82f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        DashboardIllustration()
    }
}

@Composable
private fun DashboardIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(132.dp)
            .height(96.dp)
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
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextDark
    )
}

@Composable
private fun StatCard(
    stat: DashboardStat,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .height(78.dp)
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stat.value,
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = stat.color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stat.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4B5563),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
        modifier = Modifier.size(18.dp)
    )
}

@Composable
private fun MiniWave(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(22.dp)
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
    val total = values.sum().toInt()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEDF0F4), RoundedCornerShape(15.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (values.isNotEmpty()) {
                Text(
                    text = "$total total",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        UserChartCanvas(labels = labels, values = values)
    }
}

@Composable
private fun UserChartCanvas(
    labels: List<String>,
    values: List<Float>
) {
    if (labels.isEmpty() || values.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No company data yet", color = TextGray)
        }
        return
    }

    val points = if (labels.size == values.size) values else values.take(labels.size)
    val axisLabels = labels.take(points.size).map { formatMonthLabel(it) }
    val yMax = niceCeiling(max(points.maxOrNull() ?: 0f, 1f))
    val tickCount = 4
    val yTicks = (tickCount downTo 0).map { tick ->
        ((yMax / tickCount) * tick).toInt().toString()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp)
    ) {
        Column(
            modifier = Modifier
                .width(28.dp)
                .height(150.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            yTicks.forEach { tick ->
                Text(
                    text = tick,
                    fontSize = 10.sp,
                    color = Color(0xFF687385),
                    maxLines = 1
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
                val lastIndex = max(1, points.lastIndex)

                for (i in 0..tickCount) {
                    val y = chartHeight - (chartHeight / tickCount * i)
                    drawLine(
                        color = Color(0xFFE9EDF2),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f
                    )
                }

                val mapped = points.mapIndexed { index, value ->
                    val x = if (points.size == 1) chartWidth / 2f else chartWidth * index / lastIndex
                    val ratio = (value / yMax).coerceIn(0f, 1f)
                    val y = chartHeight - (ratio * chartHeight)
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
                    style = Stroke(width = 2.4f, cap = StrokeCap.Round)
                )

                val valuePaint = Paint().apply {
                    textSize = 20f
                    color = android.graphics.Color.parseColor("#4B5563")
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                mapped.forEachIndexed { index, offset ->
                    drawCircle(color = Color.White, radius = 4.2f, center = offset)
                    drawCircle(color = Color(0xFF55A64D), radius = 2.6f, center = offset)
                    val value = points[index]
                    if (value > 0f) {
                        drawContext.canvas.nativeCanvas.drawText(
                            value.toInt().toString(),
                            offset.x,
                            offset.y - 8f,
                            valuePaint
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                axisLabels.forEachIndexed { index, label ->
                    val show = axisLabels.size <= 6 || index % 2 == 0 || index == axisLabels.lastIndex
                    Text(
                        text = if (show) label else "",
                        fontSize = 10.sp,
                        color = Color(0xFF687385),
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun formatMonthLabel(raw: String): String {
    val parts = raw.split("-")
    if (parts.size >= 2) {
        val month = parts[1].toIntOrNull() ?: return raw.take(3)
        val names = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        return names.getOrNull(month - 1) ?: raw.take(3)
    }
    return if (raw.length > 3) raw.take(3) else raw
}

private fun niceCeiling(value: Float): Float {
    if (value <= 4f) return 4f
    val magnitude = 10.0.pow(floor(log10(value.toDouble()))).toFloat()
    val normalized = value / magnitude
    val nice = when {
        normalized <= 1f -> 1f
        normalized <= 2f -> 2f
        normalized <= 5f -> 5f
        else -> 10f
    }
    return nice * magnitude
}
