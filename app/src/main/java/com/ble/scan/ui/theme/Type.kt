package com.ble.scan.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.ble.scan.R

val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.quicksand_light))
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.quicksand_medium)),
        fontSize = 25.sp,
        textAlign = TextAlign.Center
    )
)
