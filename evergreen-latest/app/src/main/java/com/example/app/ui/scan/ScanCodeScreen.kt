package com.example.app.ui.scan

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFFFCFCFE)
private val DarkText = Color(0xFF20203D)
private val SecondaryText = Color(0xFF7D819A)
private val Purple = Color(0xFF635BFF)
private val LightPurple = Color(0xFFF4F0FF)
private val Border = Color(0xFFECEBF5)

@Composable
fun ScanCodeScreen(
    onBackClick: () -> Unit = {},
    onFlashClick: () -> Unit = {},
    onPickPhotoClick: () -> Unit = {},
    flashOn: Boolean = false,
    cameraPreview: @Composable () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(15.dp))
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF1F0F7), RoundedCornerShape(15.dp))
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF64697A),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF0EEFA), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan QR",
                        tint = Purple,
                        modifier = Modifier.size(31.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Scan Code",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Point your camera at a QR code.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = SecondaryText
            )

            Text(
                text = "Once it's recognized, we'll take you to the link.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(20.dp))

            ScannerViewport(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                cameraPreview = cameraPreview
            )

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScanActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Bolt,
                    text = if (flashOn) "Turn off flash" else "Turn on flash",
                    onClick = onFlashClick
                )
                ScanActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Image,
                    text = "Pick from photos",
                    onClick = onPickPhotoClick
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            TroubleScanningCard()
        }
    }
}

@Composable
private fun ScannerViewport(
    modifier: Modifier = Modifier,
    cameraPreview: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF111318))
            .border(1.dp, Color(0xFFF0F0F7), RoundedCornerShape(20.dp))
    ) {
        cameraPreview()

        ScannerGrid(modifier = Modifier.fillMaxSize())
        ScannerCorners(modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.Center)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF8C83FF),
                            Color(0xFF6B63FF),
                            Color(0xFF8C83FF),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun ScannerGrid(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val gridColor = Color.White.copy(alpha = 0.08f)
        val spacing = 14.dp.toPx()

        var x = 0f
        while (x < size.width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += spacing
        }

        var y = 0f
        while (y < size.height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += spacing
        }
    }
}

@Composable
private fun ScannerCorners(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val purple = Color(0xFF8177FF)
        val strokeWidth = 2.dp.toPx()
        val cornerLength = 25.dp.toPx()
        val inset = 2.dp.toPx()
        val cornerRadius = 10.dp.toPx()

        val topLeft = Path()
        topLeft.moveTo(inset, inset + cornerLength)
        topLeft.lineTo(inset, inset + cornerRadius)
        topLeft.quadraticBezierTo(inset, inset, inset + cornerRadius, inset)
        topLeft.lineTo(inset + cornerLength, inset)
        drawPath(
            path = topLeft,
            color = purple,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val topRight = Path()
        topRight.moveTo(size.width - inset - cornerLength, inset)
        topRight.lineTo(size.width - inset - cornerRadius, inset)
        topRight.quadraticBezierTo(
            size.width - inset,
            inset,
            size.width - inset,
            inset + cornerRadius
        )
        topRight.lineTo(size.width - inset, inset + cornerLength)
        drawPath(
            path = topRight,
            color = purple,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val bottomLeft = Path()
        bottomLeft.moveTo(inset, size.height - inset - cornerLength)
        bottomLeft.lineTo(inset, size.height - inset - cornerRadius)
        bottomLeft.quadraticBezierTo(
            inset,
            size.height - inset,
            inset + cornerRadius,
            size.height - inset
        )
        bottomLeft.lineTo(inset + cornerLength, size.height - inset)
        drawPath(
            path = bottomLeft,
            color = purple,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val bottomRight = Path()
        bottomRight.moveTo(size.width - inset - cornerLength, size.height - inset)
        bottomRight.lineTo(size.width - inset - cornerRadius, size.height - inset)
        bottomRight.quadraticBezierTo(
            size.width - inset,
            size.height - inset,
            size.width - inset,
            size.height - inset - cornerRadius
        )
        bottomRight.lineTo(size.width - inset, size.height - inset - cornerLength)
        drawPath(
            path = bottomRight,
            color = purple,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun ScanActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(51.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White)
            .border(1.dp, Border, RoundedCornerShape(13.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(31.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LightPurple),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = DarkText,
            maxLines = 1
        )
    }
}

@Composable
private fun TroubleScanningCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFF0E8FF), Color(0xFFF9F5FF))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Having trouble scanning?",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Make sure the QR code is well lit and in focus.",
                fontSize = 9.sp,
                color = SecondaryText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "It should be centered on your viewfinder.",
                fontSize = 9.sp,
                color = SecondaryText
            )
        }
    }
}
