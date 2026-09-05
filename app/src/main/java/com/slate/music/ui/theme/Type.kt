package com.slate.music.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.slate.music.R

// 1. Properly declare your custom Six Caps font family
val sixCaps = FontFamily(
    Font(resId = R.font.sixcaps_regular)
)

val geist = FontFamily (
    Font(resId = R.font.geist_var)
)

// 2. Put all text styles inside the single Typography configuration
val Typography = Typography(

    // default body font
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = geist,
    ),

    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),

    // Six Caps expressive heading styles
    displayLarge = TextStyle(
        fontFamily = sixCaps,
        fontSize = 57.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = sixCaps,
        fontSize = 45.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = sixCaps,
        fontSize = 36.sp
    ),

    titleLarge = TextStyle(
        fontFamily = geist,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )
)
