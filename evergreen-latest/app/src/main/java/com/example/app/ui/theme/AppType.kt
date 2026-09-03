package com.example.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type scale uses [sp] so Android system font size / display size is applied.
 * Screens should use [MaterialTheme.typography] instead of hardcoded sizes.
 */
val AppTypography = Typography(
    headlineSmall = TextStyle(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.Bold
    ),
    titleLarge = TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Bold
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.SemiBold
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Medium
    )
)

object AppType {
    val screenTitle: TextStyle
        @Composable get() = MaterialTheme.typography.titleMedium
    val screenSubtitle: TextStyle
        @Composable get() = MaterialTheme.typography.bodySmall
    val cardTitle: TextStyle
        @Composable get() = MaterialTheme.typography.titleSmall
    val body: TextStyle
        @Composable get() = MaterialTheme.typography.bodyMedium
    val caption: TextStyle
        @Composable get() = MaterialTheme.typography.labelMedium
    val label: TextStyle
        @Composable get() = MaterialTheme.typography.labelLarge
    val nav: TextStyle
        @Composable get() = MaterialTheme.typography.labelSmall
}
