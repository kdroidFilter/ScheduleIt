package dev.nucleus.scheduleit.ui.mobile.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Suppress("LongParameterList")
data class MobileColors(
    val bg: Color,
    val bgAlt: Color,
    val bgElev: Color,
    val bgChip: Color,
    val line: Color,
    val lineStrong: Color,
    val text: Color,
    val textSec: Color,
    val textTer: Color,
    val accent: Color,
    val accentSoft: Color,
    val danger: Color,
    val sheetBg: Color,
    val chipText: Color,
    val overlay: Color,
    val switchOff: Color,
    val switchOn: Color,
)

val LightColors = MobileColors(
    bg = Color(0xFFFAFAFA),
    bgAlt = Color(0xFFF1F1F2),
    bgElev = Color(0xFFFFFFFF),
    bgChip = Color(0xFFFFFFFF),
    line = Color(0x14000000),
    lineStrong = Color(0x29000000),
    text = Color(0xFF111114),
    textSec = Color(0xFF5F5F66),
    textTer = Color(0xFF9A9AA0),
    accent = Color(0xFF2D5BFF),
    accentSoft = Color(0x1A2D5BFF),
    danger = Color(0xFFD6453A),
    sheetBg = Color(0xFFFFFFFF),
    chipText = Color(0xFFFFFFFF),
    overlay = Color(0x73141418),
    switchOff = Color(0x1F000000),
    switchOn = Color(0xFF2D5BFF),
)

val DarkColors = MobileColors(
    bg = Color(0xFF0E0E10),
    bgAlt = Color(0xFF16161A),
    bgElev = Color(0xFF1B1B1F),
    bgChip = Color(0xFF1B1B1F),
    line = Color(0x14FFFFFF),
    lineStrong = Color(0x29FFFFFF),
    text = Color(0xFFF2F2F4),
    textSec = Color(0xFF9A9AA2),
    textTer = Color(0xFF5C5C62),
    accent = Color(0xFF7AA0FF),
    accentSoft = Color(0x247AA0FF),
    danger = Color(0xFFFF6A60),
    sheetBg = Color(0xFF1B1B1F),
    chipText = Color(0xFFFFFFFF),
    overlay = Color(0x99000000),
    switchOff = Color(0x24FFFFFF),
    switchOn = Color(0xFF7AA0FF),
)

data class MobileTypography(
    val displayLarge: TextUnit = 26.sp,
    val titleLarge: TextUnit = 22.sp,
    val titleMedium: TextUnit = 18.sp,
    val titleSmall: TextUnit = 17.sp,
    val body: TextUnit = 14.sp,
    val bodySmall: TextUnit = 13.sp,
    val label: TextUnit = 12.sp,
    val labelSmall: TextUnit = 11.sp,
    val caption: TextUnit = 10.sp,
)

val DefaultTypography = MobileTypography()

val EventColors: List<Long> = listOf(
    0xFF42A5F5L,
    0xFFEF5350L,
    0xFF66BB6AL,
    0xFFFFCA28L,
    0xFFAB47BCL,
    0xFFFF7043L,
    0xFF26C6DAL,
    0xFF8D6E63L,
)
