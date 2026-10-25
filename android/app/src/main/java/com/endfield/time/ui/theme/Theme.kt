package com.endfield.time.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val SkyBlue = Color(0xFF66CCFF)
val SkyBlueDeep = Color(0xFF3FA9F5)
val SkyBlueLight = Color(0xFFE3F4FE)
val PageBackground = Color(0xFFF6FAFD)
val FieldBackground = Color(0xFFEFF5FA)
val InkPrimary = Color(0xFF22313F)
val InkSecondary = Color(0xFF7C93A3)
val InkTertiary = Color(0xFF5A6B78)
val PlaceholderGray = Color(0xFF9AB0C0)
val DangerRed = Color(0xFFFF5252)
val DividerGray = Color(0xFFE5EDF3)

private val AppColorScheme = lightColorScheme(
    primary = SkyBlue,
    onPrimary = Color.White,
    primaryContainer = SkyBlueLight,
    onPrimaryContainer = InkPrimary,
    secondary = SkyBlueDeep,
    onSecondary = Color.White,
    background = PageBackground,
    onBackground = InkPrimary,
    surface = Color.White,
    onSurface = InkPrimary,
    surfaceVariant = FieldBackground,
    onSurfaceVariant = InkTertiary,
    // 弹层/下拉菜单/对话框使用 surfaceContainer* 系列 token，
    // 默认值偏淡紫（#F3EDF7 等），与半平面白底风格不符，这里统一为白色。
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    surfaceTint = SkyBlue,
    outline = DividerGray,
    outlineVariant = DividerGray,
    error = DangerRed,
    onError = Color.White
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(15.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val AppTypography = Typography(
    titleLarge = TextStyle(fontSize = 27.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontSize = 15.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelSmall = TextStyle(fontSize = 11.sp)
)

@Composable
fun EndFieldTimeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        shapes = AppShapes,
        typography = AppTypography,
        content = content
    )
}
