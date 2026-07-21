package com.patta.pharmacy.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Sizes/weights mirror DESIGN.md. Plus Jakarta Sans / Inter font files can be
// dropped into res/font later and wired here; for now the system default is used
// so the project builds without bundling fonts.
private val Display = FontFamily.Default
private val Body = FontFamily.Default

val PattaTypography = Typography(
    // Big numeric displays (totals, stock counts) — read at arm's length.
    displayMedium = TextStyle(
        fontFamily = Display, fontWeight = FontWeight.Bold,
        fontSize = 36.sp, lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Display, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Display, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Display, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 30.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Body, fontWeight = FontWeight.Medium,
        fontSize = 18.sp, lineHeight = 26.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Body, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp
    ),
    // Hinglish labels — semi-bold (Udhaar, Khata, etc.)
    labelLarge = TextStyle(
        fontFamily = Body, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
)
